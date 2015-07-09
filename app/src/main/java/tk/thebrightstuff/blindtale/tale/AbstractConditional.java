package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by niluje on 08/07/15.
 *
 */
public abstract class AbstractConditional implements Conditional, Serializable {

    @Attribute(required=false)
    private Condition condition;

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public boolean isConditionValid(Map<String,String> state){
        return condition==null || condition.check(state);
    }

}
