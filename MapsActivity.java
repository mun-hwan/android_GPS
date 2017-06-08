package com.example.user.mapexam1;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ToggleButton;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.jar.Manifest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mGoogleMap;
    // GPSTracker class
    Button btnShowLocation;
    Button btnclearLocation;
    EditText editText;

    // GPSTracker class
    GPSTracker gps = null;
    GPSTracker new_gps = null;

    public Handler mHandler;

    public static int RENEW_GPS = 1;
    public static int SEND_PRINT = 2;

    private PolylineOptions Options;
    private ArrayList<LatLng> arrayPoints;
    private ArrayList<LatLng> arrayReal;


    private static int j=1,i=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }



        editText = (EditText) findViewById(R.id.editText);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnclearLocation = (Button) findViewById(R.id.btnclearLocation);

        // 맵 마커 연결 하는 줄 및 배열 셋팅
        Options = new PolylineOptions();
        Options.color(Color.RED);
        Options.width(5);
        arrayPoints = new ArrayList<>();


        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==RENEW_GPS){
                    makeNewGpsService();
                }
                if(msg.what==SEND_PRINT){
                    logPrint((String)msg.obj);
                }
            }
        };

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {


                // create class object
                if(gps == null) {
                    gps = new GPSTracker(MapsActivity.this,mHandler);

                }else{
                    gps.Update();
                }

                // check if GPS enabled
                if(gps.canGetLocation()){
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    // \n is for new line
                    Toast.makeText(getApplicationContext(), "현재 위치는 - \n경도: " + latitude + "\n위도: " + longitude + "입니다.", Toast.LENGTH_LONG).show();



                    LatLng MyLocation = new LatLng(latitude,longitude );
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(MyLocation));




                     /*if(i==0) {
                          arrayPoints.add(MyLocation);
                         i=35700;
                            }
*/

                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }


            }
        });

        btnclearLocation.setOnClickListener(new View.OnClickListener() {// 스탑 버튼 메소드
            @Override
            public void onClick(View arg0){
               //mGoogleMap.clear();
                //arrayPoints.clear();
                gps.stopUsingGPS();
                new_gps.stopUsingGPS();
            }
        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        LatLng Seoul = new LatLng(37.555744, 126.970431 );
        mGoogleMap.addMarker(new MarkerOptions().position(Seoul).title("Marker in Seoul"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(Seoul));


        CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
        mGoogleMap.animateCamera(zoom);



        /*  현재 위치를 받을수 있는 코드
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {return;}
        mGoogleMap.setMyLocationEnabled(true);
*/


        // marker 표시
        // market 의 위치, 타이틀, 짧은설명 추가 가능.
       /* MarkerOptions marker = new MarkerOptions();
        marker .position(new LatLng(37.555744, 126.970431))
                .title("서울역")
                .snippet("Seoul Station");
        googleMap.addMarker(marker).showInfoWindow();*/ // 마커추가,화면에출력




    }


    public void makeNewGpsService() {
        if (gps == null) {
            gps = new GPSTracker(MapsActivity.this, mHandler);
        } else {
            gps.Update();
        }

    }

    public void logPrint(String str){
        editText.append(getTimeStr()+" "+str+"\n");

        if(new_gps == null) {
            new_gps = new GPSTracker(MapsActivity.this,mHandler);

        }else{
            new_gps.Update();
        }

        // check if GPS enabled
        if(new_gps.canGetLocation()){

            LatLng MyLocation1 = new LatLng(new_gps.getLatitude(), new_gps.getLongitude());
            mGoogleMap.addMarker(new MarkerOptions().position(MyLocation1).title(Integer.toString(j)+"번째"));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(MyLocation1));
            j++;


            arrayPoints.add(MyLocation1);
            Options.addAll(arrayPoints);
            mGoogleMap.addPolyline(Options);
            arrayPoints.clear();

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }


    }

    public String getTimeStr() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("MM/dd HH:mm:ss");
        return sdfNow.format(date);
    }




}
