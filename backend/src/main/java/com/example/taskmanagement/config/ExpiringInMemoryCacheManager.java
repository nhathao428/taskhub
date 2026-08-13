package com.example.taskmanagement.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * CacheManager tối giản, in-memory, có expire-after-write (TTL) — dùng thay Caffeine
 * (chưa có trong pom.xml) khi chạy không có Redis. Không cần thêm dependency mới, chỉ
 * dùng java.util.concurrent sẵn có trong JDK.
 *
 * Chỉ phù hợp 1 instance (không share cache giữa nhiều instance/server như Redis) —
 * đủ dùng cho free tier vì các nền tảng free (Render...) đều chỉ chạy 1 instance.
 */
public class ExpiringInMemoryCacheManager implements CacheManager {

    private final Map<String, Duration> ttlByCacheName;
    private final Duration defaultTtl;
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    public ExpiringInMemoryCacheManager(Map<String, Duration> ttlByCacheName, Duration defaultTtl) {
        this.ttlByCacheName = ttlByCacheName;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name,
                n -> new ExpiringCache(n, ttlByCacheName.getOrDefault(n, defaultTtl)));
    }

    @Override
    public Set<String> getCacheNames() {
        return caches.keySet();
    }

    /** Cache có expire-after-write; entry hết hạn bị coi như miss (và dọn luôn) khi get. */
    private static class ExpiringCache implements Cache {

        private final String name;
        private final Duration ttl;
        private final ConcurrentMap<Object, Entry> store = new ConcurrentHashMap<>();

        ExpiringCache(String name, Duration ttl) {
            this.name = name;
            this.ttl = ttl;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }

        @Override
        public ValueWrapper get(Object key) {
            Entry entry = store.get(key);
            if (entry == null) {
                return null;
            }
            if (entry.isExpired(ttl)) {
                store.remove(key, entry);
                return null;
            }
            return new SimpleValueWrapper(entry.value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, @Nullable Class<T> type) {
            ValueWrapper wrapper = get(key);
            if (wrapper == null) {
                return null;
            }
            Object value = wrapper.get();
            if (type != null && value != null && !type.isInstance(value)) {
                throw new IllegalStateException(
                        "Cached value is not of required type [" + type.getName() + "]: " + value);
            }
            return (T) value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Callable<T> valueLoader) {
            Entry existing = store.get(key);
            if (existing != null && !existing.isExpired(ttl)) {
                return (T) existing.value;
            }
            T value;
            try {
                value = valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
            put(key, value);
            return value;
        }

        @Override
        public void put(Object key, Object value) {
            store.put(key, new Entry(value));
        }

        @Override
        public void evict(Object key) {
            store.remove(key);
        }

        @Override
        public void clear() {
            store.clear();
        }

        private static final class Entry {
            final Object value;
            final Instant createdAt = Instant.now();

            Entry(Object value) {
                this.value = value;
            }

            boolean isExpired(Duration ttl) {
                return Instant.now().isAfter(createdAt.plus(ttl));
            }
        }
    }
}
