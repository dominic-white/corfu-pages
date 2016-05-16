package com.bridge187.corfupages;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * AS of now, the info fragment merely holds the Corfu Pages map logo, as a placeholder for tablets before an initial search has been done.
 */
public class InfoFragment extends Fragment
{

    /** Factory method to get an instance */
    public static InfoFragment newInstance()
    {
        return new InfoFragment();
    }

    /** Necessary empty constructor */
    public InfoFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

}
