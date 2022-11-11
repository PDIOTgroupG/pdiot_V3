package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class LoginActivity2 : AppCompatActivity() {
    lateinit var username: TextInputEditText
    lateinit var password: TextInputEditText
    lateinit var loginBtn: ImageView
    lateinit var registerBtn: TextView
    lateinit var remberme: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)
        setupViews()
        setupButtons()
    }
    private fun setupViews() {
        username = findViewById(R.id.fragment_login_nameEditText)
        password = findViewById(R.id.fragment_login_passWordfEditText)
    }
    private fun setupButtons(){
        loginBtn = findViewById(R.id.fragment_login_loginButton)
        registerBtn = findViewById(R.id.fragment_login_text_register)
        remberme = findViewById(R.id.fragment_login_savePassword)


        loginBtn.setOnClickListener{
            loginUser()
        }

        registerBtn.setOnClickListener{
            val registerIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

//        remberme.setOnClickListener{
//
//        }

    }

    private fun loginUser() {
        val userID: String = username.text.toString()
        val pwd: String = password.text.toString()
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
        else if (pwd.length<8){
            Toast.makeText(this, "Please set a more complex password", Toast.LENGTH_SHORT).show()
            password.setError("Password length should be greater than 8")
            password.requestFocus()
        }else{
            Toast.makeText(this, "Welcome! "+userID, Toast.LENGTH_SHORT).show()
            val Intent = Intent(this, MainActivity::class.java)
            startActivity(Intent)
        }

    }

}