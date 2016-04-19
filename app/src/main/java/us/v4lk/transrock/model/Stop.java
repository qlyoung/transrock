package us.v4lk.transrock.model;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * A stop.
 */
public class Stop extends RealmObject {

    @PrimaryKey
    private String stopId;

    private String
            code,
            description,
            url,
            parentStationId,
            stationId,
            locationType,
            name;

    private RealmList<Route> routes;
    private double latitude, longitude;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParentStationId() {
        return parentStationId;
    }

    public void setParentStationId(String parentStationId) {
        this.parentStationId = parentStationId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(RealmList<Route> routes) {
        this.routes = routes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static void set(Stop model, JSONObject stop) throws JSONException {
        model.setCode(stop.getString("code"));
        model.setDescription(stop.getString("description"));
        model.setUrl(stop.getString("url"));
        model.setParentStationId(stop.getString("parent_station_id"));
        model.setStationId(stop.getString("station_id"));
        model.setLocationType(stop.getString("location_type"));
        model.setStopId(stop.getString("stop_id"));
        model.setName(stop.getString("name"));

        JSONObject loc = stop.getJSONObject("location");
        model.setLatitude(loc.getDouble("lat"));
        model.setLongitude(loc.getDouble("lng"));
    }
}
