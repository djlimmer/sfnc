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
    THIRD       ( "1/3",      135 
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   10, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    HALF        ( "1/2",      200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   10, 12, 2,  2,  0, 13,      9,     9,  3, 2, 1, 1,  9, 1,    4, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    ONE         (   "1",      400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   11, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWO         (   "2",      600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   13, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    THREE       (   "3",      800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   14, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    FOUR        (   "4",     1200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   16, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    FIVE        (   "5",     1600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   17, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    SIX         (   "6",     2400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   18, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    SEVEN       (   "7",     3200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   19, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    EIGHT       (   "8",     4800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   20, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    NINE        (   "9",     6400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   22, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TEN         (   "10",    9600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   23, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    ELEVEN      (   "11",   12800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   24, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWELVE      (   "12",   19200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   26, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    THIRTEEN    (   "13",   25600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   27, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    FOURTEEN    (   "14",   38400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   28, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    FIFTEEN     (   "15",   51200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   29, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    SIXTEEN     (   "16",   76800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   30, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    SEVENTEEN   (   "17",  102400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   31, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    EIGHTEEN    (   "18",  153600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   32, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    NINETEEN    (   "19",  204800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   33, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTY      (   "20",  307200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   35, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTYONE   (   "21",  409600
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   36, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTYTWO   (   "22",  614400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   38, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTYTHREE (   "23",  819200
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   10, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTYFOUR  (   "24", 1228800
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   10, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            ),
    TWENTYFIVE  (   "25", 1638400
    /*             EAC KAC F   R   W   HP  AbilDC  SplDC   ASM      SAs MSkls   GSkls   HAt LAt RDamE   RDamK   MDamS   MDam3   MDam4
       Combatant:   10, 12, 1,  1,  0,  6,      8,     8,  3, 1, 0, 1,  7, 1,    3, 2,    4,  1,  1d4,    1d4,    1d6,      0,      0
       
    */            );
    
    private final String displayString;
    private final Integer XP;
    
    ChallengeRating(String d, Integer x) {
        this.displayString = d;
        this.XP = x;
    }
    
    public String displayString() { return displayString; }
    public Integer XP() { return XP; }
}
