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
    
    Resistance(String n, Integer a) {
        this.id = n;
        this.amount = a;
        this.location = Location.RESISTANCES;
        this.outputFormat = "~n~ ~a~";
    }
}
