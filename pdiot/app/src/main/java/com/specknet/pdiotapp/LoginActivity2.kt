package com.specknet.pdiotapp

import android.content.Intent
import android.content.SharedPreferences
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
    private lateinit var mySQLite:MySQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        deleteDatabase("mySQLite.db")
        setContentView(R.layout.activity_login2)
        setupViews()
        setupButtons()
        initData()
        mySQLite = MySQLite(this)
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

    }
    private fun initData(){
        val spf:SharedPreferences = getSharedPreferences("spfRecordID", MODE_PRIVATE)
        val isRemember = spf.getBoolean("isRemember",false)
        val account = spf.getString("account", "")
        val pwd = spf.getString("password", "")

        if (isRemember){
            username.setText(account)
            password.setText(pwd)
            remberme.setChecked(true)
        }
    }

    private fun loginUser() {
        val userID: String = username.text.toString().trim()
        val pwd: String = password.text.toString().trim()
        if (userID.isEmpty()){
            Toast.makeText(this, "Please input your Email/studentID", Toast.LENGTH_SHORT).show()
            username.setError("Username cannot be empty")
            username.requestFocus()
        }
        else if (pwd.isEmpty()){
            Toast.makeText(this, "Please input your password", Toast.LENGTH_SHORT).show()
            password.setError("Password cannot be empty")
            password.requestFocus()
        }
        else if (mySQLite.checkNoSuchAccount(userID)){
            Toast.makeText(this, "Please register first!", Toast.LENGTH_SHORT).show()
            username.setError("There is no such account existed!")
            username.requestFocus()
        }
        else if(mySQLite.checkPwd(userID) != pwd){
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
            password.setError("The input password is not correct!")
            password.requestFocus()
        }
        else if(mySQLite.checkPwd(userID) == pwd){
            if(remberme.isChecked){
                val spf:SharedPreferences = getSharedPreferences("spfRecordID", MODE_PRIVATE)
                val edit:SharedPreferences.Editor = spf.edit()
                edit.putString("account",userID)
                edit.putString("password",pwd)
                edit.putBoolean("isRemember",true)
                edit.apply()
            }else{
                val spf:SharedPreferences = getSharedPreferences("spfRecordID", MODE_PRIVATE)
                val edit:SharedPreferences.Editor = spf.edit()
                edit.putBoolean("isRemeber",false)
                edit.apply()
            }

            val Intent = Intent(this, MainActivity::class.java)
            val account_name:String = mySQLite.NameOfAccount(userID)
            Toast.makeText(this, "Welcome! "+account_name, Toast.LENGTH_SHORT).show()
            Intent.putExtra("account_name",account_name)

            startActivity(Intent)
            this.finish()
        }else{
            Toast.makeText(this, "Please register first!", Toast.LENGTH_SHORT).show()
            username.setError("There is no such account existed!")
            username.requestFocus()
        }
    }
}