package us.v4lk.transrock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import us.v4lk.transrock.mapping.LocationManager;

/**
 * Shows a splash screen, loads some resources and starts MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // initialize a location manager
                LocationManager.getInstance(getApplicationContext());
                return null;
            }
        }.execute();



        // set content
        setContentView(R.layout.activity_splash);

        // start
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
    }

}