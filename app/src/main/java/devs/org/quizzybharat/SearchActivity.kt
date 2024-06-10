package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import devs.org.quizzybharat.Adapters.CategoryViewerAdapter
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private var userUid = ""
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val firebaseData = database.reference.child("QuizSet")
    private val quizSet = database.reference
    private lateinit var list :List<QuestionData>
    private lateinit var activityList :List<ActivityData>
    private lateinit var adapter: CategoryViewerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list = ArrayList()
        activityList = ArrayList()
        userUid = auth.uid.toString()
        ColorAccordingTheme(window).statusBarColor()
        clickEvents()

        loadActivity()
    }

    private fun loadActivity() {
        quizSet.child("Completed").child(FirebaseAuth.getInstance().uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val acList = mutableListOf<ActivityData>()
                val lastQuestionData = mutableMapOf<String, ActivityData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(ActivityData::class.java)
                    if (questionData != null) {
                        lastQuestionData[questionData.title] = questionData
                    }
                    acList.addAll(lastQuestionData.values)
                    activityList = acList
                }
                loadData()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadData() {
        firebaseData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionList = mutableListOf<QuestionData>()
                val lastQuestionData = mutableMapOf<String, QuestionData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null  && questionData.approved) {
                        if (!lastQuestionData.containsKey(questionData.title) ||
                            questionData.totalQuestions > (lastQuestionData[questionData.title]?.totalQuestions
                                ?: 0)
                        ) {
                            lastQuestionData[questionData.title] = questionData
                        }
                    }
                }

                questionList.addAll(lastQuestionData.values)
                list = questionList

                val searchQuery = binding.edtSearch.text.toString().trim()
                val filteredList = list.filter { questionData ->
                    questionData.title.contains(searchQuery, ignoreCase = true)
                }

                setAdapter(filteredList)
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter(filteredList: List<QuestionData>){
        adapter = CategoryViewerAdapter(this, filteredList, activityList)
        binding.searchRecycler.layoutManager = GridLayoutManager(this,1)
        binding.searchRecycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun clickEvents() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0 != null) {
                    if (p0.isNotEmpty()){
                        binding.cut.visibility = View.VISIBLE
                    }else binding.cut.visibility = View.GONE
                }else binding.cut.visibility = View.GONE
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.cut.setOnClickListener {
            binding.edtSearch.setText("")
            loadData()
        }
        binding.back.setOnClickListener {
            finish()
        }

        binding.edtSearch.setOnEditorActionListener { _, _, _ ->

            loadData()

            true
        }

    }


}