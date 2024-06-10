package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityPlayQuizBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class PlayQuizActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlayQuizBinding
    private lateinit var options:ArrayList<String>
    private var totalQuiz = 1
    private var currentQuiz = 1
    private var corrected = 0
    private var title: String? = null
    private var key = ""
    private var category = ""
    private var question = ""
    private var second = 15
    private var minuts = 0
    private var isSubmitted = false
    private var userId = ""
    private val auth = FirebaseAuth.getInstance()
    var coorectAnswer = ""
    private lateinit var handler: Handler
    private lateinit var questionList:ArrayList<QuestionData>
    private val questions = FirebaseDatabase.getInstance().reference.child("QuizSet")
    private val completedData = FirebaseDatabase.getInstance().reference.child("Completed")
    private val user = FirebaseDatabase.getInstance().reference.child("Users")
    private lateinit var t:TimerTask
    private val timer = Timer()
    private lateinit var c:Calendar
    private val map = HashMap<String,Any>()
    private lateinit var sp: SharedPreferences
    private val delay:Long = 1500

    private val unityGameID = "5564207"
    private val testMode = false
    private val placementId = "Interstitial_Android"
    private lateinit var rightAns: MediaPlayer
    private lateinit var loadingDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLoadingDialog()
        ColorAccordingTheme(window).statusBarColor()
        questionList = ArrayList()
        c = Calendar.getInstance()


        sp = this.getSharedPreferences("details", MODE_PRIVATE)
        binding.submit.isEnabled = false
        handler = Handler(Looper.getMainLooper())
        totalQuiz = intent.getIntExtra("noQuizzes", 1)
        title = intent.getStringExtra("title").toString()
        category = intent.getStringExtra("category").toString()
        key = intent.getStringExtra("key").toString()
        question = intent.getStringExtra("question").toString()
        userId = auth.uid.toString()
        initializeUnityAds()

        binding.currentQ.text = "$currentQuiz/$totalQuiz"
        second = totalQuiz * 15

        if (second > 60){
            minuts = second / 60
        }

        binding.progressBar.max = totalQuiz
        binding.progressBar.progress = currentQuiz

        setAllDefault()
        loadQuizzes()

        clickListeners()


        binding.timerProgress.max = second
        binding.timerProgress.progress = second
        t = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (second != 0) {
                        second--
                        val remainingSeconds = second % 60
                        val remainingMinutes = second / 60
                        binding.seconds.text = "${DecimalFormat("00").format(remainingMinutes)}:${DecimalFormat("00").format(remainingSeconds)}"

                        // Calculate the progress based on the remaining time
                        binding.timerProgress.progress = second

                        if (remainingSeconds <= 10 && remainingMinutes <= 0){
                            binding.timerProgress.progressDrawable = ContextCompat.getDrawable(this@PlayQuizActivity, R.drawable.progress_color)

                        }
                    } else {
                        showCustomDialog()
                        t.cancel()
                    }
                }
            }
        }
        timer.scheduleAtFixedRate(t, 1000, 1000)
    }

    private fun showLoadingDialog() {
        loadingDialog = ProgressDialog(this)
        loadingDialog.setMessage("Loading Quizzes...")
        loadingDialog.setCancelable( false)

        loadingDialog.show()
    }

    private fun showCustomDialog() {
        if (!isFinishing && !isDestroyed) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.times_up_dialog, null)
            val dialogBuilder = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()

            // Set up any interactions you want with the views in the layout
            dialogView.findViewById<Button>(R.id.button).setOnClickListener {
                finish()
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showCompletedDialog() {
        if (!isFinishing && !isDestroyed) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.quiz_completed_dialog, null)
            val dialogBuilder = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)

            val dialog = dialogBuilder.create()

            dialogView.findViewById<Button>(R.id.button).setOnClickListener {
                finish()
                dialog.dismiss()
            }
            dialogView.findViewById<TextView>(R.id.message).text =
                "You just completed $totalQuiz quizzes and scored $corrected/$totalQuiz . Great job buddy."

            dialog.show()
        }
    }


    private fun clickListeners() {
        binding.bgOptionOne.setOnClickListener {
            setAllDefault()
            binding.showProgressBar.visibility = View.VISIBLE
            binding.bgOptionOne.isClickable = false
            binding.bgOptionTwo.isClickable = false
            binding.bgOptionThree.isClickable = false
            binding.bgOptionFour.isClickable = false
            if (coorectAnswer == binding.option1.text.toString()){
                rightAnswerStyle(binding.bgOptionOne,binding.option1)
                corrected++
                binding.lottie1.visibility = View.VISIBLE
                binding.lottie1.playAnimation()
                rightAns = MediaPlayer.create(this, R.raw.correct)
                rightAns.start()
                stopCorrectSound()
            }
            if(coorectAnswer == binding.option2.text.toString()){
                wrongAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option3.text.toString()){
                wrongAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAnswerStyle( binding.bgOptionThree,binding.option3)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option4.text.toString()){
                wrongAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAnswerStyle(binding.bgOptionFour,binding.option4)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            handler.postDelayed({
                if (currentQuiz < totalQuiz) {
                    currentQuiz++
                    updateQuestion()
                    binding.bgOptionOne.isClickable = true
                    binding.bgOptionTwo.isClickable = true
                    binding.bgOptionThree.isClickable = true
                    binding.bgOptionFour.isClickable = true
                    binding.progressBar.progress = currentQuiz
                    binding.currentQ.text = "$currentQuiz/$totalQuiz"

                }else{
                    binding.submit.setBackgroundResource(R.drawable.green_corner_bg)
                    binding.submit.isEnabled = true
                    binding.leave.visibility = View.GONE
                    t.cancel()
                    binding.lnrOptions.visibility = View.GONE
                    binding.lnrAnimation.visibility = View.VISIBLE
                    binding.animation.playAnimation()
                    binding.question.text = ""
                }
                setAllDefault()
                binding.showProgressBar.visibility = View.GONE

            }, delay)
        }
        binding.bgOptionTwo.setOnClickListener {
            setAllDefault()
            binding.showProgressBar.visibility = View.VISIBLE
            binding.bgOptionOne.isClickable = false
            binding.bgOptionTwo.isClickable = false
            binding.bgOptionThree.isClickable = false
            binding.bgOptionFour.isClickable = false

            if (coorectAnswer == binding.option1.text.toString()){
                wrongAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if(coorectAnswer == binding.option2.text.toString()){
                rightAnswerStyle(binding.bgOptionTwo,binding.option2)
                corrected++
                binding.lottie2.visibility = View.VISIBLE
                binding.lottie2.playAnimation()
                rightAns = MediaPlayer.create(this, R.raw.correct)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option3.text.toString()){
                rightAnswerStyle(binding.bgOptionThree,binding.option3)
                wrongAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option4.text.toString()){
                rightAnswerStyle(binding.bgOptionFour,binding.option4)
                wrongAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            handler.postDelayed({
                if (currentQuiz < totalQuiz) {
                    currentQuiz++
                    updateQuestion()
                    binding.progressBar.progress = currentQuiz
                    binding.currentQ.text = "$currentQuiz/$totalQuiz"
                    binding.bgOptionOne.isClickable = true
                    binding.bgOptionTwo.isClickable = true
                    binding.bgOptionThree.isClickable = true
                    binding.bgOptionFour.isClickable = true
                }else{
                    binding.submit.setBackgroundResource(R.drawable.green_corner_bg)
                    binding.submit.isEnabled = true
                    binding.leave.visibility = View.GONE
                    t.cancel()
                    binding.lnrOptions.visibility = View.GONE
                    binding.lnrAnimation.visibility = View.VISIBLE
                    binding.animation.playAnimation()
                    binding.question.text = ""
                }
                setAllDefault()
                binding.showProgressBar.visibility = View.GONE

            },delay)
        }
        binding.bgOptionThree.setOnClickListener {
            setAllDefault()
            binding.showProgressBar.visibility = View.VISIBLE
            binding.bgOptionOne.isClickable = false
            binding.bgOptionTwo.isClickable = false
            binding.bgOptionThree.isClickable = false
            binding.bgOptionFour.isClickable = false

            if (coorectAnswer == binding.option1.text.toString()){
                wrongAnswerStyle (binding.bgOptionThree,binding.option3)
                rightAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if(coorectAnswer == binding.option2.text.toString()){
                wrongAnswerStyle(binding.bgOptionThree,binding.option3)
                rightAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option3.text.toString()){
                rightAnswerStyle(binding.bgOptionThree,binding.option3)
                corrected++
                binding.lottie3.visibility = View.VISIBLE
                binding.lottie3.playAnimation()
                rightAns = MediaPlayer.create(this, R.raw.correct)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option4.text.toString()){
                rightAnswerStyle(binding.bgOptionFour,binding.option4)
                wrongAnswerStyle(binding.bgOptionThree,binding.option3)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            handler.postDelayed({
                if (currentQuiz < totalQuiz) {
                    currentQuiz++
                    updateQuestion()
                    binding.progressBar.progress = currentQuiz
                    binding.currentQ.text = "$currentQuiz/$totalQuiz"
                    binding.bgOptionOne.isClickable = true
                    binding.bgOptionTwo.isClickable = true
                    binding.bgOptionThree.isClickable = true
                    binding.bgOptionFour.isClickable = true
                }else{
                    binding.submit.setBackgroundResource(R.drawable.green_corner_bg)
                    binding.submit.isEnabled = true
                    binding.leave.visibility = View.GONE
                    t.cancel()
                    binding.lnrOptions.visibility = View.GONE
                    binding.lnrAnimation.visibility = View.VISIBLE
                    binding.animation.playAnimation()
                    binding.question.text = ""
                }
                setAllDefault()
                binding.showProgressBar.visibility = View.GONE

            },delay)
        }
        binding.bgOptionFour.setOnClickListener {
            setAllDefault()
            binding.showProgressBar.visibility = View.VISIBLE

            binding.bgOptionOne.isClickable = false
            binding.bgOptionTwo.isClickable = false
            binding.bgOptionThree.isClickable = false
            binding.bgOptionFour.isClickable = false
            if (coorectAnswer == binding.option1.text.toString()){
                wrongAnswerStyle(binding.bgOptionFour,binding.option4)
                rightAnswerStyle(binding.bgOptionOne,binding.option1)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if(coorectAnswer == binding.option2.text.toString()){
                wrongAnswerStyle(binding.bgOptionFour,binding.option4)
                rightAnswerStyle(binding.bgOptionTwo,binding.option2)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option3.text.toString()){
                wrongAnswerStyle(binding.bgOptionFour,binding.option4)
                rightAnswerStyle(binding.bgOptionThree,binding.option3)
                rightAns = MediaPlayer.create(this, R.raw.wrong)
                rightAns.start()
                stopCorrectSound()
            }
            if (coorectAnswer == binding.option4.text.toString()){
                rightAnswerStyle(binding.bgOptionFour,binding.option4)
                corrected++
                binding.lottie4.visibility = View.VISIBLE
                binding.lottie4.playAnimation()
                rightAns = MediaPlayer.create(this, R.raw.correct)
                rightAns.start()
                stopCorrectSound()
            }
            handler.postDelayed({
                if (currentQuiz < totalQuiz) {
                    currentQuiz++
                    updateQuestion()
                    binding.progressBar.progress = currentQuiz
                    binding.currentQ.text = "$currentQuiz/$totalQuiz"
                    binding.bgOptionOne.isClickable = true
                    binding.bgOptionTwo.isClickable = true
                    binding.bgOptionThree.isClickable = true
                    binding.bgOptionFour.isClickable = true
                }else{
                    binding.submit.setBackgroundResource(R.drawable.green_corner_bg)
                    binding.submit.isEnabled = true
                    binding.leave.visibility = View.GONE
                    t.cancel()
                    binding.lnrOptions.visibility = View.GONE
                    binding.lnrAnimation.visibility = View.VISIBLE
                    binding.animation.playAnimation()
                    binding.question.text = ""
                }
                setAllDefault()
                binding.showProgressBar.visibility = View.GONE

            },delay)

        }
        binding.submit.setOnClickListener {
            submit()
        }
        binding.leave.setOnClickListener {
            leave()
        }
    }

    private fun submit(){
        binding.submit.isEnabled = false
        isSubmitted = true
        showAd()

    }

    fun initializeUnityAds(){
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
                Toast.makeText(this@PlayQuizActivity, "Error loading Ad.", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun showAd(){

        UnityAds.show(this, placementId,object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(
                placementId: String?,
                error: UnityAds.UnityAdsShowError?,
                message: String?
            ) {
                //failed to show ad
                Toast.makeText(this@PlayQuizActivity, "Failed to submit quiz, try again.", Toast.LENGTH_SHORT).show()
            }

            override fun onUnityAdsShowStart(placementId: String?) {
                //start
            }

            override fun onUnityAdsShowClick(placementId: String?) {
                //clicked
            }

            override fun onUnityAdsShowComplete(
                placementId: String?,
                state: UnityAds.UnityAdsShowCompletionState?
            ) {
                //completed
                submitQuiz()
                showCompletedDialog()
            }
        })

    }

    @SuppressLint("SimpleDateFormat")
    private fun submitQuiz() {
        val pushKey = completedData.push().key
        if (pushKey != null) {
            val value = ActivityData(
                title!!,
                corrected,
                totalQuiz,
                pushKey,
                SimpleDateFormat("dd/MM/yyyy").format(c.time),
                "Quiz",
                true
            )
            completedData.child(userId).child(pushKey).setValue(value).addOnCompleteListener { it ->
                if (it.isSuccessful){

                    if (sp.contains("quizCompleted")) {
                        map["quizCompleted"] = Integer.parseInt(sp.getString("quizCompleted", "0").toString()) + 1
                    }
                    if (sp.contains("rightAnswer")) {
                        map["rightAnswer"] = Integer.parseInt(sp.getString("rightAnswer", "0").toString()) + corrected
                    }
                    if (sp.contains("wrongAnswer")) {
                        val wrongAnswer:Int = totalQuiz - corrected
                        map["wrongAnswer"] = Integer.parseInt(sp.getString("wrongAnswer", "0").toString()) + wrongAnswer
                    }

                    user.child(userId).updateChildren(map).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //submitted
                        }
                    }



                }
                else Toast.makeText(this, "Error submitting the quiz, Please try again", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun wrongAnswerStyle(view2: RelativeLayout, text:TextView){
        view2.setBackgroundResource(R.drawable.red_corner_bg)
    }
    private fun rightAnswerStyle(view2: RelativeLayout, text:TextView){
        view2.setBackgroundResource(R.drawable.green_corner_bg)
    }

    private fun loadQuizzes() {
        questions.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastQuestionData = ArrayList<QuestionData>()
                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null && questionData.title== title) {
                        lastQuestionData.add(questionData)
                    }
                }
                questionList.clear() // Clear the list before adding new questions
                questionList.addAll(lastQuestionData)

                loadingDialog.dismiss()
                updateQuestion()
            }



            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun leave() {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Leave this quiz ?")
            .setMessage("Are you sure you want to leave this quiz without completing ?")
            .setPositiveButton("Yes Leave"
            ) { _, _ ->
                finish()
            }
            .setNegativeButton("Cancel"){_,_ ->

            }
            .create().show()
    }

    override fun onBackPressed() {
        if (isSubmitted){
            super.onBackPressed()
        }else {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("Leave this quiz ?")
                .setMessage("Are you sure you want to leave this quiz without completing ?")
                .setPositiveButton(
                    "Yes Leave"
                ) { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Cancel") { _, _ ->

                }
                .create().show()
        }
    }

    private fun updateQuestion() {
        binding.question.text = "Q$currentQuiz. ${questionList[currentQuiz -1].question}"
        coorectAnswer = questionList[currentQuiz -1].correctOption
        addOptionsToList(questionList[currentQuiz - 1])
    }

    private fun addOptionsToList(questionList: QuestionData) {
        options = ArrayList()
        options.add(questionList.correctOption)
        options.add(questionList.option2)
        options.add(questionList.option3)
        options.add(questionList.option4)

        options.shuffle()

        binding.option1.text = options[0]
        binding.option2.text = options[1]
        binding.option3.text = options[2]
        binding.option4.text = options[3]

    }

    private fun setAllDefault(){
        val optionsList = ArrayList<RelativeLayout>()
        optionsList.add(binding.bgOptionOne)
        optionsList.add(binding.bgOptionTwo)
        optionsList.add(binding.bgOptionThree)
        optionsList.add(binding.bgOptionFour)
        for (ope in optionsList){
            ope.setBackgroundResource(R.drawable.border)
        }

        val textList = ArrayList<TextView>()
        textList.add(binding.option1)
        textList.add(binding.option2)
        textList.add(binding.option3)
        textList.add(binding.option4)

    }
    private fun stopCorrectSound(){
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (rightAns.isPlaying) {
                rightAns.stop()
                rightAns.reset()
            }

        },delay)

    }
}