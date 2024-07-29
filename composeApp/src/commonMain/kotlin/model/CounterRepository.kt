package model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CounterId(private val id: Int) {
    val raw get() = id
}

data class Counter(val id: CounterId, val name: String, val value: Int)

data class CounterNotFound(val id: CounterId) : Throwable()

interface CounterRepository {
    suspend fun createCounter(name: String): CounterId
    suspend fun deleteCounter(counterId: CounterId)


    suspend fun incrementCounter(counterId: CounterId)
    suspend fun decrementCounter(counterId: CounterId)

    fun getCounter(counterId: CounterId): StateFlow<Counter>
    fun getAll(): StateFlow<Map<CounterId, Counter>>
}

private data class CounterState(
    val currentId: Int = 0,
    val data: Map<CounterId, Counter> = emptyMap()
)

class CounterRepositoryImpl : CounterRepository {
    private val _counters =
        MutableStateFlow(CounterState())

    override suspend fun createCounter(name: String): CounterId {
        var result: CounterId? = null

        _counters.update { state ->
            val (currentId, data) = state
            val newId = CounterId(currentId)
            result = newId
            val newCounter = Counter(id = newId, name = name, value = 0)
            state.copy(currentId = currentId + 1, data = data + (newId to newCounter))
        }

        return result!!
    }

    override suspend fun deleteCounter(counterId: CounterId) {
        _counters.update { state ->
            state.copy(data = state.data - counterId)
        }
    }

    override suspend fun incrementCounter(counterId: CounterId) {
        _counters.update { state ->
            val counter = state.data[counterId] ?: throw (CounterNotFound(counterId))
            state.copy(data = state.data + (counterId to counter.copy(value = counter.value + 1)))
        }
    }

    override suspend fun decrementCounter(counterId: CounterId) {
        _counters.update { state ->
            val counter = state.data[counterId] ?: throw (CounterNotFound(counterId))
            state.copy(data = state.data + (counterId to counter.copy(value = counter.value - 1)))
        }
    }

    override fun getCounter(counterId: CounterId): StateFlow<Counter> {
        return _counters.mapState { state ->
            state.data[counterId] ?: throw (CounterNotFound(counterId))
        }
    }

    override fun getAll(): StateFlow<Map<CounterId, Counter>> {
        return _counters.mapState { it.data }
    }
}
