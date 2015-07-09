package tk.thebrightstuff.blindtale.tale;

/**
 * Created by niluje on 02/07/15.
 *
 */
public interface AudioAdapter {

    void play() throws AudioException;
    void pause();
    void stop();
    void repeat() throws AudioException;
    void resume();
    void skip();
    void destroy();

    boolean isPlaying();

    void setCompletionListener(CompletionListener listener);

    String toString();

    String getText();



    class AudioException extends Exception {
       public AudioException(String s, Exception e){
           super(s,e);
       }
    }

    interface CompletionListener {
        void completed();
    }
}
