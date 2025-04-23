package data.cache

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.*

class InMemoryCacheTest {
    private lateinit var cache: InMemoryCache<String, String>

    @BeforeTest
    fun setUp() {
        cache = InMemoryCache("test-cache")
    }

    @Test
    fun `put and get should allow to add and retrieve values`() = runBlocking {
        // Given
        val key = "key"
        val value = "value"

        // When
        cache.put(key, value)
        val result = cache.get(key)

        // Then
        assertEquals(value, result)
    }

    @Test
    fun `putAll should allow to insert several entries`() = runBlocking {
        // Given
        val entries = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        )

        // When
        cache.putAll(entries)

        // Then
        for ((key, value) in entries) {
            assertEquals(value, cache.get(key))
        }
    }

    @Test
    fun `get should return null if key is not present`() = runBlocking {
        // Given
        val key = "nonExistentKey"

        // When
        val result = cache.get(key)

        // Then
        assertNull(result)
    }

    @Test
    fun `contains should return true if key is present`() = runBlocking {
        // Given
        val key = "key"
        val value = "value"
        cache.put(key, value)

        // When
        val result = cache.contains(key)

        // Then
        assertTrue(result)
    }

    @Test
    fun `contains should return false if key is not present`() = runBlocking {
        // Given
        val key = "nonExistentKey"

        // When
        val result = cache.contains(key)

        // Then
        assertFalse(result)
    }

    @Test
    fun `invalidate should remove entry from cache`() = runBlocking {
        // Given
        val key = "key"
        val value = "value"
        cache.put(key, value)

        // When
        cache.invalidate(key)
        val result = cache.get(key)

        // Then
        assertNull(result)
        assertFalse(cache.contains(key))
    }

    @Test
    fun `clear should remove all entries from cache`() = runBlocking {
        // Given
        cache.put("key1", "value1")
        cache.put("key2", "value2")

        // When
        cache.clear()
        val result1 = cache.get("key1")
        val result2 = cache.get("key2")

        // Then
        assertNull(result1)
        assertNull(result2)
        assertFalse(cache.contains("key1"))
        assertFalse(cache.contains("key2"))
    }

    @Test
    fun `entry should expire after TTL`() = runBlocking {
        // Given
        val key = "key"
        val value = "value"
        val ttlSeconds = 1L // very short for testing

        // When
        cache.put(key, value, ttlSeconds)

        // Then - initially the entry should be available
        assertTrue(cache.contains(key))
        assertEquals(value, cache.get(key))

        // After waiting for more than TTL, the entry should be expired
        delay(ttlSeconds * 1000 + 100)

        // Then - after expiration, the entry should be gone
        assertFalse(cache.contains(key))
        assertNull(cache.get(key))
    }

    @Test
    fun `entry should not expire if TTL is not set`() = runBlocking {
        // Given
        val key = "permanentKey"
        val value = "permanentValue"

        // When
        cache.put(key, value)

        // Then - initially the entry should be available
        assertTrue(cache.contains(key))
        assertEquals(value, cache.get(key))

        // After waiting for a long time, the entry should still be available
        delay(1000)

        // Then - the entry should still be available
        assertTrue(cache.contains(key))
        assertEquals(value, cache.get(key))
    }

    @Test
    fun `put should update existing entries`() = runBlocking {
        // Given
        val key = "updateKey"
        val initialValue = "initial"
        val updatedValue = "updated"

        // When
        cache.put(key, initialValue)
        assertEquals(initialValue, cache.get(key))

        cache.put(key, updatedValue)

        // Then
        assertEquals(updatedValue, cache.get(key))
    }

    @Test
    fun `put should update TTL on existing entries`() = runBlocking {
        // Given
        val key = "ttlUpdateKey"
        val value = "value"
        val initialTtl = 1L
        val updatedTtl = 10L

        // When - put for short TTL
        cache.put(key, value, initialTtl)

        // Then - update with longer TTL
        cache.put(key, value, updatedTtl)

        // Then - wait for initial TTL to expire
        delay(initialTtl * 1000 + 100)

        // Then - entry should still exist
        assertTrue(cache.contains(key))
        assertEquals(value, cache.get(key))
    }

    @Test
    fun `contains should invalidate expired entries`() = runBlocking {
        // Given
        val key = "expiringContainsKey"
        val value = "value"
        val ttlSeconds = 1L // very short for testing

        // When
        cache.put(key, value, ttlSeconds)

        // Then - initially the entry should be available
        assertTrue(cache.contains(key))

        // Then - after waiting for more than TTL, the entry should be expired
        delay(ttlSeconds * 1000 + 100)

        assertFalse(cache.contains(key))
        assertNull(cache.get(key))
    }

    @Test
    fun `invalidate should be idempotent`() = runBlocking {
        // Given
        val key = "idempotentKey"
        cache.put(key, "value")

        // When - invalidate multiple times
        cache.invalidate(key)
        cache.invalidate(key) // Second call should not throw

        // Then
        assertNull(cache.get(key))
    }

    @Test
    fun `clear should be idempotent`() = runBlocking {
        // Given
        cache.put("key1", "value1")

        // When - clear multiple times
        cache.clear()
        cache.clear() // Second call should not throw

        // Then
        assertNull(cache.get("key1"))
    }

    @Test
    fun `cache should handle concurrent access`() = runBlocking {
        // Given
        val numOperations = 100
        val value = "sharedValue"

        // When - multiple coroutines putting the same key
        withContext(Dispatchers.Default) {
            val putOperations = List(numOperations) { op ->
                async {
                    cache.put("concurrent$op", "$value-$op")
                }
            }
            putOperations.awaitAll()
        }

        // Then - all values should be stored correctly
        for (i in 0 until numOperations) {
            assertTrue(cache.contains("concurrent$i"))
            assertEquals("$value-$i", cache.get("concurrent$i"))
        }

        // When - multiple coroutines reading, writing and deleting
        val counter = AtomicLong(0)
        withContext(Dispatchers.Default) {
            val mixedOperations = List(numOperations) { op ->
                async {
                    when (op % 3) {
                        0 -> cache.get("concurrent${op % numOperations}")
                        1 -> cache.put("concurrent${op % numOperations}", "$value-$op")
                        2 -> cache.invalidate("concurrent${op % numOperations}")
                    }
                    counter.incrementAndGet()
                }
            }
            mixedOperations.awaitAll()
        }

        // Then - all operations should be completed without exceptions
        assertEquals(numOperations.toLong(), counter.get())
    }
}