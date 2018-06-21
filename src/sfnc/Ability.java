/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Doug
 */
public class Ability {
    String id;
    Location location;
    String outputFormat;
    Integer cost;
    Integer range;
    Integer DC;
    Integer amount;
    Dice dice;
    Integer diceBonus;
    String customText;
    
    public static Set<Ability> setOfAbilities = new HashSet<Ability>();

    Ability() {
        this("",Location.UNDETERMINED,"~n~",1,0,0,0,null,0,"");
    }
    
    Ability(String n, Location l) {
        this(n,l,"~n~",1,0,0,0,null,0,"");
    }

    Ability(String n, Location l, String o) {
        this(n,l,o,1,0,0,0,null,0,"");
    }

    Ability(String n, Location l, String o, Integer c) {
        this(n,l,o,c,0,0,0,null,0,"");
    }
    
    Ability(String n, Location l, String o, Integer c, Integer r,
            Integer dc, Integer a, Dice d, Integer db, String ct) {
        this.id = n;
        this.location = l;
        this.outputFormat = o;
        this.cost = c;
        this.range = r;
        this.DC = dc;
        this.amount = a;
        this.dice = new Dice(d);
        this.diceBonus = db;
        this.customText = ct;
    }

    Ability(Ability a) {
        this(a.id, a.location, a.outputFormat, a.cost, a.range, a.DC, a.amount, a.dice, a.diceBonus,a.customText);
    }
    
    public String getId() {
        return id;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public Integer getCost() {
        return cost;
    }
    
    public Integer getRange() {
        return range;
    }
    
    public Integer getDC() {
        return DC;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public Dice getDice() {
        return dice;
    }
    
    public Integer getDiceBonus() {
        return diceBonus;
    }
    
    public String getCustomText() {
        return customText;
    }    
    
    public void setId(String n) {
        id = n;
    }
    
    public void setLocation(Location l) {
        location = l;
    }
    
    public void setOutputFormat(String o) {
        this.outputFormat = o;
    }
    
    public void setCost(Integer c) {
        this.cost = c;
    }
    
    public void setRange(Integer r) {
        this.range = r;
    }
    
    public void setDC(Integer d) {
        this.DC = d;
    }
    
    public void setAmount(Integer a) {
        this.amount = a;
    }
    
    public void setDice(Dice d) {
        this.dice = new Dice(d);
    }
    
    public void setDiceBonus(Integer d) {
        this.diceBonus = d;
    }
    
    public void setCustomText(String ct) {
        this.customText = ct;
    }
    
    @Override
    public String toString() {
        String outputString = outputFormat;
        outputString = outputString.replace("~n~", id);
        outputString = outputString.replace("~r~", (range == null) ? "~r~" : Integer.toString(range));
        outputString = outputString.replace("~a~", (amount == null) ? "~a~" : Integer.toString(amount));
        outputString = outputString.replace("~t~", (customText == null) ? "~t~" : customText);
        return outputString;
    }

    public String makeSaveString() {
        String outputString = new String(id);
        
        outputString += "|" + location;
        outputString += "|" + outputFormat;
        outputString += "|" + cost;
        outputString += "|" + range;
        outputString += "|" + DC;
        outputString += "|" + amount;
        outputString += "|" + dice;
        outputString += "|" + diceBonus;
        outputString += "|ct:" + customText;
        
        return outputString;
    }

    public void makeFromLoadString(String s) {
        List<String> abilityParts = new ArrayList<>(Arrays.asList(s.split("\\|")));
        
        // this assumes the correct format; I should probably do error checking here
        if (abilityParts.size() != 10)
            System.err.println("abilityParts has " + abilityParts.size() + " elements.");
        
        this.id = abilityParts.get(0);
        this.location = Location.valueOf(abilityParts.get(1));
        this.outputFormat = abilityParts.get(2);
        this.cost = Integer.valueOf(abilityParts.get(3));
        this.range = Integer.valueOf(abilityParts.get(4));
        this.DC = Integer.valueOf(abilityParts.get(5));
        this.amount = Integer.valueOf(abilityParts.get(6));
        this.dice = new Dice(abilityParts.get(7));
        this.diceBonus = Integer.valueOf(abilityParts.get(8));
        this.customText = abilityParts.get(9).substring(3);
    }
    
    public static Ability getAbility(String n) {
        Optional<Ability> optionalAbility = setOfAbilities.stream()
                .filter((Ability a) -> a.id.equals(n))
                .findAny();
        
        if (!optionalAbility.isPresent())
            return null;
        
        return optionalAbility.get();
    }
}
