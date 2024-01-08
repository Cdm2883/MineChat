package vip.cdms.mcoreui.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * 简单反射工具
 * @author Cdm2883
 */
public class ReflectionUtils {
    // Class
    public static final CacheUtils.Caches<String, Class<?>> classCaches = CacheUtils.forCache(new HashMap<>());
    public static Class<?> classForName(String clazz) throws Throwable {
        return classCaches.cache(clazz, () -> Class.forName(clazz));
    }

    // Method
    public static final CacheUtils.Caches<String, Method> methodCaches = CacheUtils.forCache(new HashMap<>());
    public static final Class<?>[] AUTO_PARAMETER_TYPES = new Class[]{};
    public static final Class<?>[] A_P_T = AUTO_PARAMETER_TYPES;
    public static final Class<?>[] DOUBLE_USE_PARAMETER_TYPES = new Class[]{};
    public static final Class<?>[] D_U_P_T = DOUBLE_USE_PARAMETER_TYPES;
    public static Object invoke(Method method, Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(obj, args);
    }
    public static Object invoke(Class<?> clazz, String method, Class<?>[] parameterTypes, Object obj, Object... args) throws Throwable {
        if (parameterTypes == AUTO_PARAMETER_TYPES) {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null)
                    throw new IllegalArgumentException("null is not allow in AUTO_PARAMETER_TYPES mode");
                if (arg instanceof Class<?> argClass) {
                    parameterTypes[i] = argClass;
                    args[i] = null;
                    continue;
                }

                parameterTypes[i] = switch (arg.getClass().getName()) {
                    case "java.lang.Boolean"   -> boolean.class;
                    case "java.lang.Byte"      -> byte.class;
                    case "java.lang.Short"     -> short.class;
                    case "java.lang.Integer"   -> int.class;
                    case "java.lang.Long"      -> long.class;
                    case "java.lang.Float"     -> float.class;
                    case "java.lang.Double"    -> double.class;
                    case "java.lang.Character" -> char.class;
                    default -> arg.getClass();
                };
            }
        } else if (parameterTypes == DOUBLE_USE_PARAMETER_TYPES) {
            if (args.length % 2 != 0)
                throw new IllegalArgumentException();
            parameterTypes = new Class[args.length / 2];
            Object[] newArgs = new Object[parameterTypes.length];
            int time = -1;
            for (int i = 0; i < args.length; i = i + 2) {
                Class<?> type = (Class<?>) args[i];
                Object arg = args[i + 1];
                parameterTypes[++time] = type;
                newArgs[time] = arg;
            }
            args = newArgs;
        }

        StringBuilder cache = new StringBuilder(method);
        if (parameterTypes != null) for (Class<?> parameterType : parameterTypes)
            cache/*.append("_")*/.append(parameterType.getName());

        Class<?>[] finalParameterTypes = parameterTypes == null ? new Class[0] : parameterTypes;
        return invoke(methodCaches.cache(cache.toString(), () -> {
            for (Class<?> klass = clazz; klass != null; klass = klass.getSuperclass()) try {
                return klass.getDeclaredMethod(method, finalParameterTypes);
            } catch (NoSuchMethodException ignored) {}
            throw new NoSuchMethodException();
        }), obj, args);
    }
    public static Object invoke(String method, Object obj, Object... args) throws Throwable {
        return invoke(obj.getClass(), method, AUTO_PARAMETER_TYPES, obj, args);
    }
    public static Object invoke(String method, Class<?>[] parameterTypes, Object obj, Object... args) throws Throwable {
        return invoke(obj.getClass(), method, parameterTypes, obj, args);
    }
    public static Object invoke(Class<?> clazz, String method, Object obj, Object... args) throws Throwable {
        return invoke(clazz, method, null, obj, args);
    }
    public static Object invoke(String clazz, String method, Class<?>[] parameterTypes, Object obj, Object... args) throws Throwable {
        return invoke(classForName(clazz), method, parameterTypes, obj, args);
    }
    public static Object invoke(String clazz, String method, Object obj, Object... args) throws Throwable {
        return invoke(clazz, method, null, obj, args);
    }

    // Field
    public static final CacheUtils.Caches<String, Field> fieldCaches = CacheUtils.forCache(new HashMap<>());
    public static Field field(Class<?> clazz, String field) throws Throwable {
        return fieldCaches.cache(field, () -> {
            for (Class<?> klass = clazz; klass != null; klass = klass.getSuperclass()) try {
                return klass.getDeclaredField(field);
            } catch (NoSuchFieldException ignored) {}
            throw new NoSuchFieldException();
        });
    }
    public static Field field(String clazz, String field) throws Throwable {
        return field(classForName(clazz), field);
    }

    /** @noinspection unchecked*/
    public static <T> T get(Field field, Object obj) throws IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(obj);
    }
    public static <T> T get(Class<?> clazz, String field, Object obj) throws Throwable {
        return get(field(clazz, field), obj);
    }
    public static <T> T get(String clazz, String field, Object obj) throws Throwable {
        return get(field(clazz, field), obj);
    }
    public static <T> T get(String field, Object obj) throws Throwable {
        return get(field(obj.getClass(), field), obj);
    }

    public static void set(Field field, Object obj, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static void set(Class<?> clazz, String field, Object obj, Object value) throws Throwable {
        set(field(clazz, field), obj, value);
    }
    public static void set(String clazz, String field, Object obj, Object value) throws Throwable {
        set(field(clazz, field), obj, value);
    }
    public static void set(String field, Object obj, Object value) throws Throwable {
        set(field(obj.getClass(), field), obj, value);
    }

    public static void lockCache() {
        classCaches.lock();
        methodCaches.lock();
        fieldCaches.lock();
    }
    public static void unlockCache() {
        classCaches.unlock();
        methodCaches.unlock();
        fieldCaches.unlock();
    }
}
