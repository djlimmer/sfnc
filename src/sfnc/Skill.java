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
public class Skill {
    SkillChoice skillChoice;
    Integer customValue;
    
    Skill() {
        this.skillChoice = SkillChoice.NONE;
        this.customValue = 0;
    }
    
    Skill(SkillChoice c, Integer v) {
        this.skillChoice = c;
        this.customValue = v;
    }
    
    Skill(SkillChoice c) {
        this(c,0);
    }
    
    Skill(Integer v) {
        this(SkillChoice.CUSTOM,v);
    }
    
    public SkillChoice getSkillChoice() {
        return skillChoice;
    }
    
    public Integer getCustomValue() {
        return customValue;
    }
    
    public void setSkillChoice(SkillChoice c) {
        skillChoice = c;
    }
    
    public void setCustomValue(Integer v) {
        customValue = v;
    }
}
