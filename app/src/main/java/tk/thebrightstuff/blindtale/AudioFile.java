package tk.thebrightstuff.blindtale;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Locale;

import tk.thebrightstuff.blindtale.utils.Callback;

/**
 * Created by niluje on 02/07/15.
 *
 */
public class AudioFile extends File implements Audio, Serializable {



    public static void initialize(Context context, Tale tale, final Callback<String> callback){
        new AsyncTask<Void,Void,Exception>(){
            @Override
            protected Exception doInBackground(Void... params) {
                try{
                    getPlayer();
                }catch (Exception e){
                    return e;
                }
                return null;
            }
            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    result.printStackTrace();
                    callback.callback(null, result);
                } else {
                    callback.callback("Media player initialized", null);
                }
            }
        }.execute();
    }



    private static final String SOUND_FOLDER = "sounds";

    private transient static MediaPlayer player;
    private static MediaPlayer getPlayer(){
        if(player==null){
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            PlayerListener listener = new PlayerListener();
            player.setOnCompletionListener(listener);
            player.setOnPreparedListener(listener);
        }
        return player;
    }

    private static class PlayerListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            getPlayer().start();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if(listener!=null){
                listener.completed(playing);
            }
        }

    }

    public static CompletionListener listener;
    public static Audio playing;


    private Condition condition;

    public AudioFile(File dir, String name) {
        super(new File(dir, SOUND_FOLDER), name);
    }

    @Override
    public void play() throws AudioException {
        try{
            if(!this.exists())
                throw new Exception("Audio file does not exist: "+getAbsolutePath());
            getPlayer().reset();
            getPlayer().setDataSource(new FileInputStream(this).getFD());
            getPlayer().prepareAsync();
            playing = this;
        }catch(Exception e){
            throw new Audio.AudioException("Could not play audio file on media player", e);
        }
    }

    @Override
    public void stop() {
        getPlayer().stop();
    }

    @Override
    public void repeat() throws AudioException {
        stop();
        play();
    }

    @Override
    public void resume() {
        getPlayer().start();
    }

    @Override
    public void pause() {
        getPlayer().pause();
    }

    @Override
    public void skip() {
        player.seekTo(player.getDuration());
    }

    @Override
    public boolean isPlaying() {
        return getPlayer().isPlaying();
    }

    @Override
    public void destroy() {
        getPlayer().release();
        player = null;
    }


    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public String getText() { return null; }

    @Override
    public void setCompletionListener(CompletionListener listener) {
        AudioFile.listener = listener;
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
