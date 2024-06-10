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
import devs.org.quizzybharat.Adapters.ActivityAdapter
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.databinding.FragmentParticipatedBinding


class ParticipatedFragment : Fragment() {

    private val activityData = FirebaseDatabase.getInstance().reference.child("Completed")
    private val auth = FirebaseAuth.getInstance()
    private lateinit var list:ArrayList<ActivityData>

    private var userId = ""
    private lateinit var adapter: ActivityAdapter
    private lateinit var binding:FragmentParticipatedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParticipatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = auth.uid.toString()

        list = ArrayList()
        binding.activityRecyclerView.visibility = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE
        setAdapter()

        loadDataOfFirebase()

    }

    private fun loadDataOfFirebase() {
        activityData.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activityList = mutableListOf<ActivityData>()
                for (activitySnapshot in snapshot.children) {
                    val activityDataa = activitySnapshot.getValue(ActivityData::class.java)
                    activityDataa?.let {
                        activityList.add(it)
                    }
                }
                activityList.reverse()
                list = activityList as ArrayList<ActivityData>
                if (isAdded) {
                    setAdapter()
                }

                if (list.isNotEmpty()) {
                    binding.activityRecyclerView.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.noItem.visibility = View.GONE
                }else{
                    binding.activityRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                    binding.noItem.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter(){
        adapter = ActivityAdapter(requireContext(), list)
        binding.activityRecyclerView.layoutManager = GridLayoutManager(requireContext(),1)
        binding.activityRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}