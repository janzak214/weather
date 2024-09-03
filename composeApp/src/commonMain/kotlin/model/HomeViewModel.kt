package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class HomeViewModel : ViewModel() {
    abstract val counters: Flow<List<Pair<CounterId, Counter>>>
    abstract fun create(name: String)
}

class HomeViewModelImpl : KoinComponent, HomeViewModel() {
    private val _counterRepository: CounterRepository by inject()
    private val _coroutineScope: CoroutineScope by inject()

    override val counters = _counterRepository.getAll().map { it.toList() }
    override fun create(name: String) {
        _coroutineScope.launch {
            _counterRepository.createCounter(name)
        }
    }
}
