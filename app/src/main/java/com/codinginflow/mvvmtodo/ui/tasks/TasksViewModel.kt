package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

//    val searchQuery = MutableStateFlow("")

    val searchQuery = state.getLiveData("searchQuery", "")


    var sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    val preferencesFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    val hideCompleted = MutableStateFlow(false)

    //    private val taskFlow = searchQuery
    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
//            sortOrder,
//            hideCompleted
    ) { searchQuery, filterPref ->
//        Triple(searchQuery,sortOrder, hideCompleted)
        Pair(searchQuery, filterPref)

//    }.flatMapLatest {(searchQuery, sortOrder, hideCompleted) ->
    }.flatMapLatest { (searchQuery, filterPreferences) ->
        taskDao.getTasks(searchQuery, filterPreferences.sortOrder, filterPreferences.completed)
    }


    //    val tasks = taskDao.getTasks("blah").asLiveData()
    val tasks = taskFlow.asLiveData()


    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {

        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompleted(completed: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(completed)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, checked: Boolean) {
        viewModelScope.launch {
            taskDao.update(task.copy(completed = checked))
        }
    }

    fun onTaskSwiped(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)

            taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
        }
    }

    fun unDoDeleteClick(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun onAddNewtaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTAskScreen)
    }

    fun onAddEditResult(result: Int) {

        when(result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task Updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.showTAskSavedConfirmationMessage(message))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }


    sealed class TaskEvent {
        object NavigateToAddTAskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task):TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class showTAskSavedConfirmationMessage(val msg:String):TaskEvent()
        object NavigateToDeleteAllCompletedScreen:TaskEvent()
    }
}
