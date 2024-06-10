package devs.org.quizzybharat.ActivityFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import devs.org.quizzybharat.Adapters.MyQuizAdapter
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.databinding.FragmentMyQuizBinding


class MyQuizFragment : Fragment() {

    private lateinit var binding:FragmentMyQuizBinding
    private val activityData = FirebaseDatabase.getInstance().reference.child("QuizSet")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var list:List<QuestionData>

    private var userId = ""
    private lateinit var adapter: MyQuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = auth.uid.toString()

        list = ArrayList()
        binding.myQuizRecycler.visibility = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.noItem.visibility = View.GONE
        setAdapter()


        loadDataOfFirebase()
    }

    private fun loadDataOfFirebase() {
        activityData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activityList = mutableListOf<QuestionData>()
                for (activitySnapshot in snapshot.children) {
                    val questionData = activitySnapshot.getValue(QuestionData::class.java)
                    if (questionData != null && questionData.userId == userId) {
                        activityList.add(questionData)
                    }
                }
                activityList.reverse()
                list = activityList
                if (isAdded) {
                    setAdapter()
                }

                if (list.isEmpty()){
                    binding.myQuizRecycler.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.noItem.visibility = View.VISIBLE

                }else {
                    binding.myQuizRecycler.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.noItem.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter(){
        adapter = MyQuizAdapter(requireContext(), list)
        binding.myQuizRecycler.layoutManager = GridLayoutManager(requireContext(),1)
        binding.myQuizRecycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}