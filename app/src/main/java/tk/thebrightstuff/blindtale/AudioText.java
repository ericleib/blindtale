package tk.thebrightstuff.blindtale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

import java.io.Serializable;
import java.util.HashMap;

import tk.thebrightstuff.blindtale.speech.SpeechResource;
import tk.thebrightstuff.blindtale.utils.Callback;

/**
 * Created by niluje on 02/07/15.
 *
 */
public class AudioText implements Audio, Serializable {



    public static void initialize(Context context, final SpeechResource resource, final Callback<String> callback){
        AudioText.tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(resource.getLocale());
                    tts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener(){
                        @Override
                        public void onUtteranceCompleted(String utteranceId) {
                            System.out.println("Utterance completed");
                            listener.completed(audioText);
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

    private Condition condition;
    private String text;

    public AudioText(String text){
        this.text = text;
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
        listener.completed(this);
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
    private static AudioText audioText;
    @Override
    public void setCompletionListener(CompletionListener listener) {
        AudioText.listener = listener;
        AudioText.audioText = this;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

}
