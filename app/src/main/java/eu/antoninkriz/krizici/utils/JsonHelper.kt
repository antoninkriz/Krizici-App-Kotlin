package eu.antoninkriz.krizici.utils

import eu.antoninkriz.krizici.exceptions.UnknownException
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException

object JsonHelper {
    enum class DOWNLOADFILE(val file: String) {
        TIMETABLES("json"),
        CONTACTS("contacts"),
        JWT("jwt")
    }

    @Throws(FailedDownloadException::class, UnknownException::class)
    fun getJson(downloadfile: DOWNLOADFILE, auth: String? = null, method: Network.METHOD = Network.METHOD.GET, data: String? = null): String? {
        val url = Consts.URL_SERVER_JSON().format(downloadfile.file)
        val result = Network.downloader(url, method, data, auth, null)

        if (result.success) {
            return String(result.result ?: ByteArray(0))
        } else {
            if (result.exception != null) {
                throw result.exception
            } else {
                throw UnknownException("Unknown error while downloading file '$url'", 0)
            }
        }
    }
}
