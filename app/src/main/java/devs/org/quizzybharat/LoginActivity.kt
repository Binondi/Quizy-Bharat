package devs.org.quizzybharat

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val database = FirebaseDatabase.getInstance()
    private val users = database.reference.child("Users")
    private var userId = ""
    private var map = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClientId))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val termsText = "Terms & Conditions"
        val text = "By signing in you are agreeing \n to our $termsText"
        val ss = SpannableString(text)
        val terms = object : ClickableSpan() {
            override fun onClick(view: View) {
                Toast.makeText(this@LoginActivity, "Clicked", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginWithGoogle.setOnClickListener {
            signIn()
        }

        binding.loginWithEmail.setOnClickListener {
            startActivity(Intent(this, LoginDataActivity::class.java))
        }

        val termsStartIndex = text.indexOf(termsText)
        val termsEndIndex = termsStartIndex + termsText.length
        ss.setSpan(terms, termsStartIndex, termsEndIndex, 0)

        ss.setSpan(
            ForegroundColorSpan(Color.GREEN),
            termsStartIndex, termsEndIndex,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.termsCon.text = ss
        binding.termsCon.movementMethod = LinkMovementMethod.getInstance()

        ColorAccordingTheme(window).statusBarColor()

        binding.gotoSignin.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 144)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 144 && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleSignInResult(account)
            } catch (_: Exception) {

            }

        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        val userName = account.displayName.toString()
        val email = account.email.toString()
        val profileUrl = account.photoUrl.toString()
        val idToken = account.idToken.toString()

        val dialog = ProgressDialog(this)
        dialog.setMessage("Please Wait...")
        dialog.setCancelable(false)
        dialog.show()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful){
                if (task.result.additionalUserInfo!!.isNewUser){
                    map["completedProfile"] = false
                    users.child(auth.uid.toString()).updateChildren(map).addOnCompleteListener {
                        if (it.isSuccessful){
                            dialog.dismiss()
                            val intent = Intent(this, FillDataActivity::class.java)
                            intent.putExtra("name", userName)
                            intent.putExtra("email", email)
                            intent.putExtra("profileUrl", profileUrl)
                            intent.putExtra("isGoogle", true)
                            intent.putExtra("idToken", idToken)
                            startActivity(intent)
                            finish()
                        }else {
                            dialog.dismiss()
                        }
                    }

                }else {

                    map["email"] = email
                    users.child(auth.uid.toString()).updateChildren(map).addOnCompleteListener {
                        if (it.isSuccessful){
                            dialog.dismiss()
                            Toast.makeText(
                                this@LoginActivity,
                                "Welcome back $email", Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }else dialog.dismiss()
                    }
                }
            }
        }
    }


}