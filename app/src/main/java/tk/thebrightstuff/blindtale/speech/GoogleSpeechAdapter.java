package tk.thebrightstuff.blindtale.speech;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import tk.thebrightstuff.blindtale.R;

/**
 * Created by niluje on 25/06/15.
 *
 */
public class GoogleSpeechAdapter implements SpeechAdapter, RecognitionListener {

    private SpeechListener listener;
    private SpeechRecognizer speech;
    private Intent speechIntent;
    private Context context;

    public void initialize(SpeechResource resource, Context context) throws Exception {
        this.context = context;
        if(SpeechRecognizer.isRecognitionAvailable(context)){
            speech = SpeechRecognizer.createSpeechRecognizer(context.getApplicationContext());
            speech.setRecognitionListener(this);
            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, resource.getLang().getDisplayName());
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, resource.getLang().getDisplayName());
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, resource.getLang().getDisplayName());
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            //SPEECH_INTENT.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        }else{
            throw new Exception("Google Speech recognition is not supported!");
        }
    }

    @Override
    public void destroy(){
        if(isAvailable())
            speech.destroy();
    }

    @Override
    public void startListening(String code) {
        if(isAvailable())
            speech.startListening(speechIntent);
    }

    @Override
    public void stopListening() {
        if(isAvailable())
            speech.stopListening();
    }

    @Override
    public boolean isAvailable() {
        return speech!=null;
    }

    @Override
    public void setSpeechListener(SpeechListener listener) {
        this.listener = listener;
    }


    // Speech recognition interface implementation //
    //=============================================//

    @Override
    public void onReadyForSpeech(Bundle params) {
        listener.onReadyForSpeech();
    }

    /**
     * User starts to speak
     */
    @Override
    public void onBeginningOfSpeech() {
        listener.onBeginningOfSpeech();
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
        listener.onEndOfSpeech();
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        listener.onError(errorMessage);
    }

    @Override
    public void onResults(Bundle results) {
        listener.onResults( results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) );
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        listener.onPartialResults( partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) );
    }

    @Override
    public void onEvent(int eventType, Bundle params) {}


    public String getErrorText(int errorCode) {
        Resources res = context.getResources();
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return res.getString(R.string.error_audio);
            case SpeechRecognizer.ERROR_CLIENT: return res.getString(R.string.error_client);
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return res.getString(R.string.error_permissions);
            case SpeechRecognizer.ERROR_NETWORK: return res.getString(R.string.error_network);
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return res.getString(R.string.error_network_timeout);
            case SpeechRecognizer.ERROR_NO_MATCH: return res.getString(R.string.error_nomatch);
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return res.getString(R.string.error_busy);
            case SpeechRecognizer.ERROR_SERVER: return res.getString(R.string.error_server);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return res.getString(R.string.error_speech_timeout);
            default: return res.getString(R.string.error_default);
        }
    }

}
