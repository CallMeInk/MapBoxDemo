package com.example.yukai.mapboxdemo;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yukai on 2017/12/28.
 */

public class Utils {

    public static List<LatLng> getTubao(List<LatLng> latLngs){
        int size = latLngs.size();
        int[] index = new int[100];
        double[] lon = new double[100];
        double[] lat = new double[100];
        for (int i = 0;i < size;i++){
            index[i] = i;
            lon[i] = latLngs.get(i).getLongitude();
            lat[i] = latLngs.get(i).getLatitude();
        }
        for (int i = 0;i < size - 1;i++){
            for (int j = i + 1;j < size;j++){
                if (lon[index[i]] > lon[index[j]]){
                    int tmp = index[i];
                    index[i] = index[j];
                    index[j] = tmp;
                }
            }
        }
        int leftIndex = index[0];
        int rightIndex = index[size - 1];
        LatLng leftPoint = latLngs.get(leftIndex);
        LatLng rightPoint = latLngs.get(rightIndex);
        ArrayList<LatLng> finalLatlon = new ArrayList<>();
        finalLatlon.add(leftPoint);
        int index1 = leftIndex;
        //
        for (int i = 0;i < size;i++){
            LatLng point = latLngs.get(index1);
            if (index1 == rightIndex){
                break;
            }
            double xx = point.getLongitude();
            double yy = point.getLatitude();
            double maxAncle = -1;
            int maxIndex = 0;
            for (int j = 0;j < size;j++){
                if (i == j){
                    continue;
                }
                LatLng currentPoint = latLngs.get(j);
                double xxx = currentPoint.getLongitude();
                double yyy = currentPoint.getLatitude();
                if (xx >= xxx){
                    continue;
                }
                double currentAncle = (yyy - yy) / (Math.sqrt( (yyy-yy)*(yyy-yy) + (xxx-xx)*(xxx-xx)));
                if (currentAncle > maxAncle){
                    maxIndex = j;
                    maxAncle = currentAncle;
                }
            }
            finalLatlon.add(latLngs.get(maxIndex));
            index1 = maxIndex;
        }

        //
        for (int i = 0;i < size;i++){
            LatLng point = latLngs.get(index1);
            if (index1 == leftIndex){
                break;
            }
            double xx = point.getLongitude();
            double yy = point.getLatitude();
            double maxAncle = -1;
            int maxIndex = 0;
            for (int j = 0;j < size;j++){
                if (i == j){
                    continue;
                }
                LatLng currentPoint = latLngs.get(j);
                double xxx = currentPoint.getLongitude();
                double yyy = currentPoint.getLatitude();
                if (xx <= xxx){
                    continue;
                }
                double currentAncle = (yy - yyy) / (Math.sqrt( (yyy-yy)*(yyy-yy) + (xxx-xx)*(xxx-xx)));
                if (currentAncle > maxAncle){
                    maxIndex = j;
                    maxAncle = currentAncle;
                }
            }
            finalLatlon.add(latLngs.get(maxIndex));
            index1 = maxIndex;
        }
        return finalLatlon;
    }


}
