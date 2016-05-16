package com.bridge187.corfupages.webclient;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class does the actual http request to the server for a json object containing all the listings, in a background thread
 */
class SearchListHttpRequest extends AsyncTask<String, Integer, Object>
{
    /** An interface implemented by WebClient to communicate with the SearchListHttpRequest */
	private final SearchListRequestHandler callback;

    /**
     * Contructore
     * @param callback A class implementing SearchListRequestHandler, which wants to recieve a callback when the async http request is complete
     *                 In this case, that class is WebClient
     */
	public SearchListHttpRequest(SearchListRequestHandler callback)
	{
		this.callback = callback;
	}

    /**
     * Starts the http request for listings
     * @param url the url (including GET parameters) of the Coru Pages search api
     */
	public void start(String url)
	{
		execute(url);
	}

    /**
     * The overridden method containing the actual http request, to be run on a background thread
     * @param params an array containing onle element, the serach url (including parameter)
     * @return A JSONObject containing the results (which will have to be cast to JSONObject) or null if the connection failed for any reason
     */
	@Override
	protected Object doInBackground(String... params)
	{
		HttpURLConnection con = null;
		Object data;
		try
		{

			String urlString = params[0];
			URL url = new URL(urlString);

			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			con.setDoInput(true);
			con.connect();

			if (con.getResponseCode() != 200)
			{
				return null;
			}

			InputStream input = con.getInputStream();
			String stringFromInputStream = createStringFromInputStream(input);

			data = new JSONObject(stringFromInputStream);
			con.disconnect();
			return data;
		}
		catch (Exception e)
		{
            e.printStackTrace();
			try
			{
				if (con != null)
				{
					con.disconnect();
				}
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}

		return null;
	}

    /**
     * Overridden method called after the background task is complete
     * @param data A JSONObject containing the results (which will have to be cast to JSONObject) or null if the connection failed for any reason.
     */
	@Override
	protected void onPostExecute(Object data)
	{
        JSONObject json;
		if (data != null)
		{
			try
			{
				json = (JSONObject)data;
                callback.searchRequestCompleted(json);
			}
			catch (Exception e)
			{
                callback.searchRequestFailed();
			}
		}
        else
        {
            callback.searchRequestFailed();
        }
	}

    /**
     * Take an input stream and makes a string, which can in turn be used to create a JSONObject
     * @param inputStream the input stream from the connection in the background thread
     * @return String representation of the input stream
     */
	private static String createStringFromInputStream(final InputStream inputStream)
	{ 
		final char[] buffer = new char[4096]; 
		final StringBuilder out = new StringBuilder(); 
		try 
		{ 
			final Reader in = new InputStreamReader(inputStream, "UTF-8");
			try 
			{ 
				for (;;) 
				{ 
					int rsz = in.read(buffer, 0, buffer.length); 
					if (rsz < 0) 
						break; 
					out.append(buffer, 0, rsz); 
				} 
			} 
			catch (IOException ex) 
			{ 
				//do nothing, request will just fail
				ex.printStackTrace();
			} 
			finally 
			{ 
				try { in.close(); 
				} 
				catch (IOException ex) 
				{ 
					//do nothing, request will just fail
					ex.printStackTrace();
				} 
			} 
		} 
		catch (UnsupportedEncodingException ex) 
		{ 
			//do nothing, request will just fail
			ex.printStackTrace();
		} 
		return out.toString(); 
	} 

}	
