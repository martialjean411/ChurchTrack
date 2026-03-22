package com.churchtrack.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.databinding.ActivityLoginBinding
import com.churchtrack.app.ui.MainActivity
import com.churchtrack.app.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                showError("Veuillez remplir tous les champs")
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        val app = application as ChurchTrackApp
        lifecycleScope.launch {
            val user = app.userRepository.authenticate(username, password)
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (user != null) {
                    app.userRepository.let {
                        lifecycleScope.launch {
                            it.updateLastLogin(user.id)
                        }
                    }
                    SessionManager.saveSession(
                        this@LoginActivity,
                        user.id, user.username, user.fullName, user.role
                    )
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                } else {
                    showError("Identifiants incorrects")
                    binding.etPassword.text?.clear()
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
