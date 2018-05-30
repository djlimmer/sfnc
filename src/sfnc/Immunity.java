/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Doug
 */
public class Immunity extends Ability {
    
    public static Set<String> listOfSenses = new HashSet<String>();

    Immunity() {
        id = "";
        location = Location.IMMUNITIES;
    }
    
    Immunity (String n) {
        id = n;
        location = Location.IMMUNITIES;
    }
}
