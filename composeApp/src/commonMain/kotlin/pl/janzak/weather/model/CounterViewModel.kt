package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.model.Counter
import pl.janzak.weather.model.CounterId
import pl.janzak.weather.model.CounterRepository

abstract class CounterViewModel : ViewModel() {
    abstract val counter: Flow<Counter?>
    abstract fun increment()
    abstract fun decrement()
    abstract fun delete()
    abstract fun rename(newName: String)
}

class CounterViewModelImpl(private val id: CounterId) : CounterViewModel(), KoinComponent {
    private val _counterRepository: CounterRepository by inject()
    private val _coroutineScope: CoroutineScope by inject()

    override val counter = runBlocking { _counterRepository.getCounter(id) }

    override fun increment() {
        _coroutineScope.launch {
            _counterRepository.incrementCounter(id)
        }
    }

    override fun decrement() {
        _coroutineScope.launch {
            _counterRepository.decrementCounter(id)
        }
    }

    override fun delete() {
        _coroutineScope.launch {
            _counterRepository.deleteCounter(id)
        }
    }

    override fun rename(newName: String) {
        _coroutineScope.launch {
            _counterRepository.renameCounter(id, newName)
        }
    }
}
