package com.example.tesotp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tesotp.databinding.FragmentFirstBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        };

        auth = FirebaseAuth.getInstance()

        login()

        var verificationId = ""
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+6282117166061")
                .setTimeout(30L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                        super.onCodeSent(p0, p1)
                        log("code sent!")
                        verificationId = p0
                        log("verificationId: $verificationId")
                    }

                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        log("verificaiton COMPLETE!")
                        binding.textviewFirst.text = p0.smsCode
                        signInWithPhone(p0)
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        log("verification FAILED: ${p0.message}")
                    }
                })
                .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        log("LOGIN WITH PHONE NUMBER === SUCCESS!")
                        log(task.result.toString())
                    }
                }
    }

    lateinit var user: FirebaseUser

    fun login() {
        log("login")

        auth.createUserWithEmailAndPassword(
                "hafizdwp@gmail.com", "159357"
        ).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                log("createUser success!")
                user = auth.currentUser!!
            } else {
                log("createUser failed")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun log(m: String) {
        Log.d("mytag", m)
    }
}