package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 22/06/15.
 *
 */
public class Action extends AbstractConditional implements Serializable, Conditional {

    public Scene nextSceneObj;
    public Dialog nextDialogObj;

    @Attribute
    private String keys;
    @Attribute(required=false)
    private String description;
    @Attribute(required=false)
    private String nextScene;
    @Attribute(required=false)
    private String nextDialog;
    @Attribute(required=false)
    private Condition doImmediatelyCondition;
    @ElementList(required=false,entry="audio",inline=true)
    private List<Audio> audioList = new ArrayList<>();
    @ElementList(required=false,entry="state-change",inline=true)
    private List<StateChange> stateChangeList = new ArrayList<>();


    @Validate
    public void validate() throws Exception {
        if(getSeparatedKeys().length==0)
            throw new Exception("Action must have at least one key: "+keys);

        if(getNextScene()!=null && getNextDialog()!=null)
            throw new Exception("An action cannot have both a next dialog and next scene...");
    }

    public boolean goesSomewhere(){
        return getNextScene()!=null || getNextDialog()!=null;
    }



    public String[] getSeparatedKeys() {
        return keys.trim().split("\\s*,\\s*");
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNextScene() {
        return nextScene;
    }

    public void setNextScene(String nextScene) {
        this.nextScene = nextScene;
    }

    public String getNextDialog() {
        return nextDialog;
    }

    public void setNextDialog(String nextDialog) {
        this.nextDialog = nextDialog;
    }

    public Condition getDoImmediatelyCondition() {
        return doImmediatelyCondition;
    }

    public void setDoImmediatelyCondition(Condition doImmediatelyCondition) {
        this.doImmediatelyCondition = doImmediatelyCondition;
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public List<StateChange> getStateChangeList() {
        return stateChangeList;
    }

    public void setStateChangeList(List<StateChange> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

}
