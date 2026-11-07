package asia.aaii.soemoe

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val home = "https://soemoe.aaii.asia/"
    private lateinit var web: WebView
    private lateinit var progress: ProgressBar
    private lateinit var offline: LinearLayout
    private var chooser: android.webkit.ValueCallback<Array<Uri>>? = null
    private val fileRequest = 4099
    private var pageError = false
    private val dark = Color.rgb(21, 26, 54)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            fitsSystemWindows = true
            setBackgroundColor(Color.WHITE)
        }
        setContentView(layout)
        val bar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(dark)
            setPadding(dp(12), 0, dp(4), 0)
        }
        layout.addView(bar, LinearLayout.LayoutParams(-1, dp(50)))
        bar.addView(TextView(this).apply {
            text = "SOE MOE  •  AI TRAINER"
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, -2, 1f))
        bar.addView(icon(android.R.drawable.ic_menu_view, "Home") { web.loadUrl(home) })
        bar.addView(icon(android.R.drawable.ic_menu_revert, "Back") {
            if (web.canGoBack()) web.goBack() else web.loadUrl(home)
        })
        bar.addView(icon(android.R.drawable.ic_popup_sync, "Refresh") { web.reload() })
        bar.addView(icon(android.R.drawable.ic_menu_share, "Share") {
            val current = web.url?.takeIf { it.startsWith(home) } ?: home
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, current)
            }
            startActivity(Intent.createChooser(i, "Share Soe Moe"))
        })
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
        }
        layout.addView(progress, LinearLayout.LayoutParams(-1, dp(3)))
        val holder = FrameLayout(this)
        layout.addView(holder, LinearLayout.LayoutParams(-1, 0, 1f))
        web = WebView(this)
        holder.addView(web, FrameLayout.LayoutParams(-1, -1))
        offline = createErrorView()
        holder.addView(offline, FrameLayout.LayoutParams(-1, -1))
        offline.visibility = View.GONE
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = true
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false)
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                return route(request.url)
            }
            @Deprecated("Legacy redirects")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = route(Uri.parse(url))
            override fun onPageStarted(view: WebView, url: String, icon: android.graphics.Bitmap?) {
                pageError = false
                offline.visibility = View.GONE
                progress.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
                if (!pageError) offline.visibility = View.GONE
            }
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    pageError = true
                    offline.visibility = View.VISIBLE
                }
            }
            @Deprecated("Legacy error callback")
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                pageError = true
                offline.visibility = View.VISIBLE
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, value: Int) {
                progress.progress = value
                progress.visibility = if (value == 100) View.GONE else View.VISIBLE
            }
            override fun onShowFileChooser(view: WebView,
                 cb: android.webkit.ValueCallback<Array<Uri>>,
                 params: WebChromeClient.FileChooserParams): Boolean {
                chooser?.onReceiveValue(null)
                chooser = cb
                return try {
                    startActivityForResult(params.createIntent(), fileRequest)
                    true
                } catch (_: Exception) {
                    chooser = null
                    cb.onReceiveValue(null)
                    true
                }
            }
        }
        web.setDownloadListener(DownloadListener { url, agent, disposition, mime, _ ->
            download(url, agent, disposition, mime)
        })
        if (savedInstanceState == null || web.restoreState(savedInstanceState) == null) {
            web.loadUrl(home)
        }
    }

    private fun createErrorView(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(dp(25), dp(20), dp(25), dp(20))
            addView(TextView(this@MainActivity).apply {
                text = "Could not open the website"
                textSize = 20f
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(this@MainActivity).apply {
                text = "Check your internet connection, then try again."
                gravity = Gravity.CENTER
                setPadding(0, dp(16), 0, dp(16))
            })
            addView(Button(this@MainActivity).apply {
                text = "Retry"
                setOnClickListener {
                    offline.visibility = View.GONE
                    web.loadUrl(home)
                }
            })
        }
    }

    private fun icon(res: Int, desc: String, action: () -> Unit): ImageButton =
        ImageButton(this).apply {
            setImageResource(res)
            setColorFilter(Color.WHITE)
            contentDescription = desc
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(48))
            setOnClickListener { action() }
        }

    private fun route(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return true
        if (scheme == "https" && uri.host.equals("soemoe.aaii.asia", ignoreCase = true)
            && !uri.path.orEmpty().lowercase().endsWith(".pdf")) return false
        if (scheme !in listOf("https", "http", "tel", "mailto", "sms")) return true
        return try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No app available for this link", Toast.LENGTH_SHORT).show()
            true
        } catch (_: SecurityException) { true }
    }

    private fun download(url: String, agent: String?, disposition: String?, mime: String?) {
        if (!Uri.parse(url).scheme.equals("https", ignoreCase = true)) {
            Toast.makeText(this, "HTTPS download required", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val name = URLUtil.guessFileName(url, disposition, mime)
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val req = DownloadManager.Request(Uri.parse(url))
                .setTitle(name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            if (!mime.isNullOrBlank()) req.setMimeType(mime)
            if (!agent.isNullOrBlank()) req.addRequestHeader("User-Agent", agent)
            CookieManager.getInstance().getCookie(url)?.let { req.addRequestHeader("Cookie", it) }
            if (Build.VERSION.SDK_INT >= 29) {
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
            } else {
                req.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, name)
            }
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Could not start download", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Android back dispatch")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    @Deprecated("Legacy file chooser")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileRequest) {
            chooser?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            chooser = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        chooser?.onReceiveValue(null)
        chooser = null
        web.stopLoading()
        web.destroy()
        super.onDestroy()
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density + 0.5f).toInt()
}
