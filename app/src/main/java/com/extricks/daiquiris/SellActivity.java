package com.extricks.daiquiris;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SellActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        final EditText itemm=findViewById(R.id.item_name);
        Button sellBTN=findViewById(R.id.sellbutton);
        final Double lat=Double.valueOf(getIntent().getStringExtra("lat"));
        final Double lang=Double.valueOf(getIntent().getStringExtra("lang"));

        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(this, "Signed as "+user.getDisplayName(), Toast.LENGTH_SHORT).show();

        sellBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemname=itemm.getText().toString();
                if(TextUtils.isEmpty(itemname)){
                    Toast.makeText(SellActivity.this, "fill all Details", Toast.LENGTH_SHORT).show();
                }
                else{
                    insertintoDB(itemname,lat,lang,user);
                }
            }
        });
    }

    private void insertintoDB(String itemname, Double lat, Double lang, final FirebaseUser uname){
        GeoPoint g=new GeoPoint(lat,lang);
        item it=new item(itemname,uname.getDisplayName(),g);
        final FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("items")
                .add(it)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(SellActivity.this, "item added", Toast.LENGTH_SHORT).show();
                        Map<String, Object> userhash = new HashMap<>();
                        userhash.put("item id", documentReference.getId());
                        rootRef.collection("users").document(uname.getUid())
                                .collection("itemsid").document().set(userhash);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SellActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
