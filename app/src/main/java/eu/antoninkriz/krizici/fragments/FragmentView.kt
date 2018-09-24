package eu.antoninkriz.krizici.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import com.jaredrummler.materialspinner.MaterialSpinner
import eu.antoninkriz.krizici.R
import eu.antoninkriz.krizici.utils.Consts
import eu.antoninkriz.krizici.utils.Network
import java.io.ByteArrayInputStream
import java.util.*


class FragmentView : Fragment() {
    private var jwt: String? = null

    private val webViewClient = object : WebViewClient() {
        @SuppressLint("NewApi")
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            if (request?.url?.lastPathSegment?.endsWith("favicon.ico") == true)
                return null

            if (request != null && request.url != null && request.method.equals("get", true)) {
                val scheme = request.url.scheme.trim()
                if (scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)) {
                    try {
                        val response = Network.downloader(request.url.toString(), Network.METHOD.GET, null, jwt, null)
                        val stream =
                                if (response.success)
                                    ByteArrayInputStream(response.result)
                                else
                                    ByteArrayInputStream(response.exception?.message?.toByteArray() ?: ByteArray(0))

                        return WebResourceResponse(response.contentType, response.encoding, stream)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return null
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            if (url.endsWith("favicon.ico"))
                return null

            if (!TextUtils.isEmpty(url) && Uri.parse(url).scheme != null) {
                val scheme = Uri.parse(url).scheme.trim()
                if (scheme.equals("http", ignoreCase = true) || scheme.equals("https", ignoreCase = true)) {
                    try {
                        val response = Network.downloader(url, Network.METHOD.GET, null, jwt, null)
                        val stream =
                                if (response.success)
                                    ByteArrayInputStream(response.result)
                                else
                                    ByteArrayInputStream(response.exception?.message?.toByteArray() ?: ByteArray(0))

                        return WebResourceResponse(response.contentType, response.encoding, stream)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            return null
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            handler.proceed()
            Log.i("VWERROR - PE", error.primaryError.toString())
            Log.i("VWERROR - CE", error.certificate.issuedBy.cName)
            Log.i("VWERROR - URL", error.url)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            displayError(view)
        }

        private fun displayError(wv: WebView) {
            wv.visibility = View.INVISIBLE
            Toast.makeText(context, getString(R.string.fragmentView_webView_error), Toast.LENGTH_LONG).show()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_view, container, false)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        jwt = context?.getSharedPreferences("jwt", Context.MODE_PRIVATE)?.getString("token", "")

        // Init controls
        val spinner: MaterialSpinner = view.findViewById(R.id.spinner)
        val wv: WebView = view.findViewById(R.id.webview)
        wv.settings.javaScriptEnabled = false
        wv.settings.loadWithOverviewMode = true
        wv.settings.useWideViewPort = true
        wv.settings.setSupportZoom(true)
        wv.settings.builtInZoomControls = true
        wv.settings.displayZoomControls = false
        wv.webViewClient = webViewClient

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
            wv.visibility = View.INVISIBLE

            val btnLoadSupl: Button = view.findViewById(R.id.buttonLoadSupl)
            btnLoadSupl.visibility = View.VISIBLE

            btnLoadSupl.setOnClickListener {
                wv.loadUrl(Consts.URL_SUPL)
                btnLoadSupl.visibility = View.GONE
                wv.visibility = View.VISIBLE
            }

            return view
        }

        // Continue if not tab "Supl"
        // Get list from arguments bundle
        val list = ArrayList<String>()
        val passedAsArg = arguments!!.getStringArrayList("list")
        list.add(getString(R.string.fragmentView_selectItem))
        list.addAll(passedAsArg)
        spinner.setItems(list)

        val type: Consts.TABS = if (tabposition == 0) Consts.TABS.CLASSES else if (tabposition == 1) Consts.TABS.TEACHERS else Consts.TABS.CLASSROOMS
        val urlFormat = String.format(Consts.URL_SERVER_JSON(), Consts.URL_SERVER_IMG(type.value))

        spinner.setOnItemSelectedListener { _, position, _, _ ->
            if (position > 0) {
                val url = String.format(urlFormat, position - 1)
                val headers = mapOf(
                        Pair("Authorize", "Bearer " + context!!.getSharedPreferences("jwt", Context.MODE_PRIVATE).getString("token", ""))
                )

                wv.loadUrl(url, headers)
            } else {
                wv.loadUrl(Consts.URL_BLANK)
            }
        }

        return view
    }
}
