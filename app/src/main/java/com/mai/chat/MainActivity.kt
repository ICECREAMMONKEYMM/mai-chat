package com.mai.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var web: WebView
    private lateinit var addr: EditText

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        web = findViewById(R.id.web)
        addr = findViewById(R.id.addr)

        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        CookieManager.getInstance().setAcceptCookie(true)
        web.webViewClient = WebViewClient()

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

    /** 向固定发现 topic 查询最新地址；成功则自动连接。 */
    private fun discover() {
        val topic = getString(R.string.ntfy_topic)
        Toast.makeText(this, "正在获取最新地址…", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, "暂时没有拿到地址：请稍后再试，或在上面手动输入", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "获取地址失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /** 连接：保存地址并加载。cookie 会保留，登录一次后无需重复输入密码。 */
    private fun connect() {
        var u = addr.text.toString().trim()
        if (u.isEmpty()) {
            Toast.makeText(this, "请先输入地址", Toast.LENGTH_SHORT).show()
            return
        }
        if (!u.startsWith("http")) u = "https://$u"
        getSharedPreferences("mai", MODE_PRIVATE).edit().putString("addr", u).apply()
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
