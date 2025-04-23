package data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class InMemoryCache<K, V>(private val name: String = "default") : Cache<K, V>{
    private data class CacheEntry<V>(
        val value: V,
        val expiresAt: Instant?
    )

    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()

    override suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key] ?: return null

        // Check if the value is expired
        if (entry.expiresAt != null && Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(key)
            return null
        }

        return entry.value
    }

    override suspend fun put(key: K, value: V, ttlSeconds: Long?) : Unit = mutex.withLock {
        val expiresAt = ttlSeconds?.let { Instant.now().plusSeconds(it) }
        cache[key] = CacheEntry(value, expiresAt)
    }

    override suspend fun putAll(map: Map<K, V>, ttlSeconds: Long?) {
        map.entries.forEach { (key, value) ->
            put(key, value, ttlSeconds)
        }
    }

    override suspend fun invalidate(key: K): Unit = mutex.withLock {
        cache.remove(key)
    }

    override suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    override suspend fun contains(key: K): Boolean =  mutex.withLock {
        val entry = cache[key] ?: return false

        // Check if the value is expired
        if (entry.expiresAt != null && Instant.now().isAfter(entry.expiresAt)) {
            cache.remove(key)
            return false
        }

        return true
    }
}