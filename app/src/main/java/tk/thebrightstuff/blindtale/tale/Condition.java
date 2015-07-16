package tk.thebrightstuff.blindtale.tale;

import java.io.Serializable;
import java.util.Map;

import tk.thebrightstuff.blindtale.utils.StringUtils;

/**
 * Created by niluje on 24/06/15.
 *
 */
public class Condition implements Serializable {

    public String operator, operand1, operand2;

    public boolean check(Map<String,String> state){
        String op1 = state.get(operand1);
        if(op1==null){  // Handle default values
            if(operand2.equals("true") || operand2.equals("false"))
                op1 = "false";
            else if(StringUtils.isNumber(operand2))
                op1 = "0";
            else
                op1 = "null";
        }
        switch(operator){
            case "==": return StringUtils.isNumber(operand2)? Double.parseDouble(op1) == Double.parseDouble(operand2) : op1.equalsIgnoreCase(operand2);
            case "!=": return StringUtils.isNumber(operand2)? Double.parseDouble(op1) != Double.parseDouble(operand2) : ! op1.equalsIgnoreCase(operand2);
            case "<=": return Double.parseDouble(op1) <= Double.parseDouble(operand2);
            case "<": return Double.parseDouble(op1) <  Double.parseDouble(operand2);
            case ">=": return Double.parseDouble(op1) >= Double.parseDouble(operand2);
            case ">": return Double.parseDouble(op1) >  Double.parseDouble(operand2);
            default: return false;
        }
    }

    public String toString(){
        return operand1+" "+operator+" "+operand2;
    }


    public static Condition makeCondition(String input) throws Exception {

        if(input.trim().equalsIgnoreCase("true"))
            return makeDummyCondition(true);
        else if(input.trim().equalsIgnoreCase("false"))
            return makeDummyCondition(false);

        Condition c = new Condition();
        String[] tokens = input.trim().split("\\b");
        if(tokens.length==4){
            c.operand1 = tokens[1].trim();
            c.operator = tokens[2].trim();
            c.operand2 = tokens[3].trim();
        }else if(tokens.length==3){
            c.operand1 = tokens[0].trim();
            c.operator = tokens[1].trim();
            c.operand2 = tokens[2].trim();
        }else
            throw new Exception("Incorrect condition syntax: "+input);
        if(! (c.operator.equals("==") || c.operator.equals("!=") || c.operator.equals("<=") || c.operator.equals("<") || c.operator.equals(">=") || c.operator.equals(">")) )
            throw new Exception("Incorrect operator: "+c.operator);
        return c;
    }

    private static Condition makeDummyCondition(boolean isTrue){
        Condition c = new Condition();
        c.operand1 = "";
        c.operator = isTrue? "==" : "!=";
        c.operand2 = "false";
        return c;
    }
}
