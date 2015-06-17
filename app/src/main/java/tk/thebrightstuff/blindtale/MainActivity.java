package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;


public class MainActivity extends Activity {

    public static final String TALE = "TALE";
    private Spinner taleSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taleSpinner = (Spinner)findViewById(R.id.tale_spinner);
        SpinnerAdapter adapter = new ArrayAdapter<Tale>(this, android.R.layout.simple_spinner_dropdown_item, getAvailableTales());
        taleSpinner.setAdapter(adapter);
    }

    public void exitApp(View view){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void play(View view){
        Intent intent = new Intent(this, SceneActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(TALE, getSelectedTale());
        intent.putExtras(b);
        startActivity(intent);
    }

    private Tale getSelectedTale(){
        return (Tale) taleSpinner.getSelectedItem();
    }

    private Tale[] getAvailableTales(){
        return new Tale[] {Tale.getDummyTale()};
    }
}
