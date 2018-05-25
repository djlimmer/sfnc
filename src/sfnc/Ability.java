/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Doug
 */
public class Ability {
    String name;
    Location location;
    String outputFormat;
    
    public static Set<Ability> setOfAbilities = new HashSet<Ability>();

    Ability() {
        name = "";
        location = Location.UNDETERMINED;
        outputFormat = "~n~";
    }
    
    Ability(String n, Location l, String o) {
        this.name = n;
        this.location = l;
        this.outputFormat = o;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public void setName(String n) {
        name = n;
    }
    
    public void setLocation(Location l) {
        location = l;
    }
    
    public void setOutputFormat(String o) {
        this.outputFormat = o;
    }
    
    @Override
    public String toString() {
        String outputString = outputFormat;
        outputString = outputString.replace("~n~", name);
        return outputString;
    }
    
    public static Ability getAbility(String n) {
        return setOfAbilities.stream()
                .filter(a -> a.name.equals(n))
                .findAny().get();
    }
}
