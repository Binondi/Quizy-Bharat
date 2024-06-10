package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import devs.org.quizzybharat.Data.UpdateData
import devs.org.quizzybharat.Fragments.ActivityFragment
import devs.org.quizzybharat.Fragments.HomeFragment
import devs.org.quizzybharat.Internet.ChackInternet
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityMainBinding
import java.text.Normalizer
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Environment
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import java.io.File


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var sp: SharedPreferences
    private var name = "User"
    private var email = "example@gmail.com"
    private var phoneNo = "9999999999"
    private var gender = "Not Chosen"
    private var growthRate = "0"
    private var quizCompleted = "0"
    private var profileUrl = ""
    private var userType = ""
    private var rightAnswer = "0"
    private var wrongAnswer = "0"
    var count = 0
    private val users = FirebaseDatabase.getInstance().reference.child("Users")

    private val RC_POST_NOTIFICATION = 123
    private var bottomBarPosition = 0

    private val update = FirebaseDatabase.getInstance().reference.child("Update")
    private var version = "1.0"
    private var shortDesc = "New Update Available"
    private var longDesc = "Bug Fixes"
    private var versionName = "1.0"
    private var link = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ColorAccordingTheme(window).statusBarColor()

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        getUpdate()
        subscribeFCM()
        getSharedPrefs()
        loadUserData()
        loadFragment(HomeFragment())
        binding.homeIcon.setImageResource(R.drawable.home_filled)
        binding.profileIcon.setImageResource(R.drawable.search)
        binding.activityIcon.setImageResource(R.drawable.activity)
        binding.randomIcon.setImageResource(R.drawable.random)

        binding.home.setOnClickListener {
            loadFragment(HomeFragment())
            binding.homeIcon.setImageResource(R.drawable.home_filled)
            binding.profileIcon.setImageResource(R.drawable.search)
            binding.activityIcon.setImageResource(R.drawable.activity)
            binding.randomIcon.setImageResource(R.drawable.random)
        }
        binding.random.setOnClickListener {
            startActivity(Intent(this, RandomActivity::class.java))

        }
        binding.activity.setOnClickListener {
            loadFragment(ActivityFragment())
            binding.homeIcon.setImageResource(R.drawable.home)
            binding.profileIcon.setImageResource(R.drawable.search)
            binding.activityIcon.setImageResource(R.drawable.activity_filled)
            binding.randomIcon.setImageResource(R.drawable.random)
        }
        binding.search.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.plus.setOnClickListener {
            startActivity(Intent(this, AddQuizActivity::class.java))
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),RC_POST_NOTIFICATION)
        }
        chakBan()
    }

    private fun subscribeFCM() {
        val name = "all"

        var topicName = Normalizer.normalize(name, Normalizer.Form.NFD)
        topicName = topicName.replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
        FirebaseMessaging.getInstance().subscribeToTopic(topicName)
            .addOnCompleteListener {

            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        if (!ChackInternet().isInternetAvailable(this)){
            showNoNetDialog()
        }
    }

    private fun getSharedPrefs() {
        sp = this.getSharedPreferences("details", MODE_PRIVATE)
        if (sp.contains("name")){
            name = sp.getString("name","").toString()
        }
        if (sp.contains("email")){
            email = sp.getString("email","").toString()
        }
        if (sp.contains("phoneNo")){
            phoneNo = sp.getString("phoneNo","").toString()
        }
        if (sp.contains("gender")){
            gender = sp.getString("gender","").toString()
        }
        if (sp.contains("growthRate")){
            growthRate = sp.getString("growthRate","0").toString()
        }
        if (sp.contains("quizCompleted")){
            quizCompleted = sp.getString("quizCompleted","0").toString()
        }
        if (sp.contains("profileUrl")){
            profileUrl = sp.getString("profileUrl","").toString()
        }
        if (sp.contains("userType")){
            userType = sp.getString("userType","").toString()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ count = 0 },2000 )
        if (count == 0){
            Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show()
            count = 1
        }else if (count == 1){
            super.onBackPressed()
            finishAffinity()
        }
    }

    private fun chakBan() {
        FirebaseDatabase.getInstance().reference.child("Users").child(FirebaseAuth.getInstance().uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                val childValue: HashMap<String?, Any?>? = snapshot.getValue(ind)
                if (childValue?.get("ban") != null){
                    if (childValue["ban"] == true){
                        startActivity(Intent(this@MainActivity, BanActivity::class.java))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getUpdate() {
        update.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val updateData = mutableListOf<UpdateData>()

                for(snap in snapshot.children){
                    val data = snap.getValue(UpdateData::class.java)
                    data?.let {
                        updateData.add(it)
                        version = it.version
                        shortDesc = it.shortDesc
                        longDesc = it.longDesc
                        link = it.link
                    }


                }

                if (version.toDouble() > versionName.toDouble()) showUpdateDialog()

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event if needed
            }
        })
    }

    @SuppressLint("QueryPermissionsNeeded", "MissingInflatedId")
    private fun showUpdateDialog() {
        if (!isFinishing && !isDestroyed) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.new_update, null)
            val dialogBuilder = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()
            val button = dialogView.findViewById<Button>(R.id.button)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

            button.setOnClickListener {

                progressBar.visibility = View.VISIBLE
                button.isClickable = false
                button.text = "Downloading..."

                downloadAndInstallApk(dialog, progressBar)
            }

            dialog.show()
        }
    }

    private fun downloadAndInstallApk(dialog: Dialog, progressBar: ProgressBar) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(link)
        val request = DownloadManager.Request(uri)

        request.setTitle("QuizzyBharat Update")
        request.setDescription("Downloading QuizzyBharat update...")
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "QuizzyBharat.apk")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Enqueue the download and get the download ID
        val downloadId = downloadManager.enqueue(request)

        // Set a broadcast receiver to listen for download completion
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // Unregister the receiver
                    unregisterReceiver(this)
                    // Install the downloaded APK
                    installApk()
                    // Dismiss the update dialog
                    dialog.dismiss()
                }
            }
        }

        // Register the receiver for download completion
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // Set a broadcast receiver to listen for download progress
        val onProgress = object : BroadcastReceiver() {
            @SuppressLint("Range")
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // Get download progress
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val bytesDownloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val bytesTotal = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val progress = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                        // Update the progress bar
                        progressBar.progress = progress
                    }
                    cursor.close()
                }
            }
        }

        // Register the receiver for download progress
        registerReceiver(onProgress, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }



    private fun installApk() {
        val fileUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "QuizzyBharat.apk")
        )
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        installIntent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        startActivity(installIntent)
    }

    private fun showNoNetDialog() {
        if (!isFinishing && !isDestroyed) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.no_internet_dialog, null)
            val dialogBuilder = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()

            dialogView.findViewById<Button>(R.id.button).setOnClickListener {
                if(ChackInternet().isInternetAvailable(this)) dialog.dismiss()
            }

            dialog.show()
        }
    }


    private fun loadUserData(){
        users.child(FirebaseAuth.getInstance().uid.toString()).child("userUid").setValue(FirebaseAuth.getInstance().uid.toString()).addOnCompleteListener {

        }
        users.child(FirebaseAuth.getInstance().uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                snapshot.key
                val childValue: HashMap<String?, Any?>? = snapshot.getValue(ind)

                if (childValue?.get("name") != null){
                    name = childValue["name"].toString()
                    sp.edit().putString("name",name).apply()
                }
                if (childValue?.get("email") != null){
                    email = childValue["email"].toString()
                    sp.edit().putString("email",email).apply()
                }
                if (childValue?.get("phoneNo") != null){
                    phoneNo = childValue["phoneNo"].toString()
                    sp.edit().putString("phoneNo",phoneNo).apply()
                }
                if (childValue?.get("gender") != null){
                    gender = childValue["gender"].toString()
                    sp.edit().putString("gender",gender).apply()
                }
                if (childValue?.get("growthRate") != null){
                    growthRate = childValue["growthRate"].toString()
                    sp.edit().putString("growthRate",growthRate).apply()
                }
                if (childValue?.get("quizCompleted") != null){
                    quizCompleted = childValue["quizCompleted"].toString()
                    sp.edit().putString("quizCompleted",quizCompleted).apply()
                }
                if (childValue?.get("profileUrl") != null){
                    profileUrl = childValue["profileUrl"].toString()
                    sp.edit().putString("profileUrl",profileUrl).apply()
                }
                if (childValue?.get("userType") != null){
                    userType = childValue["userType"].toString()
                    sp.edit().putString("userType",userType).apply()
                }
                if (childValue?.get("rightAnswer") != null){
                    rightAnswer = childValue["rightAnswer"].toString()
                    sp.edit().putString("rightAnswer",rightAnswer).apply()
                }
                if (childValue?.get("wrongAnswer") != null){
                    wrongAnswer = childValue["wrongAnswer"].toString()
                    sp.edit().putString("wrongAnswer",wrongAnswer).apply()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,
                    "Failed to load profile details, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
    }
}