package com.example.gestionnairedechantiers.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestionnairedechantiers.firebase.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import timber.log.Timber


class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private var _isConnected = MutableLiveData<Boolean>(false)
    private val isConnected: LiveData<Boolean>
        get() = _isConnected
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage
//    fun checkUserLogin(): LiveData<Boolean> {
//        checkIfUserIsConnected()
//        return isConnected
//    }

    enum class Navigation {
        AUTHENTIFICATION,
        PASSAGE_CHOIX_CHANTIER,
        DECONNECTION,
        EN_ATTENTE
    }

    private var _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation>
        get() = this._navigation

    init {
        checkIfUserIsConnected()
        Timber.i("test")
    }

    fun onClickAuth() {
        _navigation.value = Navigation.AUTHENTIFICATION
    }

    fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogle(googleAuthCredential)
    }

    private fun signInWithGoogle(googleAuthCredential: AuthCredential) {
        Timber.i("signIn with google")
        viewModelScope.launch {
            _isConnected.value = authRepository.authWithGoogle(googleAuthCredential)
            navigateIfUserIsConnected()
        }
    }

    fun onButtonClicked() {
        _navigation.value = Navigation.EN_ATTENTE
    }

    private fun checkIfUserIsConnected() {
        _isConnected.value = FirebaseAuth.getInstance().currentUser != null
        navigateIfUserIsConnected()
    }

    private fun navigateIfUserIsConnected() {
        if (isConnected.value!!) {
            _navigation.value = Navigation.PASSAGE_CHOIX_CHANTIER
        }
    }

    fun setErrorMessage(string: String) {
        _errorMessage.value = string
        Timber.i(" ${_errorMessage.value}")
    }


}