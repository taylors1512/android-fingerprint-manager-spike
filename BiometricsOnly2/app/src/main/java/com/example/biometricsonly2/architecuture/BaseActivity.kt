package com.example.biometricsonly2.architecuture

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.biometricsonly2.activity.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
abstract class BaseActivity: AppCompatActivity() {

    protected val viewModel = MainViewModel()

    override fun onDestroy() {
        super.onDestroy()
        viewModel.reset()
    }
}

fun <T> BaseActivity.observe(flow: Flow<T>, body: (T) -> Unit = {}) {
    flow.onEach { body(it) }
        .launchIn(lifecycleScope)
}