package com.bridge187.corfupages.utilities;

import android.content.res.Resources;
import android.location.Location;
import com.bridge187.corfupages.R;
import java.util.Locale;

public class AppUtilities
{

    public enum Language {ENGLISH, GREEK}

    //** The language variable is set according to the device locale. Both Greek and English text is returned from the server, this is used to decise what to display to the user *//
    private Language language = Language.GREEK;

    /** When tabletLayout is true the main activity hosts two fragments, the menu and the results. When it is false he menu is dismissed upon the search buton being pressed*/
    private boolean tabletLayout;

    /** the location warning is shown on the first search if the device is located outside of Corfu */
    private boolean locationWarningShown;

    /**
     * Used to display the distance of a building from the device in the listingss page
     * @param latitude business latitiude from server response
     * @param longitude business longitude from server response
     * @param location device location
     * @return distance in meters
     */
    private int getDistance(double latitude, double longitude, Location location)
    {
        float[] results = new float[3];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), results);
        return (int)results[0];
    }

    /**
     * Returns a string representation of the distance to a business
     * @param res Resources fom Application Context
     * @param latitude business latitiude from server response
     * @param longitude business longitude from server response
     * @param location device location
     * @return string representation of distance
     */
    public String getDistanceString(Resources res, double latitude, double longitude, Location location)
    {

        int meters = getDistance(latitude, longitude, location);

        if (meters < 1000)
        {
            int meterString = (meters == 1) ? R.string.meter_single : R.string.meter_plural;
            return Integer.toString(meters).concat(" ").concat(res.getString(meterString));
        }
        else if (meters < 10000)
        {
            meters = (meters / 100) * 100;
            double km = meters / 1000d;
            if (km == 1)
            {
                return "1 ".concat(res.getString(R.string.kilometer_single));
            }
            String kmRepString = (km == (int)km)? Integer.toString((int) km): Double.toString(km);
            return kmRepString.concat(" ").concat(res.getString(R.string.kilometer_plural));
        }
        else
        {
            int km = meters / 1000;
            return Integer.toString(km).concat(" ").concat(res.getString(R.string.kilometer_plural));
        }
    }

    /**
     * true if app is run on a large screen device, where we use a dual pane layout
     * @return true for a large screen device
     */
    public boolean isTabletLayout()
    {
        return tabletLayout;
    }

    /**
     * Call this method to set the app to display a two pane layout for large screen
     */
    public void setTabletLayout()
    {
        tabletLayout = true;
    }

    /**
     * Returns the language to be used to decide whether to display Greek or English text
     * @return Language.GREEK or Language.ENGLISH
     */
    public Language getLanguage()
    {
        return language;
    }

    /**
     * Set the language in which we display results
     * @param locale the device's locale
     */
    public void setLanguage(Locale locale)
    {
        if (locale.getLanguage().equals("el"))
        {
            language = Language.GREEK;
        }
        else
        {
            language = Language.ENGLISH;
        }
    }

    /**
     * Returns the resources id for an icon to be displayed if the server does not return a thumbnail url
     * @param category category id (from server, part of the response data)
     * @return resourced id
     */
    public int getIconId(String category)
    {
        if (category == null || category.length() == 0)
        {
            return R.drawable.cat_other;
        }
        try
        {
            int categoryInt = Integer.parseInt(category);

            return AppConstants.CATEGORY_ICONS.get(categoryInt, R.drawable.cat_other);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
            return R.drawable.cat_other;
        }
    }

    /**
     * A warning is shown once if the user does an "around Me" search when not in Corfu
     * @return true if the warning has been shown.
     */
    public boolean isLocationWarningShown()
    {
        return locationWarningShown;
    }

    /**
     * A warning is shown once if the user does an "around Me" search when not in Corfu, and this method is called to store that data.
     */
    public void setLocationWarningShown()
    {
        locationWarningShown = true;
    }

    /**
     * To check if device is in Corfu, for the sake of location based searches.
     * @param location the device location
     * @return true if the device location is in Corfu
     */
    public boolean isInLocationOfListings(Location location)
    {
        //for Corfu
        //tl 39.837965, 19.545314
        //br 39.115041, 20.267665

        return (location.getLatitude() > 39.115041 && location.getLatitude() < 39.837965  && location.getLongitude() > 19.545314 && location.getLongitude() < 20.2676654);
    }
}