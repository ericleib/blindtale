package tk.thebrightstuff.blindtale;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


public class SceneActivity extends Activity implements MediaPlayer.OnCompletionListener, RecognitionListener {

    private final static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private final static String TAG = "SceneActivity";

    private MediaPlayer player;
    private SpeechRecognizer speech;
    private Intent SPEECH_INTENT;

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

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        SPEECH_INTENT = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "fr-FR");
        SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr");
        SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        //SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);


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
        speech.startListening(SPEECH_INTENT);
    }


    // Speech recognition interface implementation //
    //=============================================//

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "onReadyForSpeech");
    }

    /**
     * User starts to speak
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
        // Init progress bar?
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
        // Called after onResult or not?
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.e(TAG, "FAILED " + errorMessage);
        // process error
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(TAG, "onResults");
        speech.stopListening();

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Toast.makeText(this, "You said: "+matches.get(0), Toast.LENGTH_SHORT).show();
        // process results
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(TAG, "onPartialResults");
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

}
