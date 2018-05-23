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
public class Ability {
    String name;
    Location location;
    
    Ability() {
        name = "";
        location = Location.UNDETERMINED;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setName(String n) {
        name = n;
    }
    
    public void setLocation(Location l) {
        location = l;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
