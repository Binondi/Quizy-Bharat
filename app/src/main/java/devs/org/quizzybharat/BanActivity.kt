package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityBanBinding

class BanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBanBinding
    private val auth = FirebaseAuth.getInstance()
    private var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uid = auth.uid.toString()
        binding.logout.setOnClickListener {
            logout()
        }
        binding.contact.setOnClickListener {
            sendEmail()
        }

        ColorAccordingTheme(window).statusBarColor()
    }

    private fun logout() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Log Out ?")
            .setMessage("Do you want to logout ?")
            .setPositiveButton("Logout"
            ) { _, _ ->
                auth.signOut()
                val i =  Intent(this, LoginActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()
            }
            .setNegativeButton("Cancel"){_,_ ->

            }
            .create().show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("binondiborthakur56@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "My account was Disabled")
        intent.putExtra(Intent.EXTRA_TEXT, "My uid: $uid")
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (false){
            super.onBackPressed()
        }

    }

}