package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.*

class TasksViewModel @ViewModelInject constructor(
    private val taskDao:TaskDao
): ViewModel() {
    
    val searchQuery = MutableStateFlow("")

    private val taskFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }


//    val tasks = taskDao.getTasks("blah").asLiveData()
    val tasks = taskFlow.asLiveData()

}
