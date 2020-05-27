package cs.ecs.dacatoref;

import co.ecso.dacato.database.cache.Cache;
import co.ecso.dacato.database.cache.CacheKey;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * ApplicationCache.
 *
 * @author Christian Scharmach (cs@e-cs.co)
 * @version $Id:$
 * @since 04.09.16
 */
final class ApplicationCache implements Cache {
    private static final HazelcastInstance HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance();
    private final Map<Object, Object> APPLICATION_CACHE = HAZELCAST_INSTANCE.getMap("application");

//    private final com.google.common.cache.Cache<K, V>
//            cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).maximumSize(50).build();

    @Override
    public <V> CompletableFuture<V> get(final CacheKey key, final Callable<CompletableFuture<V>> callable)
            throws ExecutionException {
        if (!APPLICATION_CACHE.containsKey(key)) {
            try {
                final CompletableFuture<V> future = callable.call();
                future.thenAccept(toPut -> APPLICATION_CACHE.put(key, toPut));
                return  future;
            } catch (final Exception e) {
                throw new ExecutionException(e.getMessage(), e);
            }
        }

        return CompletableFuture.completedFuture((V) APPLICATION_CACHE.get(key));
    }

    @Override
    public <V> void put(final CacheKey key, final CompletableFuture<V> value) {
        if (!APPLICATION_CACHE.containsKey(key)) {
            value.thenAccept(toPut -> APPLICATION_CACHE.put(key, toPut));
        }
    }

    @Override
    public void invalidateAll(final Iterable<CacheKey> keys) {
        keys.forEach(APPLICATION_CACHE::remove);
    }

    @Override
    public void invalidate(final CacheKey var1) {
        APPLICATION_CACHE.remove(var1);
    }

    @Override
    public void invalidateAll() {
        APPLICATION_CACHE.clear();
    }

    @Override
    public long size() {
        return APPLICATION_CACHE.size();
    }

    @Override
    public void cleanUp() {
        APPLICATION_CACHE.clear();
    }

}
