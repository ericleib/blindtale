package tk.thebrightstuff.blindtale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 22/06/15.
 */
public class Action implements Serializable {

    public List<String> keys = new ArrayList<String>();

    public String id;
    public String nextSceneId;
    public Scene nextScene;

    public Condition condition;

}
