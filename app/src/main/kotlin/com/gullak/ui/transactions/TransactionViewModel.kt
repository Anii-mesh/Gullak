package com.animesh.gullak.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animesh.gullak.data.datastore.UserPreferencesDataStore
import com.animesh.gullak.data.repository.FinanceRepository
import com.animesh.gullak.domain.model.Transaction
import com.animesh.gullak.domain.model.TransactionCategory
import com.animesh.gullak.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransactionFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val category: TransactionCategory? = null
)

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val filter: TransactionFilter = TransactionFilter(),
    val currencySymbol: String = "₹",
    val isLoading: Boolean = true
)

data class AddEditUiState(
    val transactionId: Long? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: TransactionCategory = TransactionCategory.FOOD,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter())
    private val _transactionsUiState = MutableStateFlow(TransactionsUiState())
    val transactionsUiState: StateFlow<TransactionsUiState> = _transactionsUiState.asStateFlow()

    private val _addEditState = MutableStateFlow(AddEditUiState())
    val addEditState: StateFlow<AddEditUiState> = _addEditState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _filter.debounce(300),
                prefsDataStore.userPreferences
            ) { filter, prefs ->
                Pair(filter, prefs)
            }.flatMapLatest { (filter, prefs) ->
                val flow = when {
                    filter.query.isNotEmpty() -> repository.searchTransactions(filter.query)
                    filter.type != null -> repository.getTransactionsByType(filter.type)
                    filter.category != null -> repository.getTransactionsByCategory(filter.category)
                    else -> repository.getAllTransactions()
                }
                flow.map { transactions ->
                    TransactionsUiState(
                        transactions = transactions,
                        filter = filter,
                        currencySymbol = prefs.currencySymbol,
                        isLoading = false
                    )
                }
            }.collect { state ->
                _transactionsUiState.value = state
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _filter.update { it.copy(query = query) }
    }

    fun updateTypeFilter(type: TransactionType?) {
        _filter.update { it.copy(type = type, category = null) }
    }

    fun updateCategoryFilter(category: TransactionCategory?) {
        _filter.update { it.copy(category = category, type = null) }
    }

    fun clearFilters() {
        _filter.value = TransactionFilter()
    }

    fun loadTransactionForEdit(id: Long) {
        viewModelScope.launch {
            // Fetch from repo if needed — simplified here
            _addEditState.update { it.copy(transactionId = id) }
        }
    }

    fun updateAmount(amount: String) = _addEditState.update { it.copy(amount = amount) }
    fun updateType(type: TransactionType) = _addEditState.update { it.copy(type = type) }
    fun updateCategory(cat: TransactionCategory) = _addEditState.update { it.copy(category = cat) }
    fun updateDate(date: LocalDate) = _addEditState.update { it.copy(date = date) }
    fun updateNote(note: String) = _addEditState.update { it.copy(note = note) }

    fun saveTransaction() {
        val state = _addEditState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _addEditState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }
        viewModelScope.launch {
            _addEditState.update { it.copy(isSaving = true, errorMessage = null) }
            val transaction = Transaction(
                id = state.transactionId ?: 0L,
                amount = amount,
                type = state.type,
                category = state.category,
                date = state.date,
                note = state.note
            )
            if (state.transactionId != null) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }
            _addEditState.update { it.copy(isSaving = false, isSuccess = true) }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun resetAddEditState() {
        _addEditState.value = AddEditUiState()
    }
}
