package com.fr1z.GitWebViewBase

import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader


class CustomWebClient(
    context: Context, // refers activity context
    webView: WebView, // refers layout WebView
    localDomain: String // local domain
) : WebViewClient() {

    private var assetLoader : WebViewAssetLoader

    init {
        // Configura WebViewAssetLoader all'interno del costruttore
        this.assetLoader = WebViewAssetLoader.Builder()
            .setDomain(localDomain) // Sostituisci con il tuo dominio
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/css/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/js/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/fonts/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            /** Replace with every local subfolder in your repo*/
            .build()


        // Imposta le impostazioni della WebView
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

        if (request != null) {
            val url = request.url.toString()
            // you can manage requests here
            Log.d("WebView", "URL requested: $url")
        }
        return this.assetLoader.shouldInterceptRequest(request.url)
    }


}