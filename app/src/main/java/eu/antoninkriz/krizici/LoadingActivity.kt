package eu.antoninkriz.krizici

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.eclipsesource.json.Json
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import eu.antoninkriz.krizici.exceptions.UnknownException
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException
import eu.antoninkriz.krizici.utils.JsonHelper
import eu.antoninkriz.krizici.utils.Network
import kotlinx.android.synthetic.main.activity_loading.*
import org.json.JSONObject
import java.lang.ref.WeakReference


class LoadingActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var mBackPressed: Long = 0

    private val rcSignIn = 1337
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("542683906617-okqd28rumn16ni4fcfvo21dnan5dtgij.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("CONFAILCODE", p0.errorCode.toString())
        Log.i("CONFAILMESS", p0.errorMessage.toString())
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                authWithServer(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("ONACTIVITYRESULT", "Google sign in failed", e)
            }
        }
    }

    private fun authWithServer(acct: GoogleSignInAccount) {
        Log.d("FAWG", "serverAuth: " + acct.email)

        if (acct.email != null) {
            Log.i("CHECKLOGIN", acct.email)
            Snackbar.make(loading_layout, getString(R.string.auth_ok), Snackbar.LENGTH_SHORT).show()
            AsyncGetLogin(this, mGoogleSignInClient).execute(acct.idToken)
        } else {
            Log.i("CHECKLOGIN", "EMAIL NULL")
            Snackbar.make(loading_layout, getString(R.string.auth_accountNotInDomain), Snackbar.LENGTH_SHORT).show()

            mGoogleSignInClient?.signOut()
        }
    }

    /*private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("FAWG", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("FAWG", "signInWithCredential:success")
                        Snackbar.make(loading_layout, "Logged in", Snackbar.LENGTH_SHORT).show()
                        val user = mAuth?.currentUser
                        checkLogin(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FAWG", "signInWithCredential:failure", task.exception)
                        Snackbar.make(loading_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        checkLogin(null)
                    }
                }
    }*/

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

    private class AsyncGetLogin internal constructor(context: LoadingActivity, private val mGoogleSignInClient: GoogleSignInClient?) : AsyncTask<String, Void, AsyncGetLogin.ERROR?>() {
        private var r: String? = null
        private val activityReference: WeakReference<LoadingActivity> = WeakReference(context)

        override fun doInBackground(vararg string: String): ERROR? {
            val tkn = JSONObject()
            tkn.put("idtoken", string[0])

            try {
                r = JsonHelper.getJson(JsonHelper.DOWNLOADFILE.JWT, null, Network.METHOD.POST, tkn.toString())
            } catch (e: FailedDownloadException) {
                e.printStackTrace()
                return AsyncGetLogin.ERROR.FAILED_DOWNLOAD
            } catch (e: UnknownException) {
                e.printStackTrace()
                return AsyncGetLogin.ERROR.UNKNOWN_ERROR
            }

            return null
        }

        override fun onPostExecute(error: ERROR?) {
            val activity = activityReference.get() ?: return
            val loadingLayout = activity.findViewById<RelativeLayout>(R.id.loading_layout)

            if (error != null) {
                val message: String = when (error) {
                    ERROR.CAN_NOT_CONNECT -> activity.getString(R.string.error_canNotConnect)
                    ERROR.FAILED_DOWNLOAD -> activity.getString(R.string.error_failedDownload)
                    ERROR.NO_INTERNET -> activity.getString(R.string.error_noInternet)
                    ERROR.NO_INTERNET_PERMISSION -> activity.getString(R.string.error_noInternetPermission)
                    ERROR.UNKNOWN_ERROR -> activity.getString(R.string.error_unknown)
                }

                val prg = activity.findViewById<ProgressBar>(R.id.progressBar)

                prg.visibility = View.GONE
                Snackbar.make(loadingLayout, message, Snackbar.LENGTH_SHORT).show()

                return
            }

            if (r != null) {
                val json = Json.parse(r).asObject()
                val success = json.getBoolean("Success", false)
                val response = json.getString("Response", null)
                val expire = json.getLong("Expires", 0L)

                if (success && expire != 0L) {
                    // Everything is OK
                    val shpe = activity.getSharedPreferences("jwt", Context.MODE_PRIVATE).edit()

                    shpe.putString("token", response)
                    shpe.putLong("expire", expire)
                    shpe.apply()

                    if (expire > (System.currentTimeMillis() / 1000))

                    Snackbar.make(loadingLayout, activity.getString(R.string.auth_ok), Snackbar.LENGTH_SHORT).show()

                    // Get required async
                    AsyncGetJson(activityReference).execute(response)
                    return
                } else if (response == "email_not_in_domain") {
                    Snackbar.make(loadingLayout, activity.getString(R.string.auth_accountNotInDomain), Snackbar.LENGTH_SHORT).show()
                    mGoogleSignInClient?.signOut()
                } else if (response.startsWith("invalid_id_token_")) {
                    Snackbar.make(loadingLayout, activity.getString(R.string.auth_failed), Snackbar.LENGTH_SHORT).show()
                    mGoogleSignInClient?.signOut()
                }
            } else {
                Snackbar.make(loadingLayout, activity.getString(R.string.auth_failed), Snackbar.LENGTH_SHORT).show()
                mGoogleSignInClient?.signOut()
            }
        }

        internal enum class ERROR {
            CAN_NOT_CONNECT,
            FAILED_DOWNLOAD,
            NO_INTERNET,
            NO_INTERNET_PERMISSION,
            UNKNOWN_ERROR
        }
    }

    private class AsyncGetJson internal constructor(weakReference: WeakReference<LoadingActivity>) : AsyncTask<String, Void, AsyncGetJson.ERROR?>() {

        private var r: Result = Result()
        private var networkOk: Boolean = false
        private val activityReference: WeakReference<LoadingActivity> = weakReference

        override fun onPreExecute() {
            val activity = activityReference.get() ?: return
            val prg = activity.findViewById<ProgressBar>(R.id.progressBar)
            prg.visibility = View.VISIBLE

            // Check network
            networkOk = Network.checkNetworkConnection(activity.baseContext)
        }

        override fun doInBackground(vararg string: String?): ERROR? {
            if (string.size != 1 || string[0] == null) return null

            if (!networkOk) {
                return ERROR.NO_INTERNET
            }

            val token = string[0]

            try {
                r.jRozvrh = JsonHelper.getJson(JsonHelper.DOWNLOADFILE.TIMETABLES, token)
                r.jContacts = JsonHelper.getJson(JsonHelper.DOWNLOADFILE.CONTACTS, token)
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
