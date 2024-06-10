package devs.org.quizzybharat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityLoginDataBinding

class LoginDataActivity : AppCompatActivity() {

    private lateinit var progDialog: ProgressDialog

    private lateinit var binding : ActivityLoginDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progDialog = ProgressDialog(this)
        progDialog.setMessage("Logging you in...")
        progDialog.setCancelable(false)
        ColorAccordingTheme(window).statusBarColor()
        binding.forgotPassword.setOnClickListener {
            forgotPassword()

        }
        binding.txtLogin.setOnClickListener {
            binding.lnrBtn.visibility = View.GONE
            binding.lnrLogin.visibility = View.VISIBLE
            binding.forgotPassword.visibility = View.VISIBLE
            binding.txtLogin.visibility = View.GONE
            binding.lnrConfirmPass.visibility = View.GONE
            binding.lnrEmail.visibility = View.VISIBLE
            binding.password.hint = "Password"
        }

        binding.login.setOnClickListener {

            if (binding.email.text.isNotEmpty()){
                if (binding.password.text.isNotEmpty()){
                    if (binding.email.text.contains("@gmail.com")){
                        progDialog.show()
                        loginUser()

                    }else {
                        Toast.makeText(this@LoginDataActivity,
                            "Enter a valid email",Toast.LENGTH_SHORT)
                            .show()
                    }
                }else {
                    Toast.makeText(this@LoginDataActivity,
                        "Enter password",Toast.LENGTH_SHORT)
                        .show()
                }
            }else {
                Toast.makeText(this@LoginDataActivity,
                    "Enter email",Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun loginUser() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
            .addOnCompleteListener{
                if (it.isSuccessful){
                    progDialog.dismiss()
                    Toast.makeText(this@LoginDataActivity,
                        "Welcome Back",Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else {
                    progDialog.dismiss()
                    Toast.makeText(this@LoginDataActivity,
                        "Error login: ${it.exception}",Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun forgotPassword() {
        val email = binding.email.text.toString().trim()

        if (email.isNotEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password")
                .setMessage("Are you sure you want to send a password reset email to $email?")
                .setPositiveButton("Send") { _, _ ->
                    sendPasswordResetEmail(email)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Toast.makeText(this@LoginDataActivity,
                "Enter your email address",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun changePassword(newPassword: String) {
        // Get the current user
        val user = FirebaseAuth.getInstance().currentUser

        // Update the password in Firebase Authentication
        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password updated successfully, now update in Realtime Database
                    val userId = user.uid
                    val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    // Update the password in the Realtime Database
                    databaseReference.child("password").setValue(newPassword)
                        .addOnSuccessListener {
                            // Password updated in Realtime Database successfully
                            Toast.makeText(this@LoginDataActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Failed to update password in Realtime Database
                            Toast.makeText(this@LoginDataActivity, "Failed to update password in database: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Failed to update password in Firebase Authentication
                    Toast.makeText(this@LoginDataActivity, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun sendPasswordResetEmail(email: String) {
        progDialog.setMessage("Sending password reset email...")
        progDialog.show()

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progDialog.dismiss()
                    Toast.makeText(this@LoginDataActivity,
                        "Password reset email sent to $email",
                        Toast.LENGTH_SHORT).show()
                } else {
                    progDialog.dismiss()
                    Toast.makeText(this@LoginDataActivity,
                        "Failed to send password reset email: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }
}