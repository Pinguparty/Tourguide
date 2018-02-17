package pinguparty.de.tourguide.web;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.mapbox.services.commons.models.Position;

import org.w3c.dom.Text;

import java.util.List;

import pinguparty.de.tourguide.MainActivity;
import pinguparty.de.tourguide.PoiListActivity;
import pinguparty.de.tourguide.R;
import pinguparty.de.tourguide.web.models.Poi;




/**
 * Created by Marv on 07.02.2018.
 */

public class PoiAdapter extends RecyclerView.Adapter<PoiAdapter.MyViewHolder>{
    private List<Poi> poiList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView positionLat;
        public TextView positionLong;
        public TextView name, distance;
        public Position position;
        public String desc;

        private final Context context;


        public MyViewHolder(View view) {
            super(view);
            context = view.getContext();
            name = (TextView) view.findViewById(R.id.name);
            positionLat = (TextView) view.findViewById(R.id.positionLat);
            positionLong = (TextView) view.findViewById(R.id.positionLong);
            //distance = (TextView) view.findViewById(R.id.position);
            view.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    //Happens if you click the items in RecycleView
                    Log.d("foobar","Etwas wurde ausgewählt! " + name.getText());
                    Intent intent = new Intent(context,MainActivity.class);
                    intent.putExtra("Lat", position.getLatitude());
                    intent.putExtra("Long", position.getLongitude());
                    intent.putExtra("Name", name.getText());
                    intent.putExtra("Desc", desc);
                    context.startActivity(intent);
                }
            });
        }
    }

    public PoiAdapter(List<Poi> poiList){
        this.poiList = poiList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poi_list_row, parent,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        Poi poi = poiList.get(position);
        holder.name.setText(poi.getName());
        holder.positionLat.setText(""+poi.getPositionLat());
        holder.positionLong.setText(""+poi.getPositionLong());
        holder.position = Position.fromCoordinates(poi.getPositionLong(),poi.getPositionLat());
        holder.desc = poi.getDesc();
    }

    @Override
    public int getItemCount(){
        return poiList.size();
    }
}
