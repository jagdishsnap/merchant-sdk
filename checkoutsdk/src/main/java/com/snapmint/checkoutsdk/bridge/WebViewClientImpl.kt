package com.snapmint.checkoutsdk.bridge

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient

open class WebViewClientImpl(context: Context) : WebViewClient() {
    private var context:Context? = null
    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        webView.loadUrl(url)
        return true
    }
    init {
        this.context = context
    }
}