/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Doug
 */
public class Attack {
    String name;
    Boolean highAttackModifier;
    Dice baseDamage;
    Integer damageModifier;
    Boolean bludgeoning;
    Boolean piercing;
    Boolean slashing;
    Boolean acid;
    Boolean cold;
    Boolean electricity;
    Boolean fire;
    Boolean sonic;
    String criticalEffect;
    
    Attack() {
        this.name = "";
        this.highAttackModifier = false;
        this.baseDamage = new Dice();
        this.damageModifier = 0;
        this.bludgeoning = false;
        this.piercing = false;
        this.slashing = false;
        this.acid = false;
        this.cold = false;
        this.electricity = false;
        this.fire = false;
        this.sonic = false;
        this.criticalEffect = "";
    }
    
    public String getName() {
        return name;
    }
    
    public Boolean hasHighAttackModifier() {
        return highAttackModifier;
    }
    
    public Dice getBaseDamage() {
        return baseDamage;
    }
    
    public Integer getDamageModifier() {
        return damageModifier;
    }
    
    public Boolean isBludgeoning() {
        return bludgeoning;
    }
    
    public Boolean isPiercing() {
        return piercing;
    }
    
    public Boolean isSlashing() {
        return slashing;
    }
    
    public Boolean isAcid() {
        return acid;
    }
    
    public Boolean isCold() {
        return cold;
    }
    
    public Boolean isElectricity() {
        return electricity;
    }
    
    public Boolean isFire() {
        return fire;
    }
    
    public Boolean isSonic() {
        return sonic;
    }
    
    public String getCriticalEffect() {
        return criticalEffect;
    }

    public void setName(String n) {
        name = n;
    }
    
    public void setHighAttackModifier(Boolean h) {
        highAttackModifier = h;
    }
    
    public void setBaseDamage(Dice d) {
        baseDamage = d;
    }
    
    public void setDamageModifier(Integer d) {
        damageModifier = d;
    }
    
    public void setBludgeoning(Boolean b) {
        bludgeoning = b;
    }
    
    public void setPiercing(Boolean p) {
        piercing = p;
    }
    
    public void setSlashing(Boolean s) {
        slashing = s;
    }
    
    public void setAcid(Boolean a) {
        acid = a;
    }
    
    public void setCold(Boolean c) {
        cold = c;
    }
    
    public void setElectricity(Boolean e) {
        electricity = e;
    }
    
    public void setFire(Boolean f) {
        fire = f;
    }
    
    public void setSonic(Boolean s) {
        sonic = s;
    }
    
    public void setCriticalEffect(String c) {
        criticalEffect = c;
    }
    
    public String makeSaveString() {
        String outputString = name + "|"
                + highAttackModifier + "|"
                + baseDamage + "|" + damageModifier + "|"
                + bludgeoning + "|" + piercing + "|" + slashing + "|"
                + acid + "|" + cold + "|" + electricity + "|" + fire + "|" + sonic + "|"
                + "ce:" + criticalEffect;
        
        return outputString;
    }
    
    public void makeFromLoadString(String s) {
        System.out.println("string passed in: " + s);
        List<String> attackParts = new ArrayList<>(Arrays.asList(s.split("\\|")));
        System.out.print("string split up: ");
        for (String st : attackParts) {
            System.out.print("*" + st + "* ");
        }
        System.out.println("");
        
        // this assumes the correct format; I should probably do error checking here
        if (attackParts.size() != 13)
            System.err.println("attackParts has " + attackParts.size() + " elements.");
        
        this.name = attackParts.get(0);
        this.highAttackModifier = ("true".equals(attackParts.get(1)));
        this.baseDamage = new Dice(attackParts.get(2));
        this.damageModifier = Integer.valueOf(attackParts.get(3));
        this.bludgeoning = ("true".equals(attackParts.get(4)));
        this.piercing = ("true".equals(attackParts.get(5)));
        this.slashing = ("true".equals(attackParts.get(6)));
        this.acid = ("true".equals(attackParts.get(7)));
        this.cold = ("true".equals(attackParts.get(8)));
        this.electricity = ("true".equals(attackParts.get(9)));
        this.fire = ("true".equals(attackParts.get(10)));
        this.sonic = ("true".equals(attackParts.get(11)));
        this.criticalEffect = attackParts.get(12).substring(3);
    }
}
