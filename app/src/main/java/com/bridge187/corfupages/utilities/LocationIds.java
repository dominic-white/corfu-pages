package com.bridge187.corfupages.utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Used by the drop down list of locations on the menu screen
 */
public class LocationIds
{
    private final List<LocationIdentifier> locations = new ArrayList<>();
    public List<LocationIdentifier> getLocations()
    {
        return locations;
    }

    public class LocationIdentifier
    {
        private final int id;
        private final String englishName;
        private final String greekName;

        public int getId()
        {
            return id;
        }

        public String getGreekName()
        {
            return greekName;
        }

        public String getEnglishName()
        {
            return englishName;
        }

        public LocationIdentifier(int id, String englishName, String greekName)
        {
            this.id = id;
            this.englishName = englishName;
            this.greekName = greekName;
        }
    }

    /**
     * The ids of the locations match the ids of the locations on the server's database, and are sent with a request using the loc parameter.
     * The names are just for display to the user.
     */
    public LocationIds()
    {
        locations.add(new LocationIdentifier(-1, "Around Me", "Γυρω μου"));
        locations.add(new LocationIdentifier(0, "Lefkimmi, Kavos and the South",  "Δήμος Λευκιμμαίων"));
        locations.add(new LocationIdentifier(1, "Lake Korrision and St Georges South", "Δήμος Κορισσίων"));
        locations.add(new LocationIdentifier(2, "Meletieon, Messonghi and Moraitika", "Δήμος Μελιτειέων"));
        locations.add(new LocationIdentifier(3, "Achilion, Benitses",  "Δήμος Αχιλλείων"));
        locations.add(new LocationIdentifier(4, "Parelion, Pelekas, Glyfada, Agios Gordis", "Δήμος Παρελίων"));
        locations.add(new LocationIdentifier(5, "Corfu Town Area",  "Δήμος Κερκυραίων"));
        locations.add(new LocationIdentifier(6, "Paleokastrita area", "Δήμος Παλαιοκαστριτών"));
        locations.add(new LocationIdentifier(7, "Faiakon, Dassia, Ipsos, Glyfa, Barbati", "Δήμος Φαιάκων" ));
        locations.add(new LocationIdentifier(8, "Agios Georgios North, (Pagon) ", "Δήμος Αγίου Γεωργίου"));
        locations.add(new LocationIdentifier(9, "Esperion, Sidari, Agios Stefanos", "Δήμος Εσπερίων"));
        locations.add(new LocationIdentifier(10, "Thinalion, Roda, Acharavi",  "Δήμος Θιναλίου"));
        locations.add(new LocationIdentifier(11, "Kassiopi, Kouloura, Kalami",  "Δήμος Κασσωπαίων"));
    }














}
