package com.example.biometricsonly3.architecuture

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.biometricsonly3.activity.MainViewModel
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