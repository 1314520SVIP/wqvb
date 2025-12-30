package com.example.textadventure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
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
import android.widget.Toast;
import android.webkit.DownloadListener;
import android.speech.tts.TextToSpeech;
import android.os.Build;
import java.util.Locale;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import android.widget.ListView;
import android.widget.AdapterView;
import android.app.ProgressDialog;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.PowerManager;
import android.content.ComponentName;
import android.media.AudioManager;
import android.content.res.Configuration;
import java.util.Arrays;

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
    // 屏蔽网站功能
    private Set<String> blockedDomains = new HashSet<>();
    private static final String PREFS_NAME = "BrowserPrefs";
    private static final String KEY_BLOCKED_DOMAINS = "blocked_domains";
    // TTS 相关增强变量
    private List<String> extractedTexts = new ArrayList<>();
    private int currentTextIndex = -1;
    private boolean isTTSActive = false;
    private boolean isPlaying = false;
    private boolean ttsInitialized = false;
    
    // 新增：TTS 控制界面
    private FrameLayout ttsControlLayout;
    private Button btnPrevious, btnNext;
    
    // 新增：后台朗读与通知
    private NotificationManager notificationManager;
    private static final String TTS_NOTIFICATION_CHANNEL_ID = "tts_notification_channel";
    private static final int TTS_NOTIFICATION_ID = 1001;
    private boolean isBackgroundReadingEnabled = false;
    private PowerManager.WakeLock wakeLock;
    
    // 新增：自动滚动与高亮
    private boolean isAutoScrollEnabled = false;
    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private String currentHighlightColor = "rgba(255, 165, 0, 0.3)";
    // 新增：TTS 参数
    private float currentSpeechRate = 0.9f;
    private float currentPitch = 1.0f;
    // 新增：TTS引擎选择
    private String currentTTSEngine = ""; // 当前选择的TTS引擎包名
    private static final String PREF_TTS_ENGINE = "tts_engine";
    
    // 新增：日志管理功能
    private boolean debugLogEnabled = false;
    private static final String PREF_DEBUG_LOG = "debug_log_enabled";
    private StringBuilder logBuffer = new StringBuilder();
    private static final int MAX_LOG_BUFFER_SIZE = 50000; // 50KB日志缓冲
    
    // 新增：广播接收器
    private BroadcastReceiver ttsControlReceiver;
    private static final String ACTION_TTS_PLAY_PAUSE = "com.example.textadventure.ACTION_TTS_PLAY_PAUSE";
    private static final String ACTION_TTS_PREVIOUS = "com.example.textadventure.ACTION_TTS_PREVIOUS";
    private static final String ACTION_TTS_NEXT = "com.example.textadventure.ACTION_TTS_NEXT";
    private static final String ACTION_TTS_STOP = "com.example.textadventure.ACTION_TTS_STOP";
    
    // 音频焦点管理
    private AudioManager audioManager;
    private boolean hasAudioFocus = false;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    // 获得音频焦点，恢复朗读
                    if (isPlaying) {
                        resumeTextToSpeech();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // 永久失去音频焦点，停止朗读
                    stopTextToSpeech();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // 暂时失去音频焦点，暂停朗读
                    if (isPlaying) {
                        pauseTextToSpeech();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 暂时失去音频焦点，可以降低音量（此处选择暂停）
                    if (isPlaying) {
                        pauseTextToSpeech();
                    }
                    break;
            }
        }
    };

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
        initAudioManager();
        
        // 加载保存的TTS引擎设置
        loadTTSSettings();
        
        initTTS();
        
        // 初始化通知管理器（增加空值检查）
        try {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                createTTSNotificationChannel();
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化通知管理器失败", e);
        }
        
        // 初始化TTS控制界面
        try {
            initTTSControlLayout();
        } catch (Exception e) {
            Log.e(TAG, "初始化TTS控制界面失败", e);
        }
        
        // 注册TTS控制广播接收器
        try {
            registerTTSControlReceiver();
        } catch (Exception e) {
            Log.e(TAG, "注册TTS控制广播接收器失败", e);
        }
        
        loadBlockedDomains();
        webView.loadUrl("https://www.baidu.com");
                logOperation("应用启动 - 加载首页", 224);
    }
    
    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager == null) {
                Log.e(TAG, "无法获取AudioManager");
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化AudioManager失败", e);
        }
    }
    
    /**
     * 请求音频焦点
     */
    private boolean requestAudioFocus() {
        if (audioManager == null) {
            return false;
        }
        
        if (hasAudioFocus) {
            return true; // 已经拥有焦点
        }
        
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            );
        } else {
            result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            );
        }
        
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            hasAudioFocus = true;
            Log.d(TAG, "音频焦点请求成功");
            return true;
        } else {
            Log.w(TAG, "音频焦点请求失败");
            return false;
        }
    }
    
    /**
     * 放弃音频焦点
     */
    private void abandonAudioFocus() {
        if (audioManager != null && hasAudioFocus) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            hasAudioFocus = false;
            Log.d(TAG, "已放弃音频焦点");
        }
    }
    /**
     * 加载TTS设置
     */
    private void loadTTSSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentTTSEngine = prefs.getString(PREF_TTS_ENGINE, "");
        if (!currentTTSEngine.isEmpty()) {
            Log.d(TAG, "加载保存的TTS引擎: " + currentTTSEngine);
        }
    }
    
    /**
     * 初始化TTS（增强版 - 针对努比亚/小米澎湃系统优化）
     */
    private void initTTS() {
        try {
            Log.d(TAG, "========== 开始初始化TTS引擎 ==========");
            Log.d(TAG, "系统版本: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            Log.d(TAG, "设备厂商: " + Build.MANUFACTURER + ", 型号: " + Build.MODEL);
            // 检查TTS服务是否可用（使用字符串常量以避免编译错误）
            android.content.pm.PackageManager pm = getPackageManager();
            try {
                if (!pm.hasSystemFeature("android.software.text_to_speech")) {
                    Log.w(TAG, "设备可能不支持TTS功能");
                } else {
                    Log.d(TAG, "设备支持TTS功能");
                }
            } catch (Exception e) {
                Log.w(TAG, "TTS功能检查失败，继续尝试初始化: " + e.getMessage());
            }
            
            // 创建TTS对象 - 支持指定引擎
            TextToSpeech.OnInitListener initListener = new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.d(TAG, "========== TTS onInit回调触发 ==========");
                    Log.d(TAG, "onInit状态码: " + status + " (SUCCESS=" + TextToSpeech.SUCCESS + ")");
                    Log.d(TAG, "onInit线程: " + Thread.currentThread().getName());
                    Log.d(TAG, "TextToSpeech对象: " + (textToSpeech != null ? "已创建" : "为空"));
                    
                    if (status == TextToSpeech.SUCCESS) {
                        Log.d(TAG, "✓ TTS引擎初始化成功");
                        
                        // 尝试多种语言设置，增加兼容性
                        int langResult = TextToSpeech.LANG_NOT_SUPPORTED;
                        Locale finalLocale = null;
                        
                        // 获取可用语言列表
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Set<Locale> availableLocales = textToSpeech.getAvailableLanguages();
                            Log.d(TAG, "可用语言数量: " + (availableLocales != null ? availableLocales.size() : 0));
                            if (availableLocales != null) {
                                for (Locale loc : availableLocales) {
                                    if (loc.toString().contains("zh")) {
                                        Log.d(TAG, "发现中文语言: " + loc);
                                    }
                                }
                            }
                        }
                        
                        // 按优先级尝试语言设置
                        Locale[] priorityLocales = {
                            Locale.SIMPLIFIED_CHINESE,
                            Locale.CHINA,
                            Locale.getDefault(),
                            Locale.US
                        };
                        
                        for (int i = 0; i < priorityLocales.length; i++) {
                            Locale locale = priorityLocales[i];
                            langResult = textToSpeech.setLanguage(locale);
                            Log.d(TAG, "尝试[" + i + "] 设置语言: " + locale + " -> 结果: " + getResultCodeName(langResult));
                            
                            if (langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                                finalLocale = locale;
                                Log.d(TAG, "✓ 语言设置成功: " + locale);
                                break;
                            }
                        }
                        
                        // 检查最终语言设置结果
                        if (finalLocale != null) {
                            Log.d(TAG, "✓ TTS语言最终设置: " + finalLocale);
                            
                            // 设置语速和音调
                            textToSpeech.setSpeechRate(currentSpeechRate);
                            textToSpeech.setPitch(currentPitch);
                            Log.d(TAG, "语速: " + currentSpeechRate + ", 音调: " + currentPitch);
                            
                            // 获取TTS引擎信息
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                String defaultEngine = textToSpeech.getDefaultEngine();
                                Log.d(TAG, "TTS默认引擎: " + defaultEngine);
                            }
                            
                            // 设置朗读完成监听器
                            textToSpeech.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    Log.d(TAG, "开始朗读: " + utteranceId);
                                    try {
                                        currentTextIndex = Integer.parseInt(utteranceId.replace("sentence_", ""));
                                        highlightCurrentSentence();
                                    } catch (NumberFormatException e) {
                                        // Ignore
                                    }
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    Log.d(TAG, "朗读完成: " + utteranceId);
                                    currentTextIndex++;
                                    if (currentTextIndex < extractedTexts.size()) {
                                        speakCurrentSentence();
                                    } else {
                                        isPlaying = false;
                                        isTTSActive = false;
                                        abandonAudioFocus();
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "朗读结束", Toast.LENGTH_SHORT).show());
                                    }
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.e(TAG, "朗读错误: " + utteranceId);
                                }
                            });
                            
                            ttsInitialized = true;
                            Log.d(TAG, "========== TTS完全初始化成功 ==========");
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "TTS初始化成功", Toast.LENGTH_SHORT).show());
                        } else {
                            Log.e(TAG, "✗ TTS语言完全不支持");
                            Log.e(TAG, "最后一次语言设置结果: " + getResultCodeName(langResult));
                            ttsInitialized = false;
                            runOnUiThread(() -> {
                                String msg = "系统TTS语言不支持\n\n建议:\n1. 进入系统设置\n2. 语言和输入法\n3. 文字转语音(TTS)\n4. 检查是否安装中文语音包";
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        Log.e(TAG, "✗ TTS初始化失败，状态码: " + status);
                        Log.e(TAG, "状态码含义: " + getStatusCodeName(status));
                        ttsInitialized = false;
                        runOnUiThread(() -> {
                            String errorMsg = "TTS初始化失败 (" + status + ")\n\n";
                            if (status == TextToSpeech.ERROR) {
                                errorMsg += "系统TTS服务不可用\n请检查系统TTS设置";
                            } else {
                                errorMsg += "请重启应用或重启设备";
                            }
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            };
            
            // 如果指定了引擎包名，使用指定的引擎
            if (!currentTTSEngine.isEmpty() && !currentTTSEngine.equals("系统默认")) {
                Log.d(TAG, "尝试使用指定的TTS引擎: " + currentTTSEngine);
                textToSpeech = new TextToSpeech(this, initListener, currentTTSEngine);
            } else {
                Log.d(TAG, "使用系统默认TTS引擎");
                textToSpeech = new TextToSpeech(this, initListener);
            }
            
            // 设置超时检测，防止onInit回调不执行
            final Handler timeoutHandler = new Handler();
            timeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!ttsInitialized) {
                        Log.e(TAG, "✗ TTS初始化超时！onInit回调可能未执行");
                        Log.e(TAG, "textToSpeech对象: " + (textToSpeech != null ? "存在" : "为空"));
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "TTS初始化超时\n\n可能原因:\n1. 系统TTS服务未响应\n2. 设备性能不足\n3. 系统限制\n\n建议重启应用", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }, 5000); // 5秒超时
            
            Log.d(TAG, "TTS初始化请求已发送，等待onInit回调...");
        } catch (Exception e) {
            Log.e(TAG, "✗ TTS初始化异常", e);
            e.printStackTrace();
            ttsInitialized = false;
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "TTS初始化异常: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
    
    /**
     * 获取语言设置结果码名称
     */
    private String getResultCodeName(int code) {
        switch (code) {
            case TextToSpeech.LANG_AVAILABLE: return "LANG_AVAILABLE";
            case TextToSpeech.LANG_COUNTRY_AVAILABLE: return "LANG_COUNTRY_AVAILABLE";
            case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE: return "LANG_COUNTRY_VAR_AVAILABLE";
            case TextToSpeech.LANG_MISSING_DATA: return "LANG_MISSING_DATA";
            case TextToSpeech.LANG_NOT_SUPPORTED: return "LANG_NOT_SUPPORTED";
            default: return "UNKNOWN(" + code + ")";
        }
    }
    /**
     * 获取状态码名称
     */
    private String getStatusCodeName(int code) {
        switch (code) {
            case TextToSpeech.SUCCESS: return "SUCCESS";
            case TextToSpeech.ERROR: return "ERROR";
            default: return "UNKNOWN(" + code + ")";
        }
    }
    
    /**
     * 测试TTS是否工作
     */
    private void testTTS() {
        if (ttsInitialized && textToSpeech != null) {
            new Handler().postDelayed(() -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("TTS测试", TextToSpeech.QUEUE_FLUSH, null, "test");
                    } else {
                        HashMap<String, String> params = new HashMap<>();
                        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "test");
                        textToSpeech.speak("TTS测试", TextToSpeech.QUEUE_FLUSH, params);
                    }
                    Log.d(TAG, "TTS测试已执行");
                } catch (Exception e) {
                    Log.e(TAG, "TTS测试失败", e);
                }
            }, 1000);
        }
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
                logOperation("后退", 613);
        btnForward.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
                logOperation("前进", 614);
        btnRefresh.setOnClickListener(v -> webView.reload());
                logOperation("刷新页面", 615);

        btnGo.setOnClickListener(v -> {
            String url = etUrl.getText().toString();
                    logOperation("准备加载URL", 617, url);
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
                logOperation("打开设置", 631);

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
                // 检查屏蔽网站
                if (isUrlBlocked(url)) {
                    Toast.makeText(MainActivity.this, "该网站已被屏蔽", Toast.LENGTH_SHORT).show();
                    return true; // 阻止加载
                }

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
        
        // 加载调试日志开关状态
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        debugLogEnabled = prefs.getBoolean(PREF_DEBUG_LOG, false);
        
        String[] options = {
            "搜索设置",
            "用户代理设置",
            "外部应用跳转: " + (shouldOverrideExternalApp ? "开启" : "关闭"),
            "查看下载文件",
            "语音引擎设置",
            "朗读当前页面",
            "屏蔽网站管理",
            "调试日志: " + (debugLogEnabled ? "开启" : "关闭"),
            "查看日志"
        };
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: showSearchEngineDialog(); break;
                case 1: showUserAgentDialog(); break;
                case 2: toggleExternalAppOverride(); break;
                case 3: openDownloadFolder(); break;
                case 4: showTTSEngineDialog(); break;
                case 5: readPage(); break;
                case 6: showBlockedDomainsDialog(); break;
                case 7: toggleDebugLog(); break;
                case 8: showLogDialog(); break;
            }
        });
        builder.show();
    }
    /**
     * 显示TTS引擎选择对话框
     */
    private void showTTSEngineDialog() {
                logOperation("打开TTS引擎设置", 828);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择语音引擎");
        
        // 获取系统中已安装的TTS引擎列表
        final List<EngineInfo> engineList = new ArrayList<>();
        
        // 添加系统默认选项
        EngineInfo defaultEngine = new EngineInfo();
        defaultEngine.name = "";
        defaultEngine.label = "系统默认引擎";
        defaultEngine.packageName = "";
        engineList.add(defaultEngine);
        
        // 通过PackageManager查询TTS引擎（更可靠的方式）
        android.content.pm.PackageManager pm = getPackageManager();
        android.content.Intent ttsIntent = new android.content.Intent("android.speech.tts.engine.INSTALL_TTS_DATA");
        android.content.pm.ResolveInfo[] resolveInfos = null;
        
        try {
            java.util.List<android.content.pm.ResolveInfo> list = pm.queryIntentActivities(ttsIntent, 0);
            if (list != null) {
                resolveInfos = list.toArray(new android.content.pm.ResolveInfo[0]);
            }
        } catch (Exception e) {
            Log.w(TAG, "通过PackageManager查询TTS引擎失败: " + e.getMessage());
        }
        
        // 尝试使用TextToSpeech获取引擎列表
        android.speech.tts.TextToSpeech.EngineInfo[] engines = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                android.speech.tts.TextToSpeech tempTTS = new android.speech.tts.TextToSpeech(this, new android.speech.tts.TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        // 静默回调
                    }
                });
                engines = tempTTS.getEngines().toArray(new android.speech.tts.TextToSpeech.EngineInfo[0]);
                tempTTS.shutdown();
            } catch (Exception e) {
                Log.w(TAG, "通过TextToSpeech获取引擎列表失败: " + e.getMessage());
            }
        }
        
        // 合并引擎列表
        java.util.Set<String> addedPackages = new java.util.HashSet<>();
        
        if (engines != null) {
            for (android.speech.tts.TextToSpeech.EngineInfo info : engines) {
                if (!addedPackages.contains(info.name)) {
                    EngineInfo engine = new EngineInfo();
                    engine.name = info.name;
                    engine.label = info.label;
                    engine.packageName = info.name;
                    engineList.add(engine);
                    addedPackages.add(info.name);
                }
            }
        }
        
        // 添加常见预设引擎（如果系统中存在但未被检测到）
        String[] commonEngines = {
            "com.google.android.tts",           // Google TTS
            "com.iflytek.speechsuite",         // 科大讯飞（小爱语音可能使用）
            "com.miui.weather.tts",            // 小米系统TTS
            "com.xiaomi.tts",                  // 小米TTS
            "com.iflytek.cloudspeech",         // 讯飞云语音
            "com.baidu.duersdk.opensdk",       // 百度TTS
            "com.samsung.SMT",                 // 三星TTS
        };
        
        for (String pkg : commonEngines) {
            if (!addedPackages.contains(pkg)) {
                try {
                    android.content.pm.ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                    EngineInfo engine = new EngineInfo();
                    engine.name = pkg;
                    engine.label = pm.getApplicationLabel(appInfo).toString();
                    engine.packageName = pkg;
                    engineList.add(engine);
                    addedPackages.add(pkg);
                } catch (android.content.pm.PackageManager.NameNotFoundException e) {
                    // 引擎未安装，跳过
                }
            }
        }
        
        // 生成显示名称
        String[] engineNames = new String[engineList.size()];
        int selectedIndex = 0;
        
        for (int i = 0; i < engineList.size(); i++) {
            EngineInfo info = engineList.get(i);
            String displayName = info.label;
            String packageName = info.packageName;
            
            // 识别并美化常见引擎名称
            if (i == 0) {
                displayName = "⭐ 系统默认引擎";
            } else if (packageName != null) {
                if (packageName.contains("google")) {
                    displayName = "🔊 Google语音合成 (Google TTS)";
                } else if (packageName.contains("iflytek")) {
                    displayName = "🎤 小爱语音/科大讯飞 (iFlytek)";
                } else if (packageName.contains("xiaomi") || packageName.contains("miui")) {
                    displayName = "🎤 小爱语音 (Xiaomi TTS)";
                } else if (packageName.contains("baidu")) {
                    displayName = "🔊 百度语音 (Baidu TTS)";
                } else if (packageName.contains("samsung")) {
                    displayName = "🔊 三星语音 (Samsung TTS)";
                } else {
                    displayName = displayName + " (" + packageName + ")";
                }
            }
            
            engineNames[i] = displayName;
            
            // 查找当前选择的引擎索引
            if (currentTTSEngine != null && currentTTSEngine.equals(info.name)) {
                selectedIndex = i;
            }
        }
        
        builder.setSingleChoiceItems(engineNames, selectedIndex, (dialog, which) -> {
            // 保存选择的引擎
            currentTTSEngine = engineList.get(which).name;
            
            // 保存到SharedPreferences
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(PREF_TTS_ENGINE, currentTTSEngine).apply();
            
            dialog.dismiss();
            
            // 重新初始化TTS
            if (textToSpeech != null) {
                textToSpeech.shutdown();
            }
            ttsInitialized = false;
            initTTS();
            
            String engineName = which == 0 ? "系统默认" : engineNames[which];
            Toast.makeText(this, "已切换到: " + engineName + "\n正在重新初始化...", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 切换调试日志开关
     */
    private void toggleDebugLog() {
                logOperation("切换调试日志开关", 980);
        debugLogEnabled = !debugLogEnabled;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_DEBUG_LOG, debugLogEnabled).apply();
        
        String status = debugLogEnabled ? "开启" : "关闭";
        Toast.makeText(this, "调试日志已" + status, Toast.LENGTH_SHORT).show();
        
        if (debugLogEnabled) {
            addLog("========== 调试日志已开启 ==========");
            addLog("系统版本: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            addLog("设备厂商: " + Build.MANUFACTURER + ", 型号: " + Build.MODEL);
        } else {
            addLog("========== 调试日志已关闭 ==========");
        }
    }
    
    /**
     * 显示日志管理对话框
     */
    private void showLogDialog() {
                logOperation("查看日志", 1000);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("日志管理");
        
        String[] options = {
            "查看日志",
            "复制日志",
            "保存日志到本地",
            "清除日志"
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: showLogContentDialog(); break;
                case 1: copyLogToClipboard(); break;
                case 2: saveLogToFile(); break;
                case 3: clearLog(); break;
            }
        });
        builder.show();
    }
    
    /**
     * 显示日志内容对话框
     */
    private void showLogContentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("应用日志");
        
        ScrollView scrollView = new ScrollView(this);
        TextView logView = new TextView(this);
        logView.setTextSize(12);
        logView.setPadding(20, 20, 20, 20);
        logView.setTypeface(android.graphics.Typeface.MONOSPACE);
        
        String logContent = logBuffer.length() > 0 ? logBuffer.toString() : "暂无日志记录";
        logView.setText(logContent);
        
        scrollView.addView(logView);
        builder.setView(scrollView);
        
        builder.setPositiveButton("复制", (dialog, which) -> copyLogToClipboard());
        builder.setNegativeButton("关闭", null);
        builder.show();
    }
    
    /**
     * 复制日志到剪贴板
     */
    private void copyLogToClipboard() {
        if (logBuffer.length() == 0) {
            Toast.makeText(this, "没有日志可复制", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("应用日志", logBuffer.toString());
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "日志已复制到剪贴板", Toast.LENGTH_SHORT).show();
        addLog("========== 日志已复制到剪贴板 ==========");
    }
    
    /**
     * 保存日志到本地文件
     */
    private void saveLogToFile() {
        if (logBuffer.length() == 0) {
            Toast.makeText(this, "没有日志可保存", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // 生成文件名：app_log_时间戳.txt
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(new java.util.Date());
            String fileName = "app_log_" + timestamp + ".txt";
            
            // 获取下载目录
            java.io.File downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
            java.io.File logFile = new java.io.File(downloadDir, fileName);
            
            // 写入日志
            java.io.FileWriter writer = new java.io.FileWriter(logFile);
            writer.write(logBuffer.toString());
            writer.close();
            
            // 通知系统扫描文件
            addLog("========== 日志已保存到: " + logFile.getAbsolutePath() + " ==========");
            
            Toast.makeText(this, "日志已保存到:\n" + logFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            addLog("保存日志失败: " + e.getMessage());
            Toast.makeText(this, "保存日志失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 清除日志
     */
    private void clearLog() {
        new AlertDialog.Builder(this)
            .setTitle("清除日志")
            .setMessage("确定要清除所有日志吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                logBuffer = new StringBuilder();
                addLog("========== 日志已清除 ==========");
                Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 添加日志到缓冲区
     */
    private void addLog(String message) {
        if (!debugLogEnabled && logBuffer.length() == 0) {
            return; // 如果日志未开启且缓冲区为空，不记录
        }
        
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US).format(new java.util.Date());
        String logEntry = "[" + timestamp + "] " + message + "\n";
        
        logBuffer.append(logEntry);
        
        // 控制缓冲区大小
        if (logBuffer.length() > MAX_LOG_BUFFER_SIZE) {
            int excess = logBuffer.length() - MAX_LOG_BUFFER_SIZE;
            logBuffer.delete(0, excess);
        }
        
        // 同时输出到Logcat
        if (message.contains("==========")) {
            Log.d(TAG, message);
        } else {
            Log.d(TAG, message);
        }
    }
    
    /**
     * 日志辅助方法：记录操作和行号
     */
    private void logOperation(String operation, int lineNumber) {
        if (debugLogEnabled) {
            addLog("[" + lineNumber + "] " + operation);
        }
    }
    
    private void logOperation(String operation, int lineNumber, String details) {
        if (debugLogEnabled) {
            addLog("[" + lineNumber + "] " + operation + " - " + details);
        }
    }
    
    /**
     * 引擎信息类（独立于TextToSpeech.EngineInfo，避免兼容性问题）
     */

    private static class EngineInfo {
        public String name;
        public String label;
        public String packageName;
    }
    
    /**
     * 自定义引擎信息包装类（用于系统默认选项）
     */
    private static class CustomEngineInfo extends android.speech.tts.TextToSpeech.EngineInfo {
        public CustomEngineInfo(String name, String label, boolean isDefault) {
            this.name = name;
            this.label = label;
        }
    }
    /**
     * 读取当前页面并朗读（修复版）
     */
    private void readPage() {
                logOperation("朗读当前页面", 1174);
        // 检查TTS是否已初始化
        if (!ttsInitialized || textToSpeech == null) {
            // TTS未初始化，显示提示并尝试等待初始化完成
            Toast.makeText(this, "TTS正在初始化中，请稍后再试...", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "readPage: TTS未完全初始化，等待中...");
            
            // 设置一个延迟，等待TTS初始化完成
            new Handler().postDelayed(() -> {
                if (ttsInitialized && textToSpeech != null) {
                    Toast.makeText(MainActivity.this, "TTS初始化完成，开始朗读", Toast.LENGTH_SHORT).show();
                    readPage();
                } else {
                    Toast.makeText(MainActivity.this, "TTS初始化失败，请检查系统TTS设置", Toast.LENGTH_LONG).show();
                }
            }, 2000);
            return;
        }

        // 检查音频流是否被静音
        if (audioManager != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (currentVolume == 0) {
                Toast.makeText(this, "媒体音量为0，请调高音量", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "媒体音量为0");
            }
        }

        // 停止当前正在朗读的内容
        textToSpeech.stop();
        isPlaying = false;
        isTTSActive = false;
        extractedTexts.clear();
        currentTextIndex = -1;

        // 请求音频焦点
        if (!requestAudioFocus()) {
            Toast.makeText(this, "无法获取音频焦点，可能无法朗读", Toast.LENGTH_SHORT).show();
        }

        // 使用改进的文本提取逻辑
        String jsCode = "javascript:(function() {" +
            "var allText = '';" +
            "var elements = document.querySelectorAll('p, div, span, h1, h2, h3, h4, h5, h6, li, td, th, article, section');" +
            "elements.forEach(function(el) {" +
            "   var text = el.innerText || el.textContent || '';" +
            "   if (text) {" +
            "       text = text.trim();" +
            "       if (text.length > 5) {" +
            "           allText += text + '。';" +
            "       }" +
            "   }" +
            "});" +
            "return allText;" +
            "})()";
        
        Log.d(TAG, "开始提取网页文本...");
        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(TAG, "JavaScript返回值: " + value);
                
                if (value != null && !value.equals("") && !value.trim().isEmpty()) {
                    try {
                        // 清理文本：移除JSON引号包装，处理转义字符
                        String cleanText = value.replaceAll("^\"|\"$", "").replace("\\n", "\n").replace("\\\"", "\"").trim();
                        
                        if (cleanText.isEmpty() || cleanText.equals("")) {
                            Toast.makeText(MainActivity.this, "页面中没有可朗读的文字", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 按句号分割成句子列表
                        String[] sentences = cleanText.split("。");
                        for (String sentence : sentences) {
                            String trimmedSentence = sentence.trim();
                            if (!trimmedSentence.isEmpty() && trimmedSentence.length() > 2) {
                                // 重新添加句号
                                extractedTexts.add(trimmedSentence + "。");
                            }
                        }

                        if (extractedTexts.isEmpty()) {
                            Toast.makeText(MainActivity.this, "未能提取到有效句子", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // 限制句子数量，防止过长
                        if (extractedTexts.size() > 500) {
                            extractedTexts = extractedTexts.subList(0, 500);
                            Toast.makeText(MainActivity.this, "文本过长，只朗读前500句", Toast.LENGTH_SHORT).show();
                        }
                        
                        // 设置当前朗读状态
                        isTTSActive = true;
                        currentTextIndex = 0;
                        isPlaying = true;
                        
                        // 开始朗读第一句
                        speakCurrentSentence();
                        
                        Toast.makeText(MainActivity.this, "开始朗读，共 " + extractedTexts.size() + " 句", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "提取到 " + extractedTexts.size() + " 句，开始朗读");
                    } catch (Exception e) {
                        Log.e(TAG, "朗读异常", e);
                        Toast.makeText(MainActivity.this, "朗读出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "页面中没有可朗读的文字", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "未提取到文本");
                }
            }
        });
    }
    
    /**
     * 朗读当前句子（修复版）
     */
    private void speakCurrentSentence() {
        if (!isTTSActive || extractedTexts.isEmpty() || currentTextIndex < 0 || currentTextIndex >= extractedTexts.size()) {
            Log.w(TAG, "无法朗读：索引无效或未激活");
            return;
        }
        
        try {
            String text = extractedTexts.get(currentTextIndex);
            if (textToSpeech != null && !TextUtils.isEmpty(text)) {
                isPlaying = true;
                int result;
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sentence_" + currentTextIndex);
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sentence_" + currentTextIndex);
                    result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
                }
                
                if (result == TextToSpeech.ERROR) {
                    Log.e(TAG, "speak()返回ERROR");
                    Toast.makeText(this, "朗读失败，请重试", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "正在朗读句子 " + currentTextIndex + ": " + text.substring(0, Math.min(20, text.length())));
                }
            } else {
                Log.e(TAG, "无法朗读：TTS未初始化或文本为空");
            }
        } catch (Exception e) {
            Log.e(TAG, "朗读句子时发生异常", e);
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化TTS控制界面
     */
    private void initTTSControlLayout() {
        ttsControlLayout = new FrameLayout(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = android.view.Gravity.LEFT | android.view.Gravity.CENTER_VERTICAL;
        layoutParams.setMargins(10, 0, 0, 0);
        ttsControlLayout.setLayoutParams(layoutParams);
        ttsControlLayout.setVisibility(View.GONE);
        ttsControlLayout.setAlpha(0.7f);
        
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setBackgroundColor(android.graphics.Color.argb(180, 50, 50, 50));
        buttonLayout.setPadding(8, 15, 8, 15);
        
        // 上一句按钮
        btnPrevious = new Button(this);
        btnPrevious.setText("⬆");
        btnPrevious.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnPrevious.setTextColor(android.graphics.Color.WHITE);
        btnPrevious.setTextSize(16);
        btnPrevious.setPadding(15, 12, 15, 12);
        btnPrevious.setOnClickListener(v -> previousTextToSpeech());
        
        // 播放/暂停按钮
        Button btnPlayPause = new Button(this);
        btnPlayPause.setText("▶");
        btnPlayPause.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnPlayPause.setTextColor(android.graphics.Color.WHITE);
        btnPlayPause.setTextSize(18);
        btnPlayPause.setPadding(15, 15, 15, 15);
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        
        // 下一句按钮
        btnNext = new Button(this);
        btnNext.setText("⬇");
        btnNext.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnNext.setTextColor(android.graphics.Color.WHITE);
        btnNext.setTextSize(16);
        btnNext.setPadding(15, 12, 15, 12);
        btnNext.setOnClickListener(v -> nextTextToSpeech());
        
        buttonLayout.addView(btnPrevious);
        buttonLayout.addView(btnPlayPause);
        buttonLayout.addView(btnNext);
        
        ttsControlLayout.addView(buttonLayout);
        
        FrameLayout mainLayout = findViewById(android.R.id.content);
        mainLayout.addView(ttsControlLayout);
        
        // 设置触摸监听器实现滑动显示/隐藏
        setupTTSControlTouchListener();
    }
    
    private void setupTTSControlTouchListener() {
        ttsControlLayout.setOnTouchListener(new View.OnTouchListener() {
            float initialX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        float deltaX = event.getX() - initialX;
                        if (Math.abs(deltaX) > 50) {
                            if (ttsControlLayout.getVisibility() == View.VISIBLE) {
                                ttsControlLayout.setVisibility(View.GONE);
                            } else {
                                ttsControlLayout.setVisibility(View.VISIBLE);
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 切换播放/暂停
     */
    private void togglePlayPause() {
        if (isPlaying) {
            pauseTextToSpeech();
        } else {
            if (!isTTSActive) {
                readPage();
            } else {
                resumeTextToSpeech();
            }
        }
    }
    
    /**
     * 暂停朗读
     */
    private void pauseTextToSpeech() {
        if (textToSpeech != null && isPlaying) {
            textToSpeech.stop();
            isPlaying = false;
            stopAutoScroll();
            Toast.makeText(this, "朗读已暂停", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 停止朗读
     */
    private void stopTextToSpeech() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            isPlaying = false;
            isTTSActive = false;
            currentTextIndex = -1;
            extractedTexts.clear();
            abandonAudioFocus();
            Log.d(TAG, "朗读已停止");
        }
    }
    
    /**
     * 恢复朗读
     */
    private void resumeTextToSpeech() {
        if (textToSpeech != null && !isPlaying && !extractedTexts.isEmpty()) {
            speakCurrentSentence();
            Toast.makeText(this, "朗读继续", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 上一句
     */
    private void previousTextToSpeech() {
        if (currentTextIndex > 0) {
            currentTextIndex--;
            speakCurrentSentence();
        } else {
            Toast.makeText(this, "已经是第一句了", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 下一句
     */
    private void nextTextToSpeech() {
        if (currentTextIndex < extractedTexts.size() - 1) {
            currentTextIndex++;
            speakCurrentSentence();
        } else {
            Toast.makeText(this, "已经是最后一句了", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 高亮当前句子（简化版，仅占位）
     */
    private void highlightCurrentSentence() {
        // TODO: 实现JavaScript注入高亮逻辑
        Log.d(TAG, "Highlight sentence " + currentTextIndex);
    }
    
    /**
     * 开始自动滚动
     */
    private void startAutoScroll() {
        if (!isAutoScrollEnabled) return;
        stopAutoScroll();
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying && webView != null) {
                    webView.scrollBy(0, 5);
                    autoScrollHandler.postDelayed(this, 100);
                }
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, 100);
    }
    
    /**
     * 停止自动滚动
     */
    private void stopAutoScroll() {
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollRunnable = null;
        }
    }
    
    /**
     * 创建TTS通知渠道
     */
    private void createTTSNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                TTS_NOTIFICATION_CHANNEL_ID,
                "文字朗读控制",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("文字朗读控制通知");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * 注册TTS控制广播接收器
     */
    private void registerTTSControlReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TTS_PLAY_PAUSE);
        filter.addAction(ACTION_TTS_PREVIOUS);
        filter.addAction(ACTION_TTS_NEXT);
        filter.addAction(ACTION_TTS_STOP);
        
        if (ttsControlReceiver == null) {
            ttsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ACTION_TTS_PLAY_PAUSE.equals(action)) {
                        togglePlayPause();
                    } else if (ACTION_TTS_PREVIOUS.equals(action)) {
                        previousTextToSpeech();
                    } else if (ACTION_TTS_NEXT.equals(action)) {
                        nextTextToSpeech();
                    } else if (ACTION_TTS_STOP.equals(action)) {
                        pauseTextToSpeech();
                        isTTSActive = false;
                    }
                }
            };
            registerReceiver(ttsControlReceiver, filter);
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
                logOperation("打开搜索引擎设置", 1588);
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
                logOperation("打开用户代理设置", 1607);
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
                logOperation("切换外部应用跳转", 1622);
        shouldOverrideExternalApp = !shouldOverrideExternalApp;
        showSettingsDialog();
    }
    // ==================== 屏蔽网站管理功能 ====================

    private boolean isUrlBlocked(String url) {
        if (url == null || url.isEmpty()) return false;
        
        // 提取域名
        String domain = url;
        if (domain.startsWith("http://")) domain = domain.substring(7);
        if (domain.startsWith("https://")) domain = domain.substring(8);
        int slashIndex = domain.indexOf('/');
        if (slashIndex > 0) domain = domain.substring(0, slashIndex);
        
        // 检查是否在屏蔽列表中
        return blockedDomains.contains(domain);
    }

    private void loadBlockedDomains() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        blockedDomains = prefs.getStringSet(KEY_BLOCKED_DOMAINS, new HashSet<>());
        if (blockedDomains == null) {
            blockedDomains = new HashSet<>();
        }
    }

    private void saveBlockedDomains() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_BLOCKED_DOMAINS, blockedDomains);
        editor.apply();
    }

    private void showBlockedDomainsDialog() {
                logOperation("打开屏蔽网站管理", 1657);
        final java.util.List<String> domainList = new java.util.ArrayList<>(blockedDomains);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("屏蔽网站管理");

        if (domainList.isEmpty()) {
            builder.setMessage("当前没有屏蔽任何网站");
            builder.setPositiveButton("确定", null);
            builder.setNeutralButton("添加", (dialog, which) -> showAddBlockedDomainDialog());
            builder.show();
            return;
        }

        builder.setItems(domainList.toArray(new String[0]), (dialog, which) -> {
            // 点击项可以查看详情或删除，这里简化为长按删除
        });

        builder.setNegativeButton("返回", null);
        builder.setNeutralButton("添加", (dialog, which) -> showAddBlockedDomainDialog());
        
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            ListView listView = dialog.getListView();
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                final String domain = domainList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("确认删除")
                    .setMessage("确定要取消屏蔽 " + domain + " 吗？")
                    .setPositiveButton("确定", (dialogInner, whichInner) -> {
                        blockedDomains.remove(domain);
                        saveBlockedDomains();
                        Toast.makeText(MainActivity.this, "已取消屏蔽: " + domain, Toast.LENGTH_SHORT).show();
                        showBlockedDomainsDialog();
                    })
                    .setNegativeButton("取消", null)
                    .show();
                return true;
            });
        });
        dialog.show();
    }

    private void showAddBlockedDomainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加屏蔽网站");
        
        final EditText input = new EditText(this);
        input.setHint("请输入域名，例如: example.com");
        builder.setView(input);
        
        builder.setPositiveButton("添加", (dialog, which) -> {
            String domain = input.getText().toString().trim();
            if (!TextUtils.isEmpty(domain)) {
                // 简单的域名处理
                if (domain.startsWith("http://")) domain = domain.substring(7);
                if (domain.startsWith("https://")) domain = domain.substring(8);
                int slashIndex = domain.indexOf('/');
                if (slashIndex > 0) domain = domain.substring(0, slashIndex);
                
                if (!blockedDomains.contains(domain)) {
                    blockedDomains.add(domain);
                    saveBlockedDomains();
                    Toast.makeText(MainActivity.this, "已添加屏蔽: " + domain, Toast.LENGTH_SHORT).show();
                    showBlockedDomainsDialog();
                } else {
                    Toast.makeText(MainActivity.this, "该域名已在屏蔽列表中", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "请输入有效的域名", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理TTS资源
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        // 放弃音频焦点
        abandonAudioFocus();
        // 注销广播接收器
        if (ttsControlReceiver != null) {
            try {
                unregisterReceiver(ttsControlReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播接收器失败", e);
            }
        }
    }
}