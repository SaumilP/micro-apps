package org.sandcastle.apps.framework.server;

import org.sandcastle.apps.framework.annotations.Component;
import org.sandcastle.apps.framework.annotations.RequestMapping;
import org.sandcastle.apps.framework.di.Container;
import org.sandcastle.apps.framework.http.HttpRequest;
import org.sandcastle.apps.framework.http.HttpResponse;
import org.sandcastle.apps.web.SimpleController;

public class LiteServer {
    private Container container = new Container();

    public LiteServer(Class<?>... classes) {
        // register all the classes
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    container.register(clazz, instance);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Autowire all dependencies after registration
        for (Object instance : container.getInstances().values()) {
            container.autowire(instance);
        }
    }

    public HttpResponse handleRequest(HttpRequest request) {
        Object handler = container.getBean(requestHandlerFor(request));
        if (handler != null) {
            try {
                var methods = handler.getClass().getMethods();
                for (var method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        var mapping = method.getAnnotation(RequestMapping.class);
                        if (mapping.value().equals(request.getPath())) {
                            return (HttpResponse) method.invoke(handler, request);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
        return null;
    }

    private Class<?> requestHandlerFor(HttpRequest request) {
        return SimpleController.class;
    }
}
