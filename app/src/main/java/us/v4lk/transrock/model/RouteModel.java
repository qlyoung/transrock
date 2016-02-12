package us.v4lk.transrock.model;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Full Route object for use on client side.
 *
 * This class defines routes that the user has saved and is used as
 * TransRock's internal representation of a route. The information
 * it contains is not time-dependent and should only need to be fetched
 * at time of creation. Information that is time-dependent is only keyed
 * here and should be accessed using said keys in conjunction with the
 * TransLoc API.
 */
public class RouteModel extends RealmObject {

    @PrimaryKey
    private String routeId;

    private String
            description,
            shortName,
            color,
            textColor,
            longName,
            url,
            type,
            agencyId;

    private boolean activated = false, saved = false;

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    private RealmList<SegmentModel> segments;
    private RealmList<StopModel> stops;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public RealmList<SegmentModel> getSegments() {
        return segments;
    }

    public void setSegments(RealmList<SegmentModel> segments) {
        this.segments = segments;
    }

    public RealmList<StopModel> getStops() {
        return stops;
    }

    public void setStops(RealmList<StopModel> stops) {
        this.stops = stops;
    }

    // JSON setter
    public static void set(RouteModel model, JSONObject route) throws JSONException {
        model.setRouteId(route.getString("route_id"));
        model.setDescription(route.getString("description"));
        model.setShortName(route.getString("short_name"));
        model.setColor(route.getString("color"));
        model.setAgencyId(route.getString("agency_id"));
        model.setTextColor(route.getString("text_color"));
        model.setLongName(route.getString("long_name"));
        model.setUrl(route.getString("url"));
        model.setType(route.getString("type"));
    }


}
