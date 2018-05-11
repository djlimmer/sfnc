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
public class Creature {
    String name;
    ChallengeRating CR;
    
    Creature() {
        this.name = "";
        this.CR = ChallengeRating.NONE;
    }
    
    Creature(String n, ChallengeRating c) {
        this.name = n;
        this.CR = c;
    }

    public String getName() {
        return name;
    }
    
    public ChallengeRating getCR() {
        return CR;
    }
    
    public String getCRDisplayString() {
        return CR.displayString();
    }
    
    public Integer getXP() {
        return CR.XP();
    }
}
