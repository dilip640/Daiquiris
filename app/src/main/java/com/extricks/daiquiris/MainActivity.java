package com.extricks.daiquiris;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mEmailAddress;
    Location mLoc;

    Button Searchbtn;
    EditText Searchtext;

    private List<item> itemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private itemAdapter mAdapter;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    String LocationProv;
    public static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            //Not signed in, launch the Sign In Activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mEmailAddress = mFirebaseUser.getEmail();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        try {
            LocationProv = locationManager.getBestProvider(new Criteria(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLoc=location;
                //getData();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, REQUEST_LOCATION);

                return;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
        prepareItemeData(mLoc);

        Searchbtn=findViewById(R.id.Searchbutton);
        Searchtext=findViewById(R.id.searchtext);
        final EditText nearby=findViewById(R.id.nearby);
        Searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String srchtxt=Searchtext.getText().toString();
                String neard=nearby.getText().toString();
                if(!TextUtils.isEmpty(srchtxt)){
                    mAdapter.searchfood(srchtxt,neard);
                }
                else
                    mAdapter.searchfood("l","999999999999");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    prepareItemeData(mLoc);
                }
        }
    }

    private void prepareItemeData(Location locc){
        mAdapter = new itemAdapter(itemList,locc);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        getData();
    }

    private List<item> getData(){
        final List<item> itemList1 = new ArrayList<>();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                item anitem=document.toObject(item.class);
                                itemList1.add(anitem);
                            }
                            if(mLoc==null)
                                mLoc=new Location("a");
                            for(int i=0; i<itemList1.size()-1 ; i++){
                                float min=999999999;
                                int k=i;
                                for(int j=i+1; j<itemList1.size() ;j++){
                                    Location l=new Location("A");
                                    l.setLatitude(itemList1.get(j).getLatlang().getLatitude());
                                    l.setLongitude(itemList1.get(j).getLatlang().getLongitude());
                                    float dist=l.distanceTo(mLoc);
                                    if(min > dist){
                                        k=j;
                                        min = dist;
                                    }
                                }
                                item it=itemList1.get(i);
                                itemList1.set(i, itemList1.get(k));
                                itemList1.set(k, it);

                                if(i==itemList1.size()-2){
                                    mAdapter.setData(itemList1,mLoc);
                                }
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "get data error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return itemList1;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_location_found:
                Intent intent=new Intent(this,SellActivity.class);

                if(mLoc==null){
                    intent.putExtra("lat","0");
                    intent.putExtra("lang","0");
                    startActivity(intent);
                }
                else {
                    intent.putExtra("lat",Double.toString(mLoc.getLatitude()));
                    intent.putExtra("lang",Double.toString(mLoc.getLongitude()));
                    startActivity(intent);
                }
                return true;
            case  R.id.refresh:
                mAdapter.setData(getData(),mLoc);
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
