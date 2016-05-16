package com.bridge187.corfupages.utilities;

import android.location.Location;

/**
 * Just a singleton to store the devices current location
 */
public final class LocationModel
{

	private static LocationModel instance;
	private Location location;

	public static LocationModel getInstance()
	{
		if (instance == null)
		{
			instance = new LocationModel();
		}
		return instance;
	}

    /**
     * private constructor so we cannot make more than one of these
     */
	private LocationModel() { /* Not instantiable */ }

    /**
     * To retreive the location of the device
     * @return device location
     */
	public Location getLocation()
	{
		return location;
	}

	public void setLocation(Location location)
	{
		this.location = location;
	}

}
