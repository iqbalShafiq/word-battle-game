package id.usecase.word_battle.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base MVI ViewModel that handles intents and produces states & side effects
 */
abstract class MviViewModel<Intent, State, Effect>(initialState: State) : ViewModel() {

    // UI state as StateFlow
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    // Side effects as Channel
    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Intent consumer
    private val _intent = MutableSharedFlow<Intent>()

    init {
        viewModelScope.launch {
            _intent.collect {
                handleIntent(it, _state.value)
            }
        }
    }

    /**
     * Process user intent
     */
    fun processIntent(intent: Intent) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    /**
     * Update state
     */
    protected fun updateState(reduce: State.() -> State) {
        val newState = _state.value.reduce()
        _state.value = newState
    }

    /**
     * Send side effect
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Handle intent implementation
     */
    protected abstract suspend fun handleIntent(intent: Intent, state: State)
}