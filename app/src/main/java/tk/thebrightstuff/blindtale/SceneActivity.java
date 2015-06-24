package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        Intent intent = getIntent();
        Scene scene = (Scene) intent.getExtras().getSerializable(MainActivity.SCENE);
        Map state = (Map) intent.getExtras().getSerializable(MainActivity.STATE);

        AudioManager m_amAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.STREAM_MUSIC);
        m_amAudioManager.setSpeakerphoneOn(true);

        newScene(scene, state);
    }


    private void newScene(Scene s, Map<String,String> state){

        this.state = state;

        if( SpeechRecognizer.isRecognitionAvailable(this) ){
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "fr-FR");
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr");
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            //SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        }else{
            Toast.makeText(this, "Speech recognition is unavailable!!", Toast.LENGTH_LONG).show();
        }

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
        player.stop();
        if(speech!=null){
            speech.cancel();
            speech.stopListening();
            findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText("");
        }
        if(nextScene!=null)
            newScene(nextScene, state);
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
                if (player.isPlaying())
                    player.stop();
                if (speech != null) {
                    speech.cancel();
                    speech.stopListening();
                }
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
        startSpeechRecognition();
        buttonMap.get(getNString(R.string.pause)).setEnabled(false);
        buttonMap.get(getNString(R.string.skip)).setEnabled(false);
    }

    private void startSpeechRecognition() {
        if(speech!=null){
            speech.startListening(speechIntent);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_start);
        }
    }

    private void checkSpeechRecognitionResults(Bundle results) {

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Toast.makeText(this, "You said: "+matches.get(0), Toast.LENGTH_SHORT).show();
        // process results
        for(String match : matches){
            for(String word : match.split(" ")){
                word = StringUtils.removeAccents(word);
                if(buttonMap.containsKey(word)){
                    Button btn = buttonMap.get(word);
                    if(btn.isEnabled())
                        btn.performClick();
                }
            }
        }
    }


    // Speech recognition interface implementation //
    //=============================================//

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "onReadyForSpeech");
        ((TextView)findViewById(R.id.scene_speech_text)).setText(R.string.speech_ready);
        ((ProgressBar)findViewById(R.id.progress_bar)).setIndeterminate(true);
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
        // Called after onResult or not?
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.e(TAG, "FAILED " + errorMessage);
        ((TextView)findViewById(R.id.scene_speech_text)).setText(errorMessage);
        // process error
        // Reset?
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

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
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
