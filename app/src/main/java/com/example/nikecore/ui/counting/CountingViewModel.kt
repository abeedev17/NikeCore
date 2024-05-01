package com.example.nikecore.ui.counting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountingViewModel : ViewModel() {

    var number: MutableLiveData<Int> = MutableLiveData(1)

    val navigateToAccountingFragment = MutableLiveData(false)

    init {
        viewModelScope.launch {
            repeat(3) {
                delay(1000)
                if (it != 2)
                    number.value = it + 2
            }
            navigateToAccountingFragment.value = true
        }
    }
}