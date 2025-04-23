package data.cache

interface Cache<K, V> {
    suspend fun get(key: K): V?
    suspend fun put(key: K, value: V, ttlSeconds: Long? = null)
    suspend fun putAll(map: Map<K, V>, ttlSeconds: Long? = null)
    suspend fun invalidate(key: K)
    suspend fun clear()
    suspend fun contains(key: K): Boolean
}