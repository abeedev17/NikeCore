package com.example.nikecore.ui.nfc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nikecore.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NfcViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
    val enterAmount: MutableLiveData<Int> = MutableLiveData(0)
}