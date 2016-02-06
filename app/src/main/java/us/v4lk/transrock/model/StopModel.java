package us.v4lk.transrock.model;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * A stop.
 */
public class StopModel extends RealmObject {

    @PrimaryKey
    String routeId;

    String
            code,
            description,
            url,
            parentStationId,
            stationId,
            locationType,
            name;

    RealmList<RouteModel> routes;
    double latitude, longitude;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
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

    public RealmList<RouteModel> getRoutes() {
        return routes;
    }

    public void setRoutes(RealmList<RouteModel> routes) {
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

    public static void set(RouteModel model, JSONObject route) throws JSONException {
        //TODO: IMPLEMENT
    }
}
