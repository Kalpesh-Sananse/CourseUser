package com.psi.dpsi.fragmentauth

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.psi.dpsi.R
import com.psi.dpsi.databinding.FragmentVerificationBinding
import com.psi.dpsi.factory.AuthViewModelFactory
import com.psi.dpsi.repository.AuthRepository
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth


class VerificationFragment : Fragment() {
    private val binding by lazy { FragmentVerificationBinding.inflate(layoutInflater) }
    private lateinit var authViewModel: AuthViewModel
    private lateinit var progress: AlertDialog
    private val emailId : VerificationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = Utils.showLoading(requireContext())

        val authRepository = AuthRepository(FirebaseAuth.getInstance(), requireContext())
        val factory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        authViewModel.verificationLink.observe(viewLifecycleOwner) { success ->
            if(success) {
                progress.dismiss()
                binding.btLogin.text = "Login Account"
                Utils.showMessage(requireContext(), "Verification Link Sent")

                binding.btLogin.setOnClickListener {
                    if(success) {
                        findNavController().navigate(R.id.action_verificationFragment_to_loginFragment)
                    } else {
                        Utils.showMessage(requireContext(), "Please Wait")
                    }
                }

            } else {
                progress.dismiss()
                Utils.showMessage(requireContext(), "Link Send Failed")
            }

        }

        binding.apply {
            tvEmailId.text = emailId.emailId

            btLogin.setOnClickListener {
                progress.show()
                authViewModel.sendVerificationLink()
            }
        }
    }


}