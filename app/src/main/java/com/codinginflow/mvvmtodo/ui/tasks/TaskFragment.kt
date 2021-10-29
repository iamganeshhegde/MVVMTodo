package com.codinginflow.mvvmtodo.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTaskBinding
import com.codinginflow.mvvmtodo.util.exhuastive
import com.codinginflow.mvvmtodo.util.onQuerySearchChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task), TasksAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView:SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTaskBinding.bind(view)

        val taskAdapter = TasksAdapter(this)

        binding.apply {
            recycleViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    var task = taskAdapter.currentList[viewHolder.adapterPosition]

                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(recycleViewTasks)

            fabAddTask.setOnClickListener {
                viewModel.onAddNewtaskClick()
            }
        }

        setFragmentResultListener("add_edit_request"){
            _,bundle -> val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect {
                event ->
                when(event) {
                    is TasksViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(),"Task deleted", Snackbar.LENGTH_SHORT).setAction("Undo" ){
                            viewModel.unDoDeleteClick(event.task)
                        }.show()
                    }
                    is TasksViewModel.TaskEvent.NavigateToAddTAskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(null, "New task")
                        findNavController().navigate(action)

                    }
                    is TasksViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action = TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(event.task, "Edit Task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TaskEvent.showTAskSavedConfirmationMessage -> {

                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_SHORT).show()
                    }
                    TasksViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {

                        val action = TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhuastive
            }
        }


        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value

        if(pendingQuery != null && pendingQuery.isNotEmpty()) {

            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }

        searchView.onQuerySearchChanged {
            // update search query
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked = viewModel.preferencesFlow.first().completed
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_sort_by_name -> {

//                viewModel.sortOrder.value = SortOrder.BY_NAME

                viewModel.onSortOrderSelected(SortOrder.BY_NAME)

                true
            }
            R.id.action_sort_by_date_created -> {

//                viewModel.sortOrder.value = SortOrder.BY_DATE

                viewModel.onSortOrderSelected(SortOrder.BY_DATE)


                true
            }
            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
//                viewModel.hideCompleted.value = item.isChecked
                viewModel.onHideCompleted(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_tasks -> {
                viewModel.onDeleteAllCompletedClick()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemCLick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        searchView.setOnQueryTextListener(null)
    }
}