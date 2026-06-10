package com.example.mylog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mylog.ui.MylogApp
import com.example.mylog.ui.MylogViewModel
import com.example.mylog.ui.theme.MylogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as MylogApplication).container
        setContent {
            MylogTheme {
                val viewModel: MylogViewModel = viewModel(factory = MylogViewModelFactory(container))
                MylogApp(viewModel = viewModel)
            }
        }
    }
}

class MylogViewModelFactory(
    private val container: MylogContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MylogViewModel::class.java)) {
            return MylogViewModel(
                authRepository = container.authRepository,
                diaryRepository = container.diaryRepository,
                recordRepository = container.recordRepository,
                photoRepository = container.photoRepository,
                routineRepository = container.routineRepository,
                notificationRepository = container.notificationRepository,
                recordCategoryStore = container.recordCategoryStore
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
