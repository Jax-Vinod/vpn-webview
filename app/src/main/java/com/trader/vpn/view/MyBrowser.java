package com.trader.vpn.view;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyBrowser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            view.loadUrl(request.getUrl().toString());
        view.loadUrl("https://www.google.com");
        return true;
    }
}
