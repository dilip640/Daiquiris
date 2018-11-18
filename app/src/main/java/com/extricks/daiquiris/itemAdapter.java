package com.extricks.daiquiris;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class itemAdapter extends RecyclerView.Adapter<itemAdapter.MyViewHolder> {

    private List<item> itemsList;
    private List<item> itemsList1;
    Location mloc;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, seller, loc;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.item_name);
            seller = (TextView) view.findViewById(R.id.seller);
            loc = (TextView) view.findViewById(R.id.location);
        }
    }


    public itemAdapter(List<item> itemList, Location mloc) {
        this.itemsList = itemList;
        this.itemsList1=itemList;
        if(mloc==null)
            this.mloc=new Location("a");
        else
            this.mloc=mloc;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.singleitem, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        item mitem = itemsList.get(position);
        holder.name.setText(mitem.getName());
        holder.seller.setText(mitem.getUser());
        holder.loc.setText(GetDist(mitem.getLatlang())+" m");
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    private String GetDist(GeoPoint gp){
        Location l=new Location("A");
        l.setLatitude(gp.getLatitude());
        l.setLongitude(gp.getLongitude());
        return Float.toString(mloc.distanceTo(l));
    }

    public void update(Location locc){
        mloc=locc;
        notifyDataSetChanged();
    }

    void searchfood(String foodtxt,String neard){
        if(TextUtils.isEmpty(neard))
            neard="10000";
        List<item> itemListt = new ArrayList<>();
        for (item it:itemsList1){
            if(it.getName().toLowerCase().contains(foodtxt.toLowerCase()) &&
                    Float.valueOf(GetDist(it.getLatlang()))<=Float.valueOf(neard))
                itemListt.add(it);
        }
        if(foodtxt.equals("l"))
            itemsList=itemsList1;
        else itemsList=itemListt;
        notifyDataSetChanged();
    }

    void setData(List<item> itlst, Location loc){
        mloc=loc;
        itemsList1=itlst;
        itemsList=itlst;
        notifyDataSetChanged();
    }

}