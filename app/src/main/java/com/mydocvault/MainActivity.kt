package com.mydocvault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.mydocvault.ui.MyDocVaultApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        val error = intent.getStringExtra("global_error")
        if (error != null) {
            Toast.makeText(this, "Recovered from error: $error", Toast.LENGTH_LONG).show()
        }

        setContent {
            MyDocVaultApp()
        }
    }
}
