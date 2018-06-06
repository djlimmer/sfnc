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
public enum Alignment {
    LAWFUL_GOOD     ("Lawful Good","LG"),
    NEUTRAL_GOOD    ("Neutral Good","NG"),
    CHAOTIC_GOOD    ("Chaotic Good","CG"),
    LAWFUL_NEUTRAL  ("Lawful Neutral","LN"),
    NEUTRAL         ("Neutral","N"),
    CHAOTIC_NEUTRAL ("Chaotic Neutral","CN"),
    LAWFUL_EVIL     ("Lawful Evil","LE"),
    NEUTRAL_EVIL    ("Neutral Evil","NE"),
    CHAOTIC_EVIL    ("Chaotic Evil","CE");
    
    private final String displayString;
    private final String abbrev;
    
    Alignment(String d, String a) {
        this.displayString = d;
        this.abbrev = a;
    }
    
    @Override
    public String toString() {
        return displayString;
    }
    
    public String getAbbrev() {
        return abbrev;
    }
}
