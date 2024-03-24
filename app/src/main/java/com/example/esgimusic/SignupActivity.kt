package com.example.esgimusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.esgimusic.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountBtn.setOnClickListener{
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            val confirmPassword = binding.confirmPasswordEdittext.text.toString()
            if(!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)){
                binding.emailEdittext.setError("Invalid email")
                return@setOnClickListener
            }
            if(password.length < 6){
                binding.passwordEdittext.setError("Password must be at least 6 characters")
                return@setOnClickListener
            }
            if(password != confirmPassword){
                binding.confirmPasswordEdittext.setError("Passwords do not match")
                return@setOnClickListener
            }

            createAccountWithFirebase(email, password)
        }
        binding.gotoLoginBtn.setOnClickListener {
            finish()
        }
    }

    fun createAccountWithFirebase(email: String, password: String){
        // Firebase authentication
        setInProgress(true)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Account created successfully", Toast.LENGTH_SHORT).show()
                setInProgress(false)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Create account failed", Toast.LENGTH_SHORT).show()
                setInProgress(false)
            }
    }

    fun setInProgress(inProgress: Boolean){
        if(inProgress){
            binding.createAccountBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.createAccountBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }
}