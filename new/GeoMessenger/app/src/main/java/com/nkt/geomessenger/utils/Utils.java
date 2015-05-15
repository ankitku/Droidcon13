package com.nkt.geomessenger.utils;

import com.google.android.gms.maps.model.LatLng;
import com.nkt.geomessenger.GeoMessenger;

/**
 * Created by ankitku on 04/05/15.
 */
public class Utils {

    public static LatLng getCustomerLatLng()
    {
        return new LatLng(GeoMessenger.customerLocation.getLatitude(), GeoMessenger.customerLocation.getLongitude());
    }
}
