package com.example.nikecore.ui.runpaused

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nikecore.database.Run
import com.example.nikecore.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunPausedViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {


    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}