package us.v4lk.transrock;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import us.v4lk.transrock.transloc.TransLocAPI;

/**
 * Shows a splash screen and decides which activity should be started next.
 */
public class MainActivity extends AppCompatActivity {

    class ApiInitializer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            TransLocAPI.initialize();
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
        setContentView(R.layout.activity_main);
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
}
