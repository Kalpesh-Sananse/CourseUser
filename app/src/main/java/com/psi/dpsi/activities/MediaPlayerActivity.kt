package com.psi.dpsi.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.psi.dpsi.databinding.ActivityMediaPlayerBinding
import com.psi.dpsi.utils.Constants

class MediaPlayerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMediaPlayerBinding.inflate(layoutInflater) }
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var videoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        videoUrl = intent.getStringExtra(Constants.COURSE)!!

        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()



        onBackPressedDispatcher.addCallback(this@MediaPlayerActivity, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        })

    }


    override fun onStart() {
        super.onStart()
        exoPlayer.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}



