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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val RC_SIGN_IN = 123
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val database = FirebaseDatabase.getInstance()
    private val users = database.reference.child("Users")
    private var userId = ""
    private var map = HashMap<String, Any>()
    private lateinit var dialog:ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        val termsText = "Terms & Conditions"
        val text = "By signing in you are agreeing \n to our $termsText"
        val ss = SpannableString(text)
        val terms = object : ClickableSpan() {
            override fun onClick(view: View) {
                Toast.makeText(this@SignupActivity, "Clicked", Toast.LENGTH_SHORT).show()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClientId))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val termsStartIndex = text.indexOf(termsText)
        val termsEndIndex = termsStartIndex + termsText.length
        ss.setSpan(terms, termsStartIndex, termsEndIndex, 0)

        // Change the color of the clickable text
        ss.setSpan(
            ForegroundColorSpan(Color.GREEN), // Change the color to blue
            termsStartIndex, termsEndIndex,
            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.termsCon.text = ss
        binding.termsCon.movementMethod = LinkMovementMethod.getInstance()
        ColorAccordingTheme(window).statusBarColor()
        binding.gotoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.signinWithEmail.setOnClickListener{
            startActivity(Intent(this, FillDataActivity::class.java))
        }
        binding.signinWithGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            dialog = ProgressDialog(this)
            dialog.setMessage("Please Wait...")
            dialog.setCancelable(false)
            dialog.show()
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleSignInResult(account)
            }catch (_:Exception){
                dialog.dismiss()
            }

        }
    }



    private fun handleSignInResult(account: GoogleSignInAccount) {
        val userName = account.displayName.toString()
        val email = account.email.toString()
        val profileUrl = account.photoUrl.toString()
        val idToken = account.idToken.toString()
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
                        }else dialog.dismiss()
                    }

                }else {

                    map["email"] = email
                    users.child(auth.uid.toString()).updateChildren(map).addOnCompleteListener {
                        if (it.isSuccessful){
                            dialog.dismiss()
                            Toast.makeText(
                                this@SignupActivity,
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