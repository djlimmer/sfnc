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
}
