package com.bridge187.corfupages.webclient;

import org.json.JSONObject;

/**
 * interface to facilitate communication between the SearchListHttpRequest and whatever wnats to use it (in our cas the WebClient)
 */
interface SearchListRequestHandler
{
	void searchRequestFailed();
	void searchRequestCompleted(JSONObject json);
}
