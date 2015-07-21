package tk.thebrightstuff.blindtale.speech;

/**
 * Created by niluje on 25/06/15.
 *
 */
public interface SpeechAdapter {
    void destroy();
    void startListening(String code);
    void stopListening();
    boolean isAvailable();
    void setSpeechListener(SpeechListener listener);
}
