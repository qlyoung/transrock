package us.v4lk.transrock.model;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * A transit agency.
 */

public class AgencyModel extends RealmObject {

    @PrimaryKey
    private String agencyId;

    private String
            longName,
            language,
            name,
            shortName,
            phone,
            url,
            timezone;

    private double latitude, longitude;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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

    // JSON setter
    public static void set(AgencyModel model, JSONObject agency) throws JSONException {
        // unpack returned object
        model.setAgencyId(agency.getString("agency_id"));
        model.setLongName(agency.getString("long_name"));
        model.setLanguage(agency.getString("language"));
        model.setName(agency.getString("name"));
        model.setShortName(agency.getString("short_name"));
        model.setPhone(agency.getString("phone"));
        model.setUrl(agency.getString("url"));
        model.setTimezone(agency.getString("timezone"));

        JSONObject position = agency.getJSONObject("position");
        model.setLatitude(position.getDouble("lat"));
        model.setLongitude(position.getDouble("lng"));
    }
}
