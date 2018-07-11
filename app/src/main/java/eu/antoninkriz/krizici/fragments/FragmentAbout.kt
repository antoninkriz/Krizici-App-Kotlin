package eu.antoninkriz.krizici.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast

import eu.antoninkriz.krizici.R

class FragmentAbout : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val activity = activity

        if (activity == null) {
            Toast.makeText(context, getString(R.string.fragmentAbout_toast_error), Toast.LENGTH_LONG).show()
            return view
        }

        val tl: TabLayout = activity.findViewById(R.id.tab_layout)
        tl.animate().scaleY(1f).setInterpolator(DecelerateInterpolator()).start()
        tl.visibility = View.GONE

        return view
    }
}
