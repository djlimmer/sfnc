/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.HashSet;
import java.util.Set;

public class Sense extends Ability {

    public static Set<String> listOfSenses = new HashSet<>();
    
    Sense() {
        this.id = "";
        this.location = Location.SENSES;
        this.outputFormat = "~n~ ~r~ ft.";
        this.range = 0;
    }
    
    Sense(String n) {
        this.id = n;
        this.location = Location.SENSES;
        this.outputFormat = "~n~";
        this.range = 0;
    }

    Sense(String n, Integer r) {
        this.id = n;
        this.location = Location.SENSES;
        this.outputFormat = "~n~ ~r~ ft.";
        this.range = r;
    }
}
