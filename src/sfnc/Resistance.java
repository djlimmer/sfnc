package sfnc;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Doug
 */
public class Resistance extends Ability {
    Integer amount;
    
    Resistance(String n, Integer a) {
        this.id = n;
        this.amount = a;
        this.location = Location.RESISTANCES;
        this.outputFormat = "~n~ ~a~";
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public void setAmount(Integer a) {
        amount = a;
    }

    @Override
    public String toString() {
        String outputString = outputFormat;
        outputString = outputString.replace("~n~",id);
        outputString = outputString.replace("~a~",Integer.toString(amount));
        return outputString;
    }
    
}
