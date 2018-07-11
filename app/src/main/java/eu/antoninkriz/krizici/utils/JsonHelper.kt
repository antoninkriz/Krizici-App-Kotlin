package eu.antoninkriz.krizici.utils

import eu.antoninkriz.krizici.exceptions.UnknownException
import eu.antoninkriz.krizici.exceptions.network.FailedDownloadException

object JsonHelper {
    enum class DOWNLOADFILE(val file: String) {
        TIMETABLES("json"),
        CONTACTS("contacts")
    }

    @Throws(FailedDownloadException::class, UnknownException::class)
    fun getJson(downloadfile: DOWNLOADFILE): String? {
        val url = Consts.URL_SERVER_JSON.format(downloadfile.file)
        val result = Network.downloadString(url)

        if (result.success) {
            return result.result
        } else {
            if (result.exception != null) {
                throw result.exception
            } else {
                throw UnknownException("Unknown error while downloading file '$url'", 0)
            }
        }
    }
}
