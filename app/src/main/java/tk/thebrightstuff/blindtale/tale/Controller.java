package tk.thebrightstuff.blindtale.tale;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tk.thebrightstuff.blindtale.speech.SpeechAdapter;
import tk.thebrightstuff.blindtale.speech.SpeechListener;
import tk.thebrightstuff.blindtale.utils.Log;
import tk.thebrightstuff.blindtale.utils.StringUtils;

/**
 * Created by niluje on 08/07/15.
 *
 */
public class Controller implements SpeechListener, AudioAdapter.CompletionListener {

    private final static String TAG = "Controller", SCENE = "scene";

    public final static int A_GAME_BY = 0, REPEAT = 1, PAUSE = 2, SKIP = 3, QUIT = 4 ;

    public final static int STATUS_READY = 0, STATUS_LISTENING = 1, STATUS_ANALYSIS = 2, STATUS_ERROR = 3, STATUS_HIDDEN = 4;

    private final static  List<Audio> dummy = new ArrayList<>();

    private Scene scene;
    private Dialog dialog;
    private final Map<String,String> state;
    private final TaleView view;

    private Map<String,UIAction> currentActions = new HashMap<>();

    private AudioAdapter currentAudio;


    public Controller(Scene scene, Map<String,String> state, TaleView view){
        this.scene = scene;
        this.state = state;
        this.view = view;
        view.setLocale(getTale().getLang());
        view.getSpeechAdapter().setSpeechListener(this);
    }

    public Tale getTale(){
        return scene.tale;
    }

    public void startScene(){
        startScene(false);
    }

    public void shutdown(){
        currentAudio.destroy();
        view.getSpeechAdapter().stopListening();
    }


    private void startScene(boolean skipAudio){
        view.getLog().info(TAG, "Starting scene: " + scene.getId());
        view.clean();   // Reset UI components

        String title = scene.getTitle()==null? getTale().getTitle() : scene.getTitle();
        view.setTitle(title);

        addActions(scene.getActionList());

        // Update state
        updateState();

        // Saving player's progress
        saveProgress();

        if(skipAudio){
            playAudioList(dummy, null);
        }else{
            playAudioList(scene.getAudioList(), null);
        }
    }


    private void startDialog(){ // From a dialog or action
        view.getLog().info(TAG, "Starting dialog: " + dialog.getId());
        view.clean();   // Reset UI components

        addActions(dialog.getLineList());

        playAudioList(dialog.getAudioList(), null);
    }


    private void endScene(){
        view.getLog().info(TAG, "Ending scene: " + scene.getId());
        currentActions.clear();
        currentAudio.stop();
        stopSpeechRecognition();
        view.setText("");
    }

    private void addActions(List<? extends Action> actionList) {
        currentActions.put(view.getNString(PAUSE), pauseAction);
        currentActions.put(view.getNString(SKIP), skipAction);
        currentActions.put(view.getNString(REPEAT), repeatAction);
        currentActions.put(view.getNString(QUIT), quitAction);
        for(Action a: actionList)
            if(a.isConditionValid(state))
                addActionButton(a);
    }

    private void performAction(Action action) {
        updateState(action.getStateChangeList());
        playAudioList(action.getAudioList(), action);
    }

    private void updateState() {
        state.put(SCENE + "_" + scene.getId() + "_visited", "true");
        String nb = state.get(SCENE + "_" + scene.getId() + "_visited_nb");
        if(nb==null){
            state.put(SCENE + "_" + scene.getId() + "_visited_nb", Integer.toString(1));
        }else{
            state.put(SCENE + "_" + scene.getId() + "_visited_nb", Integer.toString(Integer.parseInt(nb)+1));
        }
        updateState(scene.getStateChangeList());
    }

    private void updateState(List<StateChange> stateChangeList) {
        for(StateChange sc : stateChangeList)
            if(sc.isConditionValid(state))
                sc.changeState(state);
    }

    private void saveProgress(){
        File saveFile = scene.tale.getSaveFile();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
            bw.write("scene = " + scene.getId() + "\n");
            for(Map.Entry<String,String> e : state.entrySet())
                bw.write(e.getKey()+ " = "+e.getValue() + "\n");
            bw.close();
        } catch (IOException e) {
            view.showMessage("Could not save progress to file: "+saveFile.getAbsolutePath(), true);
            view.getLog().error(TAG, "Could not save progress to file: "+saveFile.getAbsolutePath(), e);
        }
    }

    private void showCredits(){
        String credits = "";
        credits += scene.tale.toString()+"\n";
        credits += view.getIString(A_GAME_BY)+"\n";
        for(Credit c : scene.tale.getCredits())
            credits += c.getRole()+": "+c.getName()+"\n";
        view.setText(credits);
    }





    // UI

    private void addActionButton(Action action) {
        UIAction uiAction = new UIAction(true, action) {
            @Override
            public void doAction() {
                performAction(action);
            }
        };
        String btnText = action.getSeparatedKeys()[0] + (action.getDescription()!=null? " ("+action.getDescription()+")" : "");
        view.addButton(btnText, uiAction);
        for(String key : action.getSeparatedKeys())
            currentActions.put(StringUtils.removeAccents(key), uiAction);
    }

    public final UIAction repeatAction = new UIAction(true, null) {
        @Override
        public void doAction() {
            view.updatePause(true, true);
            if(currentAudio.isPlaying())
                currentAudio.stop();
            audioIndex = 0;
            if(isMoreToPlay())
                playAudioList();
            else
                completed();
        }
    };

    public final UIAction pauseAction = new UIAction(true, null) {
        @Override
        public void doAction() {
            if (currentAudio.isPlaying()) {
                currentAudio.pause();
                view.updatePause(true, false);
            } else {
                currentAudio.resume();
                view.updatePause(true, true);
            }
        }
    };

    public final UIAction skipAction = new UIAction(true, null) {
        @Override
        public void doAction() {
            currentAudio.skip();
        }
    };

    public final UIAction quitAction = new UIAction(true, null) {
        @Override
        public void doAction() {
            view.finish();
        }
    };


    // AUDIO

    private int audioIndex;
    private List<Audio> audioList;
    private Action nextAction;
    private void playAudioList(List<Audio> audioList, Action action) {
        view.getLog().info(TAG, "Start playing");
        this.audioIndex = 0;
        this.audioList = audioList;
        this.nextAction = action;
        if(isMoreToPlay())
            playAudioList();
        else
            completed();
    }

    private void playAudioList(){
        Audio audio = audioList.get(audioIndex++);
        if(audio.isConditionValid(state)){
            currentAudio = view.getAudioProvider(audio);
            view.getLog().info(TAG, "Playing sound: " + currentAudio.toString());
            try{
                if(audio.getText()!=null){
                    view.setText(audio.getText());
                }
                currentAudio.setCompletionListener(this);
                currentAudio.play();
            }catch(Exception e){
                view.getLog().error(TAG, "Error playing sound: " + currentAudio.toString(), e);
                view.showMessage("Oops... There's a problem with that scene!", true);
            }
        }else if(isMoreToPlay()){
            playAudioList();
        }else{
            completed();
        }
    }

    private boolean isMoreToPlay(){
        return audioIndex < audioList.size();
    }

    /**
     * Called when sound has finished playing
     */
    @Override
    public void completed() {
        view.getLog().info(TAG, "Finished playing");

        if(isMoreToPlay()){

            playAudioList();

        }else{

            view.updatePause(false, true);  // Update Pause/Resume buttons

            if(nextAction==null){ // No next action: simply go on
                if(scene.isEnd())
                    showCredits();
                else{
                    for(UIAction a : currentActions.values()){  // Perform immediate actions
                        if(a.action!=null &&
                                a.action.getDoImmediatelyCondition()!=null &&
                                a.action.getDoImmediatelyCondition().check(state)){
                            a.doAction();
                            return;
                        }
                    }
                    startSpeechRecognition();    // If no immediate action, start listening
                }

            }else if(nextAction.getNextScene()!=null){  // We move to a new scene
                endScene();
                scene = nextAction.nextSceneObj;
                startScene();

            }else if(nextAction.getNextDialog()!=null){  // We start a new dialog
                endScene();
                dialog = nextAction.nextDialogObj;
                startDialog();

            }else{  // We go back to the current scene but skipping audio
                startScene(true);
            }

        }
    }



    // SPEECH RECOG

    private void startSpeechRecognition() {
        if(view.getSpeechAdapter().isAvailable()){
            view.getLog().info(TAG, "Starting speech recognition");
            view.getSpeechAdapter().startListening();
        }
    }

    private void stopSpeechRecognition() {
        if(view.getSpeechAdapter().isAvailable()){
            view.getLog().info(TAG, "Stopping speech recognition");
            view.getSpeechAdapter().stopListening();
            view.setSpeechProgress(STATUS_HIDDEN);
        }
    }

    private void processSpeechRecognitionResults(List<String> matches) {

        view.getLog().info(TAG, "Checking speech recognition results");

        view.setSpeechProgress(STATUS_ANALYSIS);

        view.showMessage("You said: " + matches.get(0), false);

        for(String match : matches){
            view.getLog().info(TAG, "Match: " + match);
            for(String word : match.split(" ")){
                word = StringUtils.removeAccents(word);
                if(currentActions.containsKey(word)) {
                    UIAction action = currentActions.get(word);
                    if(action.isActive){
                        view.getLog().info(TAG, "Word '" + word + "' matches active action!");
                        stopSpeechRecognition();
                        action.doAction();
                        return;
                    }
                }
            }
        }

        view.getLog().info(TAG, "Did not recognize a usable match: starting to listen again!");
        view.getSpeechAdapter().startListening();

    }



    @Override
    public void onReadyForSpeech() {
        view.getLog().info(TAG, "onReadyForSpeech");
        view.setSpeechProgress(STATUS_READY);
    }

    @Override
    public void onBeginningOfSpeech() {
        view.getLog().info(TAG, "onBeginningOfSpeech");
        view.setSpeechProgress(STATUS_LISTENING);
    }

    @Override
    public void onEndOfSpeech() {
        view.getLog().info(TAG, "onEndOfSpeech");
        view.setSpeechProgress(STATUS_LISTENING);
    }

    @Override
    public void onError(String errorMessage) {
        view.getLog().error(TAG, "Failed speech recognition: " + errorMessage, null);
        view.setSpeechProgress(STATUS_ERROR);
        view.getSpeechAdapter().startListening();
    }

    @Override
    public void onResults(List<String> results) {
        view.getLog().info(TAG, "onResults");
        processSpeechRecognitionResults(results);
    }

    @Override
    public void onPartialResults(List<String> results) {
        view.getLog().info(TAG, "onPartialResults");
        processSpeechRecognitionResults(results);
    }



    public interface TaleView {
        void clean();
        void addButton(String text, UIAction callback);
        void setTitle(String txt);
        void setText(String txt);
        void showMessage(String message, boolean longMsg);
        SpeechAdapter getSpeechAdapter();
        void setSpeechProgress(int status);
        AudioAdapter getAudioProvider(Audio audio);
        void updatePause(boolean enabled, boolean pauseResume);
        void setLocale(Locale locale);
        String getIString(int ref);
        String getNString(int ref);
        Log getLog();
        void finish();
    }

    public abstract class UIAction {
        Action action;
        boolean isActive;
        public UIAction(boolean isActive, Action action){
            this.isActive = isActive;
            this.action = action;
        }
        public abstract void doAction();
    }
}
