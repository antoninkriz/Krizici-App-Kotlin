package eu.antoninkriz.krizici

import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

import java.lang.ref.WeakReference

import eu.antoninkriz.krizici.exceptions.UnknownException
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException
import eu.antoninkriz.krizici.utils.JsonHelper
import eu.antoninkriz.krizici.utils.Network


class LoadingActivity : AppCompatActivity() {

    private var mBackPressed: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Check network
        val haveInternet = Network.checkNetworkConnection(baseContext)

        // Do stuff async
        AsyncGetJson(this).execute(haveInternet)
    }

    override fun onBackPressed() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }
            return
        }

        mBackPressed = System.currentTimeMillis()
    }

    private class AsyncGetJson internal constructor(context: LoadingActivity) : AsyncTask<Boolean, Void, AsyncGetJson.ERROR?>() {

        private var r: Result = Result()
        private val activityReference: WeakReference<LoadingActivity> = WeakReference(context)

        override fun doInBackground(vararg boolean: Boolean?): ERROR? {
            if (boolean.size != 1) return null
            if (boolean[0] == null) return null

            val bool: Boolean = boolean[0]!!

            if (!bool) {
                return ERROR.NO_INTERNET
            }

            try {
                r.jRozvrh = JsonHelper.getJson(JsonHelper.DOWNLOADFILE.TIMETABLES)
                r.jContacts = JsonHelper.getJson(JsonHelper.DOWNLOADFILE.CONTACTS)
            } catch (e: FailedDownloadException) {
                e.printStackTrace()
                return ERROR.FAILED_DOWNLOAD
            } catch (e: UnknownException) {
                e.printStackTrace()
                return ERROR.UNKNOWN_ERROR
            }

            return null
        }

        override fun onPostExecute(error: ERROR?) {
            val activity = activityReference.get() ?: return

            if (error != null) {
                val message: String = when (error) {
                    ERROR.CAN_NOT_CONNECT -> activity.getString(R.string.error_canNotConnect)
                    ERROR.FAILED_DOWNLOAD -> activity.getString(R.string.error_failedDownload)
                    ERROR.NO_INTERNET -> activity.getString(R.string.error_noInternet)
                    ERROR.NO_INTERNET_PERMISSION -> activity.getString(R.string.error_noInternetPermission)
                    ERROR.UNKNOWN_ERROR -> activity.getString(R.string.error_unknown)
                }

                val prg = activity.findViewById<ProgressBar>(R.id.progressBar)
                val reloadButton = activity.findViewById<Button>(R.id.button)
                val errorTextView = activity.findViewById<TextView>(R.id.errorTextView)

                prg.visibility = View.GONE

                reloadButton.setOnClickListener {
                    activity.finish()
                    activity.startActivity(activity.intent)
                }
                reloadButton.visibility = View.VISIBLE

                errorTextView.text = message
                errorTextView.visibility = View.VISIBLE

                return
            }


            val i = Intent(activity, MainActivity::class.java)
            i.putExtra("jsonRozvrh", r.jRozvrh)
            i.putExtra("jsonContacts", r.jContacts)
            activity.startActivity(i)
            activity.finish()
        }

        private inner class Result {
            internal var jRozvrh: String? = ""
            internal var jContacts: String? = ""
        }

        internal enum class ERROR {
            CAN_NOT_CONNECT,
            FAILED_DOWNLOAD,
            NO_INTERNET,
            NO_INTERNET_PERMISSION,
            UNKNOWN_ERROR
        }
    }
}
