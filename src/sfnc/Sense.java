/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.HashSet;
import java.util.Set;


public class Sense extends Ability {
    Integer range;
    
    Sense() {
        this.id = "";
        this.location = Location.SENSES;
        this.range = 0;
    }
    
    Sense(String n, Integer r) {
        this.id = n;
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
            return id;
        else
            return id + " " + range.toString() + " ft.";
    }
}
