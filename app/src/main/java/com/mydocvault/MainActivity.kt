package com.mydocvault

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.WindowManager
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
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        
        val error = intent?.getStringExtra("global_error")
        if (error != null) {
            Toast.makeText(this, "Recovered from error: $error", Toast.LENGTH_LONG).show()
        }

        val openDocId = intent?.getLongExtra("open_document_id", -1L)?.takeIf { it > 0 }

        setContent {
            MyDocVaultApp(initialDocumentId = openDocId)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val openDocId = intent.getLongExtra("open_document_id", -1L).takeIf { it > 0 }
        setContent {
            MyDocVaultApp(initialDocumentId = openDocId)
        }
    }
}
