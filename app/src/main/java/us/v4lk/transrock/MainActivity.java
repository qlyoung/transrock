package us.v4lk.transrock;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import us.v4lk.transrock.transloc.Cache;
import us.v4lk.transrock.transloc.TransLocAPI;

/**
 * Shows a splash screen and decides which activity should be started next.
 */
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient apiclient;

    class ApiInitializer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // cache agencies
            TransLocAPI.getAgencies();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity.this.startMapActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_main);

        // get google api
        apiclient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ApiInitializer init = new ApiInitializer();
        init.execute();
    }

    /**
     * nice name for an intent, isn't it?
     */
    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /* google api callbacks */
    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiclient);
        Cache.cacheLocation(lastLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}