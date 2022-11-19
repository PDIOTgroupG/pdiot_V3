package com.specknet.pdiotapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.specknet.pdiotapp.bean.User

//db = SQLiteOpenHelper.getWritableDatabase()
class RegisterActivity : AppCompatActivity() {
    lateinit var name: TextInputEditText
    lateinit var account: TextInputEditText
    lateinit var password: TextInputEditText
    lateinit var confirm: TextInputEditText
    lateinit var signupBtn: TextView
    lateinit var loginBtn: TextView

    private lateinit var mySQLite:MySQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setupViews()
        mySQLite = MySQLite(this)
        setupButtons()
    }

    private fun setupViews() {
        account = findViewById(R.id.fragment_register_idEditText)
        password = findViewById(R.id.fragment_register_passwordEditText)
        confirm = findViewById(R.id.fragment_register_confirmpasswordEditText)
        name = findViewById(R.id.fragment_register_nameEditText)
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



    private fun insertToDataBase(){
        val UserName:String = name.text.toString().trim()
        val userID: String = account.text.toString().trim()
        val pwd: String = password.text.toString().trim()

        val user: User = User()
        user.setName(UserName)
        user.setUserID(userID)
        user.setPassword(pwd)

        if (mySQLite.checkNoSuchAccount(userID)){
            var rowID = mySQLite.insertRegister(user)
            if (!rowID.equals(-1)){
                Toast.makeText(this, "User Registered successfully! You can now Login in your account", Toast.LENGTH_SHORT).show()
                val Intent = Intent(this, LoginActivity2::class.java)
                startActivity(Intent)
            }
        }else{
            Toast.makeText(this, "Failed to Register! User already existed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(){
        val UserName:String = name.text.toString().trim()
        val userID: String = account.text.toString().trim()
        val pwd: String = password.text.toString().trim()
        val pwd2:String = confirm.text.toString().trim()
        if (UserName.isEmpty()){
            Toast.makeText(this, "Please input your name", Toast.LENGTH_SHORT).show()
            name.setError("Username cannot be empty")
            name.requestFocus()
        }

        else if (userID.isEmpty()){
            Toast.makeText(this, "Please input your email/studentID", Toast.LENGTH_SHORT).show()
            account.setError("User account cannot be empty")
            account.requestFocus()
        }
        else if (pwd.isEmpty()){
            Toast.makeText(this, "Please input your password", Toast.LENGTH_SHORT).show()
            password.setError("Password cannot be empty")
            password.requestFocus()
        }
        else if(pwd.length<8){
            Toast.makeText(this, "Please set your password more complex", Toast.LENGTH_SHORT).show()
            password.setError("Password length should be greater than 8")
            password.requestFocus()
        }
        else if (pwd2.isEmpty()){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
            confirm.setError("Confirmation of your Password cannot be empty")
            confirm.requestFocus()
        }
        else if (pwd != pwd2){
            Toast.makeText(this, "Your confirmation of your password is different", Toast.LENGTH_SHORT).show()
        }
        else{
            insertToDataBase()
        }

    }
}