package eu.antoninkriz.krizici.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object Network {
    class Result internal constructor(internal val success: Boolean, internal val result: String?, internal val exception: FailedDownloadException?)

    fun downloadString(requestUrl: String): Result {
        try {
            // Init connection
            val url = URL(requestUrl)
            val con = url.openConnection() as HttpsURLConnection
            con.requestMethod = "GET"
            con.connectTimeout = Consts.SERVER_TIMEOUT
            con.readTimeout = Consts.SERVER_TIMEOUT

            // Get response
            val responseCode = con.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(con.inputStream))
                val response = StringBuilder()

                // Read response
                response.append(input.readLines())

                return Result(true, response.toString(), null)
            }
        } catch (e: Exception) {
            Log.i("NETWORK", e.message)
        }

        return Result(false, null, FailedDownloadException("Failed to download file '$requestUrl'", 0))
    }

    fun checkNetworkConnection(c: Context): Boolean {
        val connectivityManager = c.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }
}
