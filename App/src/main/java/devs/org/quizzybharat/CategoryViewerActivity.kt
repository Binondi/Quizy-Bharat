package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import devs.org.quizzybharat.Adapters.CategoryViewerAdapter
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.Heading
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.Internet.ChackInternet
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivityCategoryViewerBinding

class CategoryViewerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCategoryViewerBinding
    private var heading = ""
    private val quizSet = FirebaseDatabase.getInstance().reference
    private lateinit var headingList:List<Heading>
    private lateinit var list:List<QuestionData>
    private lateinit var activityList:List<ActivityData>
    private lateinit var adapter: CategoryViewerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ColorAccordingTheme(window).statusBarColor()
        list = ArrayList()
        headingList = ArrayList()
        activityList = ArrayList()
        heading = intent.getStringExtra("title").toString()

        binding.heading.text = heading
        setAdapter()
        binding.back.setOnClickListener {
            finish()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter(){
        adapter = CategoryViewerAdapter(this,list, activityList)
        binding.recyclerView.layoutManager = GridLayoutManager(this,1)
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    private fun loadCategoryData() {
        quizSet.child("Completed").child(FirebaseAuth.getInstance().uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acList = mutableListOf<ActivityData>()
                val lastQuestionData = mutableMapOf<String, ActivityData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(ActivityData::class.java)
                    if (questionData != null ) {
                        lastQuestionData[questionData.title] = questionData
                    }
                    acList.addAll(lastQuestionData.values)
                    activityList = acList


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        loadSecondData()

    }
    override fun onResume() {
        super.onResume()

        if (!ChackInternet().isInternetAvailable(this)){
            showNoNetDialog()
        }
        loadCategoryData()
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

    private fun loadSecondData(){
        quizSet.child("QuizSet").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionList = mutableListOf<QuestionData>()
                val lastQuestionData = mutableMapOf<String, QuestionData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null && questionData.category == heading && questionData.approved) {
                        if (!lastQuestionData.containsKey(questionData.title) ||
                            questionData.totalQuestions > (lastQuestionData[questionData.title]?.totalQuestions
                                ?: 0)
                        ) {
                            lastQuestionData[questionData.title] = questionData
                        }
                    }
                }

                questionList.addAll(lastQuestionData.values)

                questionList.shuffle()
                list = questionList

                if (list.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.noItem.visibility = View.GONE
                }else{
                    binding.recyclerView.visibility = View.GONE
                    binding.noItem.visibility = View.VISIBLE
                }
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

}