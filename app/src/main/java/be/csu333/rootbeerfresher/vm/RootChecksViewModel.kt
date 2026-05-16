package be.csu333.rootbeerfresher.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.csu333.rootbeerfresher.checks.CheckRegistry
import be.csu333.rootbeerfresher.checks.CheckRunner
import be.csu333.rootbeerfresher.checks.CheckState
import be.csu333.rootbeerfresher.checks.toInitialState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootChecksViewModel(
    private val runner: CheckRunner = CheckRunner(CheckRegistry.ALL)
) : ViewModel() {

    private val _checks = MutableStateFlow(runner.checks.map { it.toInitialState() })
    val checks: StateFlow<List<CheckState>> = _checks.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun runChecks(context: Context) {
        _checks.value = runner.checks.map { it.toInitialState() }
        _isRunning.value = true
        viewModelScope.launch {
            runner.run(context) { updated ->
                _checks.update { current ->
                    current.map { if (it.id == updated.id) updated else it }
                }
            }
            _isRunning.value = false
        }
    }

    fun toggleExpanded(id: String) {
        _checks.update { current ->
            current.map { if (it.id == id) it.copy(isExpanded = !it.isExpanded) else it }
        }
    }
}
