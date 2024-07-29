package model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {
    private val _counterRepository: CounterRepository by inject()
    private val _coroutineScope: CoroutineScope by inject()

    val counters = _counterRepository.getAll().mapState { it.toList() }

    fun create(name: String) {
        _coroutineScope.launch {
            _counterRepository.createCounter(name)
        }
    }
}
