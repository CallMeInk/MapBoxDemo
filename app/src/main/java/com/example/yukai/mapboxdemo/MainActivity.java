package com.example.yukai.mapboxdemo;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yukai.mapboxdemo.HttpUtil.HttpRequestUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LatLng latLng = new LatLng(39.994827, 116.457092);
    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private Handler mHandler = new Handler();
    private ListView mListView;
    private BaseAdapter mAdapter;
    private final String[] mockData = {"update camera(or called update center)",
    "update center with animation(2000ms)",
    "add marker",
    "clear marker",
    "marker moving for newer than Android 25.0.0",
    "draw polyLine",
    "draw polyRegion"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        requestPermission();
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setStyleUrl(Style.MAPBOX_STREETS);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                Log.e("yk", "onMapReady");
                if (mapboxMap == null){
                    return;
                }
                mMapboxMap = mapboxMap;
            }
        });
        mListView = findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter = new MyAdapter());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                handleItemClick(i);
            }
        });
    }

    private void handleItemClick(int position){
        switch (position){
            case 0:
                updateMapCenter();
                break;
            case 1:
                updateMapCenterWithAnimation();
                break;
            case 2:
                addMarker();
                break;
            case 3:
                clearMarker();
                break;
            case 4:
                markerMoving();
                break;
            case 5:
                drawpolyLine();
                break;
            case 6:
                drawpolyRegion();
                break;
            case 7:
                sendPoiService();
                break;
            default:
                return;
        }
    }

    private void sendPoiService(){
        final String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/coffee.json?&proximity=-77.032,38.912&limit=10&types=poi&access_token=" + getString(R.string.access_token);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = HttpRequestUtil.getXpath(url);
                JsonArray jsonArray = (JsonArray) jsonObject.get("features");
                int size = jsonArray.size();
                for (int i = 0;i < size;i++) {
                        JsonElement jsonElement = jsonArray.get(i);
                        //jsonElement.
                }
            }
        }).start();
    }

    private void drawpolyLine(){
        List<LatLng> polyLine = new ArrayList<>();
        polyLine.add(new LatLng(39.994827, 117.457092));
        polyLine.add(new LatLng(42.994827, 115.457092));
        polyLine.add(new LatLng(41.994827, 122.457092));
        polyLine.add(new LatLng(45.994827, 121.457092));
        polyLine.add(new LatLng(36.994827, 126.457092));
        polyLine.add(new LatLng(32.994827, 119.457092));
        mMapboxMap.addPolyline(new PolylineOptions()
        .addAll(polyLine)
        .color(ContextCompat.getColor(MainActivity.this, R.color.mapbox_blue))
        .alpha(0.5f)
        .width(2));
        mMapboxMap.setZoom(5);
    }

    private void drawpolyRegion(){
        List<LatLng> polyLine = new ArrayList<>();
        polyLine.add(new LatLng(39.994827, 117.457092));
        polyLine.add(new LatLng(42.994827, 115.457092));
        polyLine.add(new LatLng(41.994827, 122.457092));
        polyLine.add(new LatLng(45.994827, 121.457092));
        polyLine.add(new LatLng(36.994827, 126.457092));
        polyLine.add(new LatLng(32.994827, 119.457092));
        mMapboxMap.addPolygon(new PolygonOptions()
        .addAll(polyLine)
        .fillColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent))
        .alpha(0.5f)
        .strokeColor(ContextCompat.getColor(MainActivity.this, R.color.mapbox_blue)));

    }

    private void markerMoving(){
        final Marker marker = mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("moving")
                .snippet("moving snippet"));
        mMapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                ValueAnimator valueAnimator = ObjectAnimator.ofObject(marker, "position", new LatLngEvaluator(), marker.getPosition(), latLng);
                valueAnimator.setDuration(2000);
                valueAnimator.start();
            }
        });
    }

    private void clearMarker(){
        for (Marker marker : mMapboxMap.getMarkers()){
            mMapboxMap.removeMarker(marker);
        }
    }

    private void addMarker(){
        LatLng latLng1 = new LatLng(39.994827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng1)
                .title("title")
                .snippet("snippet"));

        LatLng latLng2 = new LatLng(39.995827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng2)
                .title("title")
                .snippet("snippet"));

        LatLng latLng3 = new LatLng(39.996827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng3)
                .title("title")
                .snippet("snippet"));

        LatLng latLng4 = new LatLng(39.997827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng4)
                .title("title")
                .snippet("snippet"));

        LatLng latLng5 = new LatLng(39.998827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng5)
                .title("title")
                .snippet("snippet"));

        LatLng latLng6 = new LatLng(39.999827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng6)
                .title("title")
                .snippet("snippet"));

        LatLng latLng7 = new LatLng(39.993827, 116.457092);
        mMapboxMap.addMarker(new MarkerOptions()
                .position(latLng7)
                .title("title")
                .snippet("snippet"));

    }

    private void updateMapCenter(){
        mMapboxMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void updateMapCenterWithAnimation(){
        mMapboxMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void log(String logTrace){
        Log.e("yk", logTrace);
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 3);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 4);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 5);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 6);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mockData.length;
        }

        @Override
        public Object getItem(int i) {
            return mockData[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_view_item, viewGroup, false);
            TextView textView = view1.findViewById(R.id.item_tv);
            textView.setText(mockData[i]);
            return view1;
        }
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng>{
        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() + (endValue.getLatitude() - startValue.getLatitude()) * fraction);
            latLng.setLongitude(startValue.getLongitude() + (endValue.getLongitude() - startValue.getLongitude()) * fraction);
            return latLng;
        }
    }
}
