package tk.thebrightstuff.blindtale.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import tk.thebrightstuff.blindtale.speech.SpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SpeechListener;
import tk.thebrightstuff.blindtale.tale.Audio;
import tk.thebrightstuff.blindtale.tale.AudioAdapter;
import tk.thebrightstuff.blindtale.tale.Controller;
import tk.thebrightstuff.blindtale.tale.Tale;
import tk.thebrightstuff.blindtale.tale.TaleParser;
import tk.thebrightstuff.blindtale.utils.Log;

/**
 * Created by niluje on 09/07/15.
 *
 */
public class CliTaleView implements Controller.TaleView, SpeechAdapter, Log, AudioAdapter {

    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private List<String> buttons = new ArrayList<>();
    private CompletionListener audioListener;
    private SpeechListener speechListener;

    private final boolean debug;

    public CliTaleView(boolean debug){
        this.debug = debug;
    }

    @Override
    public void clean() {
        buttons.clear();
    }

    @Override
    public void addButton(String text, Controller.UIAction callback) {
        buttons.add(text);
    }

    @Override
    public void setTitle(String txt) {
        System.out.println("==> " + txt + " <==");
    }

    @Override
    public void setText(String txt) {
        if(!txt.trim().equals("")){
            System.out.println("--  "+txt.trim().replaceAll(" +", " ").replaceAll(" *\\n *","\n").replaceAll("\\n","\n--  "));
        }
    }

    @Override
    public void setSpeechProgress(int status) {
        if(status==Controller.STATUS_LISTENING)
            System.out.println("Listening...");
    }

    @Override
    public SpeechAdapter getSpeechAdapter() {
        return this;
    }

    @Override
    public AudioAdapter getAudioProvider(Audio audio) {
        return this;
    }

    @Override
    public void updatePause(boolean enabled, boolean pauseResume) {

    }

    @Override
    public void setLocale(Locale locale) {

    }

    private final static String[] strings = {"A game by","repeat","pause","skip","quit","choices","you said:"};
    @Override
    public String getIString(int ref) {
        return  strings[ref];
    }

    @Override
    public String getNString(int ref) {
        return getIString(ref);
    }

    @Override
    public void showMessage(String message, boolean longMsg) {
        System.out.println("Message: "+message);
    }

    @Override
    public Log getLog() {
        return this;
    }

    @Override
    public void finish() {

    }


    // Speech adapter
    @Override
    public void destroy() {
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void setCompletionListener(CompletionListener listener) {
        this.audioListener = listener;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void startListening(String code) {
        for(String s:buttons)
            System.out.println("=> "+s+"?");
        speechListener.onReadyForSpeech();
        speechListener.onBeginningOfSpeech();
        System.out.print("=> ");
        try {
            String input = reader.readLine();
            speechListener.onResults(Arrays.asList(input.trim().split("\\s+")));
        } catch (IOException e) {
            speechListener.onError(e.getMessage());
        }
    }

    @Override
    public void stopListening() {

    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void setSpeechListener(SpeechListener listener) {
        this.speechListener = listener;
    }


    // Audio Adapter

    @Override
    public void play() throws AudioException {
        audioListener.completed();
    }

    @Override
    public void pause() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void repeat() throws AudioException {
        audioListener.completed();
    }

    @Override
    public void resume() {
    }

    @Override
    public void skip() {
    }



    // Log

    @Override
    public void info(String tag, String message) {
        if(debug)
            System.out.println("info: " + message);
    }

    @Override
    public void error(String tag, String message, Exception e) {
        System.out.println("error: "+message);
        if(e!=null && debug)
            e.printStackTrace();
    }


    public static void main(String[] args) throws Exception {
        String path = args.length>0? args[0] : "/mnt/data/DEV/BlindTale/app/src/main/assets/labyrinth/descriptor.xml";
        Tale tale = new TaleParser().parse(new File(path));
        Controller controller = new Controller(tale.getScene(), new HashMap<String,String>(), new CliTaleView(false));
        controller.startScene();
    }

}
