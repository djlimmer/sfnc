/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

/**
 *
 * @author Doug
 */
public class Dice {
    Integer numberOfDice;
    Integer sidesOfDie;
    
    Dice() {
        this.numberOfDice = 0;
        this.sidesOfDie = 0;
    }
    
    Dice(Integer n, Integer s) {
        this.numberOfDice = Integer.max(n,0);
        this.sidesOfDie = Integer.max(s,0);
    }
    
    Dice(String d) {
        String[] dvals = d.split("d");
        if (dvals.length != 2) {
            this.numberOfDice = 0;
            this.sidesOfDie = 0;
        } else {
            this.numberOfDice = Integer.max(Integer.valueOf(dvals[0]),0);
            this.sidesOfDie = Integer.max(Integer.valueOf(dvals[1]),0);
        }
    }
    
    Dice(Dice d) {
        this.numberOfDice = d.numberOfDice;
        this.sidesOfDie = d.sidesOfDie;
    }
    
    @Override
    public String toString() {
        if ((numberOfDice == 0) || (sidesOfDie == 0))
            return "0";
        return numberOfDice.toString() + "d" + sidesOfDie.toString();
    }
}


