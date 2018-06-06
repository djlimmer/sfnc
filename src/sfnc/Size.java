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
public enum Size {
    FINE        ("Fine",        "F",    "1/2 ft.",      0,  0),
    DIMINUTIVE  ("Diminutive",  "D",    "1 ft.",        0,  0),
    TINY        ("Tiny",        "T",    "2-1/2 ft.",    0,  0),
    SMALL       ("Small",       "S",    "5 ft.",        5,  5),
    MEDIUM      ("Medium",      "M",    "5 ft.",        5,  5),
    LARGE       ("Large",       "L",    "10 ft.",       10, 5),
    HUGE        ("Huge",        "H",    "15 ft.",       15, 10),
    GARGANTUAN  ("Gargantuan",  "G",    "20 ft.",       20, 15),
    COLOSSAL    ("Colossal",    "C",    "30 ft.",       30, 20);
    
    String displayString;
    String abbrev;
    String space;
    Integer reachTall;
    Integer reachLong;
    
    Size(String d, String a, String s, Integer rt, Integer rl) {
        this.displayString = d;
        this.abbrev = a;
        this.space = s;
        this.reachTall = rt;
        this.reachLong = rl;
    }
    
    @Override
    public String toString() {
        return this.displayString;
    }
    
    public String getAbbrev() {
        return this.abbrev;
    }
    
    public String getSpace() {
        return this.space;
    }
    
    public Integer getReachTall() {
        return this.reachTall;
    }
    
    public Integer getReachLong() {
        return this.reachLong;
    }
}
