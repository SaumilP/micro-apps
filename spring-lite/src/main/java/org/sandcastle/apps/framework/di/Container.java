package org.sandcastle.apps.framework.di;

import java.util.HashMap;
import java.util.Map;

import org.sandcastle.apps.framework.annotations.Autowired;

public class Container {
    private Map<Class<?>, Object> instances = new HashMap<>();

    public void register(Class<?> tClass, Object instance) {
        instances.put(tClass, instance);
    }

    public Object getBean(Class<?> tClass) {
        return instances.get(tClass);
    }

    public void autowire(Object instance) {
        var fields = instance.getClass().getDeclaredFields();
        for (var field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = instances.get(field.getType());
                if (dependency != null) {
                    field.setAccessible(true);
                    try {
                        field.set(instance, dependency);
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public Map<Class<?>, Object> getInstances() {
        return instances;
    }
}
