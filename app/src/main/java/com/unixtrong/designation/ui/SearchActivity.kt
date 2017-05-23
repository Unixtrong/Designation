package com.unixtrong.designation.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.unixtrong.designation.R
import com.unixtrong.designation.bean.SearchFile
import com.unixtrong.designation.utils.debug
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SearchActivity : AppCompatActivity() {

    val designEditText by lazy { findViewById(R.id.et_ser_designation) as EditText }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    fun search(view: View) {
        val keyword = designEditText.text.toString()
        debug("keyword: $keyword")
        val job = async(CommonPool, CoroutineStart.LAZY) {
            val resHtml = fetchHtml(keyword)
            resHtml?.let { parseHtml(it) }
        }
        launch(UI) {
            val list = job.await()
            list?.forEach {
                debug(it.toString())
            }
        }
    }

    private fun parseHtml(html: String): MutableList<SearchFile> {
        val regex = """href="(.+)".+title="(.+)"""".toRegex()
        return html.reader().readLines()
                .map { it.trim() }
                .filter { it.contains("https://btso.pw/magnet/detail/hash/") }
                .map {
                    val result = regex.find(it)
                    val groups = result?.groups
                    return@map if (groups?.size == 3) {
                        SearchFile(groups[2]!!.value, groups[1]!!.value)
                    } else SearchFile("?", "?")
                }.toMutableList()
    }

    private fun fetchHtml(keyword: String): String? {
        val urlConnection = URL("https://btso.pw/search/$keyword").openConnection() as HttpURLConnection
        if (urlConnection is HttpsURLConnection) {
            val sslContext: SSLContext? = getSSLContext()
            val sslSocketFactory = sslContext?.socketFactory
            urlConnection.sslSocketFactory = sslSocketFactory
        }
        urlConnection.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,zh-TW;q=0.4")
        urlConnection.doInput = true
        val responseCode = urlConnection.responseCode
        debug("${Thread.currentThread().name} - code: $responseCode")
        if (responseCode == 200) {
            val inputStream = urlConnection.inputStream
            inputStream.use { return it.reader().readText() }
        }
        return null
    }

    private fun getSSLContext(): SSLContext? {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        }

                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
            ), SecureRandom())
            return sslContext
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
