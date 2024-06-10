package devs.org.quizzybharat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import devs.org.quizzybharat.Adapters.ViewPagerAdapter
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.Data.ViewsData
import devs.org.quizzybharat.StatusBarColor.ColorAccordingTheme
import devs.org.quizzybharat.databinding.FragmentRandomBinding
import java.util.*
import kotlin.random.Random

class RandomActivity : AppCompatActivity() {

    private lateinit var binding : FragmentRandomBinding
    private val reference = FirebaseDatabase.getInstance().reference.child("QuizSet")
    private val viewSystem = FirebaseDatabase.getInstance().reference.child("ViewSystem")
    private lateinit var list:List<QuestionData>
    private lateinit var viewsList:ArrayList<ViewsData>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var sp: SharedPreferences
    private var profileUrl = ""
    private var userType = ""
    private val questionsLiveData = MutableLiveData<List<QuestionData>>()
    private var userId = ""
    private lateinit var t:ArrayList<TimerTask?>
    private lateinit var second:ArrayList<Int>

    private var isFirstTime = true // Flag to track if the app is opened for the first time

    private val colors = listOf(
        Color.parseColor("#374550"),
        Color.parseColor("#000000"),
        Color.parseColor("#3B3C36"),
        Color.parseColor("#1B1110"),
        Color.parseColor("#343839"),
        Color.parseColor("#4B3623"),
        Color.parseColor("#3D0D03"),
        Color.parseColor("#424A4D"),
        Color.parseColor("#555D52"))

    private val unityGameID = "5564207"
    private val testMode = false
    private val placementId = "Interstitial_Android"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRandomBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ColorAccordingTheme(window).statusBarColor()
        list = ArrayList()
        viewsList = ArrayList()
        t = ArrayList()
        second = ArrayList()
        userId = FirebaseAuth.getInstance().uid.toString()

        sp = getSharedPreferences("details", MODE_PRIVATE)
        setProfileAndSps()

        initializeUnityAds()

        binding.profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.addbg.setOnClickListener {
            gotoAdd()
            Toast.makeText(this,"clicked",Toast.LENGTH_SHORT).show()
        }

        questionsLiveData.observe(this) { questionsList ->
            viewPagerAdapter.updateList(questionsList)
        }

        loadQuizzes()

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val randomColor = colors.random()
                    binding.bg.setBackgroundColor(randomColor)
                    window.statusBarColor = randomColor
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION


                    val a = 6
                    val b = Random.nextInt(1,20)
                    if (a == b) showAd()
                }
            }
        )

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

    private fun gotoAdd(){
        startActivity(Intent(this, AddQuizActivity::class.java))
    }

    override fun onPause() {
        super.onPause()

        sp.edit().putBoolean("isFirstTime", isFirstTime).apply()
    }

    private fun setProfileAndSps() {
        if (sp.contains("userType")){
            userType = sp.getString("userType","").toString()
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
        if (sp.contains("profileUrl")){
            profileUrl = sp.getString("profileUrl","").toString()
            Glide.with(this)
                .load(profileUrl)
                .into(binding.profile)
        }

        // Retrieve the isFirstTime value from SharedPreferences
        isFirstTime = sp.getBoolean("isFirstTime", true)
    }

    private fun loadQuizzes(){
        list = emptyList()
        second.clear()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val questionList = mutableListOf<QuestionData>()
                val seconds = mutableListOf<Int>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null && questionData.userId != userId && questionData.approved) {
                        questionList.add(questionData)
                        t.add(null)
                        seconds.add(20)
                    }
                }

                second = seconds as ArrayList<Int>

                questionList.shuffle()

                list = questionList

                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
        viewSystem.child(FirebaseAuth.getInstance().uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val vlist = mutableListOf<ViewsData>()

                for (categorySnapshot in snapshot.children) {
                    val questionData = categorySnapshot.getValue(ViewsData::class.java)
                    if (questionData != null) {
                        vlist.add(questionData)
                    }
                }

                viewsList = vlist as ArrayList<ViewsData>

                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        viewPagerAdapter = ViewPagerAdapter(this,list,viewsList,t,second)
        binding.viewPager.adapter = viewPagerAdapter
        viewPagerAdapter.notifyDataSetChanged()
    }

    fun gotoAdd(view: View) {}

}
