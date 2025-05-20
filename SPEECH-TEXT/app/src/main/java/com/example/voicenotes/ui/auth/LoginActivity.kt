package com.example.voicenotes.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.voicenotes.R
import com.example.voicenotes.databinding.ActivityLoginBinding
import com.example.voicenotes.ui.notes.NotesActivity
import com.example.voicenotes.util.hideKeyboard
import com.example.voicenotes.util.showError
import com.example.voicenotes.util.showSnackbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupTextWatchers()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener { attemptLogin() }
        binding.signUpPromptText.setOnClickListener { navigateToSignUp() }
        
        binding.passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }

    private fun setupTextWatchers() {
        // Clear errors when user starts typing
        binding.emailInput.addTextChangedListener { clearError(binding.emailInputLayout) }
        binding.passwordInput.addTextChangedListener { clearError(binding.passwordInputLayout) }
    }

    private fun observeAuthState() {
        viewModel.authState.collectWhenStarted(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Success -> navigateToNotes()
                is AuthState.Error -> handleError(state.message)
                is AuthState.Initial -> showLoading(false)
            }
        }
    }

    private fun attemptLogin() {
        hideKeyboard()
        
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        
        var hasError = false
        
        if (email.isEmpty()) {
            binding.emailInputLayout.error = getString(R.string.error_field_required)
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = getString(R.string.error_invalid_email)
            hasError = true
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.error = getString(R.string.error_field_required)
            hasError = true
        } else if (password.length < 6) {
            binding.passwordInputLayout.error = getString(R.string.error_password_too_short)
            hasError = true
        }
        
        if (!hasError) {
            viewModel.signIn(email, password)
        }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToNotes() {
        startActivity(Intent(this, NotesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun handleError(message: String) {
        showLoading(false)
        showSnackbar(binding.root, message, Snackbar.LENGTH_LONG)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.signUpPromptText.isEnabled = !show
    }

    private fun clearError(inputLayout: TextInputLayout) {
        if (inputLayout.error != null) {
            inputLayout.error = null
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }
}

// Extension function to collect flows in a lifecycle-aware manner
fun <T> androidx.lifecycle.LiveData<T>.observe(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    crossinline onChanged: (T) -> Unit
) {
    observe(lifecycleOwner, androidx.lifecycle.Observer { it?.let(onChanged) })
}

// Extension function to collect flows in a lifecycle-aware manner
fun <T> kotlinx.coroutines.flow.Flow<T>.collectWhenStarted(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    crossinline onChanged: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launchWhenStarted {
        collect { onChanged(it) }
    }
}
