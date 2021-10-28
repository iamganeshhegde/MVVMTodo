package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
        private val taskDao: TaskDao,
        private val preferencesManager: PreferencesManager
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    var sortOrder = MutableStateFlow(SortOrder.BY_DATE)

    val preferencesFlow = preferencesManager.preferencesFlow

    val hideCompleted = MutableStateFlow(false)

    //    private val taskFlow = searchQuery
    private val taskFlow = combine(
            searchQuery,
            preferencesFlow
//            sortOrder,
//            hideCompleted
    ) { searchQuery, filterPref ->
//        Triple(searchQuery,sortOrder, hideCompleted)
        Pair(searchQuery, filterPref)

//    }.flatMapLatest {(searchQuery, sortOrder, hideCompleted) ->
    }.flatMapLatest {(searchQuery, filterPreferences) ->
                taskDao.getTasks(searchQuery, filterPreferences.sortOrder,filterPreferences.completed)
            }



    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {

        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompleted(completed: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(completed)
    }

    //    val tasks = taskDao.getTasks("blah").asLiveData()
    val tasks = taskFlow.asLiveData()

}
