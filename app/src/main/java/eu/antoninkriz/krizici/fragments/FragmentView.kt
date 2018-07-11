package eu.antoninkriz.krizici.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import com.jaredrummler.materialspinner.MaterialSpinner
import eu.antoninkriz.krizici.R
import eu.antoninkriz.krizici.utils.Consts
import java.util.*

class FragmentView : Fragment() {

    private var vw: WebView? = null
    private val webViewClient = object : WebViewClient() {
        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            displayError(view)
        }

        private fun displayError(wv: WebView) {
            wv.visibility = View.INVISIBLE
            Toast.makeText(context, getString(R.string.fragmentView_webView_error), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_view, container, false)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Init controls
        val spinner: MaterialSpinner = view.findViewById(R.id.spinner)
        vw = view.findViewById(R.id.webview)
        vw!!.settings.javaScriptEnabled = false
        vw!!.settings.loadWithOverviewMode = true
        vw!!.settings.useWideViewPort = true
        vw!!.settings.setSupportZoom(true)
        vw!!.settings.builtInZoomControls = true
        vw!!.settings.displayZoomControls = false
        vw!!.webViewClient = webViewClient

        // Get selected tab from arguments bundle
        val bundle = arguments
        if (bundle == null || !bundle.containsKey("pos")) {
            Toast.makeText(context, getString(R.string.fragmentView_toast_error), Toast.LENGTH_LONG).show()
            return view
        }

        val tabposition = arguments!!.getInt("pos")

        // If tab "Supl"
        if (tabposition == -1) {
            spinner.visibility = View.GONE
            vw!!.visibility = View.INVISIBLE

            val btnLoadSupl: Button = view.findViewById(R.id.buttonLoadSupl)
            btnLoadSupl.visibility = View.VISIBLE

            btnLoadSupl.setOnClickListener {
                vw!!.loadUrl(Consts.URL_SUPL)
                btnLoadSupl.visibility = View.GONE
                vw!!.visibility = View.VISIBLE
            }

            return view
        }

        // Continue if not tab "Supl"
        // Get list from arguments bundle
        val list = ArrayList<String>()
        val passedAsArg = arguments!!.getStringArrayList("list")
        list.add(getString(R.string.fragmentView_selectItem))
        list.addAll(passedAsArg)

        val type: Consts.TABS = if (tabposition == 0) Consts.TABS.CLASSES else if (tabposition == 1) Consts.TABS.TEACHERS else Consts.TABS.CLASSROOMS
        val urlFormat = Consts.URL_SERVER_IMG(type.value)

        spinner.setItems(list)
        spinner.setOnItemSelectedListener { _, position, _, _ ->
            if (position > 0) {
                vw!!.loadUrl(String.format(urlFormat, position - 1))
            } else {
                vw!!.loadUrl(Consts.URL_BLANK)
            }
        }

        return view
    }
}
