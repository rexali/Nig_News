package com.ebizebiz.android.nignews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_NEWS_URL;
import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_UPDATE_NEWS_NAME;
import static com.ebizebiz.android.nignews.MainActivity.EXTRA_DATA_UPDATE_NEWS_URL;

public class WebViewActivity extends AppCompatActivity {

    WebView webView;
    final Activity activity = this;
    String html = "<input type=\"button\" value=\"Say hello\" onClick=\"showAndroidToast('Hello Android!')\" />\n" +
            "\n" +
            "<script type=\"text/javascript\">\n" +
            "    function showAndroidToast(toast) {\n" +
            "        Android.showToast(toast);\n" +
            "    }\n" +
            "</script>";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // webView = new WebView(this);

        // Let's display the progress in the activity title bar, like the
        // browser app does.
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        //setContentView(webView);

        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity=Gravity.CENTER;
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        progressBar = new ProgressBar(this);
        LinearLayout.LayoutParams layoutParams1 =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

        progressBar.setLayoutParams(layoutParams1);
        progressBar.setVisibility(View.VISIBLE);
        //progressBar.setProgress(1);
        linearLayout.addView(progressBar);

        WebView webView = new WebView(this);
        LinearLayout.LayoutParams layoutParams2 =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

        webView.setLayoutParams(layoutParams2);
        linearLayout.addView(webView);

        setContentView(linearLayout);



        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        if (extras != null) {
            //String name = extras.getString(EXTRA_DATA_UPDATE_NEWS_NAME, "");
            String urls = extras.getString(EXTRA_DATA_NEWS_URL, "");


            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webView.setVisibility(View.VISIBLE);
            webView.pageUp(true);

            webView.setWebChromeClient(new MyWebChromeClient());

            webView.setWebViewClient(new MyWebViewClient());

            webView.addJavascriptInterface(new WebAppInterface(this), "Android");

           webView.loadUrl("http://"+urls+"/");

        }
    }


    public class MyWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            // Activities and WebViews measure progress with different scales.
            // The progress meter will automatically disappear when we reach 100%
            // activity.setProgress(newProgress * 1000);
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                // progressBar.setVisibility(View.GONE);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            //super.onLoadResource(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

    }


    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }


}
