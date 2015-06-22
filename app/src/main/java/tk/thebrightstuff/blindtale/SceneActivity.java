package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;


public class SceneActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private final static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private final static String TAG = "SceneActivity";

    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        Intent intent = getIntent();
        Tale t = (Tale) intent.getExtras().getSerializable(MainActivity.TALE);
        Scene s = t.getScene();
        newScene(s);
    }


    private void newScene(Scene s){

        Log.v(TAG, "New scene: " + s.id);
        TextView tv = (TextView) findViewById(R.id.scene_title);
        tv.setText(s.id);

        LinearLayout lm = (LinearLayout) findViewById(R.id.container);
        lm.removeAllViews();
        int i = 0;
        for(Action a: s.actions){
            addActionButton(lm, a, i++);
        }
        addStandardButtons(lm, i, s.actions.size() > 0);

        File soundFile = s.getSoundFile();
        Log.v(TAG, "Playing sound: "+soundFile.getAbsolutePath());
        if(!soundFile.exists())
            Log.e(TAG, "Sound file does not exist: "+soundFile.getAbsolutePath());
        //player = MediaPlayer.create(this, Uri.fromFile(soundFile));
        try{
            player = new MediaPlayer();
            player.setOnCompletionListener(this);
            player.setDataSource(new FileInputStream(soundFile).getFD());
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.start();
                }
            });
            player.prepareAsync();
        }catch(Exception e){
            Log.e(TAG, "Error playing sound: "+soundFile.getAbsolutePath(), e);
        }
    }

    private void addActionButton(LinearLayout lm, final Action a, int id) {
        Button btn = addButton(lm, a.keys.get(0), id);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                newScene(a.nextScene);
            }
        });
    }

    private void addStandardButtons(LinearLayout lm, int id, boolean end) {
        Button repeat = addButton(lm, "Repeat", id++);
        repeat.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });

        Button pause = addButton(lm, "Pause", id++);
        pause.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying())
                    player.pause();
                else
                    player.start();
            }
        });

        Button quit = addButton(lm, "Quit", id++);
        quit.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying())
                    player.stop();
                finish();
            }
        });
    }

    private Button addButton(LinearLayout lm, String text, int id){
        Log.v(TAG, "Adding button: " + text);
        Button btn = new Button(this);
        btn.setText(text);
        btn.setId(id);
        btn.setTextColor(getResources().getColor(R.color.text));
        btn.setLayoutParams(params);
        lm.addView(btn);
        return btn;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }
}
