package tk.thebrightstuff.blindtale.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import tk.thebrightstuff.blindtale.R;
import tk.thebrightstuff.blindtale.speech.SphinxSpeechAdapter;
import tk.thebrightstuff.blindtale.tale.Scene;
import tk.thebrightstuff.blindtale.tale.Tale;
import tk.thebrightstuff.blindtale.utils.AssetCopier;


public class MainActivity extends Activity {

    public static final String SCENE = "SCENE", STATE = "STATE", PREFERENCE_FIRST_RUN = "FIRST_RUN", TAG="MainActivity";
    private Spinner taleSpinner;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Log.v(TAG, "Setting content view");
        setContentView(R.layout.activity_main);

        findViewById(R.id.play_button).getBackground().setColorFilter(getResources().getColor(R.color.play_bg), PorterDuff.Mode.MULTIPLY);

        findViewById(R.id.resume_button).setEnabled(false);

        Log.v(TAG, "First run: deploying default tale");
        if(!copyTaleToInternalStorage())
            Log.e(TAG, "Failed to copy the default tale to internal storage");

        initSpinner();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();
    }

    @Override
    protected void onResume(){
        Log.v(TAG, "OnResume");
        super.onResume();
        findViewById(R.id.resume_button).setEnabled(getSelectedTale().getSaveFile().exists());
    }

    @Override
    protected void onDestroy(){
        Log.v(TAG, "OnDestroy");
        super.onDestroy();
        mWakeLock.release();
        SphinxSpeechAdapter.cleanup();
    }

    private boolean copyTaleToInternalStorage() {
        Log.v(TAG, "Copying default Tale to Internal storage");
        return AssetCopier.copyAssets(this);
    }

    private void initSpinner() {
        Log.v(TAG, "Setting up Tale spinner");
        taleSpinner = (Spinner)findViewById(R.id.tale_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<>(this, R.layout.spinner_item, Tale.getAvailableTales(getDir("", MODE_PRIVATE).getParentFile(), new tk.thebrightstuff.blindtale.utils.Log() {
            @Override
            public void info(String tag, String message) {
                Log.i(tag, message);
            }

            @Override
            public void error(String tag, String message, Exception e) {
                if(e!=null)
                    Log.e(tag, message, e);
                else
                    Log.e(tag, message);
            }
        }));
        taleSpinner.setAdapter(adapter);
        taleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tale t = (Tale) parent.getItemAtPosition(position);
                Log.v(TAG, "Tale selected: "+t.toString());
                Log.v(TAG, "Saved file: "+t.getSaveFile().getAbsolutePath()+" exists: "+t.getSaveFile().exists());
                findViewById(R.id.resume_button).setEnabled(t.getSaveFile().exists());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v(TAG, "No tale selected");
                findViewById(R.id.resume_button).setEnabled(false);
            }
        });
    }

    public void exitApp(View view) {
        Log.v(TAG, "Exiting app");
        //Intent intent = new Intent(Intent.ACTION_MAIN);
        //intent.addCategory(Intent.CATEGORY_HOME);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
        finish();
    }

    public void play(View view) {
        Log.v(TAG, "Play clicked");
        launchTale(getSelectedTale().getScene(), new HashMap<String, String>());
    }

    public void resume(View view) {
        Log.v(TAG, "Resume clicked");
        Tale tale = getSelectedTale();
        try{
            HashMap<String,String> state = new HashMap<>();
            BufferedReader rd = new BufferedReader(new FileReader(tale.getSaveFile()));
            String sceneId = rd.readLine().trim().split("\\s*=\\s*")[1];
            String line;
            while((line=rd.readLine())!=null){
                String[] words = line.split("\\s*=\\s*");
                state.put(words[0], words[1]);
            }
            launchTale(tale.getScene(sceneId), state);
        }catch(Exception e){
            Log.e(TAG, "Could not read saved file: "+tale.getSaveFile().getAbsolutePath(), e);
            Toast.makeText(this, "Could not restore the saved game...", Toast.LENGTH_LONG).show();
        }
    }

    public void download(View view){
        Log.v(TAG, "Download clicked");
        Toast.makeText(this, getResources().getString(R.string.download_soon), Toast.LENGTH_LONG).show();
    }

    private Tale getSelectedTale(){
        return (Tale) taleSpinner.getSelectedItem();
    }

    private void launchTale(Scene scene, HashMap<String,String> state){
        Intent intent = new Intent(this, InitializationActivity.class);
        Bundle b = new Bundle();
        //b.putSerializable(SCENE, scene);
        SceneActivity.sceneToLaunch = scene;
        b.putSerializable(STATE, state);
        intent.putExtras(b);
        Log.v(TAG, "Starting tale " + scene.tale.toString()+" ("+scene.tale.getLang().toString()+")");
        startActivity(intent);
    }
}
