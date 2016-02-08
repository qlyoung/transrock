package us.v4lk.transrock;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by qly on 2/7/16.
 */
public class TransrockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize realm
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }


}
