package com.fr1z.GitWebViewBase

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import java.io.File


class CustomWebClient(
    context: Context, // refers activity context
    webView: WebView, // refers layout WebView
    localRepoDir : String,
    localDomain: String // local domain
) : WebViewClient() {

    private var assetLoader : WebViewAssetLoader

    init {
        // Config WebViewAssetLoader
        this.assetLoader = WebViewAssetLoader.Builder()
            .setDomain(localDomain)
            .addPathHandler("/", LocalFilePathHandler(localRepoDir)) // Manage all requests
            /** Replace with every local subfolder in your repo*/
            .build()


        // Enable useful settings in WebView
        webView.settings.apply {
            javaScriptEnabled = true // Enable JavaScript
            useWideViewPort = true   // Enable Wide Viewport mode
            loadWithOverviewMode = true  // Enable Overview mode
            domStorageEnabled = true // Enable DOM Storage
            databaseEnabled = true   // Enable HTML5 local database
        }

        // Enable content debug
        WebView.setWebContentsDebuggingEnabled(true)

        // Sets WebViewClient custom with WebViewAssetLoader for intercepting resources
        webView.webViewClient = this
    }

    // Intercepts the WebView request, hijacking to load local resources
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        // You can manage requests here
        if (request != null) {
            val url = request.url.toString()
            Log.d("WebView", "URL requested: $url")
        }
        return this.assetLoader.shouldInterceptRequest(request.url)
    }

    private class LocalFilePathHandler(private val baseDir: String) : WebViewAssetLoader.PathHandler {
        override fun handle(path: String): WebResourceResponse? {
            val file = File(baseDir, path)
            return if (file.exists()) {
                val mimeType = when {
                    file.name.endsWith(".html") -> "text/html"
                    file.name.endsWith(".css") -> "text/css"
                    file.name.endsWith(".js") -> "application/javascript"
                    file.name.endsWith(".png") -> "image/png"
                    file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") -> "image/jpeg"
                    else -> "application/octet-stream"
                }
                WebResourceResponse(mimeType, "UTF-8", file.inputStream())
            } else {
                null // File not found
            }
        }
    }

}