package com.aurora.workspace.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.aurora.workspace.R
import com.aurora.workspace.data.Store
import com.aurora.workspace.databinding.ActivityAuthBinding
import com.aurora.workspace.util.GoogleAccountHelper
import com.aurora.workspace.util.NotificationHelper
import com.aurora.workspace.util.fitCenteredColumn
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AuthActivity : AuroraActivity() {

    private lateinit var binding: ActivityAuthBinding

    private val googlePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val account = GoogleAccountHelper.accountFrom(this, result)
        if (account != null) {
            val name = account.substringBefore('@')
                .replace('.', ' ')
                .split(' ')
                .filter { it.isNotBlank() }
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            signIn(name, account, account)
        } else {
            showGoogleFallback()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Store.signedIn) {
            goToMain()
            return
        }

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdge()

        // Screen-size adaptation: centered form column on tablets.
        binding.authContent.fitCenteredColumn(560)

        binding.btnGoogle.setOnClickListener { launchGooglePicker() }
        binding.btnContinue.setOnClickListener { continueWithEmail() }
        binding.btnDemo.setOnClickListener {
            signIn("Layla Hassan", "layla@aurora.app", "layla.aurora@gmail.com")
        }
    }

    private fun launchGooglePicker() {
        try {
            googlePicker.launch(GoogleAccountHelper.intent())
        } catch (e: Exception) {
            showGoogleFallback()
        }
    }

    private fun continueWithEmail() {
        val email = binding.emailInput.text.toString().trim()
        if (!email.contains("@") || email.length < 5) {
            binding.emailInput.error = getString(R.string.enter_valid_email)
            return
        }
        val typedName = binding.nameInput.text.toString().trim()
        val name = typedName.ifBlank {
            email.substringBefore('@').replaceFirstChar { it.uppercase() }
        }
        signIn(name, email, null)
    }

    private fun signIn(name: String, email: String, google: String?) {
        Store.signIn(this, name, email, google)
        // Greet the member with a real notification when reminders are enabled
        // and the permission has already been granted.
        if (Store.notifications && NotificationHelper.hasPermission(this)) {
            NotificationHelper.sendWelcomeNotification(this)
        }
        goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showGoogleFallback() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.google_not_linked)
            .setMessage(R.string.google_add_first)
            .setPositiveButton(R.string.use_demo) { _, _ ->
                signIn("Layla Hassan", "layla@aurora.app", "layla.aurora@gmail.com")
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
