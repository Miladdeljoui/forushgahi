package ir.pars.store

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.loadsImagesAutomatically = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    val scheme = uri.scheme?.lowercase() ?: return false

                    if (scheme == "file" || scheme == "about" || scheme == "data") return false

                    return try {
                        when (scheme) {
                            "tel" -> {
                                startActivity(Intent(Intent.ACTION_DIAL, uri))
                                true
                            }
                            "sms", "smsto" -> {
                                val target = if (scheme == "sms") {
                                    Uri.parse(uri.toString().replaceFirst(Regex("^sms:"), "smsto:"))
                                } else uri
                                startActivity(Intent(Intent.ACTION_SENDTO, target))
                                true
                            }
                            "mailto" -> {
                                startActivity(Intent(Intent.ACTION_SENDTO, uri))
                                true
                            }
                            "http", "https" -> {
                                startActivity(Intent(Intent.ACTION_VIEW, uri))
                                true
                            }
                            else -> false
                        }
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            "برنامه‌ای برای باز کردن این پیوند پیدا نشد.",
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "باز کردن پیوند انجام نشد. دوباره امتحان کنید.",
                            Toast.LENGTH_LONG
                        ).show()
                        true
                    }
                }
            }
        }
        setContentView(webView)
        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
