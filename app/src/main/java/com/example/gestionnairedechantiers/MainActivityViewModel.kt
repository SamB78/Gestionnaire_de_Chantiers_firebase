package com.example.gestionnairedechantiers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.entities.Personnel
import com.example.gestionnairedechantiers.entities.User
import com.example.gestionnairedechantiers.firebase.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivityViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val _logoutObserve = MutableLiveData<Boolean>().apply { value = false }
    val logoutObseve: LiveData<Boolean>
        get() = _logoutObserve

    private var _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user


    init {
        viewModelScope.launch {
            _user.value = authRepository.getDataUser()
            Timber.i("${user.value}")
            if (user.value == null) {
                logOut()
            }
        }
    }


    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        _logoutObserve.value = true
        Timber.i("logout viewModel")
    }

}