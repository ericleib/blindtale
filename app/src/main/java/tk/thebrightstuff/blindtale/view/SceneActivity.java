package tk.thebrightstuff.blindtale.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

import java.util.Locale;
import java.util.Map;

import tk.thebrightstuff.blindtale.R;
import tk.thebrightstuff.blindtale.speech.GoogleSpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SphinxSpeechAdapter;
import tk.thebrightstuff.blindtale.tale.Audio;
import tk.thebrightstuff.blindtale.tale.AudioAdapter;
import tk.thebrightstuff.blindtale.tale.Controller;
import tk.thebrightstuff.blindtale.tale.Scene;
import tk.thebrightstuff.blindtale.utils.StringUtils;


public class SceneActivity extends Activity implements Controller.TaleView, tk.thebrightstuff.blindtale.utils.Log {

    private final static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private PowerManager.WakeLock mWakeLock;

    private final static String TAG = "SceneActivity";

    private final static int GOOGLE = 0, SPHINX = 1;
    private final static int SPEECH_ENGINE = SPHINX;
    private SpeechAdapter speech = SPEECH_ENGINE==GOOGLE? new GoogleSpeechAdapter() : new SphinxSpeechAdapter();

    private Controller controller;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_scene);
        findViewById(R.id.button_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Pause clicked");
                controller.pauseAction.doAction();
            }
        });
        findViewById(R.id.button_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Skip clicked");
                controller.skipAction.doAction();
            }
        });
        findViewById(R.id.button_repeat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Repeat clicked");
                controller.repeatAction.doAction();
            }
        });
        findViewById(R.id.button_quit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Quit clicked");
                controller.quitAction.doAction();
            }
        });

        AudioManager m_amAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.MODE_NORMAL);
        m_amAudioManager.setSpeakerphoneOn(true);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        this.mWakeLock.acquire();

        Intent intent = getIntent();
        Scene scene = (Scene) intent.getExtras().getSerializable(MainActivity.SCENE);
        Map state = (Map) intent.getExtras().getSerializable(MainActivity.STATE);

        controller = new Controller(scene, state, this);
        speech.setSpeechListener(controller);
        controller.startScene();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "OnDestroy");
        this.mWakeLock.release();
        controller.shutdown();
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        setLocale(controller.getTale().getLang());
    }

    @Override
    public void clean() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setSpeechProgress(Controller.STATUS_HIDDEN);

                ((LinearLayout) findViewById(R.id.container)).removeAllViews();

                ((TextView) findViewById(R.id.scene_text)).setText("");

                ((TextView) findViewById(R.id.scene_title)).setText("");
            }
        });
    }

    private static int id = 0;
    @Override
    public void addButton(String text, final Controller.UIAction action) {
        Log.v(TAG, "Adding button: " + text);
        final Button btn = new Button(this);
        btn.setText(StringUtils.capitalize(text));
        btn.setId(id++);
        btn.setLayoutParams(params);
        btn.setTypeface(null, Typeface.BOLD);
        btn.getBackground().setColorFilter(getResources().getColor(R.color.play_bg), PorterDuff.Mode.MULTIPLY);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.doAction();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout lm = (LinearLayout) findViewById(R.id.container);
                lm.addView(btn);
            }
        });
    }

    public void setTitle(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.scene_title)).setText(text);
            }
        });
    }

    public void setText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.scene_text)).setText(text);
            }
        });
    }

    @Override
    public void showMessage(final String msg, final boolean longMsg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SceneActivity.this, msg, longMsg ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public SpeechAdapter getSpeechAdapter() {
        return speech;
    }

    public void setSpeechProgress(final int status){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.scene_speech_text).setVisibility(View.VISIBLE);
                findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                switch (status){
                    case Controller.STATUS_READY:
                        ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_ready);
                        ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(true);
                        ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                        break;
                    case Controller.STATUS_LISTENING:
                        ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_listening);
                        ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(true);
                        ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                        break;
                    case Controller.STATUS_ANALYSIS:
                        ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_end);
                        ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(true);
                        ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                        break;
                    case Controller.STATUS_ERROR:
                        ((TextView) findViewById(R.id.scene_speech_text)).setText(R.string.speech_error);
                        ((ProgressBar) findViewById(R.id.progress_bar)).setIndeterminate(false);
                        ((ProgressBar) findViewById(R.id.progress_bar)).getIndeterminateDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                        break;
                    case Controller.STATUS_HIDDEN:
                        findViewById(R.id.scene_speech_text).setVisibility(View.INVISIBLE);
                        findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });
    }

    @Override
    public AudioAdapter getAudioProvider(Audio audio) {
        if(audio.getFile()!=null){
            return new AudioFileAdapter(controller.getTale().getTaleFolder(), audio.getFile());
        }else{
            return new AudioTextAdapter(audio.getText());
        }
    }

    @Override
    public void updatePause(final boolean enabled, final boolean pauseResume){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String str = StringUtils.capitalize(getResources().getString(pauseResume ? R.string.pause : R.string.resume));
                ((Button) findViewById(R.id.button_pause)).setText(str);
                findViewById(R.id.button_pause).setEnabled(enabled);
                findViewById(R.id.button_skip).setEnabled(enabled);
            }
        });

    }

    @Override
    public void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public String getNString(int ref){
        Log.v(TAG, "Removing accents from "+getIString(ref)+" ("+ref+")");
        return StringUtils.removeAccents(getIString(ref));
    }


    @Override
    public String getIString(int ref){
        switch(ref){
            case Controller.A_GAME_BY: return getResources().getString(R.string.aGameBy);
            case Controller.PAUSE: return getResources().getString(R.string.pause);
            case Controller.QUIT: return getResources().getString(R.string.quit);
            case Controller.REPEAT: return getResources().getString(R.string.repeat);
            case Controller.SKIP: return getResources().getString(R.string.skip);
        }
        return null;
    }


    @Override
    public tk.thebrightstuff.blindtale.utils.Log getLog(){
        return this;
    }

    // Log

    @Override
    public void info(String tag, String message) {
        Log.i(TAG, message);
    }

    @Override
    public void error(String tag, String message, Exception e) {
        if(e!=null)
            Log.e(TAG, message, e);
        else
            Log.e(TAG, message);
    }
}
