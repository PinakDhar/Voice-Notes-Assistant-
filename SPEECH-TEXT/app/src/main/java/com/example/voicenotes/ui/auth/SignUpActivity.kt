package com.example.voicenotes.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.voicenotes.R
import com.example.voicenotes.databinding.ActivitySignUpBinding
import com.example.voicenotes.ui.notes.NotesActivity
import com.example.voicenotes.util.hideKeyboard
import com.example.voicenotes.util.showError
import com.example.voicenotes.util.showSnackbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupTextWatchers()
        observeAuthState()
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener { attemptSignUp() }
        binding.signInPromptText.setOnClickListener { navigateToLogin() }
        
        // Handle keyboard done action
        binding.confirmPasswordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptSignUp()
                true
            } else {
                false
            }
        }
    }

    private fun setupTextWatchers() {
        // Clear errors when user starts typing
        binding.nameInput.addTextChangedListener { clearError(binding.nameInputLayout) }
        binding.emailInput.addTextChangedListener { clearError(binding.emailInputLayout) }
        binding.passwordInput.addTextChangedListener { clearError(binding.passwordInputLayout) }
        binding.confirmPasswordInput.addTextChangedListener { clearError(binding.confirmPasswordInputLayout) }
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

    private fun attemptSignUp() {
        hideKeyboard()
        
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val confirmPassword = binding.confirmPasswordInput.text.toString().trim()
        
        var hasError = false
        
        if (name.isEmpty()) {
            binding.nameInputLayout.error = getString(R.string.error_field_required)
            hasError = true
        }
        
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
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.error = getString(R.string.error_field_required)
            hasError = true
        } else if (password != confirmPassword) {
            binding.confirmPasswordInputLayout.error = getString(R.string.error_passwords_dont_match)
            hasError = true
        }
        
        if (!hasError) {
            viewModel.signUp(name, email, password, confirmPassword)
        }
    }

    private fun navigateToLogin() {
        finish()
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
        binding.signUpButton.isEnabled = !show
        binding.signInPromptText.isEnabled = !show
    }

    private fun clearError(inputLayout: TextInputLayout) {
        if (inputLayout.error != null) {
            inputLayout.error = null
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
