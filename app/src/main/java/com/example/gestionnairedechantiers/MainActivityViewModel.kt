package com.example.gestionnairedechantiers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.entities.User
import com.example.gestionnairedechantiers.firebase.AuthRepository
import com.example.gestionnairedechantiers.utils.State
import com.example.gestionnairedechantiers.utils.Status
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

    var state = MutableLiveData(State(Status.LOADING))


    init {
        viewModelScope.launch {
            try {
                _user.value = authRepository.getDataUser()
                state.value = State.success()
            } catch (e: Exception) {
                Timber.i("ERROR getDataUser: $e")
                state.value = State.error(e.toString())
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