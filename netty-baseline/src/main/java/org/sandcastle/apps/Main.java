package org.sandcastle.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Main {

    private static DataSource dataSource;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        dataSource = createDataSource();
        int port = getPort();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast(new HttpObjectAggregator(65536))
                            .addLast(new HttpRequestHandler());
                    }
                });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Netty server started on port: " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            try {
                String uri = request.uri();
                HttpMethod method = request.method();

                if ("/health".equals(uri) && method == HttpMethod.GET) {
                    writeResponse(ctx, OK, "text/plain", "OK");
                    return;
                }

                if (uri.startsWith("/api/projects")) {
                    if (uri.equals("/api/projects") && method == HttpMethod.GET) {
                        handleListProjects(ctx, request);
                    } else if (uri.equals("/api/projects") && method == HttpMethod.POST) {
                        handleCreateProject(ctx, request);
                    } else if (uri.matches("/api/projects/[a-f0-9-]+") && method == HttpMethod.GET) {
                        handleGetProject(ctx, uri);
                    } else if (uri.matches("/api/projects/[a-f0-9-]+") && method == HttpMethod.DELETE) {
                        handleDeleteProject(ctx, uri);
                    } else {
                        writeResponse(ctx, NOT_FOUND, "text/plain", "Not Found");
                    }
                } else {
                    writeResponse(ctx, NOT_FOUND, "text/plain", "Not Found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                writeResponse(ctx, INTERNAL_SERVER_ERROR, "text/plain", e.getMessage());
            }
        }

        private void handleListProjects(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            String uri = request.uri();
            String userId = null;
            if (uri.contains("?")) {
                String query = uri.substring(uri.indexOf('?') + 1);
                for (String param : query.split("&")) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "userId".equals(kv[0])) {
                        userId = kv[1];
                    }
                }
            }

            List<Project> projects = new ArrayList<>();

            try (Connection conn = dataSource.getConnection()) {
                String sql = userId != null
                    ? "SELECT id, user_id, name, description FROM project WHERE user_id = ?"
                    : "SELECT id, user_id, name, description FROM project";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (userId != null) {
                        stmt.setString(1, userId);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            projects.add(new Project(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("user_id"),
                                rs.getString("name"),
                                rs.getString("description")
                            ));
                        }
                    }
                }
            }

            String json = objectMapper.writeValueAsString(projects);
            writeResponse(ctx, OK, "application/json", json);
        }

        private void handleGetProject(ChannelHandlerContext ctx, String uri) throws Exception {
            String id = uri.substring(uri.lastIndexOf('/') + 1);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id, user_id, name, description FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Project project = new Project(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("user_id"),
                            rs.getString("name"),
                            rs.getString("description")
                        );
                        String json = objectMapper.writeValueAsString(project);
                        writeResponse(ctx, OK, "application/json", json);
                    } else {
                        writeResponse(ctx, NOT_FOUND, "text/plain", "");
                    }
                }
            }
        }

        private void handleCreateProject(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            String body = request.content().toString(CharsetUtil.UTF_8);
            Project project = objectMapper.readValue(body, Project.class);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO project (user_id, name, description) VALUES (?, ?, ?) RETURNING id")) {

                stmt.setString(1, project.getUserId());
                stmt.setString(2, project.getName());
                stmt.setString(3, project.getDescription());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        project.setId(UUID.fromString(rs.getString("id")));
                    }
                }
            }

            String json = objectMapper.writeValueAsString(project);
            writeResponse(ctx, CREATED, "application/json", json);
        }

        private void handleDeleteProject(ChannelHandlerContext ctx, String uri) throws Exception {
            String id = uri.substring(uri.lastIndexOf('/') + 1);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM project WHERE id = ?")) {

                stmt.setObject(1, UUID.fromString(id));
                stmt.executeUpdate();
            }

            writeResponse(ctx, NO_CONTENT, "text/plain", "");
        }

        private void writeResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                   String contentType, String content) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("JDBC_URL",
            "jdbc:postgresql://" + System.getenv().getOrDefault("DB_HOST", "localhost") +
            ":" + System.getenv().getOrDefault("DB_PORT", "5432") +
            "/" + System.getenv().getOrDefault("DB_NAME", "projectdb")));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "postgres"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "postgres"));
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        return new HikariDataSource(config);
    }

    private static int getPort() {
        String port = System.getenv("SERVER_PORT");
        return port != null ? Integer.parseInt(port) : 8086;
    }
}
