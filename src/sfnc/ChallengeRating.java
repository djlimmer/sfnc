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
public enum ChallengeRating {
    NONE        ( "\u2013",     0 ),
    THIRD       ( "1/3",      135 ),
    HALF        ( "1/2",      200 ),
    ONE         (   "1",      400 ),
    TWO         (   "2",      600 ),
    THREE       (   "3",      800 ),
    FOUR        (   "4",     1200 ),
    FIVE        (   "5",     1600 ),
    SIX         (   "6",     2400 ),
    SEVEN       (   "7",     3200 ),
    EIGHT       (   "8",     4800 ),
    NINE        (   "9",     6400 ),
    TEN         (   "10",    9600 ),
    ELEVEN      (   "11",   12800 ),
    TWELVE      (   "12",   19200 ),
    THIRTEEN    (   "13",   25600 ),
    FOURTEEN    (   "14",   38400 ),
    FIFTEEN     (   "15",   51200 ),
    SIXTEEN     (   "16",   76800 ),
    SEVENTEEN   (   "17",  102400 ),
    EIGHTEEN    (   "18",  153600 ),
    NINETEEN    (   "19",  204800 ),
    TWENTY      (   "20",  307200 ),
    TWENTYONE   (   "21",  409600 ),
    TWENTYTWO   (   "22",  614400 ),
    TWENTYTHREE (   "23",  819200 ),
    TWENTYFOUR  (   "24", 1228800 ),
    TWENTYFIVE  (   "25", 1638400 );
    
    private final String displayString;
    private final Integer XP;
    
    ChallengeRating(String d, Integer x) {
        this.displayString = d;
        this.XP = x;
    }
    
    @Override
    public String toString() { return displayString; }
    
    public Integer getXP() { return XP; }
}
