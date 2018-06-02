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
    
    public static Set<Ability> setOfAbilities = new HashSet<Ability>();

    Ability() {
        this("",Location.UNDETERMINED,"~n~",1);
    }
    
    Ability(String n, Location l) {
        this(n,l,"~n~",1);
    }

    Ability(String n, Location l, String o) {
        this(n,l,o,1);
    }

    Ability(String n, Location l, String o, Integer c) {
        this.id = n;
        this.location = l;
        this.outputFormat = o;
        this.cost = c;
    }
    
    Ability(Ability a) {
        this(a.id, a.location, a.outputFormat, a.cost);
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
    
    @Override
    public String toString() {
        String outputString = outputFormat;
        outputString = outputString.replace("~n~", id);
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
