package tk.thebrightstuff.blindtale.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

import tk.thebrightstuff.blindtale.tale.AudioAdapter;
import tk.thebrightstuff.blindtale.tale.Tale;
import tk.thebrightstuff.blindtale.utils.Callback;

/**
 * Created by niluje on 02/07/15.
 *
 */
public class AudioFileAdapter implements AudioAdapter, Serializable {



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
                listener.completed();
            }
        }

    }

    public static CompletionListener listener;
    public static AudioAdapter playing;

    private final File file;

    public AudioFileAdapter(File dir, String name) {
        this.file = new File(new File(dir, SOUND_FOLDER), name);
    }

    @Override
    public void play() throws AudioException {
        try{
            if(!file.exists())
                throw new Exception("AudioAdapter file does not exist: "+file.getAbsolutePath());
            getPlayer().reset();
            getPlayer().setDataSource(new FileInputStream(file).getFD());
            getPlayer().prepareAsync();
            playing = this;
        }catch(Exception e){
            throw new AudioAdapter.AudioException("Could not play audio file on media player", e);
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
        return this.file.getName();
    }

    @Override
    public String getText() { return null; }

    @Override
    public void setCompletionListener(CompletionListener listener) {
        AudioFileAdapter.listener = listener;
    }

}
