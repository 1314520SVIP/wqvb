package com.example.textadventure;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        // Configure WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Configure WebView Client to handle custom URL schemes
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Block custom schemes like baiduboxapp:// to prevent errors
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return true; // Return true to indicate we handled the URL (by ignoring it)
                }
                return false; // Let WebView handle standard HTTP/HTTPS URLs
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Compatibility for older Android versions
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    return true;
                }
                return false;
            }
        });

        // Load Baidu Homepage
        webView.loadUrl("https://www.baidu.com");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}