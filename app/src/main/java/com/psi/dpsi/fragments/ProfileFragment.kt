package com.psi.dpsi.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.psi.dpsi.activities.WelcomeActivity
import com.psi.dpsi.databinding.FragmentProfileBinding
import com.psi.dpsi.factory.AuthViewModelFactory
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.repository.AuthRepository
import com.psi.dpsi.utils.SharedPref
import com.psi.dpsi.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth


class ProfileFragment : Fragment() {
    private val binding by lazy { FragmentProfileBinding.inflate(layoutInflater) }
    private lateinit var user: UserModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = AuthRepository(FirebaseAuth.getInstance(),  requireContext())
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        user = SharedPref.getUserData(requireContext()) ?: UserModel()

        setData(user)

    }

    private fun setData(user: UserModel) {
        binding.apply {
            etHomeStayName.setText(user.name)
            etCityName.setText(user.city)
            etEmailId.setText(user.email)
            etMobileNumber.setText(user.phoneNumber)

            btLogout.setOnClickListener {
                authViewModel.signOut()
                val intent = Intent(requireActivity(), WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }


        }
    }

}