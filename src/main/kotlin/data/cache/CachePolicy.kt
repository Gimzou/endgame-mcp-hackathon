package data.cache

interface CachePolicy<T> {
    fun shouldCache(data: T): Boolean
    fun getCacheTtl(data: T): Long // in seconds
    fun generateCacheKey(vararg params: Any): String
}