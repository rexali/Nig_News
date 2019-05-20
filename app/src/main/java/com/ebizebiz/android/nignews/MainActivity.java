package com.ebizebiz.android.nignews;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final int NEW_NEWS_ACTIVITY_REQUEST_CODE = 1;
    public static final int UPDATE_NEWS_ACTIVITY_REQUEST_CODE = 2;

    public static final String EXTRA_DATA_UPDATE_NEWS_NAME = "extra_name_to_be_updated";
    public static final String EXTRA_DATA_UPDATE_NEWS_URL = "extra_url_to_be_updated";
    public static final String EXTRA_DATA_ID = "extra_data_id";

    public static final String EXTRA_DATA_NEWS_URL = "extra_url_to_load_web_view";

    public static final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";  // Change when in stable

    private static final String ACTION_CUSTOM_TABS_CONNECTION="android.support.customtabs.action.CustomTabsService" ;

    private CustomTabsClient mCustomTabsClient;

    private NewsViewModel mNewsViewModel;

    boolean isWebViewLoaded = false;

    private static final String ACTION_SHARE =
            BuildConfig.APPLICATION_ID + ".ACTION_SHARE";
    private static final String ACTION_DAILY_TRUST =
            BuildConfig.APPLICATION_ID + ".ACTION_DAILY_TRUST";
    private static final String ACTION_SUN_NEWS =
            BuildConfig.APPLICATION_ID + ".ACTION_SUN_NEWS";

    private MyReceiver myReceiver = new MyReceiver();

    private AdView mAdView;
    private AdView mAd_View;
    private InterstitialAd mInterstitialAd;

    private static String script ="<script>\n" +
            "  (function() {\n" +
            "    var cx = 'partner-pub-4192644863425218:raj23olhqr4';\n" +
            "    var gcse = document.createElement('script');\n" +
            "    gcse.type = 'text/javascript';\n" +
            "    gcse.async = true;\n" +
            "    gcse.src = 'https://cse.google.com/cse.js?cx=' + cx;\n" +
            "    var s = document.getElementsByTagName('script')[0];\n" +
            "    s.parentNode.insertBefore(gcse, s);\n" +
            "  })();\n" +
            "</script>\n" +
            "<gcse:searchbox-only></gcse:searchbox-only>" +

            "<script type=\"text/javascript\" src=\"http://www.google.com/cse/query_renderer.js\"></script>\n" +
            "<div id=\"queries\"></div>\n" +
            "<script src=\"http://www.google.com/cse/api/partner-pub-4192644863425218/cse/raj23olhqr4/queries/js?oe=UTF-8&amp;callback=(new+PopularQueryRenderer(document.getElementById(%22queries%22))).render\"></script>";

    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customSearchWebView();


        mAdView = findViewById(R.id.adView);
        mAd_View = findViewById(R.id.ad_View);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713

        MobileAds.initialize(this, getResources().getString(R.string.AdMob_App_Id));

        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);
        mAd_View.loadAd(adRequest);

        //Check network connectivity before lunching the browser.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // If the network is available, connected, and the search field
        // is not empty, start a BookLoader AsyncTask.
        if (networkInfo != null && networkInfo.isConnected()) {
           mAdView.setVisibility(View.VISIBLE);
           mAd_View.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
            mAd_View.setVisibility(View.GONE);
        }


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.AdMob_Interstitial_Ad_Unit_Id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                //load the next interstitial ad
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        // Register the receiver to receive custom broadcast.

        LocalBroadcastManager.getInstance(this).registerReceiver
                (myReceiver, new IntentFilter(ACTION_SUN_NEWS));
        LocalBroadcastManager.getInstance(this).registerReceiver
                (myReceiver, new IntentFilter(ACTION_DAILY_TRUST));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the RecyclerView.
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final NewsListAdapter adapter = new NewsListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the WordViewModel.
        mNewsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        // Get all the words from the database
        // and associate them to the adapter.
        mNewsViewModel.getAllNews().observe(this, new Observer<List<News>>() {
            @Override
            public void onChanged(@Nullable final List<News> news) {
                // Update the cached copy of the news in the adapter.
                adapter.setNews(news);
            }
        });

        // Floating action button setup
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewNewsActivity.class);
                startActivityForResult(intent, NEW_NEWS_ACTIVITY_REQUEST_CODE);
            }
        });
        // Add the functionality to swipe items in the
        // RecyclerView to delete the swiped item.
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    // We are not implementing onMove() in this app.
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    // When the use swipes a word,
                    // delete that word from the database.
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        News news = adapter.getNewsAtPosition(position);
                        Toast.makeText(MainActivity.this,
                                getString(R.string.edit_news_preamble) + " " +
                                        news.getName(), Toast.LENGTH_LONG).show();

                        // Delete the word.
                        //mNewsViewModel.deleteWord(myWord);
                        launchUpdateNewsActivity(news);
                    }

                });
        // Attach the item touch helper to the recycler view.
        helper.attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NewsListAdapter.ClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                News news = adapter.getNewsAtPosition(position);

                //Check network connectivity before lunching the browser.
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;
                if (connMgr != null) {
                    networkInfo = connMgr.getActiveNetworkInfo();
                }

                // If the network is available, connected, and the search field
                // is not empty, start a BookLoader AsyncTask.
                if (networkInfo != null && networkInfo.isConnected()) {

                    Snackbar.make(view, "Loading... Please wait.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .show();

                    if (mInterstitialAd.isLoaded()){
                        mInterstitialAd.show();
                    } else {
                        Toast.makeText(
                                MainActivity.this,"The interstitial wasn't loaded yet.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    if (isPackageExisted()) {
                        useChromeBrowser(news);
                    } else {
                        if (isOperaExisted()) {
                            launchOperaBrowser(news);
                        } else if(isFirefoxExisted()){
                            launchFirefoxBrowser(news);
                        }else {
                            //useWebView(news); or
                            //lunchWebViewActivity(news);
                            anyBrowser(news);
                        }
                    }

                } else {

                    Snackbar.make(view, "No network connection. Connect and try again", Snackbar.LENGTH_LONG)
                            .setAction("View", null).show();

                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        CustomTabsServiceConnection customTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(1);
                mCustomTabsClient.newSession(new CustomTabsCallback());
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        boolean ok = CustomTabsClient.bindCustomTabsService(this, CUSTOM_TAB_PACKAGE_NAME, customTabsServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    // The options menu has a single item "Clear all data now"
    // that deletes all the entries in the database.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, as long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.clear_data) {
            // Add delete confirmation dialog
            showDeleteConfirmationDialog();
            return true;
        }

        if (id == R.id.action_help) {
            // Add show help and feedback dialog
            showHelpDialog();
            return true;
        }

        if (id == R.id.action_feedback) {
            // Add show contact and feedback
            startActivity(new Intent(this,FeedbackActivity.class));
            return true;
        }

        if (id == R.id.action_about) {
            // Add show about dialog
            showAboutDialog();
            return true;
        }

        if (id == R.id.action_share) {
            //
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,"I found this App useful, please download it you may find it" +"good too.");
            Intent chooserIntent = Intent.createChooser(shareIntent,"Share this App");
            startActivity(chooserIntent);
            return true;
        }


        return super.onOptionsItemSelected(item);

    }

    /**
     * When the user enters a new word in the NewWordActivity,
     * that activity returns the result to this activity.
     * If the user entered a new word, save it in the database.
     *
     * @param requestCode ID for the request
     * @param resultCode  indicates success or failure
     * @param data        The Intent sent back from the NewWordActivity,
     *                    which includes the word that the user entered
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_NEWS_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            News news = new News(data.getStringExtra(NewNewsActivity.EXTRA_REPLY_NAME), data.getStringExtra(NewNewsActivity.EXTRA_REPLY_URL));
            // Save the data.
            mNewsViewModel.insert(news);
        } else if (requestCode == UPDATE_NEWS_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {
            String newsname_data = data.getStringExtra(NewNewsActivity.EXTRA_REPLY_NAME);
            String newsurl_data = data.getStringExtra(NewNewsActivity.EXTRA_REPLY_URL);
            int id = data.getIntExtra(NewNewsActivity.EXTRA_REPLY_ID, -1);

            if (id != -1) {
                mNewsViewModel.update(new News(id, newsname_data, newsurl_data));
            } else {
                Toast.makeText(this, R.string.unable_to_update,
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(
                    this, R.string.empty_not_saved, Toast.LENGTH_LONG).show();
        }
    }

    public void launchUpdateNewsActivity(News news) {
        Intent intent = new Intent(this, NewNewsActivity.class);
        intent.putExtra(EXTRA_DATA_UPDATE_NEWS_NAME, news.getName());
        intent.putExtra(EXTRA_DATA_UPDATE_NEWS_URL, news.getUrl());
        intent.putExtra(EXTRA_DATA_ID, news.getId());
        startActivityForResult(intent, UPDATE_NEWS_ACTIVITY_REQUEST_CODE);
    }

    public void anyBrowser(News news) {
        // Moving to custom chrome browser
        // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        // and launch the desired Url with CustomTabsIntent.launchUrl()

        String url = "http://" + news.getUrl();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.addDefaultShareMenuItem();
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_arrow_back));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
    }

    public void launchOperaBrowser(News news) {
        // Moving to custom chrome browser
        // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        // and launch the desired Url with CustomTabsIntent.launchUrl()

        String url = "http://" + news.getUrl();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.addDefaultShareMenuItem();
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_arrow_back));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.opera.browser");
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,Uri.parse(Intent.URI_ANDROID_APP_SCHEME+"//"
                +this.getApplicationContext().getPackageName()));
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
    }

    public void launchFirefoxBrowser(News news) {
        // Moving to custom chrome browser
        // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        // and launch the desired Url with CustomTabsIntent.launchUrl()

        String url = "http://" + news.getUrl();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.addDefaultShareMenuItem();
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_arrow_back));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("org.mozilla.firefox");
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,Uri.parse(Intent.URI_ANDROID_APP_SCHEME+"//"
                +this.getApplicationContext().getPackageName()));
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
    }


    public void useChromeBrowser(News news) {
        // Moving to custom chrome browser
        // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        // and launch the desired Url with CustomTabsIntent.launchUrl()
        Bitmap backIcon =BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_arrow_back);

        Bitmap shareIcon =BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_share);

        // Pending intent to my broadcast receiver(MyReceiver) implementation and to share url of
        // the content user is reading
      Intent sIntent = new Intent(this.getApplicationContext(),MyReceiver.class);
      sIntent.setAction(ACTION_SHARE);
      PendingIntent sPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0,sIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dailytrustIntent = new Intent(this.getApplicationContext(),MyReceiver.class);
        dailytrustIntent.setAction(ACTION_DAILY_TRUST);
        PendingIntent dailytrustPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),0,dailytrustIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Pending intent to to activity to share app
        // the content user is reading
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND)
                   .setType("text/plain")
                   .putExtra(Intent.EXTRA_TEXT, "Download Read News App to differentiate between fake and real news, http://www");
        PendingIntent shareAppPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,shareIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        // Pending intent to to activity to open sun news
        Intent sunNewsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://sunnewsonline.com"));
        PendingIntent sunNewsPendingIntent =
                PendingIntent.getActivity(getApplicationContext(),0,sunNewsIntent,0);

        Intent vanNewsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vanguardngr.com"));
        PendingIntent vanNewsPendingIntent =
                PendingIntent.getActivity(getApplicationContext(),0,vanNewsIntent,0);


        String url = "http://" + news.getUrl();
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        //builder.addDefaultShareMenuItem();
        builder.setActionButton(shareIcon,"Share",sPendingIntent);
        builder.addMenuItem("Share this App",shareAppPendingIntent);
        builder.addMenuItem("Sun",sunNewsPendingIntent);
        builder.addMenuItem("Vanguard",vanNewsPendingIntent);
        builder.addMenuItem("Daily Trust",dailytrustPendingIntent);
        builder.setShowTitle(true);
        //builder.enableUrlBarHiding();
        builder.setCloseButtonIcon(backIcon);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.setPackage("com.android.chrome");
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,Uri.parse(Intent.URI_ANDROID_APP_SCHEME+"//"
                +this.getApplicationContext().getPackageName()));
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));

    }

    public void customSearchWebView() {
        webView = findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setVisibility(View.VISIBLE);
        webView.pageUp(true);
        webView.loadData(script,"text/html","UTF-8");
    }
    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the news
                // Delete the existing data.
                mNewsViewModel.deleteAll();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void lunchWebViewActivity(News news) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(EXTRA_DATA_NEWS_URL, news.getUrl());
        startActivity(intent);
    }

    public void useWebView(News news) {

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setVisibility(View.VISIBLE);
        webView.pageUp(true);
        webView.loadUrl("http://" + news.getUrl());

        Toast tost = Toast.makeText(MainActivity.this, "Loading... Wait for few seconds", Toast.LENGTH_LONG);
        tost.show();

        isWebViewLoaded =true;

    }

    private void showHelpDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.help_feedback);
        builder.setPositiveButton(R.string.ok_help_feedback, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Ok" button, so dismiss the help dialog
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showAboutDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about);
        builder.setPositiveButton(R.string.ok_help_feedback, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Ok" button, so dismiss the help dialog
                dialog.dismiss();
            }
        });
//        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User clicked the "Cancel" button, so dismiss the dialog.
//                if (dialog != null) {
//                    dialog.dismiss();
//                }
//            }
//        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the receivers.
        //this.unregisterReceiver(myReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdView.loadAd(new AdRequest.Builder().build());
        mAd_View.loadAd(new AdRequest.Builder().build());
        mAdView.setVisibility(View.VISIBLE);
        mAd_View.setVisibility(View.VISIBLE);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //view.loadUrl(url);
            launchBrowser(url);
            return true;
        }
    }

    public void launchBrowser(String url) {

        // Moving to custom chrome browser
        // Use a CustomTabsIntent.Builder to configure CustomTabsIntent.
        // Once ready, call CustomTabsIntent.Builder.build() to create a CustomTabsIntent
        // and launch the desired Url with CustomTabsIntent.launchUrl()
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        builder.addDefaultShareMenuItem();
        builder.enableUrlBarHiding();
        builder.setShowTitle(true);
        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                getResources(), R.drawable.ic_arrow_back));
        CustomTabsIntent customTabsIntent = builder.build();
        if (isPackageExisted()){
            customTabsIntent.intent.setPackage("com.android.chrome");
        } else if(isOperaMiniExisted()){
            customTabsIntent.intent.setPackage("com.opera.mini.native");
        } else if (isOperaExisted()){
            customTabsIntent.intent.setPackage("com.opera.browser");
        }else if(isFirefoxExisted()){
            customTabsIntent.intent.setPackage("org.mozilla.firefox");
        } else {
            customTabsIntent.intent.setPackage("com.android.browser");
        }
        customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER,Uri.parse(Intent.URI_ANDROID_APP_SCHEME+"//"
                +this.getApplicationContext().getPackageName()));
        customTabsIntent.launchUrl(MainActivity.this, Uri.parse(url));
    }

    public boolean isPackageExisted() {
        List<PackageInfo> packages;
        PackageManager packageManager;
        packageManager = getPackageManager();
        packages = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.android.chrome")) {
                return true;
            }
        }
        return false;
    }

    public boolean isOperaExisted() {
        List<PackageInfo> packages;
        PackageManager packageManager;
        packageManager = getPackageManager();
        packages = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.opera.browser")) {
                return true;
            }
        }
        return false;
    }
    public boolean isOperaMiniExisted() {
        List<PackageInfo> packages;
        PackageManager packageManager;
        packageManager = getPackageManager();
        packages = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("com.opera.mini.native")) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirefoxExisted() {
        List<PackageInfo> packages;
        PackageManager packageManager;
        packageManager = getPackageManager();
        packages = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals("org.mozilla.firefox")) {
                return true;
            }
        }
        return false;
    }

    public class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
        @Override
        public void onLoadResource(WebView webView, String url) {

        }
    }
}



