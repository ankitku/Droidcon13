package com.nkt.geomessenger.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.nkt.geomessenger.GeoMessenger;
import com.nkt.geomessenger.activity.R;
import com.nkt.geomessenger.utils.Utils;
import com.software.shell.fab.ActionButton;

/**
 * Created by ankitku on 04/05/15.
 */
public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private ActionButton actionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_map, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        actionButton = (ActionButton) v.findViewById(R.id.action_button);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setBuildingsEnabled(true);

        return v;
    }

    public void centerMap()
    {
        if( GeoMessenger.customerLocation != null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Utils.getCustomerLatLng(),16));
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        centerMap();
        actionButton.playShowAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}