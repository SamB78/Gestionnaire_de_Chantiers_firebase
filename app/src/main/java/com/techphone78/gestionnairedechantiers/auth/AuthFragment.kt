package com.techphone78.gestionnairedechantiers.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.FragmentAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber


class AuthFragment : Fragment() {
    private val RC_SIGN_IN = 123

    private val viewModel: AuthViewModel by activityViewModels()
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAuthBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        initGoogleSignInClient()

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            when (navigation) {
                AuthViewModel.Navigation.AUTHENTIFICATION -> {
                    val signInIntent = googleSignInClient!!.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
                AuthViewModel.Navigation.PASSAGE_ECRAN_PRINCIPAL->{
                    val action = AuthFragmentDirections.actionAuthFragmentToListeChantiersFragment()
                    findNavController().navigate(action)
                    viewModel.onButtonClicked()
                }
            }
        })


        // Inflate the layout for this fragment
        return binding.root
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
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
    }
}