package com.psi.dpsi.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.psi.dpsi.model.EmailModel
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.SharedPref
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth, val context: Context) {

    private val fireStore = FirebaseFirestore.getInstance()

    val authStatus: MutableLiveData<Boolean> = MutableLiveData()
    val loginStatus: MutableLiveData<Boolean> = MutableLiveData()
    val forgetPassStatus: MutableLiveData<Boolean> = MutableLiveData()
    val verificationLink: MutableLiveData<Boolean> = MutableLiveData()
    val emailRegistered: MutableLiveData<Boolean> = MutableLiveData()


    suspend fun signUpWithEmail(name: String, password: String, phoneNumber: String, email: String, city: String, active: Boolean) {
       try {
           val authResult = auth.createUserWithEmailAndPassword(email, password).await()
           if (authResult.user != null) {
               val userId = authResult.user!!.uid
               registerUser(userId, name, phoneNumber, email, city, active)
           } else {
               authStatus.postValue(false)
           }

       } catch (e: Exception) {
           authStatus.postValue(false)
       }
    }



    suspend fun registerUser(userId: String, name: String, phoneNumber: String, email: String, city: String, active: Boolean) {
        try {
            val userModel = UserModel(userId, name, phoneNumber, email, city, active)
            val userEmail = EmailModel(userId, email)
            Log.d("TAGssss", "registerUser: attmpt to save email")
            fireStore.collection(Constants.EMAIL_REF).document(userId).set(userEmail).await()
            Log.d("TAGssss", "registerUser: attmpt to save email saved")
            Log.d("TAGssss", "registerUser: attmpt to save user cred")
            fireStore.collection(Constants.USER_REF).document(userId).set(userModel).await()
            Log.d("TAGssss", "registerUser: attmpt to save user cred saved")
            SharedPref.saveUserData(context, userModel)
            authStatus.postValue(true)

        } catch (e: Exception) {
            authStatus.postValue(false)
        }

    }

    suspend fun isEmailAvailable(email: String) {
        try {
            val query = fireStore.collection(Constants.EMAIL_REF).whereEqualTo("email", email)
            val result = query.get().await()
            emailRegistered.postValue(!result.isEmpty)
        } catch (e: Exception) {
            emailRegistered.postValue(false)
        }

    }

    private fun fetchUserData(uid: String) {
        fireStore.collection(Constants.USER_REF).document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }
                    if (documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(UserModel::class.java)
                        user?.let {
                            SharedPref.saveUserData(context, user)
                            loginStatus.postValue(true)
                        }
                    } else {
                        loginStatus.postValue(false)
                    }
                })
            }
            .addOnFailureListener {
                loginStatus.postValue(false)
            }
    }



    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = Firebase.auth.currentUser!!.uid
                    fetchUserData(userId)
                } else {
                    authStatus.postValue(false)
                }
            }.await()

    }

    fun signOut() {
        SharedPref.clearData(context)
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        forgetPassStatus.postValue(true)
                    } else {
                        forgetPassStatus.postValue(false)
                    }
                }.await()
        }catch (e: Exception) {
            forgetPassStatus.postValue(false)
        }
    }

    fun sendVerificationEmail() {
        try {
            auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        verificationLink.postValue(true)
                    } else {
                        verificationLink.postValue(false)
                    }
                }
        } catch (e: Exception) {
            verificationLink.postValue(false)
        }
    }

}