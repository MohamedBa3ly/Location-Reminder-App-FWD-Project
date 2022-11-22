package com.udacity.project4

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout for this activity
        binding = DataBindingUtil.setContentView(this,R.layout.activity_authentication)

        //Initialize Firebase:
        auth = Firebase.auth

        //Button to login :
        binding.btnSignin.setOnClickListener {
            signInWithMailAndGoogle()
        }
    }

    //Check first if user make signIn before then let it enter auto and transfer to RemindersActivity :)
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser!=null){
            val intent = Intent(this,RemindersActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Fun to signIn :
    private fun signInWithMailAndGoogle(){
        //Using 2 methods of email and google :
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
            listOf( AuthUI.IdpConfig.EmailBuilder().build(),AuthUI.IdpConfig.GoogleBuilder().build())
        ).build(),RESULT_CODE)

    }

    //Constant val for Result code :
    companion object{
        const val RESULT_CODE = 100
    }

    //Fun onActivityResult :
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Check first if requestCode equal result code :
        if (requestCode== RESULT_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode== Activity.RESULT_OK){
                Toast.makeText(this, " signIn complete", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, " failed to SignIn ", Toast.LENGTH_SHORT).show()
            }
        }

    }
}