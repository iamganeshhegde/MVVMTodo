package com.codinginflow.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PreferencesManager"
data class FilePreferences(val sortOrder: SortOrder, val completed:Boolean)

enum class SortOrder {
    BY_NAME,
    BY_DATE
}

@Singleton
class PreferencesManager @Inject constructor(
        @ApplicationContext context: Context
) {

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
            .catch { exception ->
                if(exception is IOException) {
                    Log.e(TAG, "Error reading preferences: $exception", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val sortOrder = SortOrder.valueOf(
                        preferences[PreferenceKeys.SORT_ORDER]?:SortOrder.BY_DATE.name
                )
                val completed = preferences[PreferenceKeys.HIDE_COMPLETED] ?: false

                FilePreferences(sortOrder,completed)
            }


    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.HIDE_COMPLETED] = hideCompleted
        }
    }


    private object PreferenceKeys  {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("completed")
    }
}