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
public class Immunity extends Ability {
    Immunity() {
        name = "";
        location = Location.IMMUNITIES;
    }
    
    Immunity (String n) {
        name = n;
        location = Location.IMMUNITIES;
    }
}
