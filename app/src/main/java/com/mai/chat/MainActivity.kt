package com.mai.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var addr: EditText
    private lateinit var panel: LinearLayout
    private lateinit var status: TextView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        addr = findViewById(R.id.addr)
        panel = findViewById(R.id.panel)
        status = findViewById(R.id.status)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                status.text = "在线"
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                status.text = "连接失败"
            }
        }

        findViewById<Button>(R.id.menu).setOnClickListener {
            panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<Button>(R.id.refresh).setOnClickListener { discover() }
        findViewById<Button>(R.id.connect).setOnClickListener { connect() }

        val saved = getSharedPreferences("mai", MODE_PRIVATE).getString("addr", "") ?: ""
        if (saved.isNotEmpty()) {
            addr.setText(saved)
            connect()
        } else {
            discover()
        }
    }

    private fun discover() {
        status.text = "正在获取地址"
        val topic = getString(R.string.ntfy_topic)
        thread {
            try {
                val conn = URL("https://ntfy.sh/$topic/json?poll=1&since=all")
                    .openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 12000
                val text = conn.inputStream.bufferedReader().readText()
                var url: String? = null
                for (line in text.split("\n")) {
                    if (line.isBlank()) continue
                    try {
                        val msg = JSONObject(line).optString("message", "")
                        val m = Regex("https://[a-z0-9-]+\\.trycloudflare\\.com").find(msg)
                        if (m != null) url = m.value
                    } catch (_: Exception) {
                    }
                }
                runOnUiThread {
                    if (url != null) {
                        addr.setText(url)
                        connect()
                    } else {
                        status.text = "没拿到地址"
                        Toast.makeText(this, "没拿到地址：点 ⚙ 可手动输入", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = "取地址失败"
                    Toast.makeText(this, "获取失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun connect() {
        var u = addr.text.toString().trim()
        if (u.isEmpty()) {
            status.text = "请先输入地址"
            return
        }
        if (!u.startsWith("http")) u = "https://$u"
        getSharedPreferences("mai", MODE_PRIVATE).edit().putString("addr", u).apply()
        status.text = "连接中…"
        web.loadUrl(u, mapOf("ngrok-skip-browser-warning" to "true"))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
