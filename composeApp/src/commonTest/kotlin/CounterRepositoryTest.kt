import kotlinx.coroutines.test.runTest
import model.CounterRepository
import model.CounterRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CounterRepositoryTest {
    fun get(): CounterRepository = CounterRepositoryImpl()

    @Test
    fun canCreateCounter() = runTest {
        val repository = get()
        val id = repository.createCounter("counter")
        val counter = repository.getCounter(id)
        assertEquals("counter", counter.value?.name)
    }

    @Test
    fun canIncrementCounter() = runTest {
        val repository = get()
        val id = repository.createCounter("counter")
        repository.incrementCounter(id)
        val counter = repository.getCounter(id)
        assertEquals(1, counter.value?.value)
    }

    @Test
    fun canDecrementCounter() = runTest {
        val repository = get()
        val id = repository.createCounter("counter")
        repository.decrementCounter(id)
        val counter = repository.getCounter(id)
        assertEquals(-1, counter.value?.value)
    }

    @Test
    fun canCreateMultipleCounters() = runTest {
        val repository = get()
        repository.createCounter("counter1")
        repository.createCounter("counter2")
        val counters = repository.getAll().value
        assertEquals(2, counters.size)
    }

    @Test
    fun canDeleteCounter() = runTest {
        val repository = get()
        val id = repository.createCounter("counter")
        repository.deleteCounter(id)
        assertFails {
            repository.getCounter(id).value
        }
    }
}
