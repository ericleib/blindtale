package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Validate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niluje on 17/06/15.
 *
 */
public class Scene implements Serializable {

    public Tale tale;

    private final Map<String,Dialog> dialogMap = new HashMap<>();

    @Attribute
    private String id;
    @Attribute(required=false)
    private String title;
    @Attribute(required=false)
    private boolean end;
    @ElementList(required=false,inline=true)
    private List<Audio> audioList = new ArrayList<>();
    @ElementList(required=false,inline=true)
    private List<Action> actionList = new ArrayList<>();
    @ElementList(required=false,entry="dialog",inline=true)
    private List<Dialog> dialogList = new ArrayList<>();
    @ElementList(required=false,entry="state-change",inline=true)
    private List<StateChange> stateChangeList = new ArrayList<>();


    @Validate
    public void validate() throws Exception {
        if(end && hasNextScene())
            throw new Exception("End scene has action/dialog leading to other scenes");

        if( (!end) && (!hasNextScene()) )
            throw new Exception("Scene is not ending but does not have action/dialog leading to other scenes");

        for(Dialog d: getDialogList())
            if(! dialogMap.containsKey(d.getId()))
                dialogMap.put(d.getId(), d);
            else
                throw new Exception("Duplicate dialog id: "+d.getId());

        for(Action a : getActionList()){
            if(a.getNextDialog()!=null){
                if(dialogMap.containsKey(a.getNextDialog()))
                    a.nextDialogObj = dialogMap.get(a.getNextDialog());
                else
                    throw new Exception("Action points to unknown dialog: "+a.getNextDialog());
            }
        }
        for (Dialog d : getDialogList()){
            for(Line l : d.getLineList()){
                if(l.getNextDialog()!=null){
                    if(dialogMap.containsKey(l.getNextDialog()))
                        l.nextDialogObj = dialogMap.get(l.getNextDialog());
                    else
                        throw new Exception("Dialog line points to unknown dialog: "+l.getNextDialog());
                }
            }
            d.scene = this;
        }
    }

    public boolean hasNextScene(){
        for(Action a : actionList)
            if(a.getNextScene()!=null)
                return true;

        for(Dialog d : dialogList)
            for(Line l: d.getLineList())
                if(l.getNextScene()!=null)
                    return true;

        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }

    public List<Dialog> getDialogList() {
        return dialogList;
    }

    public void setDialogList(List<Dialog> dialogList) {
        this.dialogList = dialogList;
    }

    public List<StateChange> getStateChangeList() {
        return stateChangeList;
    }

    public void setStateChangeList(List<StateChange> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

}
