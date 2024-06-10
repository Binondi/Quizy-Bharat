package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val update = FirebaseDatabase.getInstance().reference.child("Update")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        ColorAccordingTheme(window).statusBarColor()
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, SignupActivity::class.java))
                finish()
            }


        },2000)
    }

    private fun getUpdateData(){
        update.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}