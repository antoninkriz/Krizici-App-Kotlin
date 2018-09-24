package eu.antoninkriz.krizici.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object Network {
    class Result internal constructor(
            internal val success: Boolean,
            internal val result: ByteArray?,
            internal val exception: FailedDownloadException?,
            internal val encoding: String?,
            internal val contentType: String?)

    fun downloader(requestUrl: String, method: METHOD, data: String?, auth: String?, contentType: String?): Result {
        try {
            // Init connection
            val url = URL(requestUrl)
            val con = if (url.protocol == "https")
                url.openConnection() as HttpsURLConnection
            else
                url.openConnection() as HttpURLConnection


            con.setRequestProperty("Content-Type", contentType ?: "application/json")
            con.requestMethod = method.name
            con.connectTimeout = Consts.SERVER_TIMEOUT
            con.readTimeout = Consts.SERVER_TIMEOUT

            // Add JWT
            if (auth != null) {
                con.setRequestProperty("Authorization", "Bearer $auth")
            }

            var post: OutputStreamWriter? = null

            // Add POST request data
            if (method == METHOD.POST && data != null) {
                post = OutputStreamWriter(con.outputStream)
                post.write(data)
                post.flush()
            }

            // Get response
            val responseCode = con.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val input = con.inputStream

                // Read response
                val response = input.readBytes()

                post?.close()
                input.close()
                return Result(true, response, null, con.contentEncoding, con.contentType)
            } else {
                Log.i("NETWORK", "Response code: $responseCode")
                Log.i("NETWORK", con.responseMessage)
            }

            post?.close()
        } catch (e: Exception) {
            Log.i("NETWORK", e.message ?: "<no message provided>")
            e.printStackTrace()
        }

        return Result(false, null, FailedDownloadException("Failed to download file '$requestUrl'", 0), null, null)
    }

    fun checkNetworkConnection(c: Context): Boolean {
        val connectivityManager = c.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    enum class METHOD {
        GET,
        POST
    }
}
