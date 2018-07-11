package eu.antoninkriz.krizici.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import com.eclipsesource.json.Json
import eu.antoninkriz.krizici.R
import java.util.*

class FragmentMain : Fragment() {

    internal var list: MutableList<ArrayList<String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)

        val json: String?

        try {
            val bundle = arguments
            if (bundle == null || !bundle.containsKey("jsonRozvrh")) {
                return
            }

            bundle.getString("jsonRozvrh")
            json = arguments!!.getString("jsonRozvrh")
        } catch (ex: Exception) {
            ex.printStackTrace()
            return
        }

        if (json == null) {
            Toast.makeText(context, getString(R.string.fragmentMain_toast_error), Toast.LENGTH_LONG).show()
            return
        }

        try {
            val joObject = Json.parse(json).asArray()[0].asObject()

            var templist = ArrayList<String>()

            var jaArr = joObject.get("tridy").asArray()
            jaArr.forEach { v -> templist.add(v.asString()) }
            list.add(templist)
            templist = ArrayList()

            jaArr = joObject.get("ucitele").asArray()
            jaArr.forEach { v -> templist.add(v.asString()) }
            list.add(templist)
            templist = ArrayList()

            jaArr = joObject.get("ucebny").asArray()
            jaArr.forEach { v -> templist.add(v.asString()) }
            list.add(templist)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.fragmentMain_toast_error2), Toast.LENGTH_LONG).show()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Init viewpager adapter
        val adapter = ViewPagerAdapter(childFragmentManager)

        // Add fragments to adapter
        adapter.add(FragmentView())
        adapter.add(FragmentView())
        adapter.add(FragmentView())
        adapter.add(FragmentView())

        // Set adapter to viewPager
        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        viewPager.offscreenPageLimit = adapter.count
        viewPager.adapter = adapter

        // Setup tabs and show them in a cool way
        val activity = activity ?: return view

        val tl = activity.findViewById<TabLayout>(R.id.tab_layout)
        tl.animate().scaleY(1f).setInterpolator(DecelerateInterpolator()).start()
        tl.visibility = View.VISIBLE
        tl.setupWithViewPager(viewPager, true)

        // Add tabs to tabLayout, this is defense against reloading this fragment
        val tabCount = tl.tabCount
        if (tabCount != 4) {
            tl.removeAllTabs()
            tl.addTab(tl.newTab())
            tl.addTab(tl.newTab())
            tl.addTab(tl.newTab())
            tl.addTab(tl.newTab())
        }

        return view
    }

    inner class ViewPagerAdapter(activity: FragmentManager) : FragmentPagerAdapter(activity) {

        private val _fragments: ArrayList<Fragment> = ArrayList()

        fun add(fragment: Fragment) {
            this._fragments.add(fragment)
        }

        override fun getItem(position: Int): Fragment {
            // Add tab pos to show correct supplementation in FragmentView
            val bundle = Bundle()
            if (position > 0) bundle.putStringArrayList("list", list[position - 1])
            bundle.putInt("pos", position - 1)
            val fragment = _fragments[position]
            fragment.arguments = bundle
            return fragment
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> getString(R.string.tab_supl)
                1 -> getString(R.string.tab_classes)
                2 -> getString(R.string.tab_teachers)
                3 -> getString(R.string.tab_classrooms)
                else -> "UNEXPECTED"
            }
        }
    }
}