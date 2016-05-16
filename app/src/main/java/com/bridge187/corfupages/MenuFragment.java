package com.bridge187.corfupages;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.bridge187.corfupages.utilities.AppUtilities;
import com.bridge187.corfupages.utilities.LocationIds;
import com.bridge187.corfupages.webclient.WebClient;

/**
 * This fragments contains the search box and some extra info.
 * It constitutes the whole first screen on phone layouts, and the omnipresent left pane on tabley layouts
 */
public class MenuFragment extends Fragment
{
    /** Dummy Callback used when the fragment is not attached to any activity,it does nothing */
    private Callbacks callbacks = dummyCallbacks;

    /** where the user types what they want to find, ie pubs, restaurants etc */
    private AutoCompleteTextView searchText;

    /** The language is set from the device's default locale, and is needed to decide whether to display the Greek or English language for the location autosuggestions */
    private AppUtilities.Language language;

    /** location id is used when the user does a search by selecting a location from the drop down list. It matches the broad locations in the online database */
    private int locationId = -1;

    /**the app version, used as a parameter in the http request */
    private String version;

    /** saved from the onCreate method and resued in the location adapter */
    private LayoutInflater inflater;

    /** Dummy Callback used when the fragment is not attached to any activity,it does nothing */
    public interface Callbacks
    {
        void onSearchPressed(WebClient.SearchMode searchMode, String searchTerms, int loc);
    }

    /** Dummy Callback used when the fragment is not attached to any activity,it does nothing */
    private static final Callbacks dummyCallbacks = new Callbacks()
    {
        @Override
        public void onSearchPressed(WebClient.SearchMode searchMode,  String searchTerms, int loc)
        {
        }
    };

    /** necessary empty constructor */
    public MenuFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.menu_layout, container, false);
        this.inflater = inflater;
        Button searchButton = (Button)rootView.findViewById(R.id.searchButton);
        searchText = (AutoCompleteTextView)rootView.findViewById(R.id.searchTerm);
        searchText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        String[] suggestions = getActivity().getResources().getStringArray(R.array.suggestions_terms);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.autocomplete_item, suggestions);
        searchText.setAdapter(adapter);
        searchText.setThreshold(1);


        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    return true;
                }
                return false;
            }
        });

        Spinner locationSelector = (Spinner)rootView.findViewById(R.id.locationTerm);
        LocationAdapter locationAdapter = new  LocationAdapter();

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSelector.setAdapter(locationAdapter);
        locationSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                locationId = (int) id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener()
        {
            //lastClickTime is used to overcome the famous double click bug, where we get twon new fragments when the user does a double click on the search button
            private long lastClickTime = 0;

            @Override
            public void onClick(View view)
            {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime > (lastClickTime + 750))
                {
                    lastClickTime = currentTime;
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                    String terms = searchText.getText().toString().trim();
                    WebClient.SearchMode mode = (locationId == -1) ? WebClient.SearchMode.GPS : WebClient.SearchMode.LOCATION_ID;
                    WebClient.getInstance().setVersion(version);
                    callbacks.onSearchPressed(mode, terms, locationId);
                }

            }
        });

        try
        {
            PackageInfo pInfo = this.getActivity().getPackageManager().getPackageInfo(this.getActivity().getPackageName(), 0);
            version = this.getString(R.string.version) + pInfo.versionName;
        }

        catch (PackageManager.NameNotFoundException e)
        {
            version = "version not found";
        }
        ((TextView)rootView.findViewById(R.id.version)).setText(version);

        Button webButton = (Button)rootView.findViewById(R.id.web_link);
        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setData(Uri.parse("https://corfupages.com"));
                MenuFragment.this.getActivity().getApplicationContext().startActivity(intent);
            }
        });

        //just a credit to the peoplwe who privided the icons on the listing page
        Button iconButton = (Button)rootView.findViewById(R.id.icon_link);
        iconButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.setData(Uri.parse("http://icons8.com"));
                MenuFragment.this.getActivity().getApplicationContext().startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        // Activities containing this fragment must implement its callbacks.
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
    }

    @Override
    public void onResume()
    {
        super.onResume();
        language = ((MainApplication)this.getActivity().getApplication()).getAppUtilities().getLanguage();
    }

    /**
     * for the drop down list of locations
     */
    class LocationAdapter extends BaseAdapter
    {
        private final LocationIds locationIds = new LocationIds();

        public LocationAdapter()
        {
        }

        @Override
        public int getCount()
        {
            return locationIds.getLocations().size();
        }


        @Override
        public Object getItem(int position)
        {
            return locationIds.getLocations().get(position);
        }


        @Override
        public long getItemId(int position)
        {
            return locationIds.getLocations().get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;

            if (view == null)
            {
                view = inflater.inflate(R.layout.autosuggest_item, parent, false);
            }

            LocationIds.LocationIdentifier locationIdentifier = locationIds.getLocations().get(position);
            TextView textView = (TextView)view.findViewById(R.id.autosuggest_item_title);
            switch (language)
            {
                case ENGLISH:
                    textView.setText(locationIdentifier.getEnglishName());
                    break;
                case GREEK:
                    textView.setText(locationIdentifier.getGreekName());
                    break;
            }

            return view;
        }
    }
}
