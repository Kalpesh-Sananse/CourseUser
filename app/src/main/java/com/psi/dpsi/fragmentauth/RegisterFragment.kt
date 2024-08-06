package com.psi.dpsi.fragmentauth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.psi.dpsi.R
import com.psi.dpsi.activities.HomeMainActivity
import com.psi.dpsi.databinding.FragmentRegisterBinding
import com.psi.dpsi.factory.AuthViewModelFactory
import com.psi.dpsi.repository.AuthRepository
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class RegisterFragment : Fragment() {
    private val binding by lazy { FragmentRegisterBinding.inflate(layoutInflater) }
    private lateinit var storage: FirebaseStorage
    private lateinit var authViewModel: AuthViewModel
    private lateinit var progress: AlertDialog
    private val google: RegisterFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance()
        progress = Utils.showLoading(requireContext())

        val authRepository = AuthRepository(FirebaseAuth.getInstance(), requireContext())
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        if(google.google) {
            val uId = FirebaseAuth.getInstance().currentUser
            binding.etEmailId.setText(uId?.email)
            binding.etConfirmPasswordLayout.gone()
            binding.etCreatePasswordLayout.gone()
            binding.btRegister.text = "Save & Continue"
            binding.etEmailId.isEnabled = false
        } else {
            binding.etConfirmPasswordLayout.visible()
            binding.etCreatePasswordLayout.visible()
            binding.btRegister.text = "Register Account"
            binding.etEmailId.isEnabled = true
        }


        authViewModel.authStatus.observe(viewLifecycleOwner) { success ->
            if(success) {
                if(google.google) {
                    startActivity(Intent(requireActivity(), HomeMainActivity::class.java))
                    progress.dismiss()
                    requireActivity().finish()
                } else {
                    val action = RegisterFragmentDirections.actionRegisterFragmentToVerificationFragment(binding.etEmailId.text.toString())
                    findNavController().navigate(action)
                    progress.dismiss()
                }

            } else {
                progress.dismiss()
                Utils.showMessage(requireContext(), "Registration Failed")
            }

        }


        binding.apply {

            tvLogin.setOnClickListener {
                Utils.navigate(it, R.id.action_registerFragment_to_loginFragment)
            }

            tvLogin1.setOnClickListener {
                Utils.navigate(it, R.id.action_registerFragment_to_loginFragment)
            }

            btRegister.setOnClickListener {
                validateUserInput()
            }
        }

    }

    private fun validateUserInput() {
        binding.apply {
            val message = "Empty"
            if (name.text.toString().isEmpty()) {
                name.requestFocus()
                name.error = message
            } else if (etMobileNumber.text.toString().isEmpty()) {
                etMobileNumber.requestFocus()
                etMobileNumber.error = message
            } else if (etCityName.text.toString().isEmpty()) {
                etCityName.requestFocus()
                etCityName.error = message
            } else if (etEmailId.text.toString().isEmpty()) {
                etEmailId.requestFocus()
                etEmailId.error = message
            }  else if(google.google) {
                progress.show()
                val uId = FirebaseAuth.getInstance().currentUser?.uid
                authViewModel.registerGoogleUser(uId!!, name.text.toString(), etMobileNumber.text.toString(), etEmailId.text.toString(), etCityName.text.toString(), true )
            } else if (etCreatePassword.text.toString().isEmpty()) {
                etCreatePassword.requestFocus()
                etCreatePassword.error = message
            } else if (etConfirmPassword.text.toString().isEmpty()) {
                etConfirmPassword.requestFocus()
                etConfirmPassword.error = message
            }  else if (etConfirmPassword.text.toString() != etCreatePassword.text.toString()) {
                etConfirmPassword.error = "Error"
                etCreatePassword.error = "Error"
            } else {
                progress.show()
                createRegister()
            }
        }
    }


    private fun createRegister() {
        binding.apply {
            authViewModel.registerUser(
                name.text.toString(),
                etConfirmPassword.text.toString(),
                etMobileNumber.text.toString(),
                etEmailId.text.toString(),
                etCityName.text.toString(),
               true
            )
        }
    }


}