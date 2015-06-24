package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tk.thebrightstuff.blindtale.tk.thebrightstuff.blindtale.utils.StringUtils;


public class SceneActivity extends Activity implements MediaPlayer.OnCompletionListener, RecognitionListener {

    private final static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private final Map<String,Button> buttonMap = new HashMap<>();

    private final static String TAG = "SceneActivity";

    private MediaPlayer player;
    private SpeechRecognizer speech;
    private Intent speechIntent;

    private Map<String,String> state;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        Intent intent = getIntent();
        Scene scene = (Scene) intent.getExtras().getSerializable(MainActivity.SCENE);
        this.state = (Map) intent.getExtras().getSerializable(MainActivity.STATE);

        AudioManager m_amAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        //m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
        m_amAudioManager.setMode(AudioManager.MODE_NORMAL);
        m_amAudioManager.setSpeakerphoneOn(true);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();

        if( SpeechRecognizer.isRecognitionAvailable(this) ){
            Log.v(TAG, "Starting new speech recognition instance");
            speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            speech.setRecognitionListener(this);
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, scene.tale.getLang());
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, scene.tale.getLang());
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, scene.tale.getLang());
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            //SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        }else{
            Log.e(TAG, "Speech recognition is unavailable!!");
            Toast.makeText(this, "Speech recognition is unavailable!!", Toast.LENGTH_LONG).show();
        }

        newScene(scene);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "OnDestroy");
        this.mWakeLock.release();
        this.player.release();
        this.speech.destroy();
        super.onDestroy();
    }

    private void newScene(Scene s){
        Log.v(TAG, "New scene: " + s.id);

        TextView tv = (TextView) findViewById(R.id.scene_title);
        tv.setText(s.title);

        LinearLayout lm = (LinearLayout) findViewById(R.id.container);
        lm.removeAllViews();
        int i = 0;
        for(Action a: s.actions){
            addActionButton(lm, a, i++);
        }
        addStandardButtons(lm, i, s.actions.size() > 0);

        File soundFile = s.getSoundFile();
        Log.v(TAG, "Playing sound: " + soundFile.getAbsolutePath());
        if(!soundFile.exists())
            Log.e(TAG, "Sound file does not exist: "+soundFile.getAbsolutePath());
        //player = MediaPlayer.create(this, Uri.fromFile(soundFile));
        try{
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
            Toast.makeText(this, "Oops... There's a problem with that scene!", Toast.LENGTH_LONG).show();
        }

        saveProgress(s);
    }

    private void endScene(Scene nextScene){
        Log.v(TAG, "Ending scene");
        player.stop();
        if(speech!=null){
            findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText("");
        }
        if(nextScene!=null)
            newScene(nextScene);
    }

    private void addActionButton(LinearLayout lm, final Action a, int id) {
        Button btn = addButton(lm, a.keys.get(0), id);
        btn.setTypeface(null, Typeface.BOLD);
        btn.getBackground().setColorFilter(getResources().getColor(R.color.play_bg), PorterDuff.Mode.MULTIPLY);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                player.stop();
                try {
                    player.prepare();
                    player.seekTo(0);
                    player.start();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.pause)));
                    buttonMap.get(getNString(R.string.pause)).setEnabled(true);
                    buttonMap.get(getNString(R.string.skip)).setEnabled(true);
                } catch (IOException e) {
                    Log.e(TAG, "Error preparing data after repeat...", e);
                }
            }
        });

        buttonMap.put(getNString(R.string.pause), addButton(lm, getResources().getString(R.string.pause), id++));
        buttonMap.get(getNString(R.string.pause)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.resume)));
                } else {
                    player.start();
                    buttonMap.get(getNString(R.string.pause)).setText(StringUtils.capitalize(getResources().getString(R.string.pause)));
                }
            }
        });

        buttonMap.put(getNString(R.string.skip), addButton(lm, getResources().getString(R.string.skip), id++));
        buttonMap.get(getNString(R.string.skip)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekTo(player.getDuration());
            }
        });

        buttonMap.put(getNString(R.string.quit), addButton(lm, getResources().getString(R.string.quit), id++));
        buttonMap.get(getNString(R.string.quit)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Quit clicked");
                if (player.isPlaying()) {
                    Log.i(TAG, "Stopping player");
                    player.stop();
                }
                if (speech != null) {
                    Log.i(TAG, "Stopping speech recognition");
                    speech.cancel();
                    speech.stopListening();
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
     * @param mp media player
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "Finished playing");
        startSpeechRecognition();
        buttonMap.get(getNString(R.string.pause)).setEnabled(false);
        buttonMap.get(getNString(R.string.skip)).setEnabled(false);
    }

    private void startSpeechRecognition() {
        if(speech!=null){
            Log.i(TAG, "Starting speech recognition");
            speech.startListening(speechIntent);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_start);
        }
    }

    private void checkSpeechRecognitionResults(Bundle results) {

        Log.i(TAG, "Checking speech recognition results");

        //Log.i(TAG, "Stopping speech recognition");
        //speech.cancel();
        //speech.stopListening();

        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(false);

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
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
                        btn.performClick();
                        found = true;
                        break;
                    }
                }
            }
            if(found)
                break;
        }

        if(found){

        }else{
            Log.i(TAG, "Did not recognize a usable match: starting to listen again!");
            speech.startListening(speechIntent);
        }
    }


    // Speech recognition interface implementation //
    //=============================================//

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "onReadyForSpeech");
        ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_ready);
        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(true);
        ((ProgressBar)findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * User starts to speak
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
        ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_listening);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Log.i(TAG, "onRmsChanged: " + rmsdB);
        // Sound level changed (update progress bar ?)
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // Log.i(TAG, "onBufferReceived: " + buffer);
    }

    /**
     * User stopped speaking
     */
    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_end);
        ((ProgressBar)findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        // Called after onResult or not?
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.e(TAG, "Failed speech recognition: " + errorMessage);
        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(false);
        ((ProgressBar)findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        // process error
        //speech.cancel();
        //speech.stopListening();
        speech.startListening(speechIntent);
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults");
        checkSpeechRecognitionResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(TAG, "onPartialResults");
        checkSpeechRecognitionResults(partialResults);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(TAG, "onEvent");
    }

    public String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return getResources().getString(R.string.error_audio);
            case SpeechRecognizer.ERROR_CLIENT: return getResources().getString(R.string.error_client);
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return getResources().getString(R.string.error_permissions);
            case SpeechRecognizer.ERROR_NETWORK: return getResources().getString(R.string.error_network);
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return getResources().getString(R.string.error_network_timeout);
            case SpeechRecognizer.ERROR_NO_MATCH: return getResources().getString(R.string.error_nomatch);
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return getResources().getString(R.string.error_busy);
            case SpeechRecognizer.ERROR_SERVER: return getResources().getString(R.string.error_server);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return getResources().getString(R.string.error_speech_timeout);
            default: return getResources().getString(R.string.error_default);
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
}
