package com.codinginflow.mvvmtodo.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import com.codinginflow.mvvmtodo.ui.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted
    private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportant = state.get<Boolean>("taskImportant") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportant", value)
        }


    fun onSaveClick() {
        if (taskName.isBlank()) {
            // show invalid input message

            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportant)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportant)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(text: String) {
        viewModelScope.launch {
            addEventTaskChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
        }
    }

    private val addEventTaskChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEventTaskChannel.receiveAsFlow()

    private fun createTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.insert(newTask)
            //navigate back
            addEventTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
        }
    }

    private fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            taskDao.update(updatedTask)
            //navigate back
            addEventTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))


        }
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidInputMessage(val message: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }

}