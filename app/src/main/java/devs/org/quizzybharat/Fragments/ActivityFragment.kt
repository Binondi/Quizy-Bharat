package devs.org.quizzybharat.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import devs.org.quizzybharat.Adapters.TabViewPagerAdapter
import devs.org.quizzybharat.databinding.FragmentActivityBinding

class ActivityFragment : Fragment() {
    private lateinit var binding:FragmentActivityBinding
    private lateinit var tabAdapter : TabViewPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabAdapter = TabViewPagerAdapter(childFragmentManager)
        binding.activityViewPager.adapter = tabAdapter
        binding.tabLayout.setupWithViewPager(binding.activityViewPager)


    }

}