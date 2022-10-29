package com.techphone78.gestionnairedechantiers.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber


class AuthActivity : AppCompatActivity() {
    private val RC_SIGN_IN = 123
    private var googleSignInClient: GoogleSignInClient? = null
    private val viewModel: AuthViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        val errorMessage = intent.getStringExtra("error")
        Timber.i("errorMessage: $errorMessage")
        Timber.e("test")
        val binding: ActivityAuthBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initGoogleSignInClient()

        viewModel.navigation.observe(this) { navigation ->
            when (navigation) {
                AuthViewModel.Navigation.AUTHENTIFICATION -> {
                    val signInIntent = googleSignInClient!!.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                    viewModel.onButtonClicked()
                }
                AuthViewModel.Navigation.PASSAGE_ECRAN_PRINCIPAL -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                    viewModel.onButtonClicked()
                }
                else -> {}
            }
        }

        errorMessage?.let {
            viewModel.setErrorMessage(it)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(
                    ApiException::class.java
                )
                googleSignInAccount?.let { viewModel.getGoogleAuthCredential(it) }
            } catch (e: ApiException) {
                Timber.e(e)
            }
        }
    }

    private fun initGoogleSignInClient() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }
}