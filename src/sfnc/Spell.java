/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Doug
 */
public class Spell {
    String name;
    // a negative level means it's not on that class list
    Integer mysticLevel;
    Integer technomancerLevel;
    Boolean showDC;
    Boolean showRangedAttack;
    Boolean showMeleeAttack;

    public static Set<Spell> setOfSpells = new HashSet<>();

    Spell(String n, Integer ml, Integer tl, Boolean dc, Boolean r, Boolean m) {
        this.name = n;
        this.mysticLevel = ml;
        this.technomancerLevel = tl;
        this.showDC = dc;
        this.showRangedAttack = r;
        this.showMeleeAttack = m;
    }
    
    Spell(String n, Integer ml, Integer tl) {
        this(n,ml,tl,false,false,false);
    }
    
    Spell(Spell s) {
        this(s.name,s.mysticLevel,s.technomancerLevel,s.showDC,s.showRangedAttack,s.showMeleeAttack);
    }
    
    public String getName() {
        return name;
    }
    
    public Integer getMysticLevel() {
        return mysticLevel;
    }
    
    public Integer getTechnomancerLevel() {
        return technomancerLevel;
    }

    public Boolean getShowDC() {
        return showDC;
    }
    
    public Boolean getShowRangedAttack() {
        return showRangedAttack;
    }
    
    public Boolean getShowMeleeAttack() {
        return showMeleeAttack;
    }
    
    public String makeSaveString() {
        String outputString = name;
        
        outputString += "|" + mysticLevel;
        outputString += "|" + technomancerLevel;
        outputString += "|" + showDC;
        outputString += "|" + showRangedAttack;
        outputString += "|" + showMeleeAttack;
        
        return outputString;
    }
    
    public void makeFromLoadString(String s) {
        List<String> spellParts = new ArrayList<>(Arrays.asList(s.split("\\|")));
        
        // this assumes the correct format; I should probably do error checking here
        if (spellParts.size() != 6)
            System.err.println("stringParts has " + spellParts.size() + "elements.");
        
        this.name = spellParts.get(0);
        this.mysticLevel = Integer.valueOf(spellParts.get(1));
        this.technomancerLevel = Integer.valueOf(spellParts.get(2));
        this.showDC = Boolean.valueOf(spellParts.get(3));
        this.showRangedAttack = Boolean.valueOf(spellParts.get(4));
        this.showMeleeAttack = Boolean.valueOf(spellParts.get(5));
    }
    
    public String makeDisplayString(Integer dc) {
        String outputString = name;
        
        // not sure this is the correct way to display spell DC
        if (showDC)
            outputString += " (DC " + dc + ")";
        
        return outputString;
    }

    public static List<Spell> getSpellListByLevel(String type, Integer level) {
        List<Spell> spellList = new ArrayList<>();
        
        switch(type) {
            case "mystic":
                setOfSpells.stream().filter((s) -> (Objects.equals(s.mysticLevel, level))).forEach((s) -> {
                    spellList.add(s);
        });
                break;
            case "technomancer":
                setOfSpells.stream().filter((s) -> (Objects.equals(s.technomancerLevel, level))).forEach((s) -> {
                    spellList.add(s);
        });
                break;
            case "all":
                setOfSpells.stream().filter((s) -> ((Objects.equals(s.technomancerLevel, level)) || (Objects.equals(s.mysticLevel, level)))).forEach((s) -> {
                    spellList.add(s);
        });
                break;
            default:
                // return empty list
                break;
        }
        
        return spellList;
    }
    
    public Boolean showRangedAttack(Collection<Spell> spells) {
        return spells.stream().anyMatch((s) -> (s.showRangedAttack));
    }
    
    public Boolean showMeleeAttack(Collection<Spell> spells) {
        return spells.stream().anyMatch((s) -> (s.showMeleeAttack));
    }
    
    // this isn't sufficient; there are spells with the same name but different levels
    public static Spell getSpell(String n) {
        Optional<Spell> optionalSpell = setOfSpells.stream()
                .filter((Spell a) -> a.name.equals(n))
                .findAny();
        
        if (!optionalSpell.isPresent())
            return null;
        
        return optionalSpell.get();
    }
}
