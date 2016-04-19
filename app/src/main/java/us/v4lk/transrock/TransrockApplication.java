package us.v4lk.transrock;

import android.app.Application;
import android.os.AsyncTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import us.v4lk.transrock.mapping.LocationManager;

/**
 * Does a few housekeeping things at application launch.
 */
public class TransrockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // initialize a location manager
                LocationManager.getInstance(getApplicationContext());
                return null;
            }
        }.execute();
    }


}
