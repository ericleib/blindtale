package tk.thebrightstuff.blindtale.speech;

import java.util.Set;

/**
 * Created by niluje on 25/06/15.
 *
 */
public interface SpeechAdapter {
    void initialize(SpeechListener listener, String lang, Set<String> keywords) throws Exception;
    void destroy();
    void startListening();
    void stopListening();
    boolean isAvailable();
}
