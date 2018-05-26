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
public class MainArray {
    public static final Integer NUMBER_OF_STATS = 18;
    public static final Integer NUMBER_OF_DAMAGES = 5;

    Integer EAC;
    Integer KAC;
    Integer fort;
    Integer ref;
    Integer will;
    Integer hitPoints;
    Integer abilityDC;
    Integer baseSpellDC;
    Integer abilityScoreModifier1;
    Integer abilityScoreModifier2;
    Integer abilityScoreModifier3;
    Integer specialAbilities;
    Integer masterSkillBonus;
    Integer masterSkillNumber;
    Integer goodSkillBonus;
    Integer goodSkillNumber;
    Integer highAttackBonus;
    Integer lowAttackBonus;
    Dice energyRangedDamage;
    Dice kineticRangedDamage;
    Dice standardMeleeDamage;
    Dice threeAttackMeleeDamage;
    Dice fourAttackMeleeDamage;
    
    MainArray() {
        EAC = 0;
        KAC = 0;
        fort = 0;
        ref = 0;
        will = 0;
        hitPoints = 0;
        abilityDC = 0;
        baseSpellDC = 0;
        abilityScoreModifier1 = 0;
        abilityScoreModifier2 = 0;
        abilityScoreModifier3 = 0;
        specialAbilities = 0;
        masterSkillBonus = 0;
        masterSkillNumber = 0;
        goodSkillBonus = 0;
        goodSkillNumber = 0;
        highAttackBonus = 0;
        lowAttackBonus = 0;
        energyRangedDamage = new Dice();
        kineticRangedDamage = new Dice();
        standardMeleeDamage = new Dice();
        threeAttackMeleeDamage = new Dice();
        fourAttackMeleeDamage = new Dice();
    }
    
    MainArray(MainArray m) {
        if (m == null)
            System.out.println("m is null!");
        EAC = m.EAC;
        KAC = m.KAC;
        fort = m.fort;
        ref = m.ref;
        will = m.will;
        hitPoints = m.hitPoints;
        abilityDC = m.abilityDC;
        baseSpellDC = m.baseSpellDC;
        abilityScoreModifier1 = m.abilityScoreModifier1;
        abilityScoreModifier2 = m.abilityScoreModifier2;
        abilityScoreModifier3 = m.abilityScoreModifier3;
        specialAbilities = m.specialAbilities;
        masterSkillBonus = m.masterSkillBonus;
        masterSkillNumber = m.masterSkillNumber;
        goodSkillBonus = m.goodSkillBonus;
        goodSkillNumber = m.goodSkillNumber;
        highAttackBonus = m.highAttackBonus;
        lowAttackBonus = m.lowAttackBonus;
        energyRangedDamage = new Dice(m.energyRangedDamage);
        kineticRangedDamage = new Dice(m.kineticRangedDamage);
        standardMeleeDamage = new Dice(m.standardMeleeDamage);
        threeAttackMeleeDamage = new Dice(m.threeAttackMeleeDamage);
        fourAttackMeleeDamage = new Dice(m.fourAttackMeleeDamage);
    }
    
    MainArray(Integer[] s, Dice[] d) {
        EAC = s[0];
        KAC = s[1];
        fort = s[2];
        ref = s[3];
        will = s[4];
        hitPoints = s[5];
        abilityDC = s[6];
        baseSpellDC = s[7];
        abilityScoreModifier1 = s[8];
        abilityScoreModifier2 = s[9];
        abilityScoreModifier3 = s[10];
        specialAbilities = s[11];
        masterSkillBonus = s[12];
        masterSkillNumber = s[13];
        goodSkillBonus = s[14];
        goodSkillNumber = s[15];
        highAttackBonus = s[16];
        lowAttackBonus = s[17];
        energyRangedDamage = new Dice(d[0]);
        kineticRangedDamage = new Dice(d[1]);
        standardMeleeDamage = new Dice(d[2]);
        threeAttackMeleeDamage = new Dice(d[3]);
        fourAttackMeleeDamage = new Dice(d[4]);
    }
}
