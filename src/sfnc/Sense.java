/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;


public class Sense extends Ability {
    Integer range;
    
    Sense() {
        this.name = "";
        this.location = Location.SENSES;
        this.range = 0;
    }
    
    Sense(String n, Integer r) {
        this.name = n;
        this.range = r;
        this.location = Location.SENSES;
    }
    
    public Integer getRange() {
        return range;
    }
    
    public void setRange(Integer r) {
        this.range = r;
    }
    
    @Override
    public String toString() {
        if (range==0)
            return name;
        else
            return name + " " + range.toString() + " ft.";
    }
}
