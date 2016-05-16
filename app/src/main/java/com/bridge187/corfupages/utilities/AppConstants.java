package com.bridge187.corfupages.utilities;

import android.util.SparseArray;

import com.bridge187.corfupages.R;


public class AppConstants
{
    /** The url of the Corfu Pages search api */
    public final static String url = "https://corfupages.com/search/search.php";

    /** The JSON key for the array of results in the response, used twice so it is a constant here */
    public final static String RESULT_LISTINGS_ARRAY = "listings";

    /** When the is no image for a business on the server we use an icon from the app's resources folder. This array maps category ids to resource image ids */
    public final static SparseArray<Integer> CATEGORY_ICONS;

    static
    {
        CATEGORY_ICONS = new SparseArray<>();

        CATEGORY_ICONS.put(0, R.drawable.cat_restaurant);
        CATEGORY_ICONS.put(1, R.drawable.cat_bars);
        CATEGORY_ICONS.put(2, R.drawable.cat_holiday_appartments);
        CATEGORY_ICONS.put(3, R.drawable.cat_hotels);
        CATEGORY_ICONS.put(4, R.drawable.cat_cafes);
        CATEGORY_ICONS.put(5, R.drawable.cat_nightclubs);
        CATEGORY_ICONS.put(6, R.drawable.cat_general_shops);
        CATEGORY_ICONS.put(8, R.drawable.cat_tourist_attractions);
        CATEGORY_ICONS.put(9, R.drawable.cat_health_centers);
        CATEGORY_ICONS.put(10, R.drawable.cat_car_hire);
        CATEGORY_ICONS.put(11, R.drawable.cat_bike_hire);
        CATEGORY_ICONS.put(12, R.drawable.cat_banks);
        CATEGORY_ICONS.put(13, R.drawable.cat_post_office);
        CATEGORY_ICONS.put(14, R.drawable.cat_taxi);
        CATEGORY_ICONS.put(15, R.drawable.cat_petrol_station);
        CATEGORY_ICONS.put(16, R.drawable.cat_vehicle_repair);
        CATEGORY_ICONS.put(18, R.drawable.cat_kiosks);
        CATEGORY_ICONS.put(19, R.drawable.cat_fast_food);
        CATEGORY_ICONS.put(20, R.drawable.cat_tattoos);
        CATEGORY_ICONS.put(21, R.drawable.cat_clothes_shops);
        CATEGORY_ICONS.put(22, R.drawable.cat_engineering);
        CATEGORY_ICONS.put(23, R.drawable.cat_business_services);
        CATEGORY_ICONS.put(24, R.drawable.cat_internet_services);
        CATEGORY_ICONS.put(25, R.drawable.cat_legal_services);
        CATEGORY_ICONS.put(26, R.drawable.cat_doctors);
        CATEGORY_ICONS.put(27, R.drawable.cat_dentist);
        CATEGORY_ICONS.put(28, R.drawable.cat_child_minders);
        CATEGORY_ICONS.put(29, R.drawable.cat_tools);
        CATEGORY_ICONS.put(30, R.drawable.cat_garden_centres);
        CATEGORY_ICONS.put(31, R.drawable.cat_super_markets);
        CATEGORY_ICONS.put(32, R.drawable.cat_tourist_shops);
        CATEGORY_ICONS.put(33, R.drawable.cat_boat_hire);
        CATEGORY_ICONS.put(34, R.drawable.cat_bakeries);
        CATEGORY_ICONS.put(35, R.drawable.cat_travel_agencies);
        CATEGORY_ICONS.put(36, R.drawable.cat_camping);
        CATEGORY_ICONS.put(37, R.drawable.cat_vets);
        CATEGORY_ICONS.put(38, R.drawable.cat_pet_shops);
        CATEGORY_ICONS.put(39, R.drawable.cat_hairdressing);
        CATEGORY_ICONS.put(40, R.drawable.cat_estate_agents);
        CATEGORY_ICONS.put(41, R.drawable.cat_pharmacy);
        CATEGORY_ICONS.put(42, R.drawable.cat_home_and_building);
        CATEGORY_ICONS.put(43, R.drawable.cat_butchers);
        CATEGORY_ICONS.put(44, R.drawable.cat_driving_schools);

        CATEGORY_ICONS.put(100, R.drawable.cat_other);
    }
}