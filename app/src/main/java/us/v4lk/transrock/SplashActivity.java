package us.v4lk.transrock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import io.realm.Realm;
import us.v4lk.transrock.mapping.LocationManager;
import us.v4lk.transrock.util.RouteManager;
import us.v4lk.transrock.util.Util;

/**
 * Shows a splash screen, loads some resources and starts MainActivity
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

    /**
     * Tries to cache agencies and the current location.
     */
    class StarterUpper extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            // initialize a location manager
            LocationManager manager = LocationManager.getInstance(getApplicationContext());
            // try to cache location
            manager.getLocation();

            // initialize storage
            Hawk.init(SplashActivity.this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSqliteStorage(SplashActivity.this))
                .setLogLevel(LogLevel.FULL)
                .build();

            // initialize route storage
            RouteManager.initialize();

            // initialize db
            Util.realm = Realm.getInstance(getApplicationContext());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) { // nice one, google.
            // launch main activity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

}