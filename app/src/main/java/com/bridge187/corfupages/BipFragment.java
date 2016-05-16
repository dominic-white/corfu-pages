package com.bridge187.corfupages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bridge187.corfupages.ux.FlowLayout;
import com.bridge187.corfupages.utilities.AppUtilities;
import com.bridge187.corfupages.utilities.ImageCache;
import com.bridge187.corfupages.utilities.TextViewUtilities;
import com.bridge187.corfupages.webclient.WebClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment to display details of an individual business, viewed when a user clicks on one of the business in the search results list
 */
public class BipFragment extends Fragment implements OnMapReadyCallback
{
    /** The position of the business in the data model's JSON array of businesses */
    private int position;

    /** A JSONObject straight out of the JSONArray in the server response describing an individual business */
    private JSONObject listing;

    /** The View representing th UI of the fragment, returned form the onCreateView method */
    private View rootView;

    /** The single ImageCache which hold all images in the app and deals with memoory managment to prevent Out Of Memory errors */
    private ImageCache imageCache;

    /** The language is set from the device's default locale, and is needed to decide whether to display the Greek or English language from the listing object retreived from the server */
    private AppUtilities.Language language;

    /** The name of the business, in the language specified by the language variable */
    private String nativeTitle = null;

    /** Custom layout to display the thumbnails */
    private FlowLayout galleryLayout;

    /** A ScrollView which holds the entire view. The reference is needed to fix an issue with Google Maps, see mapCover in onCreateView method */
    private ScrollView scrollView;

    /**
     * Necessary empty constructer
     */
    public BipFragment()
    {
    }

    /**
     * Factory method to get a new instance of this class
     * @return a BipFragment for one business
     */
    public static BipFragment newInstance()
    {
        return new BipFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.bip_detail, container, false);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        scrollView = (ScrollView)rootView.findViewById(R.id.bip_scroll_view);
        ImageView mapCover = (ImageView)rootView.findViewById(R.id.transparent_image);

        mapCover.setOnTouchListener(new View.OnTouchListener()
        {

            //The below bit of code is to allow the user to swipe the map without scrolling the whole page
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getAction();
                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    case MotionEvent.ACTION_UP:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });

        language = ((MainApplication)this.getActivity().getApplication()).getAppUtilities().getLanguage();
        galleryLayout = (FlowLayout)rootView.findViewById(R.id.gallery_flow_layout);

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //This code is to deal with when the Android system resurrects an Activity after the app has exited and the data in the model is lost
        //It could be dealt with in a different fashion by persisting the data model, but we want freash data anyway, so this method works here
        if ( WebClient.getInstance().getData() == null)
        {
            this.getActivity().getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        //ImageCache is not a Singleton but we have only one in the app, which is a member of the MainApplication class
        imageCache = ((MainApplication)this.getActivity().getApplication()).getImageCache();
        position = WebClient.getInstance().getCurrentBipShowing();
        populateUI();
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        double lat = 0;
        double lon = 0;
        try
        {
            lat = Double.parseDouble(listing.getString("lat"));
            lon = Double.parseDouble(listing.getString("lon"));
        }
        catch (JSONException | NumberFormatException e)
        {
            e.printStackTrace();
        }


        LatLng center = new LatLng(lat, lon);

        map.addMarker(new MarkerOptions().position(center).title(nativeTitle));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14.0f));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
    }

    /**
     * Here we take the JSONObject listing and extract data from it to populate the views in the fragment.
     * The data should not be null or the fragment would have exited in the onResume method
     */
    private void populateUI()
    {
        Resources resources = this.getActivity().getResources();
        try
        {
            listing = WebClient.getInstance().getData().getJSONArray("listings").getJSONObject(position);
            Context con = this.getActivity().getApplicationContext();
            switch (language)
            {
                case ENGLISH:
                    nativeTitle = listing.getString("title_en");

                    TextViewUtilities.populateTextView(rootView, R.id.short_description, listing.getString("shortDescription_en"));
                    TextViewUtilities.populateTextView(rootView, R.id.long_description, listing.getString("longDescription_en"));
                    TextViewUtilities.populateTextView(rootView, R.id.price_list, listing.getString("pricelist_en"), resources.getString(R.string.price_list));

                    break;

                case GREEK:
                    nativeTitle = listing.getString("title_gr");

                    TextViewUtilities.populateTextView(rootView, R.id.short_description, listing.getString("shortDescription_gr"));
                    TextViewUtilities.populateTextView(rootView, R.id.long_description, listing.getString("longDescription_gr"));
                    TextViewUtilities.populateTextView(rootView, R.id.price_list, listing.getString("pricelist_gr"), resources.getString(R.string.price_list));

                    break;

                default:
                    throw  new RuntimeException("No such language");
            }

            TextViewUtilities.populateTextView(rootView, R.id.title_native, nativeTitle);

            TextViewUtilities.populateTextView(rootView, R.id.address1, listing.getString("address1"));
            TextViewUtilities.populateTextView(rootView, R.id.address2, listing.getString("address2"));
            TextViewUtilities.populateTextView(rootView, R.id.postcode, listing.getString("postcode"));
            TextViewUtilities.populateButton(con, rootView, R.id.main_phone, listing.getString("main_phone"), resources.getString(R.string.main_phone), TextViewUtilities.ButtonType.PHONE, true);
            TextViewUtilities.populateButton(con, rootView, R.id.mobile_phone, listing.getString("mobile_phone"), resources.getString(R.string.mobile_phone), TextViewUtilities.ButtonType.PHONE, true);
            TextViewUtilities.populateTextView(rootView, R.id.fax, listing.getString("fax"), resources.getString(R.string.fax));

            TextViewUtilities.populateButton(con, rootView, R.id.email_button, listing.getString("email"), resources.getString(R.string.email), TextViewUtilities.ButtonType.EMAIL, true);
            TextViewUtilities.populateButton(con, rootView, R.id.website_button, listing.getString("website"), resources.getString(R.string.website), TextViewUtilities.ButtonType.WEBLINK, true);
            TextViewUtilities.populateButton(con, rootView, R.id.facebook_button, listing.getString("facebook"), resources.getString(R.string.facebook), TextViewUtilities.ButtonType.WEBLINK, false);
            TextViewUtilities.populateButton(con, rootView, R.id.trip_adviser_button, listing.getString("tripadvisor"), resources.getString(R.string.trip_adviser), TextViewUtilities.ButtonType.WEBLINK, false);

            //TODO this is now redundant as there are wordpress sites for booking the businesses with this facility.
            Button bookNowButton = (Button)rootView.findViewById(R.id.book_now_button);
            if (listing.getString("booking_info") == null || listing.getString("booking_info").length() == 0)
            {
                bookNowButton.setVisibility(View.GONE);
            }
            else
            {
                bookNowButton.setVisibility(View.VISIBLE);
                bookNowButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            String url = "https://www.corfupages.com/booking/index.php?listingid=" + listing.getString("listingId");
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            intent.setData(Uri.parse(url));
                            BipFragment.this.getActivity().getApplicationContext().startActivity(intent);
                        }
                        catch (Exception e)//TODO find all possible exceptions
                        {
                            e.printStackTrace();
                            Toast.makeText(BipFragment.this.getActivity().getApplicationContext(), R.string.booking_page_error, Toast.LENGTH_LONG).show();
                        }
                    }

                });
            }


            TextView categoryLocationText = (TextView)rootView.findViewById(R.id.bip_category_location);

            StringBuilder categoryLocation = new StringBuilder();
            switch (language)
            {
                case ENGLISH:
                    categoryLocation.append(listing.getString("displayCategory_en")).append(" ")
                            .append(getText(R.string.near)).append(" ")
                            .append(listing.getString("displayLocation_en"));
                    break;
                case GREEK:
                    categoryLocation.append(listing.getString("displayCategory_gr")).append(" ")
                            .append(getText(R.string.near)).append(" ")
                            .append(listing.getString("displayLocation_gr"));
                    break;
            }

            categoryLocationText.setText(categoryLocation.toString());

            JSONArray thumbnails = listing.optJSONArray("thumbnailList");
            JSONArray images = listing.optJSONArray("imageList");

            final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, this.getActivity().getResources().getDisplayMetrics());

            if (thumbnails != null && images != null && thumbnails.length() == images.length() && thumbnails.length() > 0)
            {
                galleryLayout.setVisibility(View.VISIBLE);
                galleryLayout.removeAllViews();

                for (int i = 0; i < thumbnails.length(); i ++)
                {
                    String thumbUrl = thumbnails.getString(i);
                    final String imageUrl = images.getString(i);

                    ImageView imageView = new ImageView(this.getActivity());
                    imageView.setPadding(padding, padding, padding, padding);
                    imageView.setClickable(true);
                    imageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent imageIntent = new Intent(BipFragment.this.getActivity(), ImageViewActivity.class);
                            imageIntent.putExtra(ImageViewActivity.IMAGE_URL, imageUrl);
                            startActivity(imageIntent);
                        }
                    });


                    galleryLayout.addView(imageView);
                    imageCache.loadBitmap(thumbUrl, imageView, false, -1);
                }

                //TODO below image view is added to cover up a bug in FlowLayout which does not display last image
                ImageView imageView = new ImageView(this.getActivity());
                galleryLayout.addView(imageView);
            }
            else
            {
                galleryLayout.setVisibility(View.GONE);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem with json in bip page " + e);
        }
    }

}
