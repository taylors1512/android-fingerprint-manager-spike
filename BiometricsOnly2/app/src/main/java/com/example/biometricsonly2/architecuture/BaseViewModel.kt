package com.example.biometricsonly2.architecuture

import androidx.lifecycle.ViewModel
import com.example.biometricsonly2.activity.ViewModelIntent
import com.example.biometricsonly2.activity.ViewModelState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
abstract class BaseViewModel : ViewModel() {

    private var stateFlow = MutableStateFlow<ViewModelState?>(null)

    fun getStateFlow(): Flow<ViewModelState> = stateFlow.filterNotNull().map { it }

    val currentState = stateFlow.value

    private var lastKnownState: ViewModelState? = null
        private set

    /**
     * if the view model is restored, e.g. fragment comes back from the stack,
     * provide the last well known state to restore UI per requirement ,sending intents, etc
     */
    fun onRestoredState(block: (ViewModelState) -> Unit) {
        lastKnownState?.let {
            block(it)
        }
    }

    fun update(state: ViewModelState) {
        stateFlow.emitValue(state)
        lastKnownState = state
    }

    protected fun ViewModelState.updateState() = apply { update(this) }

    fun reset() {
        stateFlow = MutableStateFlow(null)
    }

    open fun start() {}

    abstract fun sendIntent(intent: ViewModelIntent)
}

/**
 * This cancels the default behaviour of StateFlow on distinctUntilChanged.
 * Just emits a null value before an update with a new one
 */
@ExperimentalCoroutinesApi
internal fun MutableStateFlow<ViewModelState?>.emitValue(value: ViewModelState) {
    this.value = null
    this.value = value
}