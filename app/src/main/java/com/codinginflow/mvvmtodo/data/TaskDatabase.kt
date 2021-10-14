package com.codinginflow.mvvmtodo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao():TaskDao
    class CallBack @Inject constructor(
        private val database:Provider<TaskDatabase>,
        @ApplicationScope private val appScope: CoroutineScope
    ): RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // db operation

            val dao = database.get().taskDao()


            appScope.launch {
                dao.insert(Task("Call mom", important = true))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("Do the laundry"))
                dao.insert(Task("prepare food", completed = true))
                dao.insert(Task("Repair the bike"))
                dao.insert(Task("visit grandma", completed = true))
                dao.insert(Task("call elon musk"))
            }

        }
    }

}