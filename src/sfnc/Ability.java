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
    
    public static Set<Ability> setOfAbilities = new HashSet<Ability>();

    Ability() {
        this("",Location.UNDETERMINED,"~n~",1,0,0,0,null,0);
    }
    
    Ability(String n, Location l) {
        this(n,l,"~n~",1,0,0,0,null,0);
    }

    Ability(String n, Location l, String o) {
        this(n,l,o,1,0,0,0,null,0);
    }

    Ability(String n, Location l, String o, Integer c) {
        this(n,l,o,c,0,0,0,null,0);
    }
    
    Ability(String n, Location l, String o, Integer c, Integer r,
            Integer dc, Integer a, Dice d, Integer db) {
        this.id = n;
        this.location = l;
        this.outputFormat = o;
        this.cost = c;
        this.range = r;
        this.DC = dc;
        this.amount = a;
        this.dice = new Dice(d);
        this.diceBonus = db;
    }

    Ability(Ability a) {
        this(a.id, a.location, a.outputFormat, a.cost,a.range,a.DC,a.amount,a.dice,a.diceBonus);
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
    
    @Override
    public String toString() {
        String outputString = outputFormat;
        outputString = outputString.replace("~n~", id);
        outputString = outputString.replace("~r~", (range == null) ? "0" : Integer.toString(range));
        outputString = outputString.replace("~dc~", (DC == null) ? "0" : Integer.toString(DC));
        outputString = outputString.replace("~a~", (amount == null) ? "0" : Integer.toString(amount));
        String drollReplace = "";
        if (diceBonus != null && dice != null) {
            drollReplace += dice.toString();
            if (diceBonus < 0)
                drollReplace += Integer.toString(diceBonus);
            else if (diceBonus > 0)
                drollReplace += "+" + Integer.toString(diceBonus);
        }
        outputString = outputString.replace("~droll~",drollReplace);
        
        return outputString;
    }

    public String saveString() {
        String outputString = new String(id);
        
        outputString += "|" + location;
        outputString += "|" + outputFormat;
        outputString += "|" + cost;
        outputString += "|" + range;
        outputString += "|" + DC;
        outputString += "|" + amount;
        outputString += "|" + dice;
        outputString += "|" + diceBonus;
        
        return outputString;
    }

    public void loadString(String s) {
        List<String> abilityParts = new ArrayList<>(Arrays.asList(s.split("\\|")));
        
        // this assumes the correct format; I should probably do error checking here
        if (abilityParts.size() != 9)
            System.out.println("abilityParts has " + abilityParts.size() + "elements.");

        this.id = abilityParts.get(0);
        this.location = Location.valueOf(abilityParts.get(1));
        this.outputFormat = abilityParts.get(2);
        this.cost = Integer.getInteger(abilityParts.get(3));
        this.range = Integer.getInteger(abilityParts.get(4));
        this.DC = Integer.getInteger(abilityParts.get(5));
        this.amount = Integer.getInteger(abilityParts.get(6));
        this.dice = new Dice(abilityParts.get(7));
        this.diceBonus = Integer.getInteger(abilityParts.get(8));
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
