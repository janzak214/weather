package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NumberViewModel(private val id: CounterId) : ViewModel(), KoinComponent {
    private val _counterRepository: CounterRepository by inject()
    private val _coroutineScope: CoroutineScope by inject()

    val counter = runBlocking { _counterRepository.getCounter(id) }

    fun increment() {
        _coroutineScope.launch {
            _counterRepository.incrementCounter(id)
        }
    }

    fun decrement() {
        _coroutineScope.launch {
            _counterRepository.decrementCounter(id)
        }
    }

    fun delete() {
        _coroutineScope.launch {
            _counterRepository.deleteCounter(id)
        }
    }

    fun rename(newName: String) {
        _coroutineScope.launch {
            _counterRepository.renameCounter(id, newName)
        }
    }
}

