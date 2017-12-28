package com.example.yukai.mapboxdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.yukai.mapboxdemo.HttpUtil.HttpRequestUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yukai on 2017/12/28.
 */

public class PoiActivity extends Activity implements View.OnClickListener{

    private final int UPDATE_MAP_CENTER = 1;
    private final int GET_HOTEL_POIS = 2;
    private final int DRAW_LINES_REGIONS = 3;

    private LatLng latLng = new LatLng(39.994827, 116.457092);
    private MapView mMapView;
    private MapboxMap mMapboxMap;
    private MyHandler mHandler = new MyHandler(this);
    private EditText mLatitude;
    private EditText mLongitude;
    private Button mSureButton;
    private List<LatLng> mPolyLine = new ArrayList<>();
    private LatLng mCenter;
    private String mRouteType = "driving";
    private boolean isMulti = false;
    private RadioGroup mRadioGroup1;
    private RadioGroup mRadioGroup2;
    private double[] distances = new double[100];
    private double[] duration = new double[100];
    private int distanceCount = 0;
    private TextView mInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.poi_activity_layout);
        requestPermission();
        setView(savedInstanceState);
    }

    private void setView(Bundle savedInstanceState){
        mMapView = findViewById(R.id.mapView);
        mLatitude = findViewById(R.id.latitude_btn);
        mLongitude = findViewById(R.id.longitude_btn);
        mSureButton = findViewById(R.id.sure_btn);
        mSureButton.setOnClickListener(this);
        mInfoTextView = findViewById(R.id.info_tv);
        mMapView.setStyleUrl(Style.MAPBOX_STREETS);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                if (mapboxMap == null){
                    return;
                }
                mMapboxMap = mapboxMap;
            }
        });
//        mRadioGroup1 = findViewById(R.id.poi_count_rg);
//        mRadioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                isMulti = (i == R.id.multi_poi_btn);
//            }
//        });
        mRadioGroup2 = findViewById(R.id.poi_type_rb);
        mRadioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.driving_btn){
                    mRouteType = "driving";
                }else{
                    mRouteType = "walking";
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        mInfoTextView.setText("正在获取poi信息 coffee hotel 共计20个点。。。");
        mMapboxMap.clear();
        sendCoffeePoiService();
    }

    private void sendCoffeePoiService(){
        String latitude = mLatitude.getText().toString();
        String longitude = mLongitude.getText().toString();
        if (latitude.equals("") || longitude.equals("")){
            latitude = "31.227903";
            longitude = "121.358911";
        }
        mCenter = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        final String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/coffee.json?&proximity="
                + longitude
                + ","
                + latitude
                + "&limit=10&types=poi&access_token="
                + getString(R.string.access_token);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = HttpRequestUtil.getXpath(url);
                JsonArray jsonArray = (JsonArray) jsonObject.get("features");
                mPolyLine.clear();
                int size = jsonArray.size();
                for (int i = 0;i < size;i++) {
                    JsonArray array = jsonArray.get(i).getAsJsonObject().get("center").getAsJsonArray();
                    String longitude = array.get(0).getAsString();
                    String latitude = array.get(1).getAsString();
                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mPolyLine.add(latLng);
                    mMapboxMap.addMarker(new MarkerOptions()
                            .icon(IconFactory.getInstance(PoiActivity.this).fromResource(R.drawable.icon))
                            .position(latLng)
                            .title("title")
                            .snippet("snippet"));
                }
                Message message = new Message();
                message.what = GET_HOTEL_POIS;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    private void sendHotelPoiService(){
        String latitude = mLatitude.getText().toString();
        String longitude = mLongitude.getText().toString();
        if (latitude.equals("") || longitude.equals("")){
            latitude = "31.227903";
            longitude = "121.358911";
        }
        final String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/hotel.json?&proximity="
                + longitude
                + ","
                + latitude
                + "&limit=10&types=poi&access_token="
                + getString(R.string.access_token);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = HttpRequestUtil.getXpath(url);
                JsonArray jsonArray = (JsonArray) jsonObject.get("features");
                int size = jsonArray.size();
                for (int i = 0;i < size;i++) {
                    JsonArray array = jsonArray.get(i).getAsJsonObject().get("center").getAsJsonArray();
                    String longitude = array.get(0).getAsString();
                    String latitude = array.get(1).getAsString();
                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                    mPolyLine.add(latLng);
                    mMapboxMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("title")
                            .snippet("snippet"));
                }
                Message message = new Message();
                message.what = UPDATE_MAP_CENTER;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    private void sendDistanceService(){
        mInfoTextView.setText("正在计算每个点距离中心点的距离。。。。");

        final int size = mPolyLine.size();
        distanceCount = 0;
        for (int i = 0;i < size;i++){
            LatLng latLng = mPolyLine.get(i);
            String points = getPoints(latLng);
            String radiuses = getRadiuses(latLng);
            final int k = i;
            final String url = "https://api.mapbox.com/directions/v5/mapbox/" +
                    mRouteType +
                    "/" +
                    points +
                    "?" +
                    "radiuses=" +
                    radiuses +
                    "&geometries=polyline&access_token=pk.eyJ1IjoiYmxlZWdlIiwiYSI6ImZjMDczZjc5N2U0NzFkNWVkYWUzNjkzZTY5NjU4ZDFlIn0.vsxMenwHU5mLe65GULWGQg";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("yk", "k1 = " + k);
                    JsonObject jsonObject = HttpRequestUtil.getXpath(url);
                    Log.e("yk", "k2 = " + k);
                    try{
//                        JsonObject object = jsonObject.get("routes").getAsJsonArray().get(0).getAsJsonObject();
//                        distances[k] = Double.parseDouble(object.get("distance").getAsString());
//                        duration[k] = Double.parseDouble(object.get("duration").getAsString());
                        double x = mPolyLine.get(k).getLongitude();
                        double y = mPolyLine.get(k).getLatitude();
                        double xx = mCenter.getLongitude();
                        double yy = mCenter.getLatitude();
                        distances[k] = (x-xx) * (x-xx) + (y-yy)*(y-yy);
                        duration[k] = (x-xx) * (x-xx) + (y-yy)*(y-yy);
                        Log.e("yk", "k3 = " + k);
                        distanceCount++;
                        if (distanceCount == size){
                            Log.e("yk", "send message");
                            Message message = new Message();
                            message.what = DRAW_LINES_REGIONS;
                            mHandler.sendMessage(message);
                        }
                    }catch (Exception e){
                        //
                    }

                }
            }).start();
        }
    }

    private String getPoints(LatLng latLng){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mCenter.getLongitude())
                .append(",")
                .append(mCenter.getLatitude())
                .append(";")
                .append(latLng.getLongitude())
                .append(",")
                .append(latLng.getLatitude());
        return stringBuilder.toString();
    }

    private String getRadiuses(LatLng polyLine){
        return "100;100";
    }

    private void drawpolyLine(List<LatLng> polyLine){
        List<LatLng> tubaoPoint = Utils.getTubao(polyLine);
        mMapboxMap.addPolyline(new PolylineOptions()
                .addAll(tubaoPoint)
                .color(ContextCompat.getColor(this, R.color.mapbox_blue))
                .alpha(0.5f)
                .width(2));
    }

    private void drawpolyRegion(List<LatLng> polyLine, boolean color){
        List<LatLng> tubaoPoint = Utils.getTubao(polyLine);
        if (color){
            mMapboxMap.addPolygon(new PolygonOptions()
                    .addAll(tubaoPoint)
                    .fillColor(Color.parseColor("#ff6913"))
                    .alpha(0.5f)
                    .strokeColor(ContextCompat.getColor(this, R.color.mapbox_blue)));
        }else{
            mMapboxMap.addPolygon(new PolygonOptions()
                    .addAll(tubaoPoint)
                    .fillColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .alpha(0.5f)
                    .strokeColor(ContextCompat.getColor(this, R.color.mapbox_blue)));
        }

    }

    private void startDrawing(){
        int size = mPolyLine.size();
        double max = -1;
        double min = Double.MAX_VALUE;
        for (int i = 0;i < size;i++){
            if (duration[i] > max){
                max = duration[i];
            }
            if (duration[i] < min){
                min = duration[i];
            }
        }
        double middleDistance = (max + min) / 2.0;
        ArrayList<LatLng> smallCircle = new ArrayList<>();
        ArrayList<LatLng> bigCircle = new ArrayList<>();
        for(int i = 0;i < size;i++){
            if (duration[i] > middleDistance){
                bigCircle.add(mPolyLine.get(i));
            }else{
                smallCircle.add(mPolyLine.get(i));
            }
        }
        mInfoTextView.setText("正在绘制区域");
        drawpolyLine(smallCircle);
        drawpolyRegion(smallCircle, false);

        drawpolyLine(mPolyLine);
        drawpolyRegion(mPolyLine, true);
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

    private class MyHandler extends Handler{

        private final WeakReference<Activity> mActivity;

        public MyHandler(Activity activity){
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_MAP_CENTER:
                    mMapboxMap.animateCamera(CameraUpdateFactory.newLatLng(mCenter));
                    sendDistanceService();
                    break;
                case GET_HOTEL_POIS:
                    sendHotelPoiService();
                    break;
                case DRAW_LINES_REGIONS:
                    startDrawing();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

}
