package com.taskmanagement.presentation.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskmanagement.R
import com.taskmanagement.databinding.ActivitySplashScreenBinding
import com.taskmanagement.presentation.ui.auth.AuthActivity
import com.taskmanagement.presentation.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.progressBar.visibility = View.VISIBLE

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, 3000)
    }

    private fun checkUserLoginStatus() {
        val isLoggedIn = sharedPreferences.getBoolean("loggedIn", false)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
        }
        binding.progressBar.visibility = View.GONE
        finish()
    }
}
