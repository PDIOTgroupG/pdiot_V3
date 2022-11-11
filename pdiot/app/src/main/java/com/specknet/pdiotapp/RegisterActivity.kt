package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {
    lateinit var username: TextInputEditText
    lateinit var password: TextInputEditText
    lateinit var confirm: TextInputEditText
    lateinit var signupBtn: TextView
    lateinit var loginBtn: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupViews()
        setupButtons()
    }

    private fun setupViews() {
        username = findViewById(R.id.fragment_register_idEditText)
        password = findViewById(R.id.fragment_register_passwordEditText)
        confirm = findViewById(R.id.fragment_register_confirmpasswordEditText)
    }

    private fun setupButtons(){
        loginBtn = findViewById(R.id.fragment_register_text_Login)
        signupBtn = findViewById(R.id.fragment_register_text_register)

        loginBtn.setOnClickListener{
            val loginIntent = Intent(this, LoginActivity2::class.java)
            startActivity(loginIntent)
        }

        signupBtn.setOnClickListener{
            registerUser()
        }
    }

    private fun registerUser(){
        val userID: String = username.text.toString()
        val pwd: String = password.text.toString()
        val pwd2:String = confirm.text.toString()
        if (userID.isEmpty()){
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            username.setError("Username cannot be empty")
            username.requestFocus()
        }
        else if (pwd.isEmpty()){
            Toast.makeText(this, "Please input your password", Toast.LENGTH_SHORT).show()
            password.setError("Password cannot be empty")
            password.requestFocus()
        }
        else if (pwd2.isEmpty()){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
            confirm.setError("Confirmation of your Password cannot be empty")
            confirm.requestFocus()
        }
        else if (pwd != pwd2){
            Toast.makeText(this, "Your confirmation of your password is different", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "User Registered successfully!", Toast.LENGTH_SHORT).show()
            val Intent = Intent(this, LoginActivity2::class.java)
            startActivity(Intent)
        }

    }
}