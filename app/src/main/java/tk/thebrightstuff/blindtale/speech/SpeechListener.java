package tk.thebrightstuff.blindtale.speech;

import android.content.Context;

import java.util.List;

/**
 * Created by niluje on 25/06/15.
 *
 */
public interface SpeechListener {

    void onReadyForSpeech();
    void onBeginningOfSpeech();
    void onEndOfSpeech();

    void onResults(List<String> results);
    void onPartialResults(List<String> results);

    void onError(String errorMessage);
}
