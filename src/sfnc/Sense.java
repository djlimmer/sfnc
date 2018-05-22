/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;


public class Sense extends Ability {
    Integer range;
    
    Sense() {
        this.location = Location.SENSES;
        this.range = 0;
    }
    
    Integer getRange() {
        return range;
    }
    
    void setRange(Integer r) {
        this.range = r;
    }
}
