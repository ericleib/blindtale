package tk.thebrightstuff.blindtale.tale;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by niluje on 02/07/15.
 *
 */
public interface Conditional extends Serializable{

    Condition getCondition();

    void setCondition(Condition condition);

    boolean isConditionValid(Map<String,String> state);
}
