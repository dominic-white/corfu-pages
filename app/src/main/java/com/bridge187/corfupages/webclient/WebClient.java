package com.bridge187.corfupages.webclient;

import android.location.Location;
import com.bridge187.corfupages.utilities.AppConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Observable;

/**
 * The main data model for the app.
 * It basically contains a JSON Object representing the search results, some status information (in webClientStatus),
 * the current page, the total number of listings on the server for the latest search and the number of listings downloaded so far.
 * Currently it is a Singleton, though that could change
 */
public class WebClient extends Observable implements SearchListRequestHandler
{
    /** enum containing status / progress data about the search request */
    public enum WebClientStatus
    {
        STATUS_NOT_STARTED,
        STATUS_LOADING,
        STATUS_LOADED,
        STATUS_ERROR
    }

    /** enum to represent whether we are doing an "around me" search or a search based on a pre-define location id */
    public enum SearchMode
    {
        GPS,
        LOCATION_ID
    }

    /** what we are actually searching for (ie bars, restaurants, mechanics etc) */
    public String searchTerms = null;

    /** the single instance of this WebClient*/
	private static WebClient INSTANCE;

    /** an enum used to indicate progress, success or failure of the request */
	private WebClientStatus webClientStatus;

    /** the actual data downloaded, in the form of a JSONObject */
	private JSONObject data;

    /** the location of the device */
	private Location location;

    /** the location id of where we want to search (matching location ids on the server database, see server api documentation */
	private int locationId;

    /** an object representing an AsynTask to do the actuall http request in a background thread*/
	private SearchListHttpRequest searchListHttpRequest;

    /** to handle pagination, this starts at 0 when the serach button is pressed, and increments when the user scrolls down the scree */
    private int currentPage;

    /** returned from the server as part of the JSON object, this represents the number of listings the server has for a search (not just the first page) */
    private int totalListingsCount;

    /** how many listings have allready be downloaded for a particular search */
    private int currentListingsCount;

    /** The position in the data JSONArray of the currently displayed bip */
    private int currentBipShowing;

    /** app version, for future use by api */
	private String version;

    /** GPS around be search or based on a prese location */
    private SearchMode searchMode;

    /**
     * Set the app version to be sent as part of the request
     * @param version the app vesion
     */
	public void setVersion(String version)
	{
		this.version = version;
	}

    /**
     * @return The position in the data JSONArray of the currently displayed bip
     */
	public int getCurrentBipShowing()
	{
		return currentBipShowing;
	}

    /**
     * @param currentBipShowing The position in the data JSONArray of the currently displayed bip
     */
	public void setCurrentBipShowing(int currentBipShowing)
	{
		this.currentBipShowing = currentBipShowing;
	}

    /**
     * Set according to whether we want to do an "around me" search, or a search by a pre-defined location
     * @param searchMode SearchMode enum (GPS or LOCATION_ID)
     */
	public void setSearchMode(SearchMode searchMode)
	{
		this.searchMode = searchMode;
	}

    /**
     * @return Whether we want to do an "around me" search, or a search by a pre-defined location (GPS or LOCATION_ID)
     */
	public SearchMode getSearchMode()
	{
		return searchMode;
	}

    /**
     * @return What the user actually searched for (i.e hotels, restaurants etc)
     */
	public String getSearchTerms()
	{
		return searchTerms == null ? "" : searchTerms.trim();
	}

    /**
     * Set what the user actually wants to search for (i.e hotels, restaurants etc)
     * @param searchTerms the search term entered by the user
     */
	public void setSearchTerms(String searchTerms)
	{
		this.searchTerms = searchTerms;
	}

    /**
     * Used for GPS "around me" searches
     * @param location the location of the device
     */
	public void setLocation(Location location)
	{
		this.location = location;
	}

    /**
     * Used when a user selects a location from the drop-down list
     * @param locationId the id of the location (must match the id on the server's database for broad locations (see api docs)
     */
	public void setLocationId(int locationId)
	{
		this.locationId = locationId;
	}

    /**
     * private Singleton constructor
     */
	private WebClient()
	{
		webClientStatus = WebClientStatus.STATUS_NOT_STARTED;
	}

    /**
     * Used to get the single instance of this class
     * @return a WebClient object
     */
	public static synchronized WebClient getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new WebClient();
		}
		return INSTANCE;
	}

    /** stop searching! */
	public void cancel()
	{
		webClientStatus = WebClientStatus.STATUS_NOT_STARTED;
		if (searchListHttpRequest != null )
		{
			searchListHttpRequest.cancel(true);
		}
	}

    /**
     * clear all data and set status back to STATUS_NOT_STARTED
     */
	public void reset()
	{
		data = null;
		webClientStatus = WebClientStatus.STATUS_NOT_STARTED;
		currentPage = 0;
		totalListingsCount = 0;
		currentListingsCount = 0;
	}

    /**
     * called when search button is pressed
     */
	public void requestListings()
	{
		requestListings(0);
	}

    /**
     * called when the user scrolls to the bottom of the screen and there are more listings to be had
     */
    public void requestNextPage()
    {
        requestListings(currentPage + 1);
    }

    /**
     * called for all requests, either from requestListings() method when the user presses the search button or for pagination when the user scrolls to the end.
     * @param page the page number, staring with 0 for the first page
     */
	private void requestListings(int page)
	{
        currentPage = page;
		webClientStatus = WebClientStatus.STATUS_LOADING;

		StringBuilder url = new StringBuilder(AppConstants.url);
		url.append("?term=");
		try
		{
			url.append(URLEncoder.encode(searchTerms, "UTF-8"));
		}
		catch(UnsupportedEncodingException e)
		{
            //we will allways have UTF-8 encoding available, this exception should not happen
			e.printStackTrace();
		}

		url.append("&pagesize=20");
		url.append("&page=");
		url.append(Integer.toString(page));

		switch (searchMode)
		{
			case GPS:
				url.append("&uselonlat=y");
				url.append("&lon=");
				url.append(location.getLongitude());
				url.append("&lat=");
				url.append(location.getLatitude());
				break;

			case LOCATION_ID:

				url.append("&loc=broad");
				url.append(locationId);
				break;
		}

        url.append("&mobversion=");
        url.append(version);

        searchListHttpRequest = new SearchListHttpRequest(this);
		searchListHttpRequest.start(url.toString());
	}

    /**
     * called by UI when we want to dispaly data
     * @return all the data from the server in a JSON object
     */
	public JSONObject getData()
	{
		return data ;
	}

    /**
     * called when http request fails for any reason at all
     */
	@Override
	public void searchRequestFailed()
	{
		webClientStatus = WebClientStatus.STATUS_ERROR;
		this.setChanged();
		this.notifyObservers();
	}

    /**
     * called when we have done a successful search request and have something to show the user
     * @param newData the JSONObject returned from the server
     */
	@Override
	public void searchRequestCompleted(JSONObject newData)
	{
		webClientStatus = WebClientStatus.STATUS_LOADED;
        if (currentPage == 0)
        {
            this.data = newData;
        }

        else
        {
            try
            {
                JSONArray oldListings = this.data.getJSONArray(AppConstants.RESULT_LISTINGS_ARRAY);
                JSONArray newListings = newData.getJSONArray(AppConstants.RESULT_LISTINGS_ARRAY);

                for (int i = 0; i < newListings.length(); i++)
                {
                    oldListings.put(newListings.getJSONObject(i));
                }

                this.data.remove(AppConstants.RESULT_LISTINGS_ARRAY);
                this.data.put(AppConstants.RESULT_LISTINGS_ARRAY, oldListings);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }


        try
        {
            totalListingsCount = data.getInt("numberOfResults");
            currentListingsCount = data.getJSONArray(AppConstants.RESULT_LISTINGS_ARRAY).length();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        this.setChanged();
		this.notifyObservers();

	}

    /**
     * @return information about progress, success / failure of the search request
     */
	public WebClientStatus getWebClientStatus()
	{
		return webClientStatus;
	}

    /**
     * set status to STATUS_NOT_STARTED
     */
	public void resetWebClientStatus()
	{
		webClientStatus = WebClientStatus.STATUS_NOT_STARTED;
	}

    /**
     * @return how many listings the server has for the latest search (not how many have been returned, but how many the are
     */
    public int getTotalListingsCount()
    {
        return totalListingsCount;
    }

    /**
     * @return how many listings have been returned from the server for this search and are in memory now.
     */
    public int getCurrentListingsCount()
    {
        return currentListingsCount;
    }

}
