/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.Collection;

/**
 *
 * @author Doug
 */
public class SpellWithUses extends Spell {
    // uses = 0: at will; uses < 0: constant
    Integer uses;
    
    SpellWithUses() {
        this.uses = 0;
    }
    
    SpellWithUses(Spell s, Integer u) {
        this();
        if (s==null)
            return;
        this.name = s.name;
        this.mysticLevel = s.mysticLevel;
        this.technomancerLevel = s.technomancerLevel;
        this.showDC = s.showDC;
        this.showMeleeAttack = s.showMeleeAttack;
        this.showRangedAttack = s.showRangedAttack;
        this.uses = u;
    }
    
    public Integer getUses() {
        return uses;
    }
    
    public void setUses(Integer u) {
        this.uses = u;
    }    
    
    public static Boolean showRangedAttackSWU(Collection<SpellWithUses> spells) {
        return spells.stream().anyMatch((s) -> (s.showRangedAttack));
    }
    
    public static Boolean showMeleeAttackSWU(Collection<SpellWithUses> spells) {
        return spells.stream().anyMatch((s) -> (s.showMeleeAttack));
    }
}
