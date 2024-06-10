package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import devs.org.quizzybharat.Adapters.DialogRecyclerAdapter
import devs.org.quizzybharat.Data.CategoryData
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private var editing = false
    private var isImagePicked = false
    private var imagePath: Uri? = null
    private var userUid = ""
    private val database = FirebaseDatabase.getInstance()
    private val users = database.reference.child("Users")
    private val category = database.reference.child("Category")
    private val auth = FirebaseAuth.getInstance()
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
    private var favoriteCategory = "Not Chosen"
    private lateinit var sp: SharedPreferences
    private val map = HashMap<String, Any>()
    private lateinit var list : List<String>

    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ColorAccordingTheme(window).statusBarColor()

        list = ArrayList()

        binding.back.setOnClickListener {
            finish()
        }

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
        if (sp.contains("favoriteCategory")){
            favoriteCategory = sp.getString("favoriteCategory","").toString()
        }

        updateProfileDetails()
        getCategories()

        userUid = auth.uid.toString()
        binding.edit.setOnClickListener {
            if (!editing){
                edit()
            }else {
                if (binding.edtName.text.isNotEmpty()){
                    val name2 = binding.edtName.text.toString()
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonText.visibility = View.GONE
                    done()
                    editName(name2)
                }else {
                    binding.edtName.error = "Enter a name"
                }

            }

            binding.cut.setOnClickListener {
                if (!isDestroyed) {
                    Glide.with(this)
                        .load(profileUrl)
                        .placeholder(R.drawable.app_logo)
                        .into(binding.profile)
                }
                done()
            }
        }
        binding.pencil.setOnClickListener {
            if (editing){
                val intent = Intent()
                intent.setAction(Intent.ACTION_GET_CONTENT)
                intent.setType("image/*")
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 193)
            }
        }
        binding.profile.setOnClickListener {
            if (editing){
                val intent = Intent()
                intent.setAction(Intent.ACTION_GET_CONTENT)
                intent.setType("image/*")
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 193)
            }
        }
        binding.logout.setOnClickListener {
            logout()
        }

        binding.edtCategory.setOnClickListener {
            if (list.isNotEmpty()) {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.category_selector_dialog, null)
                val dialogBuilder = MaterialAlertDialogBuilder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)

                val dialog = dialogBuilder.create()

                val adapter = DialogRecyclerAdapter(this, list)
                recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

                dialogView.findViewById<Button>(R.id.button).setOnClickListener {

                    val selectedCategories = adapter.selectedItems
                    favoriteCategory = ""
                    for ((index, category) in selectedCategories.withIndex()) {
                        favoriteCategory += category
                        if (index < selectedCategories.size - 1) {
                            favoriteCategory += ", "
                        }
                    }
                    updateFavoriteCategories()
                    dialog.dismiss()
                }
                dialogView.findViewById<ImageView>(R.id.cut).setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            } else {
                Toast.makeText(this, "Data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }


        loadUserData()
    }

    private fun updateFavoriteCategories() {
        map.clear()
        map["favoriteCategory"] = favoriteCategory
        users.child(userUid).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) Toast.makeText(this, "Categories Updated", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Categories not updated, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editName(name2: String) {
        if (isImagePicked) {
            FirebaseStorage.getInstance().reference.child("Profile")
                .child(name2)
                .putFile(imagePath!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.storage?.downloadUrl?.addOnSuccessListener { uri ->
                            val profileUrl = uri.toString()
                            map["name"] = name2
                            map["profileUrl"] = profileUrl
                            updateInDatabase(name2)
                        }?.addOnFailureListener {
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            map["name"] = name2
            updateInDatabase(name2)
        }
    }

    private fun getCategories() {
        category.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<String>()
                for (categorySnapshot in snapshot.children) {
                    val categoryData = categorySnapshot.getValue(CategoryData::class.java)
                    if (categoryData != null) {
                        categoryList.add(categoryData.name)
                    }
                }

                list = categoryList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateInDatabase(name2:String) {
        users.child(userUid).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                sp.edit().putString("name", name2).apply()
                name = name2
                binding.name.text = name2
                binding.progressBar.visibility = View.GONE
                binding.buttonText.visibility = View.VISIBLE
            }else{
                binding.progressBar.visibility = View.GONE
                binding.buttonText.visibility = View.VISIBLE
            }
        }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== 193 && resultCode == RESULT_OK){
            imagePath = data?.data
            Glide.with(this)
                .load(imagePath)
                .into(binding.profile
                )
            isImagePicked = true
        }
    }
    private fun loadUserData(){
        users.child(userUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                val childKey: String? = snapshot.key
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
                if (childValue?.get("favoriteCategory") != null){
                    favoriteCategory = childValue["favoriteCategory"].toString()
                    sp.edit().putString("favoriteCategory",favoriteCategory).apply()
                }

                updateProfileDetails()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity,
                    "Failed to load profile details, please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        binding.settings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java) )
        }
    }



    private fun updateProfileDetails(){
        when (userType) {
            "Admin" -> {
                binding.blueTick.visibility = View.VISIBLE
                binding.blueTick.setImageResource(R.drawable.green_tick)
            }
            "Verified" -> {
                binding.blueTick.visibility = View.VISIBLE
                binding.blueTick.setImageResource(R.drawable.blue_tick)
            }
            "Participant" -> {
                binding.blueTick.visibility = View.GONE
            }
            else -> binding.blueTick.visibility = View.GONE
        }

        val totalQuestions = rightAnswer.toDouble() + wrongAnswer.toDouble()
        val totalQuestion = rightAnswer.toInt() + wrongAnswer.toInt()
        val percentage = (rightAnswer.toDouble() / totalQuestions) * 100


        binding.growth.text = "${percentage.toInt()}%"
        binding.txtCategories.text = favoriteCategory
        binding.rightAnswer.text = rightAnswer
        binding.wrongAnswer.text = wrongAnswer
        binding.name.text = name
        binding.email.text = email
        binding.phone.text = "+91 $phoneNo"
        if (gender == "Male" || gender == "male")
            binding.iconGender.setImageResource(R.drawable.gender_male)
        else binding.iconGender.setImageResource(R.drawable.gender)

        binding.gender.text = gender
        binding.completedQuiz.text = totalQuestion.toString()
        if (!isDestroyed) {
            Glide.with(this)
                .load(profileUrl)
                .placeholder(R.drawable.app_logo)
                .into(binding.profile)
        }
    }

    private fun done(){
        editing = false
        binding.name.visibility = View.VISIBLE
        binding.lnrEdtName.visibility = View.GONE
        binding.pencil.visibility = View.GONE
        binding.pencil.visibility = View.GONE
        binding.cut.visibility = View.GONE
        binding.buttonText.text = "Edit"
        val editableName = Editable.Factory.getInstance().newEditable("")
        binding.edtName.text = editableName
    }

    private fun edit(){
        editing = true
        binding.name.visibility = View.GONE
        binding.lnrEdtName.visibility = View.VISIBLE
        binding.pencil.visibility = View.VISIBLE
        binding.pencil.visibility = View.VISIBLE
        binding.cut.visibility = View.VISIBLE
        binding.buttonText.text = "Done"
    }
}