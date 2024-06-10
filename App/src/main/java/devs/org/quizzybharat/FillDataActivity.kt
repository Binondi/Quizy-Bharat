package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityFillDataBinding


class FillDataActivity : AppCompatActivity() {

    private var selectedGender = false
    private var selectedCategory = true
    private var category = "Both"
    private var genderSelected = ""
    private var imagePicked = false
    private lateinit var progDialog:ProgressDialog
    private val database = FirebaseDatabase.getInstance()
    private val users = database.reference.child("Users")

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val profile = storage.reference.child("Profile")
    private var email = ""
    private var password = ""

    private var map = HashMap<String, Any>()
    private var profileUrl = ""
    private var fileName = ""
    private var filePath: Uri? = null
    private var nameUser = ""
    private var idToken = ""
    private var phoneNo = ""
    private var isGoogle = false



    private lateinit var binding : ActivityFillDataBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progDialog = ProgressDialog(this)
        progDialog.setCancelable(false)

        isGoogle = intent.getBooleanExtra("isGoogle", false)
        profileUrl = intent.getStringExtra("profileUrl").toString()
        nameUser = intent.getStringExtra("name").toString()
        email = intent.getStringExtra("email").toString()
        idToken = intent.getStringExtra("idToken").toString()
        val editableName = Editable.Factory.getInstance().newEditable(nameUser)
        val editableEmail = Editable.Factory.getInstance().newEditable(email)


        if (intent.hasExtra("isGoogle")){
            if (isGoogle){
                binding.pencil.visibility = View.GONE
                Glide.with(this)
                    .load(profileUrl)
                    .into(binding.addImage)
                binding.name.text = editableName
                binding.email.text = editableEmail
                imagePicked = true

            }
        }
        ColorAccordingTheme(window).statusBarColor()
        genderLogic()
        categoryLogic()
        next(idToken)


    }


    private fun signInWithGoogle(idToken:String){
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    uploadDataInDatabase()

                } else {
                    Toast.makeText(this@FillDataActivity, "Failed to sign up: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun next(idToken:String){
        email = binding.email.text.toString()
        password = binding.password.text.toString()
        binding.next.setOnClickListener {
            chackValues(idToken)
        }
    }

    private fun chackValues(idToken: String) {
        if (binding.name.text.isNotEmpty()) {
            if (binding.email.text.isNotEmpty()){
                if (binding.phoneNo.text.isNotEmpty()) {
                    if (binding.password.text.isNotEmpty()) {
                        if (binding.password.text.length >= 6) {
                            if (selectedGender) {
                                if (selectedCategory) {
                                    if (binding.email.text.contains("@gmail.com")) {
                                        if (binding.phoneNo.text.length == 10) {
                                            if (imagePicked) {
                                                //all ok
                                                progDialog.setMessage("Creating Account...")
                                                progDialog.show()
                                                if (isGoogle) signInWithGoogle(idToken)
                                                else createAccount()

                                            } else {
                                                Toast.makeText(
                                                    this@FillDataActivity,
                                                    "Select a profile pic",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@FillDataActivity,
                                                "Enter a valid phone no",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@FillDataActivity,
                                            "Enter a valid email",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@FillDataActivity,
                                        "Select your profile category",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@FillDataActivity,
                                    "Select your gender",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }else {
                            Toast.makeText(
                                this@FillDataActivity,
                                "Password must contains 6 or higher characters",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }else {
                        Toast.makeText(this@FillDataActivity, "Please create a password", Toast.LENGTH_SHORT)
                            .show()
                    }
                }else {
                    Toast.makeText(this@FillDataActivity, "Enter your phone no first", Toast.LENGTH_SHORT)
                        .show()
                }
            }else {
                Toast.makeText(this@FillDataActivity, "Enter your email first", Toast.LENGTH_SHORT)
                    .show()
            }

        }else {
            Toast.makeText(this@FillDataActivity, "Enter your name first", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createAccount() {
        auth.createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString()).addOnCompleteListener {
            if (it.isSuccessful){
                uploadProfilePic()

            }else {
                Toast.makeText(
                    this@FillDataActivity,
                    "Error creating your account: ${it.exception}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                progDialog.dismiss()
            }
        }
    }

    private fun uploadProfilePic() {
        progDialog.setMessage("Uploading Profile Pic...")
        if (filePath != null) {
            val uploadTask = profile.child(fileName).putFile(filePath!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                profile.child(fileName).downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    profileUrl = downloadUri.toString()
                    uploadDataInDatabase()
                } else {
                    Toast.makeText(
                        this@FillDataActivity,
                        "Failed getting link: ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()

                    progDialog.dismiss()
                }
            }
        }
    }


    private fun uploadDataInDatabase() {
        progDialog.setMessage("Uploading Details...")
        map["name"] = binding.name.text.toString()
        map["email"] = binding.email.text.toString()
        map["phoneNo"] = binding.phoneNo.text.toString()
        map["password"] = binding.password.text.toString()
        map["gender"] = genderSelected
        map["category"] = category
        map["profileUrl"] = profileUrl
        map["ban"] = false
        map["completedProfile"] = true
        map["userType"] = "Participant"
        map["growthRate"] = 0
        map["quizCompleted"] = 0
        map["wrongAnswer"] = 0
        map["rightAnswer"] = 0
        map["userUid"] = auth.uid.toString()
        users.child(auth.uid.toString()).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                progDialog.dismiss()
                val i =  Intent(this, MainActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()
            }else progDialog.dismiss()
        }
    }

    private fun genderLogic(){
        binding.male.setOnClickListener {
            binding.male.setBackgroundResource(R.drawable.selected)
            binding.female.setBackgroundResource(R.drawable.border)
            binding.other.setBackgroundResource(R.drawable.border)
            selectedGender = true
            genderSelected = "Male"
        }
        binding.female.setOnClickListener {
            binding.female.setBackgroundResource(R.drawable.selected)
            binding.male.setBackgroundResource(R.drawable.border)
            binding.other.setBackgroundResource(R.drawable.border)
            selectedGender = true
            genderSelected = "Female"
        }
        binding.other.setOnClickListener {
            binding.other.setBackgroundResource(R.drawable.selected)
            binding.male.setBackgroundResource(R.drawable.border)
            binding.female.setBackgroundResource(R.drawable.border)
            selectedGender = true
            genderSelected = "Other"
        }
        binding.addImage.setOnClickListener {
            val intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 193)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 193 && data != null){

            filePath = data.data
            fileName = binding.name.text.toString()

            Glide.with(this)
                .load(data.data)
                .placeholder(R.drawable.add_image)
                .into(binding.addImage)
            binding.pencil.visibility = View.GONE
            imagePicked = true
        }
        else{
            binding.pencil.visibility = View.VISIBLE
            imagePicked = false
        }
    }

    @SuppressLint("Range")
    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        return null
    }

    private fun categoryLogic(){
        binding.creator.setOnClickListener {
            binding.creator.setBackgroundResource(R.drawable.selected)
            binding.participant.setBackgroundResource(R.drawable.border)
            binding.both.setBackgroundResource(R.drawable.border)
            selectedCategory = true
            category = "Creator"
        }
        binding.participant.setOnClickListener {
            binding.participant.setBackgroundResource(R.drawable.selected)
            binding.creator.setBackgroundResource(R.drawable.border)
            binding.both.setBackgroundResource(R.drawable.border)
            selectedCategory = true
            category = "Participant"
        }
        binding.both.setOnClickListener {
            binding.both.setBackgroundResource(R.drawable.selected)
            binding.participant.setBackgroundResource(R.drawable.border)
            binding.creator.setBackgroundResource(R.drawable.border)
            selectedCategory = true
            category = "Both"
        }
    }
}