package devs.org.quizzybharat.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import devs.org.quizzybharat.Adapters.CategoryAdapter
import devs.org.quizzybharat.Adapters.RecommendedAdapter
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.CategoryData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.ProfileActivity
import devs.org.quizzybharat.R
import devs.org.quizzybharat.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var list: List<CategoryData>
    private lateinit var rList: List<QuestionData>
    private val database = FirebaseDatabase.getInstance()
    private val category = database.reference.child("Category")
    private val recommended = database.reference.child("QuizSet")
    private val completed = database.reference.child("Completed")
    private lateinit var adapter: CategoryAdapter
    private lateinit var rAdapter: RecommendedAdapter
    private lateinit var sp: SharedPreferences
    private var profileUrl = ""
    private var userType = ""
    private lateinit var activityList: List<ActivityData>
    private var timer: Timer? = null
    private var isSnapHelperAttached = false // Flag to track if SnapHelper is already attached



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = requireContext().getSharedPreferences("details", AppCompatActivity.MODE_PRIVATE)

        if (sp.contains("userType")) {
            userType = sp.getString("userType", "").toString()
            sp.edit().putString("userType", userType).apply()
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
        }

        if (sp.contains("profileUrl")) {
            profileUrl = sp.getString("profileUrl", "").toString()
            if (isAdded) {
                Glide.with(requireContext())
                    .load(profileUrl)
                    .into(binding.profile)
            }
        }
        list = ArrayList()
        rList = ArrayList()
        activityList = ArrayList()
        // In your onViewCreated function
        adapter = CategoryAdapter(requireContext(), list)
        rAdapter = RecommendedAdapter(requireContext(), rList, activityList)

        setAdapter()
        rAdapter()
        binding.profile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        loadCategory()
        loadActivityData()
        loadRecommended()

        if (!sp.contains("tutorial1") ) {
            if (sp.getBoolean("tutorial1",false)) {
                startAutoScroll()
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    stopAutoScroll()
                }, 4000)
            }
        }


    }

    override fun onResume() {
        super.onResume()
        loadActivityData()

        loadRecommended()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
        sp.edit().putBoolean("tutorial1", true).apply()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {

        if (!isAdded) return
        adapter = CategoryAdapter(requireContext(), list)
        binding.categoryRecycler.layoutManager = GridLayoutManager(requireContext(), 7, GridLayoutManager.HORIZONTAL, false)
        binding.categoryRecycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun rAdapter() {
        if (!isAdded) return
        rAdapter = RecommendedAdapter(requireContext(), rList, activityList)
        binding.recommendedRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recommendedRecycler.adapter = rAdapter
        rAdapter.notifyDataSetChanged()
    }

    private fun showRecycler() {
        binding.categoryRecycler.visibility = View.VISIBLE
        binding.recommendedRecycler.visibility = View.VISIBLE
        binding.categoryProgress.visibility = View.GONE
        binding.rProgress.visibility = View.GONE
    }



    private fun loadCategory() {
        try {
            category.addListenerForSingleValueEvent(object : ValueEventListener {

                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryList = mutableListOf<CategoryData>()
                    for (categorySnapshot in snapshot.children) {
                        val categoryData = categorySnapshot.getValue(CategoryData::class.java)
                        if (categoryData!= null)
                            categoryList.add(categoryData)
                    }
                    list = categoryList

                    showRecycler()

                    setAdapter()

                }

                override fun onCancelled(error: DatabaseError) {
                    showRecycler()
                    Toast.makeText(requireContext(), "Something want wrong, please try again later", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            showRecycler()
            Toast.makeText(requireContext(), "Something want wrong, please try again later", Toast.LENGTH_SHORT).show()
        }


        database.reference.child("Users").child(FirebaseAuth.getInstance()
            .uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                val childValue: HashMap<String?, Any?>? = snapshot.getValue(ind)

                if (childValue?.get("profileUrl") != null) {
                    profileUrl = childValue["profileUrl"].toString()
                    sp.edit().putString("profileUrl", profileUrl).apply()
                }
                if (isAdded) {
                    Glide.with(requireContext())
                        .load(profileUrl)
                        .into(binding.profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun loadRecommended() {
        recommended.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionList = mutableListOf<QuestionData>()
                val lastQuestionData = mutableMapOf<String, QuestionData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null && questionData.approved) {
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
                rList = questionList
                rAdapter()

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun loadActivityData() {
        completed.child(FirebaseAuth.getInstance().uid.toString()).addValueEventListener(object : ValueEventListener {
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
                rAdapter()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun startAutoScroll() {
        if (!isSnapHelperAttached) {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.categoryRecycler)
            isSnapHelperAttached = true
        }

        timer = Timer()
        timer?.scheduleAtFixedRate(ScrollAutoTask(binding.categoryRecycler), 0, 1000)
    }

    private fun stopAutoScroll() {
        timer?.cancel()
        timer = null
    }

    class ScrollAutoTask(private val recyclerView: RecyclerView) : TimerTask() {
        override fun run() {
            recyclerView.post {
                recyclerView.smoothScrollBy(100, 0)
            }
        }
    }
}
