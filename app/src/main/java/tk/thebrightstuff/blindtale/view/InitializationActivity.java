package tk.thebrightstuff.blindtale.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.Locale;

import tk.thebrightstuff.blindtale.R;
import tk.thebrightstuff.blindtale.speech.SphinxSpeechAdapter;
import tk.thebrightstuff.blindtale.tale.Scene;
import tk.thebrightstuff.blindtale.tale.Tale;
import tk.thebrightstuff.blindtale.utils.Callback;

/**
 * Created by niluje on 02/07/15.
 *
 */
public class InitializationActivity extends Activity implements Callback<String> {

    public static final String TAG="InitializationActivity";

    private Bundle bundle;
    private int cpt = 0;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.v(TAG, "Setting content view");
        setContentView(R.layout.activity_init);

        bundle = getIntent().getExtras();
        Tale tale = ((Scene) bundle.getSerializable(MainActivity.SCENE)).tale;

        Locale.setDefault(tale.getLang());
        Configuration config = new Configuration();
        config.locale = tale.getLang();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        tale.getKeywords().add(getResources().getString(R.string.repeat));
        tale.getKeywords().add(getResources().getString(R.string.pause));
        tale.getKeywords().add(getResources().getString(R.string.skip));
        tale.getKeywords().add(getResources().getString(R.string.quit));

        cpt = 0;

        Log.v(TAG, (++cpt)+" - Initializing text-to-speech");
        AudioTextAdapter.initialize(getApplicationContext(), tale, this);

        Log.v(TAG, (++cpt)+" - Initializing Media player");
        AudioFileAdapter.initialize(getApplicationContext(), tale, this);

        Log.v(TAG, (++cpt)+" - Initializing speech-to-text");
        SphinxSpeechAdapter.initialize(getApplicationContext(), tale, this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mWakeLock.release();
    }

    @Override
    public void callback(String data, Exception e) {
        if(e==null) {
            Log.i(TAG, data);
            cpt--;
        }else{
            Log.e(TAG, e.toString());
            findViewById(R.id.wait).setVisibility(View.INVISIBLE);
            TextView tv = (TextView) findViewById(R.id.init);
            tv.setText("An error occurred during initialization...");
            tv.setTextColor(Color.RED);
        }

        if(cpt==0){ // Finished loading all 3 libraries
            Log.i(TAG, "Finished loading libraries: Now starting SceneActivity");
            Intent intent = new Intent(this, SceneActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }
}
