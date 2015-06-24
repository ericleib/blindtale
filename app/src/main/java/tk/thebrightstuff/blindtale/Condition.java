package tk.thebrightstuff.blindtale;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by niluje on 24/06/15.
 *
 */
public class Condition implements Serializable {

    public String operator, operand1, operand2;

    public Condition(String input) throws Exception {
        String[] tokens = input.split("__");
        if(tokens.length!=3)
            throw new Exception("Incorrect condition syntax: "+input);
        operand1 = tokens[0];
        operator = tokens[1];
        operand2 = tokens[2];
    }

    public boolean check(Map<String,String> state){
        String op1 = state.get(operand1);
        if(op1==null)
            return false;
        switch(operator){
            case "eq": return op1.equals(operand2);
            default: return false;
        }
    }
}
