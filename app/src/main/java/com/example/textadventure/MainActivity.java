package com.example.textadventure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.DownloadListener;
import android.speech.tts.TextToSpeech;
import android.os.Build;
import java.util.Locale;
import java.util.HashMap;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText etUrl;
    private Button btnBack;
    private Button btnForward;
    private Button btnRefresh;
    private Button btnGo;
    private Button btnSettings;
    private LinearLayout toolbar;
    private static final String TAG = "Browser";
    private TextToSpeech textToSpeech;
    private GestureDetector gestureDetector;
    private GestureDetector horizontalGestureDetector;
    private boolean isToolbarVisible = true;
    private View rootView;
    private boolean shouldOverrideExternalApp = false;

    // User Agents
    private static final String UA_ANDROID_PHONE = "Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36";
    private static final String UA_ANDROID_TABLET = "Mozilla/5.0 (Linux; Android 10; SM-T860) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Safari/537.36";
    private static final String UA_WINDOWS_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String UA_WINDOWS_IE11 = "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko";
    private static final String UA_MACOS = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final String UA_IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1";
    private static final String UA_IPAD = "Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1";
    private String currentUA = UA_ANDROID_PHONE;

    // Search Engines
    private static final String SEARCH_ENGINE_BAIDU = "https://www.baidu.com/s?wd=%s";
    private static final String SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q=%s";
    private static final String SEARCH_ENGINE_BING = "https://www.bing.com/search?q=%s";
    private static final String SEARCH_ENGINE_MITA = "https://metaso.cn/search/3347e7bb-2270-4f7b-b420-b223e8e3e6e3?s=nyzav&referrer_s=nyzav&question=%s";
    private static final String SEARCH_ENGINE_SOGOU = "https://www.sogou.com/web?query=%s";
    private static final String SEARCH_ENGINE_TOUTIAO = "https://so.toutiao.com/search/?keyword=%s";
    private static final String SEARCH_ENGINE_SHENMA = "https://m.sm.cn/s?q=%s";
    private static final String SEARCH_ENGINE_360 = "https://www.so.com/s?q=%s";
    private static final String SEARCH_ENGINE_DUCKDUCKGO = "https://duckduckgo.com/?q=%s";

    private static final String SEARCH_ENGINE_BAIDU_HOME = "https://www.baidu.com";
    private static final String SEARCH_ENGINE_GOOGLE_HOME = "https://www.google.com.hk";
    private static final String SEARCH_ENGINE_BING_HOME = "https://www.bing.com";
    private static final String SEARCH_ENGINE_MITA_HOME = "https://metaso.cn";
    private static final String SEARCH_ENGINE_SOGOU_HOME = "https://m.sogou.com";
    private static final String SEARCH_ENGINE_TOUTIAO_HOME = "https://so.toutiao.com";
    private static final String SEARCH_ENGINE_SHENMA_HOME = "https://m.sm.cn";
    private static final String SEARCH_ENGINE_360_HOME = "https://www.so.com";
    private static final String SEARCH_ENGINE_DUCKDUCKGO_HOME = "https://duckduckgo.com";

    private String currentSearchEngine = SEARCH_ENGINE_BAIDU;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSystemUI();
        initViews();
        setupWebView();
        setupKeyboardListener();
        initTTS();

        webView.loadUrl("https://www.baidu.com");
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "TTS language not supported");
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            }
        });
    }

    private void setupKeyboardListener() {
        rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = rootView.getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15) {
                        String currentUrl = webView.getUrl();
                        if (currentUrl != null && (currentUrl.contains("metaso.cn") || currentUrl.contains("metaso"))) {
                            handleMetasoKeyboard(currentUrl);
                        } else {
                            webView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    webView.loadUrl("javascript:(function() { var inputBoxes = document.querySelectorAll('input[type=text], input[type=search], input[type=password], input[type=email], input[type=tel], textarea'); if(inputBoxes.length > 0) { var lastInput = inputBoxes[inputBoxes.length - 1]; lastInput.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' }); setTimeout(function() { lastInput.focus(); lastInput.click(); lastInput.scrollIntoView(false); }, 300); } else { window.scrollTo(0, document.body.scrollHeight); } })()");
                                }
                            }, 300);
                        }
                    } else {
                        String currentUrl = webView.getUrl();
                        if (currentUrl != null && (currentUrl.contains("metaso.cn") || currentUrl.contains("metaso"))) {
                            webView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    webView.clearHistory();
                                }
                            }, 300);
                        }
                    }
                }
            });
        }
    }

    private void handleMetasoKeyboard(String currentUrl) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:(function() { var inputBox = document.querySelector('textarea[placeholder*=\"聊天\"], input[placeholder*=\"聊天\"], textarea[placeholder*=\"message\"], input[placeholder*=\"message\"], textarea[placeholder*=\"输入\"], input[placeholder*=\"输入\"], textarea:last-of-type, input[type=text]:last-of-type, input[type=search]:last-of-type'); if(inputBox) { inputBox.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' }); setTimeout(function() { inputBox.focus(); inputBox.click(); inputBox.scrollIntoView(false); }, 300); } else { setTimeout(function() { window.scrollTo(0, document.body.scrollHeight); }, 300); } })()");
            }
        }, 300);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void initViews() {
        webView = findViewById(R.id.webview);
        etUrl = findViewById(R.id.et_url);
        btnBack = findViewById(R.id.btn_back);
        btnForward = findViewById(R.id.btn_forward);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnGo = findViewById(R.id.btn_go);
        btnSettings = findViewById(R.id.btn_settings);
        toolbar = findViewById(R.id.toolbar);

        gestureDetector = new GestureDetector(this, new ToolbarGestureListener());
        horizontalGestureDetector = new GestureDetector(this, new HorizontalGestureListener());

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                horizontalGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    WebView wv = (WebView) v;
                    if (wv.getContentHeight() * wv.getScale() <= (wv.getHeight() + wv.getScrollY())) {
                        if (!isToolbarVisible) {
                            showToolbar();
                        }
                    }
                }
                return false;
            }
        });

        btnBack.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        btnForward.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        btnRefresh.setOnClickListener(v -> webView.reload());

        btnGo.setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    if (!url.contains(".") || url.contains(" ")) {
                        url = String.format(currentSearchEngine, Uri.encode(url));
                    } else {
                        url = "http://" + url;
                    }
                }
                webView.loadUrl(url);
            }
        });

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                btnGo.performClick();
                return true;
            }
            return false;
        });
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setUserAgentString(currentUA);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                etUrl.setText(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false;
                } else if (url.startsWith("intent://")) {
                    final boolean allowExternalAppOverride = shouldOverrideExternalApp;
                    if (allowExternalAppOverride) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                            if (existPackage != null) {
                                startActivity(intent);
                            } else {
                                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                if (fallbackUrl != null) {
                                    view.loadUrl(fallbackUrl);
                                } else {
                                    view.loadUrl(url);
                                }
                            }
                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "无法处理Intent URL: " + url, e);
                            return false;
                        }
                    } else {
                        return true;
                    }
                } else if (isCustomProtocol(url)) {
                    final boolean allowExternalAppOverride = shouldOverrideExternalApp;
                    if (allowExternalAppOverride) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                        return true;
                    } else {
                        return true;
                    }
                }
                return true;
            }
        });
        // Set Download Listener
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent serviceIntent = new Intent(MainActivity.this, DownloadService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                Log.d(TAG, "Download started: " + url);
            }
        });
    }

    private boolean isCustomProtocol(String url) {
        return url.startsWith("market://") ||
               url.startsWith("taobao://") ||
               url.startsWith("tmall://") ||
               url.startsWith("weixin://") ||
               url.startsWith("alipays://") ||
               url.startsWith("baiduboxapp://") ||
               url.startsWith("bilibili://") ||
               url.startsWith("zhihu://") ||
               url.startsWith("tbopen://") ||
               url.startsWith("sinaweibo://") ||
               url.startsWith("mqq://") ||
               url.startsWith("meituan://") ||
               url.startsWith("eleme://") ||
               url.startsWith("pinduoduo://");
    }

    private class ToolbarGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > 10) {
                if (distanceY > 0) {
                    hideToolbar();
                } else {
                    showToolbar();
                }
                return true;
            }
            return false;
        }
    }

    private class HorizontalGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > 50 && Math.abs(velocityX) > 50) {
                    if (diffX > 0) {
                        if (webView.canGoBack()) webView.goBack();
                    } else {
                        if (webView.canGoForward()) webView.goForward();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void hideToolbar() {
        if (isToolbarVisible && toolbar != null) {
            toolbar.animate()
                .translationY(-toolbar.getHeight())
                .setDuration(200)
                .withEndAction(() -> toolbar.setVisibility(View.GONE))
                .start();
            isToolbarVisible = false;
        }
    }

    private void showToolbar() {
        if (!isToolbarVisible && toolbar != null) {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.animate().translationY(0).setDuration(200).start();
            isToolbarVisible = true;
        }
    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置");
        String[] options = {
            "搜索设置",
            "用户代理设置",
            "外部应用跳转: " + (shouldOverrideExternalApp ? "开启" : "关闭"),
            "查看下载文件",
            "朗读当前页面"
        };
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: showSearchEngineDialog(); break;
                case 1: showUserAgentDialog(); break;
                case 2: toggleExternalAppOverride(); break;
                case 3: openDownloadFolder(); break;
                case 4: readPage(); break;
            }
        });
        builder.show();
    }
    private void readPage() {
        webView.evaluateJavascript("(function(){ return document.body.innerText; })();", value -> {
            if (value != null && !value.isEmpty()) {
                String text = value.replace("\\\"", "\"");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ReadPage");
                } else {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }
    }
    }

    private void openDownloadFolder() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("resource/folder");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                intent.setData(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI);
            } else {
                intent.setData(Uri.parse("content://downloads/public_downloads"));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "打开下载文件夹"));
        } catch (Exception e) {
            Log.e(TAG, "无法打开下载文件夹", e);
        }
    }

    private void showSearchEngineDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择搜索引擎");
        String[] engines = {"百度", "Google", "Bing", "秘塔AI", "搜狗", "头条", "神马", "360", "DuckDuckGo"};
        final String[] urls = {
            SEARCH_ENGINE_BAIDU, SEARCH_ENGINE_GOOGLE, SEARCH_ENGINE_BING, SEARCH_ENGINE_MITA,
            SEARCH_ENGINE_SOGOU, SEARCH_ENGINE_TOUTIAO, SEARCH_ENGINE_SHENMA, SEARCH_ENGINE_360, SEARCH_ENGINE_DUCKDUCKGO
        };
        final String[] homes = {
            SEARCH_ENGINE_BAIDU_HOME, SEARCH_ENGINE_GOOGLE_HOME, SEARCH_ENGINE_BING_HOME, SEARCH_ENGINE_MITA_HOME,
            SEARCH_ENGINE_SOGOU_HOME, SEARCH_ENGINE_TOUTIAO_HOME, SEARCH_ENGINE_SHENMA_HOME, SEARCH_ENGINE_360_HOME, SEARCH_ENGINE_DUCKDUCKGO_HOME
        };
        builder.setItems(engines, (dialog, which) -> {
            currentSearchEngine = urls[which];
            webView.loadUrl(homes[which]);
        });
        builder.show();
    }

    private void showUserAgentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择用户代理");
        String[] agents = {"Android Phone", "Android Tablet", "Windows Chrome", "Windows IE11", "MacOS", "iPhone", "iPad"};
        final String[] agentStrings = {
            UA_ANDROID_PHONE, UA_ANDROID_TABLET, UA_WINDOWS_CHROME, UA_WINDOWS_IE11, UA_MACOS, UA_IPHONE, UA_IPAD
        };
        builder.setItems(agents, (dialog, which) -> {
            currentUA = agentStrings[which];
            webView.getSettings().setUserAgentString(currentUA);
            webView.reload();
        });
        builder.show();
    }

    private void toggleExternalAppOverride() {
        shouldOverrideExternalApp = !shouldOverrideExternalApp;
        showSettingsDialog();
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