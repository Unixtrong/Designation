package com.unixtrong.designation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.unixtrong.designation.utils.debug
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import java.net.URL

class SearchActivity : AppCompatActivity() {

    val designEditText by lazy { findViewById(R.id.et_ser_designation) as EditText }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    fun search(view: View) {
        val keyword = designEditText.text
        debug("keyword: $keyword")
        runBlocking {
            val job = async(CommonPool, CoroutineStart.LAZY) {
                val conn = URL("http://btso.pw/search/$keyword").openConnection() as HttpURLConnection
                conn.doInput = true
                val responseCode = conn.responseCode
                debug("resStr: $responseCode")
                return@async responseCode
            }
            debug("${Thread.currentThread().name} - prepare.")
            val res = job.await()
            debug("${Thread.currentThread().name} - result: $res")
        }
    }
}
