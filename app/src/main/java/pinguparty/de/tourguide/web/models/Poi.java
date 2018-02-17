package pinguparty.de.tourguide.web.models;

import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mapbox.services.commons.models.Position;

/**
 * Created by Marvin on 02.02.2018.
 */

public class Poi {
    public Poi(String name , Position pos){
        this.name = name;
        this.pos = pos;
    }
    public Poi(String name, Position pos, String desc){
        this.name = name;
        this.pos = pos;
        this.desc = desc;
    }
    @SerializedName("id")
    @Expose
    public int id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("description")
    @Expose
    public String desc;
    @SerializedName("position")
    @Expose
    public Position pos;

    public String getName(){
        return name;
    }
    public String getDesc() { return  desc;}
    public Position getPosition(){
        return pos;
    }
    public double getPositionLat(){
        return pos.getLatitude();
    }
    public double getPositionLong(){
        return pos.getLongitude();
    }
    public void setPoi(String name, String desc, Position pos){
        this.id = -1;
        this.name = name;
        this.desc = desc;
        this.pos = pos;
    }
}
