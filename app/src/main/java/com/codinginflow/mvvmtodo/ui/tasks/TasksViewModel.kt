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


// 1,2,3,4,5,6

fun maxCoins(A: IntArray): Int {
    Arrays.sort(A)
    var res = 0
    val n = A.size
    var i = n / 3
    while (i < n) {
        res += A[i]
        i += 2
    }
    return res
}