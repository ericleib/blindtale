package tk.thebrightstuff.blindtale;

/**
 * Created by niluje on 02/07/15.
 *
 */
public interface Audio extends Conditional {

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



    class AudioException extends Exception {
       public AudioException(String s, Exception e){
           super(s,e);
       }
    }

    interface CompletionListener {
        void completed(Audio audio);
    }
}
