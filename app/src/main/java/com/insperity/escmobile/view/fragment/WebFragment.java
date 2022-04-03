package com.insperity.escmobile.view.fragment;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.insperity.escmobile.R;
import com.insperity.escmobile.event.OnWebContentToLoad;
import com.insperity.escmobile.net.analytics.Tracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.internal.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class WebFragment extends BaseFragment {
    public static final String TAG = WebFragment.class.getSimpleName();

    @Inject
    EventBus bus;
    @Inject
    Tracker tracker;

    @BindView(R.id.web_view)
    WebView webView;

    public static WebFragment newInstance() {
        return new WebFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCookieHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeWebView();
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void initializeWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setSupportZoom(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                onPageStartedListener(view, url);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoadingListener(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrlLoadingListener(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                onLoadResourceListener(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                onPageFinishedListener(view, url);
            }
        });
    }

    protected void setupCookieHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            return;
        }
        CookieManager.getInstance().removeAllCookie();
    }

    protected void onPageStartedListener(WebView view, String url) {
        showProgressBar(true);
    }

    protected boolean shouldOverrideUrlLoadingListener(WebView view, String url) {
        return false;
    }

    protected void onLoadResourceListener(WebView view, String url) {
    }

    protected void onPageFinishedListener(WebView view, String url) {
        Observable.timer(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(l -> showProgressBar(false));
        syncCookies();
    }

    private void syncCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush();
            return;
        }
        CookieSyncManager.getInstance().sync();
    }

    public boolean didWebViewGoBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    @Subscribe(sticky = true)
    public void onEvent(final OnWebContentToLoad event) {
        loadData(event.htmlContent);
        bus.removeStickyEvent(event);
    }

    protected void loadData(String htmlContent) {
        webView.loadData(htmlContent, "text/html", "utf-8");
    }

    protected void loadUrl(String url) {
        webView.loadUrl(url);
    }
}
