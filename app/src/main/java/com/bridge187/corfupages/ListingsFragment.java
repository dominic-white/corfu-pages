package com.bridge187.corfupages;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bridge187.corfupages.utilities.AppConstants;
import com.bridge187.corfupages.utilities.AppUtilities;
import com.bridge187.corfupages.utilities.ImageCache;
import com.bridge187.corfupages.utilities.LocationModel;
import com.bridge187.corfupages.webclient.WebClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

/**
 * This fragment hold the list of all businesses shown after a search has been perfomed.
 * In fact, the search is instigated from this fragment in the onResume method
 */
public class ListingsFragment extends Fragment implements Observer
{
    /** This adapter interfaces with the data model (from WebClient) and provides views to popuplate the scrolling list */
    private BaseAdapter adapter;

    /** The progress dialogue is visible whilst the search request is being carried out in a background thread */
    private ProgressDialog progressDialog;

    /**T he text at the top of the screen which shows what was searched for */
    private TextView titleText;

    /**This view is made visible when the search return no results */
    private View noResultsText;

    /** A reference to the LayoutInflater used in the adapter for the scrolling list*/
    private LayoutInflater inflater;

    /** Dummy Callback used when the fragment is not attached to any activity,it does nothing */
    private Callbacks callbacks = dummyCallbacks;

    /** This alert dialoge is for when the user does a GPS search, but from another country to where the listings are based */
    private AlertDialog alertDialog;

    /** The single ImageCache which hold all images in the app and deals with memoory managment to prevent Out Of Memory errors */
    private ImageCache imageCache;

    /** The language is set from the device's default locale, and is needed to decide whether to display the Greek or English language from the listing object retreived from the server */
    private AppUtilities.Language language;

    /**
     * Callback for when an item has been selected.
     */
    public interface Callbacks
    {
        void onListingPressed(int position);
    }

    /**
     * Dummy Callback used when the fragment is not attached to any activity,it does nothing
     */
    private static final Callbacks dummyCallbacks = new Callbacks()
    {
        @Override
        public void onListingPressed(int position)
        {
        }
    };

    /**
     * Necessary private constructor
     */
    public ListingsFragment()
    {
    }

    /**
     * Factory method to get an instance of this Fragment
     * @return the ListingsFragment
     */
    public static ListingsFragment newInstance()
    {
        return new ListingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_listing_detail, container, false);
        this.inflater = inflater;
        ListView listView = (ListView)rootView.findViewById(R.id.pic_list);
        titleText = (TextView)rootView.findViewById(R.id.listing_detail);
        adapter = new ListingsAdapter();
        listView.setAdapter(adapter);
        titleText.setText(R.string.loading);

        noResultsText = rootView.findViewById(R.id.listing_detail_no_results);

        noResultsText.setVisibility(View.GONE);
        language = ((MainApplication)this.getActivity().getApplication()).getAppUtilities().getLanguage();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id == -1) {
                    return;
                }
                callbacks.onListingPressed(position);
            }
        });
        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        startSearch();
    }

    public void startSearch()
    {
        imageCache = ((MainApplication)this.getActivity().getApplication()).getImageCache();

        //Below is for in case the ANdroid system has resurrected the Activity after quitting the app, and there is no data.
        //It prevents a crash, and we want fresh data in this situation anyway.
        if (WebClient.getInstance().searchTerms == null)
        {
            this.getActivity().getSupportFragmentManager().popBackStackImmediate();
            return;
        }
        WebClient.WebClientStatus loadingStatus = WebClient.getInstance().getWebClientStatus();
        WebClient.getInstance().addObserver(this);
        //At this point the image list request could be not started, finished or in progress. (onResume could be called again during loading if phone changes orientation shortly after staring the request, so we show the loading dialogue again
        if (loadingStatus == WebClient.WebClientStatus.STATUS_NOT_STARTED || loadingStatus == WebClient.WebClientStatus.STATUS_LOADING)
        {
            progressDialog = ProgressDialog.show(this.getActivity(),  getString(R.string.please_wait), getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    WebClient.getInstance().cancel();
                    if (((MainApplication)ListingsFragment.this.getActivity().getApplication()).getAppUtilities().isTabletLayout())
                    {
                        ListingsFragment.this.getActivity().getSupportFragmentManager().popBackStackImmediate(ListingsFragment.this.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    else
                    {
                        ListingsFragment.this.getActivity().finish();
                    }
                }
            });
            progressDialog.show();

        }
        if (loadingStatus == WebClient.WebClientStatus.STATUS_NOT_STARTED)
        {
            WebClient.getInstance().requestListings();
        }
        else
        {
            refreshList();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //stop listening for changes in the data model
        WebClient.getInstance().deleteObserver(this);
        if (alertDialog != null && alertDialog.isShowing())
        {
            alertDialog.cancel();
        }
    }

    /**
     * This class implements Observer, so this is called when the datamodel (WebClient) changes
     * @param args0 not used
     * @param message not used
    */
    @Override
    public void update(Observable args0, Object message)
    {
        if (progressDialog != null && progressDialog.isShowing())
        {
            try
            {
                progressDialog.dismiss();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if (WebClient.getInstance().getSearchMode() == WebClient.SearchMode.GPS)
        {
            Location location = LocationModel.getInstance().getLocation();
            AppUtilities appUtilities = ((MainApplication)getActivity().getApplication()).getAppUtilities();
            if (!appUtilities.isLocationWarningShown()  && location != null && !appUtilities.isInLocationOfListings(location))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setMessage(R.string.not_correct_location_alert).setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.create().show();
                appUtilities.setLocationWarningShown();
            }
        }
        refreshList();
    }

    /**
     * This method is called upon a data model change (from update() method)
     * It tells the adaptor to refresh its data by calling  adapter.notifyDataSetChanged();
     */
    private void refreshList()
    {
        StringBuilder searchMessage = new StringBuilder(getActivity().getResources().getText(R.string.results));
        String searchTerms = WebClient.getInstance().getSearchTerms();

        //Set the title of the page, above the list, saying what was searched for
        //TODO localise properly
        if (searchTerms == null || searchTerms.length() == 0)
        {
            searchMessage.append(" ").append(getActivity().getResources().getText(R.string.anything));
        }
        else
        {
            searchMessage.append(" ").append(searchTerms);
        }

        titleText.setText(searchMessage);
        noResultsText.setVisibility(View.GONE);
        if (WebClient.getInstance().getWebClientStatus() == WebClient.WebClientStatus.STATUS_ERROR)
        {
            //something went wrong with the request
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListingsFragment.this.getActivity());
            alertDialogBuilder.setMessage(R.string.loading_error).setCancelable(true)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    });
            alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
        else
        {
            if (WebClient.getInstance().getTotalListingsCount() == 0)
            {
                noResultsText.setVisibility(View.VISIBLE);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * The purpose of this adapter it to take data from the data model (WebClient) and provide views to be displayed in the scrollable list
     */
    class ListingsAdapter extends BaseAdapter
    {
        /**
         * The ViewHolder is here for performance, so we don't have to keep finding view by id in the view we return.
         * It is set as a tag on the view for easy retrieval.
         * It eally helps to acheive fast scrolling
         */
        class ViewHolder
        {
            TextView title;
            TextView shortDescription;
            TextView distance;
            TextView categoryLocation;
            TextView loadingText;
            ImageView icon;
            ProgressBar spinner;
        }

        @Override
        public int getCount()
        {
            int size = WebClient.getInstance().getCurrentListingsCount();
            boolean hasMore = size < WebClient.getInstance().getTotalListingsCount();
            return size + (hasMore ? 1 : 0);
        }

        public Object getItem(int arg0)
        {
            return null;
        }

        /**
         *
         * @param position position in list
         * @return the actual id of the business in the online database, provided in the server response
         */
        public long getItemId(int position)
        {
            long id = -1;

            try
            {
                if (position < WebClient.getInstance().getCurrentListingsCount())
                {
                    id = WebClient.getInstance().getData().getJSONArray(AppConstants.RESULT_LISTINGS_ARRAY).getJSONObject(position).getLong("listingId");
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return id;
        }

        /**
         * This takes the JSON data for a listing, and returns the view to be displayed in the scrolling list
         * @param position position in the list
         * @param convertView a view which may be null, provided to be recycled with the new data
         * @param parent the parent ViewGroup
         * @return the View to be dsiplayed for one business in the list
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;

            if (view == null)
            {
                view = inflater.inflate(R.layout.list_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView)view.findViewById(R.id.list_item_title);
                viewHolder.shortDescription = (TextView)view.findViewById(R.id.list_item_short_description);
                viewHolder.distance = (TextView)view.findViewById(R.id.list_distance);
                viewHolder.categoryLocation = (TextView)view.findViewById(R.id.list_category_location);
                viewHolder.icon = (ImageView)view.findViewById(R.id.list_item_image_view);
                viewHolder.loadingText =  (TextView)view.findViewById(R.id.loading_text_view);
                viewHolder.spinner = (ProgressBar)view.findViewById(R.id.list_item_spinner);
                view.setTag(viewHolder);
            }


            ViewHolder viewHolder = (ViewHolder)view.getTag();
            if (position == WebClient.getInstance().getCurrentListingsCount())
            {
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.shortDescription.setVisibility(View.GONE);
                viewHolder.distance.setVisibility(View.GONE);
                viewHolder.categoryLocation.setVisibility(View.GONE);
                viewHolder.icon.setVisibility(View.GONE);

                viewHolder.loadingText.setVisibility(View.VISIBLE);
                viewHolder.spinner.setVisibility(View.VISIBLE);

                WebClient.getInstance().requestNextPage();

                return view;
            }


            viewHolder.title.setVisibility(View.VISIBLE);
            viewHolder.shortDescription.setVisibility(View.VISIBLE);
            viewHolder.distance.setVisibility(View.VISIBLE);
            viewHolder.categoryLocation.setVisibility(View.VISIBLE);
            viewHolder.icon.setVisibility(View.VISIBLE);

            viewHolder.loadingText.setVisibility(View.GONE);
            viewHolder.spinner.setVisibility(View.GONE);

            try
            {
                JSONObject thisListing = WebClient.getInstance().getData().getJSONArray(AppConstants.RESULT_LISTINGS_ARRAY).getJSONObject(position);
                viewHolder.icon.setTag(thisListing.getLong("listingId"));
                StringBuilder categoryLocation = new StringBuilder();
                switch (language)
                {
                    case ENGLISH:
                        viewHolder.title.setText(thisListing.getString("title_en"));
                        viewHolder.shortDescription.setText(thisListing.getString("shortDescription_en"));
                        categoryLocation.append(thisListing.getString("displayCategory_en")).append(" ")
                                .append(getText(R.string.near)).append(" ")
                                .append(thisListing.getString("displayLocation_en"));
                        break;
                    case GREEK:
                        viewHolder.title.setText(thisListing.getString("title_gr"));
                        viewHolder.shortDescription.setText(thisListing.getString("shortDescription_gr"));
                        categoryLocation.append(thisListing.getString("displayCategory_gr")).append(" ")
                                .append(getText(R.string.near)).append(" ")
                                .append(thisListing.getString("displayLocation_gr"));
                        break;
                }

                viewHolder.categoryLocation.setText(categoryLocation.toString());

                String thumbUrl = thisListing.getString("thumbnail");

                AppUtilities utils = ((MainApplication)ListingsFragment.this.getActivity().getApplication()).getAppUtilities();

                if (thumbUrl.length() > 0)
                {
                    imageCache.loadBitmap(thumbUrl, viewHolder.icon, false, thisListing.getLong("listingId"));
                }
                else
                {
                    viewHolder.icon.setImageResource(utils.getIconId(thisListing.getString("category")));
                }

                if (WebClient.getInstance().getSearchMode() == WebClient.SearchMode.GPS)
                {
                    double latitude = 0;
                    double longitude = 0;

                    try
                    {
                        latitude = Double.parseDouble(thisListing.getString("lat"));
                        longitude = Double.parseDouble(thisListing.getString("lon"));
                    }
                    catch (NumberFormatException e)
                    {
                        e.printStackTrace();
                    }

                    viewHolder.distance.setText("");

                    Location location = LocationModel.getInstance().getLocation();
                    if (location != null)
                    {
                        String dist = utils.getDistanceString(ListingsFragment.this.getResources(), latitude, longitude, location);
                        viewHolder.distance.setText(dist);
                    }

                }
                else
                {
                    viewHolder.distance.setText("");
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
                viewHolder.title.setText(e.toString());
                viewHolder.shortDescription.setText(null);
                viewHolder.distance.setText(null);
                viewHolder.categoryLocation.setText(null);
                viewHolder.icon.setImageBitmap(null);

            }

            return view;
        }

    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (!(context instanceof Callbacks))
        {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        callbacks = dummyCallbacks;
        if (progressDialog != null && progressDialog.isShowing())
        {
            try
            {
                progressDialog.dismiss();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //do nothing, it means dialogue already dismissed, if fact this try / catch is probably not needed since synchronizing this method.
            }
        }
    }
}
