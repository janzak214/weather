package model

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import pl.janzak.cmp_demo.Database

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
    suspend fun renameCounter(counterId: CounterId, newName: String)

    fun getCounter(counterId: CounterId): Flow<Counter?>
    fun getAll(): Flow<Map<CounterId, Counter>>
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

    override suspend fun renameCounter(counterId: CounterId, newName: String) {
        _counters.update { state ->
            val counter = state.data[counterId] ?: throw (CounterNotFound(counterId))
            state.copy(data = state.data + (counterId to counter.copy(name = newName)))
        }
    }

    override fun getCounter(counterId: CounterId): StateFlow<Counter?> {
        return _counters.mapState { state ->
            state.data[counterId]
        }
    }

    override fun getAll(): StateFlow<Map<CounterId, Counter>> {
        return _counters.mapState { it.data }
    }
}

class DbCounterRepository(private val driver: SqlDriver, private val scope: CoroutineScope) :
    CounterRepository {
    private val database = Database(driver)

    override suspend fun createCounter(name: String): CounterId {
        var id: Long? = null
        database.databaseQueries.transaction {
            database.databaseQueries.create(name)
            id = database.databaseQueries.getCreatedId().executeAsOne()
        }
        return CounterId(id!!.toInt())
    }

    override suspend fun deleteCounter(counterId: CounterId) {
        database.databaseQueries.delete(counterId.raw.toLong())
    }

    override suspend fun incrementCounter(counterId: CounterId) {
        database.databaseQueries.increment(counterId.raw.toLong())
    }

    override suspend fun decrementCounter(counterId: CounterId) {
        database.databaseQueries.decrement(counterId.raw.toLong())
    }

    override suspend fun renameCounter(counterId: CounterId, newName: String) {
        database.databaseQueries.rename(id = counterId.raw.toLong(), name = newName)

    }

    override fun getCounter(counterId: CounterId): Flow<Counter?> {
        return database.databaseQueries.get(
            counterId.raw.toLong(),
            mapper = { id, name, value ->
                Counter(
                    id = CounterId(id.toInt()),
                    name = name,
                    value = value.toInt()
                )
            }).asFlow()
            .map { it.executeAsOneOrNull() }
    }

    override fun getAll(): Flow<Map<CounterId, Counter>> {
        return database.databaseQueries.selectAll(
            mapper = { id, name, value ->
                Counter(
                    id = CounterId(id.toInt()),
                    name = name,
                    value = value.toInt()
                )
            }).asFlow()
            .map {
                val list = it.executeAsList();
                println(list)
                buildMap(capacity = list.size) {
                    for (item in list) {
                        put(item.id, item)
                    }
                }
            }
    }

}