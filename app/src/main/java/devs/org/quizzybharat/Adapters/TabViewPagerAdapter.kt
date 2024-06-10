package devs.org.quizzybharat.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import devs.org.quizzybharat.ActivityFragment.MyQuizFragment
import devs.org.quizzybharat.ActivityFragment.ParticipatedFragment

class TabViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0){
            ParticipatedFragment()
        } else {
            MyQuizFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0){
            "Participated"
        } else {
            "My Quizzes"
        }
    }

}
