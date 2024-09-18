package com.taskmanagement.presentation.ui.auth

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.taskmanagement.presentation.ui.auth.LoginFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.taskmanagement.R
import com.taskmanagement.databinding.ActivityAuthBinding
import com.taskmanagement.databinding.ActivitySplashScreenBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.viewPager.adapter = AuthPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Login"
                1 -> "Register"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }
    private inner class AuthPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> LoginFragment()
                1 -> RegisterFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
            finish()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }
}