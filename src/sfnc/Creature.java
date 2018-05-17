/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.text.NumberFormat;
import java.util.Locale;

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
    
    public String getXPString() {
        return CR.XP()==0 ? "\u2013" : NumberFormat.getNumberInstance(Locale.getDefault()).format(CR.XP());
    }
    
    public void setName(String n) {
        this.name = n;
    }
    
    public void setCR(ChallengeRating c) {
        this.CR = c;
    }
    
    public void setCRFromComboBox(Integer i) {
        // check for legal bounds here
        this.CR = ChallengeRating.values()[i];
    }
}
