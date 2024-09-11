package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.model.Counter
import pl.janzak.weather.model.CounterId
import pl.janzak.weather.model.CounterRepository

abstract class CounterListViewModel : ViewModel() {
    abstract val counters: Flow<List<Pair<CounterId, Counter>>>
    abstract fun create(name: String)
}

class CounterListViewModelImpl : KoinComponent, CounterListViewModel() {
    private val _counterRepository: CounterRepository by inject()
    private val _coroutineScope: CoroutineScope by inject()

    override val counters = _counterRepository.getAll().map { it.toList() }
    override fun create(name: String) {
        _coroutineScope.launch {
            _counterRepository.createCounter(name)
        }
    }
}
