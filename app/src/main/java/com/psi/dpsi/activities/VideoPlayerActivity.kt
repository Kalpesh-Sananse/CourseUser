package com.psi.dpsi.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.psi.dpsi.databinding.ActivityVideoPlayerBinding
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions


class VideoPlayerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityVideoPlayerBinding.inflate(layoutInflater) }
    private lateinit var youtubePlayer: YouTubePlayer
    private var isFullScreen = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if(isFullScreen) {
                youtubePlayer.toggleFullscreen()
            } else {
                finish()
            }
        }
    }

    //is done
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        val myIntent = intent.getStringExtra(Constants.COURSE)

        val youtubePlayerView = binding.ytPlayer
        val fullScreenContainer = binding.ytPlayerFullScreen
        lifecycle.addObserver(youtubePlayerView)


        youtubePlayerView.addFullscreenListener(object : FullscreenListener{
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                isFullScreen = true
                fullscreenView.visible()
                fullScreenContainer.addView(fullscreenView)

//                WindowInsetsControllerCompat(window!!, binding.main).apply {
//                    hide(WindowInsetsCompat.Type.systemBars())
//                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//                }

                if(requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }


            }

            override fun onExitFullscreen() {
                isFullScreen = false
                fullScreenContainer.gone()
                fullScreenContainer.removeAllViews()

                WindowInsetsControllerCompat(window!!, binding.main).apply {
                    WindowInsetsControllerCompat(window!!, binding.main).apply {
                        show(WindowInsetsCompat.Type.systemBars())
                    }

                    if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                    }
                }
            }

        })

        //getting video id here
        val videoId = Utils.extractVideoIdFromYouTubeUrl(myIntent!!)
        val youtubePlayerListener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@VideoPlayerActivity.youtubePlayer = youTubePlayer
                youTubePlayer.loadVideo(videoId!!, 0f)
                youtubePlayer.play()

            }
        }

        val iFramePlayer = IFramePlayerOptions.Builder()
            .controls(1)
            .fullscreen(1)
            .build()

        youtubePlayerView.enableAutomaticInitialization = false
        youtubePlayerView.initialize(youtubePlayerListener, iFramePlayer)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(!isFullScreen) {
                youtubePlayer.toggleFullscreen()
            }
        } else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if(isFullScreen) {
                youtubePlayer.toggleFullscreen()
            }
        }


    }


}