package com.bridge187.corfupages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.bridge187.corfupages.webclient.WebClient;

/**
 * Activity to hold Listings Fragment only when viewed on a small screened device. On tablets all fragments are contained in the MenuActivity class
 */
public class ListingsActivity extends FragmentActivity implements ListingsFragment.Callbacks
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);

        if (savedInstanceState == null)
        {
            ListingsFragment listingsFragment = ListingsFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.listing_detail_container, listingsFragment)
                    .commit();
        }
    }

    @Override
    public void onListingPressed(int position)
    {
        WebClient.getInstance().setCurrentBipShowing(position);
        Intent bipIntent = new Intent(this, BipActivity.class);
        startActivity(bipIntent);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        WebClient.getInstance().cancel();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //This code is to deal with when the Android system resurrects an Activity after the app has exited and the data in the model is lost
        //It could be dealt with in a different fashion by persisting the data model, but we want freash data anyway, so this method works here
        if (WebClient.getInstance().searchTerms == null)
        {
            finish();
        }
    }
}