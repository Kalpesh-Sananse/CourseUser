package com.psi.dpsi.fragmentauth

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.psi.dpsi.R
import com.psi.dpsi.activities.HomeMainActivity
import com.psi.dpsi.databinding.FragmentLoginBinding
import com.psi.dpsi.factory.AuthViewModelFactory
import com.psi.dpsi.repository.AuthRepository
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment() {
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }
    private lateinit var authViewModel: AuthViewModel
    private lateinit var progress: AlertDialog
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        progress = Utils.showLoading(requireContext())

        val authRepository = AuthRepository(FirebaseAuth.getInstance(), requireContext())
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.Web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }





        authViewModel.loginStatus.observe(viewLifecycleOwner) { success ->

            val user = Firebase.auth.currentUser
            val emailVerified = user?.isEmailVerified ?: false
            if(success) {
                if(emailVerified) {
                    startActivity(Intent(requireActivity(), HomeMainActivity::class.java))
                    requireActivity().finish()
                    progress.dismiss()
                } else {
                    progress.dismiss()
                    Utils.showMessage(requireContext(), "Verify Your Email")
                }
            } else {
                progress.dismiss()
                Utils.showMessage(requireContext(), "Please Check Email & Password")
            }

        }


        binding.apply {

            btLogin.setOnClickListener {
                Utils.hideKeyboard(requireContext(), it)
                validateLogin()
            }

            tvCreateAc.setOnClickListener {
                Utils.navigate(it, R.id.action_loginFragment_to_registerFragment)
            }

            tvCreateAccount.setOnClickListener {
                Utils.navigate(it, R.id.action_loginFragment_to_registerFragment)
            }

            tvForgetPassword.setOnClickListener {
                Utils.navigate(it, R.id.action_loginFragment_to_forgetPasswordFragment)
            }


        }


    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        } else {
            Toast.makeText(requireContext(), "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {

            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(requireContext(), "Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credentials).addOnCompleteListener {

            if (it.isSuccessful) {
                val currentUser: FirebaseUser? = auth.currentUser
                if (currentUser != null) {
                    val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment(true)
                    findNavController().navigate(action)
                }



            } else {
                Toast.makeText(requireContext(), "Something went Wrong", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun validateLogin() {
        binding.apply {
            val message = "Empty"
            if (etEmail.text.toString().isEmpty()) {
                etEmail.requestFocus()
                etEmail.error = message
            } else if (etPassword.text.toString().isEmpty()) {
                etPassword.requestFocus()
                etPassword.error = message
            } else {
                progress.show()
                authViewModel.singInWithEmail(etEmail.text.toString(), etPassword.text.toString())
            }
        }
    }


}