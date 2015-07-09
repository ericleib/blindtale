package tk.thebrightstuff.blindtale.view;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

import java.io.Serializable;
import java.util.HashMap;

import tk.thebrightstuff.blindtale.speech.SpeechResource;
import tk.thebrightstuff.blindtale.tale.AudioAdapter;
import tk.thebrightstuff.blindtale.utils.Callback;

/**
 * Created by niluje on 02/07/15.
 *
 */
public class AudioTextAdapter implements AudioAdapter, Serializable {



    public static void initialize(Context context, final SpeechResource resource, final Callback<String> callback){
        AudioTextAdapter.tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(resource.getLang());
                    tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener(){
                        @Override
                        public void onUtteranceCompleted(String utteranceId) {
                            System.out.println("Utterance completed");
                            listener.completed();
                        }
                    });
                    callback.callback("Text to speech engine correctly initialized", null);
                }else{
                    callback.callback(null, new Exception("Error initializing text to text to speech engine"));
                }
            }
        });
    }

    private static TextToSpeech tts;

    private static final HashMap<String, String> params = new HashMap<String, String>();
    static{
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"dummy");
    }

    private String text;

    public AudioTextAdapter(String text){
        this.text = text.trim();
    }

    public String toString(){
        return text.length()>30? text.substring(0, 30)+"..." : text;
    }

    @Override
    public String getText() { return text; }


    @Override
    public void play() throws AudioException {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
    }

    @Override
    public void stop() {
        tts.stop();
    }

    @Override
    public void repeat() throws AudioException {
        stop();
        play();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void skip() {
        tts.stop();
        listener.completed();
    }

    @Override
    public void destroy() {
        tts.shutdown();
    }

    @Override
    public boolean isPlaying() {
        return tts.isSpeaking();
    }

    private static CompletionListener listener;
    @Override
    public void setCompletionListener(CompletionListener listener) {
        AudioTextAdapter.listener = listener;
    }

}
