package com.bridge187.corfupages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import com.bridge187.corfupages.utilities.LocationModel;
import com.bridge187.corfupages.webclient.WebClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * This Activity holds all fragments when in two pane mode (ie on a tablet or a large screen), and just the menu when on a small screen.
 * On a small screen each fragment has its own Activity
 */
public class MenuActivity extends FragmentActivity implements MenuFragment.Callbacks, ListingsFragment.Callbacks, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    /** the fragment for the scrollable list of businesses */
    private ListingsFragment listingsFragment;

    /** just a background image present before anyone has done a search on the tablet layout */
    private InfoFragment infoFragment;

    /** GoogleApiClient is used for the location updates */
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    /** This is set true when we ccan't find the google api client */
    private boolean playNotConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_list);

        //here we look at which version of activity_listing_list has been loaded from resources
        //(it depends on the screen size)
        //and decide whether to use a two pane (tablet) layout or not.
        //see the refs.xml files in values folder in res folder for clarification

        if (findViewById(R.id.listing_detail_container) != null)
        {
            //this means we have used refs.xml from values-large folde, hence we want to use a tablet layout (two pane)
            ((MainApplication)this.getApplication()).getAppUtilities().setTabletLayout();
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.listing_detail_container);

            if (currentFragment == null)
            {
                if (infoFragment == null)
                {
                    infoFragment = InfoFragment.newInstance();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.listing_detail_container, infoFragment);
                    ft.commit();
                }
            }
        }
        buildGoogleApiClient();
    }

    /**
     * When the search button is pressed, we save data like search terms etc in the data model (WebClient) and just show the Listings Fragment,
     * which actually instigates the search itself
     * @param searchMode GPS or with location id
     * @param searchTerms what we want to fine (eg pubs, restaurants
     * @param loc location id (mapped to location ids on the online database, see LocationIds in utilities package
     */
    @Override
    public void onSearchPressed(WebClient.SearchMode searchMode, String searchTerms, int loc)
    {
        WebClient.getInstance().resetWebClientStatus();

        if (searchMode == WebClient.SearchMode.GPS)
        {
            if (LocationModel.getInstance().getLocation() == null)
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                String message = playNotConnected ? getString(R.string.play_not_connected) : getString(R.string.location_error);
                alertDialogBuilder.setMessage(message).setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.create().show();

                return;
            }
        }

        WebClient.getInstance().reset();
        WebClient.getInstance().setSearchMode(searchMode);
        WebClient.getInstance().setLocation(LocationModel.getInstance().getLocation());
        WebClient.getInstance().setSearchTerms(searchTerms);
        WebClient.getInstance().setLocationId(loc);

        if (((MainApplication)this.getApplication()).getAppUtilities().isTabletLayout())
        {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.listing_detail_container);

            if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals("LISTINGS_FRAGMENT"))
            {
                ((ListingsFragment)currentFragment).startSearch();
            }
            else
            {
                if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals("BIP_FRAGMENT"))
                {
                    getSupportFragmentManager().popBackStack();
                }

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                if (listingsFragment == null)
                {
                    listingsFragment = ListingsFragment.newInstance();
                }

                ft.replace(R.id.listing_detail_container, listingsFragment, "LISTINGS_FRAGMENT");
                ft.addToBackStack(null);
                ft.commit();
            }
        }
        else
        {
            Intent listingIntent = new Intent(this, ListingsActivity.class);
            startActivity(listingIntent);
        }
    }


    /**
     * Called only in tablet mode, in small screen mode the Listings Fragment has its own Activity
     * @param position position of the business in the list (starting with 0)
     */
    @Override
    public void onListingPressed(int position)
    {
        WebClient.getInstance().setCurrentBipShowing(position);
        if (((MainApplication)this.getApplication()).getAppUtilities().isTabletLayout())
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            BipFragment bipFragment = BipFragment.newInstance();
            ft.replace(R.id.listing_detail_container, bipFragment, "BIP_FRAGMENT");
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    protected void onResume()
        {
        super.onResume();
        googleApiClient.connect();
        startLocationUpdates();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopLocationUpdates();
        if (googleApiClient.isConnected())
        {
            googleApiClient.disconnect();
        }
    }

    /**
     * controlls back button behaviour only for tablet
     */
    @Override
    public void onBackPressed()
    {
        if (((MainApplication)this.getApplication()).getAppUtilities().isTabletLayout())
        {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.listing_detail_container);
            if (currentFragment != null && currentFragment.getTag() != null && currentFragment.getTag().equals("LISTINGS_FRAGMENT"))
            {
                finish();
                return;
            }
        }
        super.onBackPressed();
    }

    /**
     * connect with google api's for location updates
     */
    private synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void startLocationUpdates()
    {
        if (googleApiClient != null && googleApiClient.isConnected() && locationRequest != null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    private void stopLocationUpdates()
    {
        try
        {
            if (googleApiClient != null && googleApiClient.isConnected()  && locationRequest != null)
            {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LocationModel.getInstance().setLocation(location);
    }

    /**
     * sets a flag so we can tell the user if they can't get location updates because google api's are not present
     * @param connectionResult ignored
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        playNotConnected = true;
    }


    /**
     * means we are connected to google apis
     * @param bundle not used
     */
    @Override
    public void onConnected(Bundle bundle)
    {
        startLocationUpdates();
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationModel.getInstance().setLocation(lastLocation);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        //do nothing
    }
}
