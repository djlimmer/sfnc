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
public class AbilityModifier {
    AbilityModifierChoice abilityModifierChoice;
    Integer customValue;
    
    AbilityModifier() {
        this.abilityModifierChoice = AbilityModifierChoice.NONE;
        this.customValue = 0;
    }
    
    AbilityModifier(AbilityModifierChoice c, Integer v) {
        this.abilityModifierChoice = c;
        this.customValue = v;
    }
    
    AbilityModifier(AbilityModifierChoice c) {
        this(c,0);
    }
    
    AbilityModifier(Integer v) {
        this(AbilityModifierChoice.CUSTOM,v);
    }
    
    AbilityModifier(AbilityModifier a) {
        this(a.abilityModifierChoice,a.customValue);
    }
    
    public AbilityModifierChoice getAbilityModifierChoice() {
        return abilityModifierChoice;
    }
    
    public Integer getCustomValue() {
        return customValue;
    }
    
    public void setAbilityModifierChoice(AbilityModifierChoice c) {
        abilityModifierChoice = c;
    }
    
    public void setCustomValue(Integer v) {
        customValue = v;
    }
}
