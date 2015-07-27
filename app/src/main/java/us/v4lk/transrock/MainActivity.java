package us.v4lk.transrock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.LocationManager;

/**
 * Shows a splash screen and decides which activity should be started next.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onStart() {
        super.onStart();
        StarterUpper su = new StarterUpper();
        su.execute();
    }

    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Tries to cache agencies and the current location.
     */
    class StarterUpper extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // cache agencies
            try { TransLocAPI.getAgencies(); }
            // it isn't crucial if the api errors here, so we don't bother checking exceptions
            catch (Exception e) { Log.e("TransRock", e.getMessage()); }

            // initialize a location manager
            LocationManager manager = new LocationManager(MainActivity.this);
            // wait a few seconds for api to connect
            // TODO: fix this so that if the api connects sooner it doesn't wait
            try { Thread.sleep(2000); } catch (Exception e) { }
            // try to cache location
            manager.getLocation();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // nice one, google.
            MainActivity.this.startMapActivity();
        }
    }

}