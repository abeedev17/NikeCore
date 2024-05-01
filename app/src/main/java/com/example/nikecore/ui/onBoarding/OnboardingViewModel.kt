package com.example.nikecore.ui.onBoarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    var i: MutableLiveData<Int> = MutableLiveData(0)

    init {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                i.value = (i.value!! + 1) % 3
            }


        }
    }
}