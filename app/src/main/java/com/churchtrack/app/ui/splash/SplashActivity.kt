package com.churchtrack.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.churchtrack.app.R
import com.churchtrack.app.databinding.ActivitySplashBinding
import com.churchtrack.app.ui.MainActivity
import com.churchtrack.app.ui.auth.LoginActivity
import com.churchtrack.app.util.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate views
        val fadeUp = AnimationUtils.loadAnimation(this, R.anim.fade_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        binding.ivCross.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(fadeUp)
        binding.tvSubtitle.startAnimation(fadeUp)
        binding.tvVerse.startAnimation(fadeUp)
        binding.progressBar.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 3000)
    }

    private fun navigateNext() {
        val intent = if (SessionManager.isLoggedIn(this)) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
