package devs.org.quizzybharat

//noinspection SuspiciousImport
import android.R
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import devs.org.quizzybharat.Data.CategoryData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.Internet.ChackInternet
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityAddQuizBinding

class AddQuizActivity : AppCompatActivity() {

    private lateinit var list:List<String>
    private lateinit var categoryAdapter: ArrayAdapter<Any>
    private var currentQuestion = 0
    private var selectedLanguage = "English"
    private var selectedCategory = "Select Category"
    private var SELECT = "Select Category"
    private val database = FirebaseDatabase.getInstance()
    private val category = database.reference.child("Category")
    private val quizSet = database.reference.child("QuizSet")
    private var quizMap = HashMap<String, Any>()
    private var isSubmitted = true
    private var title = ""
    private var noQuizzes = 0
    private var categoryKey = ""
    private lateinit var sp: SharedPreferences
    private var userName = "Not Provided"
    private var profileUrl = ""
    private var isApproved = true
    private var userType = "Participant"
    private lateinit var data: ArrayList<QuestionData>
    private var pushKey = ArrayList<String>()

    private var forBack = 0

    private val unityGameID = "5564207"
    private val testMode = false
    private val placementId = "Interstitial_Android"

    private lateinit var binding : ActivityAddQuizBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        data = ArrayList()
        sp = this.getSharedPreferences("details", MODE_PRIVATE)


        if (sp.contains("userType")){
            userType = sp.getString("userType","").toString()
        }

        isApproved = when (userType) {
            "Admin" -> {
                true
            }

            "Verified" -> {
                true
            }

            "Participant" -> {
                false
            }

            else -> false
        }

        list = ArrayList()
        ColorAccordingTheme(window).statusBarColor()
        getCategories()
        val language = arrayOf("English", "Assamese", "Hindi")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, language)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter= adapter

        categoryAdapter = ArrayAdapter(this, R.layout.simple_spinner_item,list)

        categoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguage = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.back.setOnClickListener {
            if (isSubmitted){
                finish()
            }else {
                val dialog = MaterialAlertDialogBuilder(this)
                    .setTitle("Cancel Submission ?")
                    .setMessage("Do you want to exit without submitting the Questions ?")
                    .setPositiveButton("Submit & Exit",){_,_ ->
                        Toast.makeText(this,"Questions submitted and sent for Review",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, SubmittedActivity::class.java))
                        finish()

                    }
                    .setNegativeButton("Exit Directly"){_,_->

                        quizSet.child(title).removeValue().addOnCompleteListener {
                            Toast.makeText(this,"Questions submission canceled",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    .create().show()
            }
        }

        binding.backBtn.setOnClickListener {
            if (currentQuestion != 0) {
                currentQuestion--
                binding.title.setText(data[currentQuestion].title)
                binding.correctoption.setText(data[currentQuestion].correctOption)
                binding.option2.setText(data[currentQuestion].option2)
                binding.option3.setText(data[currentQuestion].option3)
                binding.option4.setText(data[currentQuestion].option4)
                binding.question.setText(data[currentQuestion].question)

                binding.hintTextQuestion.text = "Question ${currentQuestion + 1}*"

            }else{
                Toast.makeText(this, "Cannot back from here", Toast.LENGTH_SHORT).show()
            }
        }


        binding.next.setOnClickListener {
            if(binding.title.text.isNotEmpty()){
                if (selectedCategory != SELECT){
                    if (binding.question.text.isNotEmpty()){
                        if (binding.correctoption.text.isNotEmpty()){
                            if (binding.option2.text.isNotEmpty()){
                                if (binding.option3.text.isNotEmpty()){
                                    if (binding.option4.text.isNotEmpty()){


                                        title = binding.title.text.toString()
                                        isSubmitted = false
                                        val question = binding.question.text.toString()
                                        val correctOption =
                                            binding.correctoption.text.toString()
                                        val option2 = binding.option2.text.toString()
                                        val option3 = binding.option3.text.toString()
                                        val option4 = binding.option4.text.toString()
                                        pushKey.add(quizSet.push().key.toString())

                                        if (sp.contains("name")) {
                                            userName = sp.getString("name", "").toString()
                                        }
                                        if (sp.contains("profileUrl")) {
                                            profileUrl =
                                                sp.getString("profileUrl", "").toString()
                                        }
                                        val dataToAd = QuestionData(
                                            title,
                                            question,
                                            correctOption,
                                            option2,
                                            option3,
                                            option4,
                                            isApproved,
                                            currentQuestion + 1,
                                            FirebaseAuth.getInstance().uid.toString(),
                                            pushKey[currentQuestion],
                                            profileUrl,
                                            selectedCategory,
                                            userName,
                                            0,
                                            0,
                                            0,
                                            0
                                        )

                                        data.add(currentQuestion, dataToAd)

                                        uploadQuiz(data[currentQuestion])
                                        nextClicked()

                                    }else binding.option4.error = "Fill option 4"
                                }else binding.option3.error = "Fill option 3"
                            }else binding.option2.error = "Fill option 2"
                        }else binding.correctoption.error = "Fill correct option"
                    }else binding.question.error = "Fill a question"
                }else Toast.makeText(this, "Please chose a category",Toast.LENGTH_SHORT).show()
            }else binding.title.error = "Fill a title"

        }

        binding.submit.setOnClickListener {

            showAd()

            if (currentQuestion <= 1){
                Toast.makeText(this, "Attlist two question required", Toast.LENGTH_SHORT).show()
            }else {
                if(binding.title.text.isNotEmpty()){
                    if (selectedCategory != SELECT){
                        if (binding.question.text.isNotEmpty()){
                            if (binding.correctoption.text.isNotEmpty()){
                                if (binding.option2.text.isNotEmpty()){
                                    if (binding.option3.text.isNotEmpty()){
                                        if (binding.option4.text.isNotEmpty()){

                                            val dialog = MaterialAlertDialogBuilder(this)
                                                .setTitle("Submit Quiz")
                                                .setMessage("Are you sure you want to upload ${currentQuestion + 1} questions")
                                                .setPositiveButton("Submit"
                                                ) { _, _ ->

                                                    title = binding.title.text.toString()
                                                    isSubmitted = false
                                                    val question = binding.question.text.toString()
                                                    val correctOption = binding.correctoption.text.toString()
                                                    val option2 = binding.option2.text.toString()
                                                    val option3 = binding.option3.text.toString()
                                                    val option4 = binding.option4.text.toString()
                                                    pushKey.add(quizSet.push().key.toString() )


                                                    if (sp.contains("name")){
                                                        userName = sp.getString("name","").toString()
                                                    }

                                                    if (sp.contains("profileUrl")){
                                                        profileUrl = sp.getString("profileUrl","").toString()
                                                    }

                                                    val dataToAd = QuestionData(
                                                        title,
                                                        question,
                                                        correctOption,
                                                        option2,
                                                        option3,
                                                        option4,
                                                        isApproved,
                                                        currentQuestion + 1,
                                                        FirebaseAuth.getInstance().uid.toString(),
                                                        pushKey[currentQuestion],
                                                        profileUrl,
                                                        selectedCategory,
                                                        userName,
                                                        0,
                                                        0,
                                                        0,
                                                        0
                                                    )

                                                    data.add(dataToAd)

                                                    updateCategory()
                                                    uploadQuiz(data[currentQuestion])

                                                }
                                                .setNegativeButton("Cancel"){_,_ ->

                                                }
                                                .create().show()

                                        }else showDialogForNotFilled()
                                    }else showDialogForNotFilled()
                                }else showDialogForNotFilled()
                            }else showDialogForNotFilled()
                        }else showDialogForNotFilled()
                    }else showDialogForNotFilled()
                }else showDialogForNotFilled()
            }


        }
        initializeUnityAds()
    }

    private fun showAd(){

        UnityAds.show(this, placementId,object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String?,
                error: UnityAds.UnityAdsShowError?,
                message: String?
            ) {
                //failed to show ad
                UnityAds.load(placementId,object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {

                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {

                    }
                })
            }

            override fun onUnityAdsShowStart(placementId: String?) {
                //start
                UnityAds.load(placementId,object : IUnityAdsLoadListener {
                    override fun onUnityAdsAdLoaded(placementId: String?) {

                    }

                    override fun onUnityAdsFailedToLoad(
                        placementId: String?,
                        error: UnityAds.UnityAdsLoadError?,
                        message: String?
                    ) {
                        UnityAds.load(placementId,object : IUnityAdsLoadListener {
                            override fun onUnityAdsAdLoaded(placementId: String?) {

                            }

                            override fun onUnityAdsFailedToLoad(
                                placementId: String?,
                                error: UnityAds.UnityAdsLoadError?,
                                message: String?
                            ) {

                            }
                        })
                    }
                })
            }

            override fun onUnityAdsShowClick(placementId: String?) {
                //clicked
            }

            override fun onUnityAdsShowComplete(
                placementId: String?,
                state: UnityAds.UnityAdsShowCompletionState?
            ) {
                //completed

            }
        })

    }

    private fun initializeUnityAds(){
        UnityAds.initialize(this,unityGameID,testMode,object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {

            }

            override fun onInitializationFailed(
                error: UnityAds.UnityAdsInitializationError?,
                message: String?
            ) {
            }
        })

        UnityAds.load(placementId,object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {

            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {

            }
        })

    }

    private fun showDialogForNotFilled(){
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Submit Quiz")
            .setMessage("Are you sure you want to upload $currentQuestion questions")
            .setPositiveButton("Submit"
            ) { _, _ ->

                updateCategory()

            }
            .setNegativeButton("Cancel"){_,_ ->

            }
            .create().show()
    }

    override fun onResume() {
        super.onResume()

        if (!ChackInternet().isInternetAvailable(this)){
            showNoNetDialog()
        }
    }

    private fun showNoNetDialog() {
        if (!isFinishing && !isDestroyed) {
            val dialogView = LayoutInflater.from(this).inflate(devs.org.quizzybharat.R.layout.no_internet_dialog, null)
            val dialogBuilder = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()

            dialogView.findViewById<Button>(devs.org.quizzybharat.R.id.button).setOnClickListener {
                if(ChackInternet().isInternetAvailable(this)) dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun getCategories() {
        category.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<String>()
                categoryList.add(SELECT)
                for (categorySnapshot in snapshot.children) {
                    val categoryData = categorySnapshot.getValue(CategoryData::class.java)
                    if (categoryData != null) {
                        categoryList.add(categoryData.name)
                    }
                }

                list = categoryList
                categoryAdapter.addAll(list)

                binding.spinnerCategory.adapter = categoryAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    private fun uploadQuiz(data: QuestionData) {

        quizSet.child(data.key).setValue(data).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this, "Question added",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitClicked(){
        Toast.makeText(this,"Questions submitted and sent for Review",Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SubmittedActivity::class.java))
        finish()
    }
    private fun nextClicked(){
        binding.lnrTitle.visibility= View.GONE
        binding.lnrCategory.visibility=View.GONE
        binding.lnrLanguage.visibility=View.GONE

        val blank = Editable.Factory.getInstance().newEditable("")
        binding.question.text = blank
        binding.correctoption.text = blank
        binding.option2.text = blank
        binding.option3.text = blank
        binding.option4.text = blank

//        if (forBack <= currentQuestion) {
//
//        }else {
//            binding.title.setText(data[currentQuestion].title)
//            binding.correctoption.setText(data[currentQuestion].correctOption)
//            binding.option2.setText(data[currentQuestion].option2)
//            binding.option3.setText(data[currentQuestion].option3)
//            binding.option4.setText(data[currentQuestion].option4)
//            binding.question.setText(data[currentQuestion].question)
//        }

        forBack++
        currentQuestion++
        binding.hintTextQuestion.text = "Question ${currentQuestion + 1}*"

    }

    private fun updateCategory(){

        category.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    val categoryData = categorySnapshot.getValue(CategoryData::class.java)
                    if (categoryData != null) {
                        if (categoryData.name == selectedCategory) {
                            noQuizzes = categoryData.noQuizzes
                            categoryKey = categoryData.key
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            quizMap["noQuizzes"] = noQuizzes + 1
            category.child(categoryKey).updateChildren(quizMap).addOnCompleteListener {
                if (it.isSuccessful) submitClicked()
                else Toast.makeText(this, "Something want wrong, please try again.",Toast.LENGTH_SHORT).show()
            }
        },1000)



    }


    override fun onBackPressed() {
        if (isSubmitted){
            super.onBackPressed()
        }else {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Submission ?")
                .setMessage("Do you want to exit without submitting the Questions ?")
                .setPositiveButton("Submit & Exit",){_,_ ->
                    Toast.makeText(this,"Questions submitted and sent for Review",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SubmittedActivity::class.java))
                    finish()

                }
                .setNegativeButton("Exit Directly"){_,_->

                    quizSet.child(title).removeValue().addOnCompleteListener {
                        Toast.makeText(this,"Questions submission canceled",Toast.LENGTH_SHORT).show()
                        super.onBackPressed()
                    }
                }
                .create().show()
        }

    }



}