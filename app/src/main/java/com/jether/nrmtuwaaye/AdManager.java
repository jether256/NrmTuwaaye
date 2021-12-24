package com.jether.nrmtuwaaye;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class AdManager {


    private Context ctx;

    public AdManager(Context ctx)
    {

        MobileAds.initialize(ctx, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
    }


    public void createAds(AdView adView){
        AdRequest adRequest= new AdRequest.Builder().build();
        adView.setAdListener(new AdListener()
        {

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);

                Toast.makeText(ctx,""+loadAdError.getCode(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                Toast.makeText(ctx,"ads loaded..",Toast.LENGTH_LONG).show();
            }
        });

        adView.loadAd(adRequest);
    }

}
