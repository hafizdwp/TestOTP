package com.example.tesotp

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tesotp.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dmax.dialog.SpotsDialog
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser
    var otpCode = ""
    lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // otp
        auth = FirebaseAuth.getInstance()

        // login
        loginFirebase()

        // views
        loadingDialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Loading")
                .build()

        binding.btnGenerateOtp.setOnClickListener { generateOtp() }
        binding.btnSubmitOtp.setOnClickListener { submitOtp() }
    }

    private fun generateOtp() {
        loadingDialog.show()

        var phoneNumber = binding.etPhone.text.toString()
        phoneNumber = "+${phoneNumber}"

        var verificationId = ""
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(p0, p1)
                        log("code sent!")
                        verificationId = p0
                        log("verificationId: $verificationId")
                    }

                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        log("verification COMPLETE!")
                        log("SMS CODE: ${p0.smsCode}")

                        otpCode = p0.smsCode ?: ""

                        signInWithPhone(p0)
                        loadingDialog.dismiss()
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        log("verification FAILED: ${p0.message}")
                        loadingDialog.dismiss()
                    }
                })
                .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun submitOtp() {
        val inputOtpCode = binding.etOtp.text.toString()
        val isOtpValid = inputOtpCode == otpCode
        val message = if(isOtpValid) "Congratulations! your phone number is CONFIRMED"
        else "Your OTP number is wrong, please input correct phone number and try again"

        AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog?.dismiss() }
                .create()
                .show()
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        log("LOGIN WITH PHONE NUMBER === SUCCESS!")
                        log(task.result.toString())
                    }
                }
    }


    private fun loginFirebase() {
        log("login")

        auth.createUserWithEmailAndPassword(
                "hafizdwp@gmail.com", "159357"
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                log("createUser success!")
                user = auth.currentUser!!
            } else {
                log("createUser failed")
            }
        }
    }

    private fun log(m: String) {
        Log.d("mytag", m)
    }
}