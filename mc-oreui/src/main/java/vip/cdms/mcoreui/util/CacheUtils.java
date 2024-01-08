package vip.cdms.mcoreui.util;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 缓存工具
 * @author Cdm2883
 */
public class CacheUtils {
    public static abstract class Caches<K, V> implements Iterable<Map.Entry<K, V>> {
        public interface Initializer<V> {
            V get() throws Throwable;
        }
        public interface SafeInitializer<V> extends Initializer<V> {
            V get();
        }

        private boolean locked = false;
        public void lock() {
            locked = true;
        }
        public void unlock() {
            locked = false;
        }

        public abstract void put(K key, V value);
        public abstract V get(K key);
        public V cache(K key, Initializer<V> initializer) throws Throwable {
            if (locked)
                return initializer.get();

            V cache = get(key);
            if (cache != null) return cache;
            cache = initializer.get();
            put(key, cache);
            return cache;
        }
        public V safeCache(K key, SafeInitializer<V> initializer) {
            try {
                return cache(key, initializer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <K, V> Caches<K, V> forCache(Map<K, V> map) {
        return new Caches<>() {
            @Override
            public void put(K key, V value) {
                map.put(key, value);
            }
            @Override
            public V get(K key) {
                return map.get(key);
            }
            @NonNull
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return map.entrySet().iterator();
            }
        };
    }
}
