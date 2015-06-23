package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import tk.thebrightstuff.blindtale.tk.thebrightstuff.blindtale.utils.AssetCopier;


public class MainActivity extends Activity {

    public static final String TALE = "TALE", PREFERENCE_FIRST_RUN = "FIRST_RUN", TAG="MainActivity";
    private Spinner taleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "Setting content view");

        SharedPreferences p = getPreferences(MODE_PRIVATE);
        boolean firstRun = p.getBoolean(PREFERENCE_FIRST_RUN, true);
        if(firstRun){
            Log.v(TAG, "First run: deploying default tale");
            if(copyTaleToInternalStorage())
                p.edit().putBoolean(PREFERENCE_FIRST_RUN, false).commit();
            else
                Log.e(TAG, "Failed to copy the default tale to internal storage");
        }else
            Log.v(TAG, "Not the first run: tale already copied");

        Log.v(TAG, "Initializing spinner");
        initSpinner();
    }

    private boolean copyTaleToInternalStorage() {
        return AssetCopier.copyAssets(this);
    }

    private void initSpinner(){
        taleSpinner = (Spinner)findViewById(R.id.tale_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<Tale>(this, R.layout.spinner_item, Tale.getAvailableTales(getDir("", MODE_PRIVATE).getParentFile()));
        taleSpinner.setAdapter(adapter);
    }

    public void exitApp(View view){
        Log.v(TAG, "Exiting app");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void play(View view){
        Intent intent = new Intent(this, SceneActivity.class);
        Bundle b = new Bundle();
        Tale tale = getSelectedTale();
        b.putSerializable(TALE, tale);
        intent.putExtras(b);
        Log.v(TAG, "Starting tale " + tale.toString());
        startActivity(intent);
    }

    public void download(View view){
        Toast.makeText(this, getResources().getString(R.string.download_soon), Toast.LENGTH_LONG).show();
    }

    private Tale getSelectedTale(){
        return (Tale) taleSpinner.getSelectedItem();
    }

}
