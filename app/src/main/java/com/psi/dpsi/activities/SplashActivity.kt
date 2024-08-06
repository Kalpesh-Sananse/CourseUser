package com.psi.dpsi.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.psi.dpsi.databinding.ActivitySplashBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var textView: TextView
    private val textToAnimate = "Journey from constable to PSI"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        handler.postDelayed({
            if(Firebase.auth.currentUser != null) {
                startActivity(Intent(this@SplashActivity, HomeMainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                finish()
            }

        },2500)

        textView = binding.textView

        animateText()

    }

    private fun animateText() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentIndex < textToAnimate.length) {
                    textView.append(textToAnimate[currentIndex].toString())
                    currentIndex++
                    handler.postDelayed(this, 80)
                }
            }
        }, 70)
    }


}