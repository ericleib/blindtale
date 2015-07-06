package tk.thebrightstuff.blindtale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by niluje on 22/06/15.
 *
 */
public class Action implements Serializable, Conditional {

    public List<String> keys = new ArrayList<>();

    public String id;
    public String nextSceneId;
    public Scene nextScene;

    private Condition condition;

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
