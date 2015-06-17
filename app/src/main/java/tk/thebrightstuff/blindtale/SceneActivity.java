package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


public class SceneActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        Intent intent = getIntent();
        Tale t = (Tale) intent.getExtras().getSerializable(MainActivity.TALE);
        Scene s = t.getScene();
        TextView tv = (TextView) findViewById(R.id.scene_title);
        tv.setText(s.title);
    }

}
