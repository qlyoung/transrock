package us.v4lk.transrock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import us.v4lk.transrock.transloc.TransLocAPI;
import us.v4lk.transrock.util.LocationManager;

/**
 * Shows a splash screen and decides which activity should be started next.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content
        setContentView(R.layout.activity_splash);
    }
    @Override
    protected void onStart() {
        super.onStart();
        StarterUpper su = new StarterUpper();
        su.execute();
    }

    private void startMapActivity() {
        Intent intent = new Intent(this, MainActivity.class);
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
            LocationManager manager = new LocationManager(SplashActivity.this);
            // try to cache location
            manager.getLocation();

            // initialize storage
            Hawk.init(SplashActivity.this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSqliteStorage(SplashActivity.this))
                .setLogLevel(LogLevel.FULL)
                .build();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // nice one, google.
            SplashActivity.this.startMapActivity();
        }
    }

}