package com.example.mystagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.example.mystagram.MyApplication.Companion.auth
import com.example.mystagram.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        // 로그인 버튼에 setOnClickListener 설정 후 로그인 함수 호출.
        // 파이어베이스에 Authentication 기능 작동시키고 난 후 앱 실행
        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }

        // 인텐트 후 처리
        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            Log.d("lsy","구글 인증 버튼 클릭 후, 후처리되는 부분의 it.data : ${it.data.toString()}")
            //구글 로그인 결과 처리...........................
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            Log.d("lsy","val task")
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("lsy","try 1")
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                Log.d("lsy","try 2")
                MyApplication.auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){ task ->
//                        if(task.isSuccessful){
                        MyApplication.email = account.email
                        Log.d("lsy","구글 인증 버튼 클릭 후, account.email : ${account.email}")
//                        }else {
//                        }
                    }
            } catch (e: ApiException){
            }
        }
        // 구글 로그인
        fun googleLogin() {
            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }
        // googleLoginButton 리스너
        binding.googleLoginButton.setOnClickListener {
            googleLogin()
        }
    }


        // 메인페이지 호출하는 코드 startActivity(intent())
        fun moveMain(user : FirebaseUser?){
            if(user != null){
                if(user.isEmailVerified){
                    finish()
                    startActivity(Intent(this,MainActivity::class.java))
                }else{
//                    user.sendEmailVerification()
                    startActivity(Intent(this,MainActivity::class.java))
                }
            }
        }

        // 로그인(email, password가 맞아서 task가 isSuccessful 일때, moveMain 실행
        fun signinEmail(){
            var email = binding.edittextEmail.text.toString()
            var password = binding.edittextPassword.text.toString()
            // signInWithEmailAndPassword(email,password) : FirebaseAuth.class 에서 제공하는 이메일, 패스워드 로그인 기능
            // .addOnCompleteListener 리턴 값에 따라 작동할 기능 설정.
            auth?.signInWithEmailAndPassword(email,password)?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    moveMain(task.result?.user)
                }else{
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 이메일 회원가입 및 로그인 기능
        fun signinAndSignup(){
            var email = binding.edittextEmail.text.toString()
            var password = binding.edittextPassword.text.toString()
            // FirebaseAuth.class 에서 제공하는 이메일, 패스워드 가입 기능
            // .addOnCompleteListener 리턴 값에 따라 작동할 기능 설정.
            auth?.createUserWithEmailAndPassword(email,password)?.addOnCompleteListener {
                task ->
                if(task.isSuccessful){
        //                    saveFindIdData()
                    moveMain(task.result?.user)
                    //아이디 생성 후 메인화면 이동
                } else {
                    //이미 아이디가 있을 경우
                    signinEmail()
                }
            }
        }
    }

