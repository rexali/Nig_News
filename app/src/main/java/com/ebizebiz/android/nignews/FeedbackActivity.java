package com.ebizebiz.android.nignews;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class FeedbackActivity extends AppCompatActivity {

    private AdView mAdViewMedium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mAdViewMedium = findViewById(R.id.ad_View_Medium);
        MobileAds.initialize(this, getResources().getString(R.string.AdMob_App_Id));

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdViewMedium.loadAd(new AdRequest.Builder().build());
    }
}
