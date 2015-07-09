package tk.thebrightstuff.blindtale.tale;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.core.Validate;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by niluje on 08/07/15.
 *
 */
public class StateChange extends AbstractConditional implements Serializable, Conditional {

    public static final String
            INCREMENT = "increment",
            DECREMENT = "decrement",
            SET_TRUE = "set-true",
            SET_FALSE = "set-false",
            SET_ZERO = "set-zero";

    @Attribute
    private String change;
    @Attribute
    private String state;


    @Validate
    public void validate() throws Exception {
        if( ! (change.equals(INCREMENT) || change.equals(DECREMENT) || change.equals(SET_TRUE) || change.equals(SET_FALSE) || change.equals(SET_ZERO)) )
            throw new Exception("Unknown state change: "+change);
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public void changeState(Map<String,String> state){
        String prop = state.get(state);
        switch (change){
            case INCREMENT:
                if(prop==null)
                    state.put(this.state, "0");
                state.put(this.state, Double.toString(Double.parseDouble(state.get(this.state)) + 1));
                break;
            case DECREMENT:
                if(prop==null)
                    state.put(this.state, "0");
                state.put(this.state, Double.toString(Double.parseDouble(state.get(this.state)) - 1));
                break;
            case SET_ZERO:
                state.put(this.state, "0");
                break;
            case SET_TRUE:
                state.put(this.state, "true");
                break;
            case SET_FALSE:
                state.put(this.state, "false");
                break;
        }
    }
}
