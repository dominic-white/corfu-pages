package com.bridge187.corfupages;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.bridge187.corfupages.webclient.WebClient;

/**
 * Activity to hold Business Information Fragment only when viewed on a small screened device. On tablets all fragments are contained in the MenuActivity class
 */
public class BipActivity  extends FragmentActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        if (savedInstanceState == null)
        {
            BipFragment fragment = new BipFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.listing_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //This code is to deal with when the Android system resurrects an Activity after the app has exited and the data in the model is lost
        //It could be dealt with in a different fashion by persisting the data model, but we want freash data anyway, so this method works here
        if ( WebClient.getInstance().getData() == null)
        {
            finish();
        }
    }
}
