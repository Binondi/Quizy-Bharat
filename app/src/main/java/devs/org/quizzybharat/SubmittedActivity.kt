package devs.org.quizzybharat

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme

class SubmittedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submitted)
        val done = findViewById<Button>(R.id.done)
        done.setOnClickListener {
            finish()
        }
        ColorAccordingTheme(window).statusBarColor()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


}