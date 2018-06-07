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
    
    AbilityModifier(String s) {
        // s is a line from a save file: abilityModifierChoice|customValue
        String[] parts = s.split("\\|");

        this.abilityModifierChoice = AbilityModifierChoice.valueOf(parts[0]);
        this.customValue = Integer.valueOf(parts[1]);
    }
    
    public AbilityModifierChoice getAbilityModifierChoice() {
        return abilityModifierChoice;
    }
    
    public Integer getCustomValue() {
        return customValue;
    }
    
    public String getSaveString() {
        return abilityModifierChoice.name() + "|" + customValue.toString();
    }
    public void setAbilityModifierChoice(AbilityModifierChoice c) {
        abilityModifierChoice = c;
    }
    
    public void setCustomValue(Integer v) {
        customValue = v;
    }
}
