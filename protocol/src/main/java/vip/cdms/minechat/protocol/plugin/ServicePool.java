package vip.cdms.minechat.protocol.plugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class ServicePool {
    private static final HashMap<String, ServiceExtension> services = new HashMap<>();

    public static void putInstance(ServiceExtension service) {
        services.put(service.getClass().getName(), service);
    }

    public static ServiceExtension[] getInstances() {
        return services.values().toArray(new ServiceExtension[0]);
    }
    /** @noinspection unchecked*/
    public static <P extends ServiceExtension> P getInstance(String clazz) {
        return (P) services.get(clazz);
    }
    public static <P extends ServiceExtension> P getInstance(Class<P> clazz) {
        return getInstance(clazz.getName());
    }
}
