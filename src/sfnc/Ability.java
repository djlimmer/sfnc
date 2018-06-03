/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.HashSet;
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
        outputString = outputString.replace("~r~", Integer.toString(range));
        outputString = outputString.replace("~dc~", Integer.toString(DC));
        outputString = outputString.replace("~a~", Integer.toString(amount));
        if (diceBonus < 0) 
            outputString = outputString.replace("~droll~",dice.toString()+Integer.toString(diceBonus));
        else if (diceBonus == 0)
            outputString = outputString.replace("~droll~",dice.toString());
        else
            outputString = outputString.replace("~droll~",dice.toString()+"+"+Integer.toString(diceBonus));
        
        return outputString;
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
