package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 08/07/15.
 *
 */
public class Dialog implements Serializable {

    public Scene scene;

    @Attribute
    private String id;
    @ElementList(required=false,entry="audio",inline=true)
    private List<Audio> audioList = new ArrayList<>();
    @ElementList(entry="line",inline=true)
    private List<Line> lineList = new ArrayList<>();

    @Validate
    public void validate() throws Exception {
        if(lineList.size()==0)
            throw new Exception("A dialog must have at least one line");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public List<Line> getLineList() {
        return lineList;
    }

    public void setLineList(List<Line> lineList) {
        this.lineList = lineList;
    }

}
