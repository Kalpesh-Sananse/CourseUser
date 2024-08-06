package com.psi.dpsi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psi.dpsi.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository): ViewModel() {

    val authStatus: LiveData<Boolean> = authRepository.authStatus
    val verificationLink: LiveData<Boolean> = authRepository.verificationLink
    val loginStatus: LiveData<Boolean> = authRepository.loginStatus
    val forgetPassStatus: LiveData<Boolean> = authRepository.forgetPassStatus
    val emailRegistered: LiveData<Boolean> = authRepository.emailRegistered



    fun registerUser(name: String, password: String, phoneNumber: String, email: String, city: String, active: Boolean) {
        viewModelScope.launch {
            try {
                authRepository.signUpWithEmail(name, password, phoneNumber, email, city, active)
            } catch (e: Exception) {
                authRepository.authStatus.postValue(false)
            }
        }
    }

    fun registerGoogleUser(userId: String, name: String,  phoneNumber: String, email: String, city: String, active: Boolean) {
        viewModelScope.launch {
            try {
                authRepository.registerUser(userId, name, phoneNumber, email, city, active)
            } catch (e: Exception) {
                authRepository.authStatus.postValue(false)
            }
        }
    }

    fun singInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.signInWithEmail(email, password)
            } catch (e: Exception) {
                authRepository.loginStatus.postValue(false)
            }
        }

    }

    fun checkIfEmailRegistered(email: String) {
        viewModelScope.launch {
            try {
                authRepository.isEmailAvailable(email)
            } catch (e: Exception) {
                authRepository.forgetPassStatus.postValue(false)
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetEmail(email)
            } catch (e: Exception) {
                authRepository.forgetPassStatus.postValue(false)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun sendVerificationLink() {
        authRepository.sendVerificationEmail()
    }

}