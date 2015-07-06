package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tk.thebrightstuff.blindtale.speech.GoogleSpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SpeechListener;
import tk.thebrightstuff.blindtale.speech.SphinxSpeechAdapter;
import tk.thebrightstuff.blindtale.utils.StringUtils;


public class SceneActivity extends Activity implements SpeechListener, Audio.CompletionListener {

    private final static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private final Map<String,Button> buttonMap = new HashMap<>();

    private final static String TAG = "SceneActivity", SCENE = "scene", ACTION = "action";

    private final static int GOOGLE = 0, SPHINX = 1;
    private final static int SPEECH_ENGINE = SPHINX;

    private SpeechAdapter speech = SPEECH_ENGINE==GOOGLE? new GoogleSpeechAdapter() : new SphinxSpeechAdapter(this);

    private Audio audio;

    private Map<String,String> state;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scene);
        Intent intent = getIntent();
        Scene scene = (Scene) intent.getExtras().getSerializable(MainActivity.SCENE);
        this.state = (Map) intent.getExtras().getSerializable(MainActivity.STATE);

        AudioManager m_amAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        //m_amAudioManager.setsetMode(AudioManager.STREAM_MUSIC);
        m_amAudioManager.setMode(AudioManager.MODE_NORMAL);
        m_amAudioManager.setSpeakerphoneOn(true);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();

        newScene(scene);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "OnDestroy");
        this.mWakeLock.release();
        audio.destroy();
        speech.destroy();
        super.onDestroy();
    }

    private void newScene(Scene s){
        Log.v(TAG, "New scene: " + s.id);

        // Set title
        ((TextView) findViewById(R.id.scene_title)).setText(s.title);

        // Add buttons
        LinearLayout lm = (LinearLayout) findViewById(R.id.container);
        lm.removeAllViews();
        int i = 0;
        for(Action a: s.actions){
            if(a.getCondition()==null || a.getCondition().check(state))
                addActionButton(lm, a, i++);
        }
        addStandardButtons(lm, i, s.end);

        // Play sound
        this.audio = s.audio.get(0);
        Log.v(TAG, "Playing sound: " + this.audio.toString());
        try{
            this.audio.setCompletionListener(this);
            this.audio.play();
        }catch(Exception e){
            Log.e(TAG, "Error playing sound: "+this.audio.toString(), e);
            Toast.makeText(this, "Oops... There's a problem with that scene!", Toast.LENGTH_LONG).show();
        }

        // Update state
        updateState(s);

        // Saving player's progress
        saveProgress(s);
    }

    private void endScene(Scene nextScene){
        Log.v(TAG, "Ending scene");
        buttonMap.clear();
        this.audio.stop();
        stopSpeechRecognition();
        if(nextScene!=null)
            newScene(nextScene);
    }

    private void updateState(Scene s) {
        state.put(SCENE + "_" + s.id + "_visited", "true");
        String nb = state.get(SCENE + "_" + s.id + "_visited_nb");
        if(nb==null){
            state.put(SCENE + "_" + s.id + "_visited_nb", Integer.toString(1));
        }else{
            state.put(SCENE + "_" + s.id + "_visited_nb", Integer.toString(Integer.parseInt(nb)+1));
        }
    }

    private void updateAction(Action a) {
        if(a.id!=null)
            state.put(ACTION+"_"+a.id+"_completed", "true");
    }


    // Buttons

    private void addActionButton(LinearLayout lm, final Action a, int id) {
        Button btn = addButton(lm, a.keys.get(0), id);
        btn.setTypeface(null, Typeface.BOLD);
        btn.getBackground().setColorFilter(getResources().getColor(R.color.play_bg), PorterDuff.Mode.MULTIPLY);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAction(a);
                endScene(a.nextScene);
            }
        });
        for(String key : a.keys)
            buttonMap.put(StringUtils.removeAccents(key), btn);
    }

    private void addStandardButtons(LinearLayout lm, int id, boolean end) {
        buttonMap.put(getNString(R.string.repeat), addButton(lm, getResources().getString(R.string.repeat), id++));
        buttonMap.get(getNString(R.string.repeat)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    audio.repeat();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.pause)));
                    buttonMap.get(getNString(R.string.pause)).setEnabled(true);
                    buttonMap.get(getNString(R.string.skip)).setEnabled(true);
                } catch (Audio.AudioException e) {
                    Log.e(TAG, "Error repeating audio "+audio.toString(), e);
                }
            }
        });

        buttonMap.put(getNString(R.string.pause), addButton(lm, getResources().getString(R.string.pause), id++));
        buttonMap.get(getNString(R.string.pause)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio.isPlaying()) {
                    audio.pause();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.resume)));
                } else {
                    audio.resume();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.pause)));
                }
            }
        });

        buttonMap.put(getNString(R.string.skip), addButton(lm, getResources().getString(R.string.skip), id++));
        buttonMap.get(getNString(R.string.skip)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                audio.skip();
            }
        });

        buttonMap.put(getNString(R.string.quit), addButton(lm, getResources().getString(R.string.quit), id++));
        buttonMap.get(getNString(R.string.quit)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Quit clicked");
                if (audio.isPlaying()) {
                    Log.i(TAG, "Stopping player");
                    audio.stop();
                }
                if (speech != null) {
                    Log.i(TAG, "Stopping speech recognition");
                    speech.destroy();
                }
                Log.i(TAG, "Killing activity");
                finish();
            }
        });
    }

    private Button addButton(LinearLayout lm, String text, int id){
        Log.v(TAG, "Adding button: " + text);
        Button btn = new Button(this);
        btn.setText(StringUtils.capitalize(text));
        btn.setId(id);
        btn.setLayoutParams(params);
        lm.addView(btn);
        return btn;
    }


    /**
     * Called when sound is finished playing
     */
    @Override
    public void completed(Audio audio) {
        Log.i(TAG, "Finished playing");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startSpeechRecognition();
                buttonMap.get(getNString(R.string.pause)).setEnabled(false);
                buttonMap.get(getNString(R.string.skip)).setEnabled(false);
            }
        });
    }

    private void startSpeechRecognition() {
        if(speech.isAvailable()){
            Log.i(TAG, "Starting speech recognition");
            speech.startListening();
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_start);
        }
    }

    private void stopSpeechRecognition() {
        if(speech.isAvailable()){
            Log.i(TAG, "Stopping speech recognition");
            speech.stopListening();
            findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText("");
        }
    }

    private void checkSpeechRecognitionResults(List<String> matches) {

        Log.i(TAG, "Checking speech recognition results");

        //Log.i(TAG, "Stopping speech recognition");
        //speech.cancel();
        //speech.stopListening();

        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(false);

        Toast.makeText(this, "You said: "+matches.get(0), Toast.LENGTH_SHORT).show();

        Log.i(TAG, "Processing results");
        boolean found = false;
        for(String match : matches){
            Log.i(TAG, "Match: "+match);
            for(String word : match.split(" ")){
                word = StringUtils.removeAccents(word);
                if(buttonMap.containsKey(word)) {
                    Button btn = buttonMap.get(word);
                    if(btn.isEnabled()){
                        Log.i(TAG, "Word '"+word+"' matches active button!");
                        stopSpeechRecognition();
                        btn.performClick();
                        found = true;
                        break;
                    }
                }
            }
            if(found)
                break;
        }

        if(!found){
            Log.i(TAG, "Did not recognize a usable match: starting to listen again!");
            speech.startListening();
        }
    }



    private void saveProgress(Scene scene){
        File saveFile = scene.tale.getSaveFile();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
            bw.write("scene = " + scene.id + "\n");
            for(Map.Entry<String,String> e : state.entrySet())
                bw.write(e.getKey()+ " = "+e.getValue() + "\n");
            bw.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not save progress to save file: " + saveFile.getAbsolutePath(), e);
            Toast.makeText(this, "Could not save progress...", Toast.LENGTH_LONG).show();
        }
    }

    private String getNString(int id){
        return StringUtils.removeAccents(getResources().getString(id));
    }



    // Speech listener interface implementation

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onReadyForSpeech() {
        Log.i(TAG, "onReadyForSpeech");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_ready);
                ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(true);
                ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            }
        });
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_listening);
            }
        });
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_end);
                ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
            }
        });
        // Called after onResult or not?
    }

    @Override
    public void onError(final String errorMessage) {
        Log.e(TAG, "Failed speech recognition: " + errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(false);
                ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                Toast.makeText(SceneActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
        speech.startListening();
    }

    @Override
    public void onResults(final List<String> results) {
        Log.i(TAG, "onResults");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkSpeechRecognitionResults(results);
            }
        });
    }


    @Override
    public void onPartialResults(final List<String> partialResults) {
        Log.i(TAG, "onPartialResults");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkSpeechRecognitionResults(partialResults);
            }
        });
    }

}
