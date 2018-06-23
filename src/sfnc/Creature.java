/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sfnc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author Doug
 */
public class Creature {
    String name;
    ChallengeRating CR;
    Alignment alignment;
    Size size;
    Boolean longReach;
    String array;
    String type;
    Boolean useTypeAdjustments;
    Integer typeOption;
    // typeOption: 0 - no choice, 1 - first choice, 2 - second choice, 3 - third choice
    // int for Animals, save bonus for Humanoid and Outsider.
    Set<String> generalSubtypes;
    Set<String> humanoidSubtypes;
    Set<String> outsiderSubtypes;
    Set<String> freeformSubtypes;
    Set<Ability> chosenAbilities;
    public Skill acrobatics;
    public Skill athletics;
    public Skill bluff;
    public Skill computers;
    public Skill culture;
    public Skill diplomacy;
    public Skill disguise;
    public Skill engineering;
    public Skill intimidate;
    public Skill lifeScience;
    public Skill medicine;
    public Skill mysticism;
    public Skill perception;
    public Skill physicalScience;
    public Skill piloting;
    public Skill profession;
    public Skill senseMotive;
    public Skill sleightOfHand;
    public Skill stealth;
    public Skill survival;
    AbilityModifier strength;
    AbilityModifier dexterity;
    AbilityModifier constitution;
    AbilityModifier intelligence;
    AbilityModifier wisdom;
    AbilityModifier charisma;
    Integer groundSpeed;
    Integer burrowSpeed;
    Integer climbSpeed;
    Integer flySpeed;
    String flyType;
    String flyManeuverability;
    Integer swimSpeed;
    List<Attack> meleeAttacks;
    List<Attack> rangedAttacks;
    Boolean usesSLAs;
    String spellType;
    Set<Spell> highSpells;
    Set<Spell> midSpells;
    Set<Spell> lowSpells;
    
    // temporary status variables
    Boolean hasChanged;
    
    Creature() {
        this.name = "";
        this.CR = ChallengeRating.NONE;
        this.alignment = Alignment.NEUTRAL;
        this.size = Size.MEDIUM;
        this.longReach = false;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.typeOption = 0;
        this.hasChanged = true;
        this.generalSubtypes = new HashSet<String>();
        this.humanoidSubtypes = new HashSet<String>();
        this.outsiderSubtypes = new HashSet<String>();
        this.freeformSubtypes = new HashSet<String>();
        this.chosenAbilities = new HashSet<Ability>();
        this.acrobatics = new Skill();
        this.athletics = new Skill();
        this.bluff = new Skill();
        this.computers = new Skill();
        this.culture = new Skill();
        this.diplomacy = new Skill();
        this.disguise = new Skill();
        this.engineering = new Skill();
        this.intimidate = new Skill();
        this.lifeScience = new Skill();
        this.medicine = new Skill();
        this.mysticism = new Skill();
        this.perception = new Skill(SkillChoice.GOOD);
        this.physicalScience = new Skill();
        this.piloting = new Skill();
        this.profession = new Skill();
        this.senseMotive = new Skill();
        this.sleightOfHand = new Skill();
        this.stealth = new Skill();
        this.survival = new Skill();
        this.strength = new AbilityModifier();
        this.dexterity = new AbilityModifier();
        this.constitution = new AbilityModifier();
        this.intelligence = new AbilityModifier();
        this.wisdom = new AbilityModifier();
        this.charisma = new AbilityModifier();
        this.groundSpeed = 30;
        this.burrowSpeed = 0;
        this.climbSpeed = 0;
        this.flySpeed = 0;
        this.flyType = "";
        this.flyManeuverability = "";
        this.swimSpeed = 0;
        meleeAttacks = new ArrayList<>();
        rangedAttacks = new ArrayList<>();
        this.usesSLAs = true;
        this.spellType = "";
        this.highSpells = new HashSet<>();
        this.midSpells = new HashSet<>();
        this.lowSpells = new HashSet<>();
    }
    
    Creature(String n, ChallengeRating c) {
        this();
        this.name = n;
        this.CR = c;
    }

    public String getName() {
        return name;
    }
    
    public ChallengeRating getCR() {
        return CR;
    }
    
    public String getCRDisplayString() {
        return CR.toString();
    }
    
    public Alignment getAlignment() {
        return alignment;
    }
    
    public Size getSize() {
        return size;
    }
    
    public Boolean hasTallReach() {
        return !longReach;
    }

    public Boolean hasLongReach() {
        return longReach;
    }
    
    public Integer getReach() {
        return longReach ? size.getReachLong() : size.getReachTall();
    }

    public Integer getXP() {
        return CR.getXP();
    }
    
    public String getXPString() {
        return CR.getXP()==0 ? "\u2013" : NumberFormat.getNumberInstance(Locale.getDefault()).format(CR.getXP());
    }
    
    String getArray() {
        return array;
    }
    
    String getType() {
        return type;
    }
    
    Set<Ability> getChosenAbilities() {
        return chosenAbilities;
    }
    
    public Boolean useTypeAdjustments() {
        return useTypeAdjustments;
    }

    public Integer getTypeOption() {
        return typeOption;
    }
    
    public Set<String> getGeneralSubtypes() {
        return generalSubtypes;
    }
    
    public Set<String> getHumanoidSubtypes() {
        return humanoidSubtypes;
    }
    
    public Set<String> getOutsiderSubtypes() {
        return outsiderSubtypes;
    }
    
    public Set<String> getFreeformSubtypes() {
        return freeformSubtypes;
    }
    
    public List<String> getAllSubtypes() {
        List<String> s = new ArrayList<>();
        if (generalSubtypes != null)
            s.addAll(generalSubtypes);
        if (humanoidSubtypes != null)
            s.addAll(humanoidSubtypes);
        if (outsiderSubtypes != null)
            s.addAll(outsiderSubtypes);
        if (freeformSubtypes != null)
            s.addAll(freeformSubtypes);
        
        return s;
    }
    
    public AbilityModifier getStrength() {
        return strength;
    }
    
    public AbilityModifier getDexterity() {
        return dexterity;
    }
    
    public AbilityModifier getConstitution() {
        return constitution;
    }
    
    public AbilityModifier getIntelligence() {
        return intelligence;
    }
    
    public AbilityModifier getWisdom() {
        return wisdom;
    }
    
    public AbilityModifier getCharisma() {
        return charisma;
    }
    
    public String getHighStat() {
        if (strength.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "strength";
        if (dexterity.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "dexterity";
        if (constitution.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "constitution";
        if (intelligence.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "intelligence";
        if (wisdom.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "wisdom";
        if (charisma.getAbilityModifierChoice() == AbilityModifierChoice.HIGH)
            return "charisma";
        return "none";
    }
    
    public String getMidStat() {
        if (strength.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "strength";
        if (dexterity.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "dexterity";
        if (constitution.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "constitution";
        if (intelligence.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "intelligence";
        if (wisdom.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "wisdom";
        if (charisma.getAbilityModifierChoice() == AbilityModifierChoice.MID)
            return "charisma";
        return "none";
    }
    
    public String getLowStat() {
        if (strength.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "strength";
        if (dexterity.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "dexterity";
        if (constitution.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "constitution";
        if (intelligence.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "intelligence";
        if (wisdom.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "wisdom";
        if (charisma.getAbilityModifierChoice() == AbilityModifierChoice.LOW)
            return "charisma";
        return "none";
    }
    
    public Integer getGroundSpeed() {
        return groundSpeed;
    }
    
    public Integer getBurrowSpeed() {
        return burrowSpeed;
    }
    
    public Integer getClimbSpeed() {
        return climbSpeed;
    }
    
    public Integer getFlySpeed() {
        return flySpeed;
    }
    
    public Integer getSwimSpeed() {
        return swimSpeed;
    }
    
    public String getFlyType() {
        return flyType;
    }
    
    public String getFlyManeuverability() {
        return flyManeuverability;
    }
    
    public Boolean isSpellcaster() {
        if (array.equals("Spellcaster"))
            return true;
        if (chosenAbilities == null)
            return false;
        return chosenAbilities.stream().anyMatch(a -> (a.getId().startsWith("secondary magic")));
    }
    
    public Boolean usesSLAs() {
        return isSpellcaster() && usesSLAs;
    }
    
    public Boolean usesSpells() {
        return isSpellcaster() && !usesSLAs;
    }
    
    public String getSpellType() {
        return spellType;
    }
    
    public Set<Spell> getHighSpells() {
        return highSpells;
    }
    
    public Set<Spell> getMidSpells() {
        return midSpells;
    }
    
    public Set<Spell> getLowSpells() {
        return lowSpells;
    }
    
    public Boolean hasSpell(Spell s) {
        if (highSpells.stream().anyMatch((z) -> (z.name.equals(s.name)))) {
            return true;
        }
        if (midSpells.stream().anyMatch((z) -> (z.name.equals(s.name)))) {
            return true;
        }
        return lowSpells.stream().anyMatch((z) -> (z.name.equals(s.name)));
    }
    
    public Boolean hasChanged() {
        return hasChanged;
    }
    
    public void setName(String n) {
        this.name = n;
        this.hasChanged = true;
    }
    
    public void setCR(ChallengeRating c) {
        this.CR = c;
        this.hasChanged = true;
    }
    
    public void setCRFromComboBox(Integer i) {
        // check for legal bounds here
        this.CR = ChallengeRating.values()[i];
        this.hasChanged = true;
    }

    public void setAlignment(Alignment a) {
        this.alignment = a;
        this.hasChanged = true;
    }
    
    public void setAlignmentFromComboBox(Integer i) {
        // check for legal bounds here
        this.alignment = Alignment.values()[i];
        this.hasChanged = true;
    }
    
    public void setSize(Size s) {
        this.size = s;
        this.hasChanged = true;
    }
    
    public void setSizeFromComboBox(Integer i) {
        // check for legal bounds here
        this.size = Size.values()[i];
        this.hasChanged = true;
    }
    
    public void setLongReach(Boolean r) {
        this.longReach = r;
        this.hasChanged = true;
    }
    
    void setArray(String a) {
        this.array = a;
        this.hasChanged = true;
    }

    void setType(String t) {
        this.type = t;
        this.hasChanged = true;
    }
    
    public void setUseTypeAdjustments(Boolean u) {
        this.useTypeAdjustments = u;
        this.hasChanged = true;
    }
    
    public void setTypeOption(Integer i) {
        if ((i == 0) || (i > 3))
            typeOption = 0;
        else
            typeOption = i;
        this.hasChanged = true;
    }
    
    public void setGeneralSubtypes(List<String> s) {
        this.generalSubtypes = new HashSet<>();
        this.generalSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setHumanoidSubtypes(List<String> s) {
        this.humanoidSubtypes = new HashSet<>();
        this.humanoidSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setOutsiderSubtypes(List<String> s) {
        this.outsiderSubtypes = new HashSet<>();
        this.outsiderSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setFreeformSubtypes(List<String> s) {
        this.freeformSubtypes = new HashSet<>();
        this.freeformSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setChosenAbilities(List<String> a) {
        this.chosenAbilities = new HashSet<>();
        a.stream().forEach((String s) -> {
            if (Ability.getAbility(s)!=null)
                this.chosenAbilities.add(new Ability(Ability.getAbility(s)));
        });
        this.hasChanged = true;
    }
    
    public void addAbility(Ability a) {
        Ability aa = new Ability(a);
        
        for (Ability i : chosenAbilities) {
            if (aa.getId().equals(i.getId()))
                return;
        }
        this.chosenAbilities.add(aa);
        this.hasChanged = true;
    }
    
    public void dropAbility(Ability a) {
        for (Ability i : chosenAbilities) {
            if (a.getId().equals(i.getId())) {
                chosenAbilities.remove(i);
                break;
            }
        }
        this.hasChanged = true;
    }
    
    public void dropSubtype(String s) {
        generalSubtypes.remove(s);
        humanoidSubtypes.remove(s);
        outsiderSubtypes.remove(s);
        this.hasChanged = true;
    }
    
    public void setStrength(AbilityModifier a) {
        this.strength = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setDexterity(AbilityModifier a) {
        this.dexterity = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setConstitution(AbilityModifier a) {
        this.constitution = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setIntelligence(AbilityModifier a) {
        this.intelligence = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setWisdom(AbilityModifier a) {
        this.wisdom = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setCharisma(AbilityModifier a) {
        this.charisma = new AbilityModifier(a);
        this.hasChanged = true;
    }
    
    public void setGroundSpeed(Integer s) {
        this.groundSpeed = s;
        this.hasChanged = true;
    }
    
    public void setBurrowSpeed(Integer s) {
        this.burrowSpeed = s;
        this.hasChanged = true;
    }
    
    public void setClimbSpeed(Integer s) {
        this.climbSpeed = s;
        this.hasChanged = true;
    }
    
    public void setFlySpeed(Integer s) {
        this.flySpeed = s;
        this.hasChanged = true;
    }
    
    public void setSwimSpeed(Integer s) {
        this.swimSpeed = s;
        this.hasChanged = true;
    }
    
    public void setFlyType(String s) {
        this.flyType = s;
        this.hasChanged = true;
    }
    
    public void setFlyManeuverability(String s) {
        this.flyManeuverability = s;
        this.hasChanged = true;
    }
    
    public void setUsesSLAs(Boolean b) {
        this.usesSLAs = b;
        this.hasChanged = true;
    }
    
    public void setSpellType(String s) {
        this.spellType = s;
        this.hasChanged = true;
    }
    
    public void setHighSpells(List<Spell> sl) {
        this.highSpells = new HashSet<>();
        sl.stream().forEach((Spell s) -> {
            this.highSpells.add(new Spell(s));
        });
        this.hasChanged = true;
    }
    
    public void setMidSpells(List<Spell> sl) {
        this.midSpells = new HashSet<>();
        sl.stream().forEach((Spell s) -> {
            this.midSpells.add(new Spell(s));
        });
        this.hasChanged = true;
    }
    
    public void setLowSpells(List<Spell> sl) {
        this.lowSpells = new HashSet<>();
        sl.stream().forEach((Spell s) -> {
            this.lowSpells.add(new Spell(s));
        });
        this.hasChanged = true;
    }
    
    public void addHighSpell(Spell s) {
        for(Spell t : highSpells) {
            if(s.getName().equals(t.getName()))
                return;
        }
        this.highSpells.add(new Spell(s));
        this.hasChanged = true;
    }
    
    public void addMidSpell(Spell s) {
        for(Spell t : midSpells) {
            if(s.getName().equals(t.getName()))
                return;
        }
        this.midSpells.add(new Spell(s));
        this.hasChanged = true;
    }
    
    public void addLowSpell(Spell s) {
        for(Spell t : lowSpells) {
            if(s.getName().equals(t.getName()))
                return;
        }
        this.lowSpells.add(new Spell(s));
        this.hasChanged = true;
    }
    
    public void dropHighSpell(Spell s) {
        highSpells.stream().filter((t) -> (t.name.equals(s.name))).forEach((t) -> {
            this.highSpells.remove(t);
        });
        this.hasChanged = true;
    }
    
    public void dropMidSpell(Spell s) {
        midSpells.stream().filter((t) -> (t.name.equals(s.name))).forEach((t) -> {
            this.midSpells.remove(t);
        });
        this.hasChanged = true;
    }
    
    public void dropLowSpell(Spell s) {
        lowSpells.stream().filter((t) -> (t.name.equals(s.name))).forEach((t) -> {
            this.lowSpells.remove(t);
        });
        this.hasChanged = true;
    }
    
    public void dropAllSpells() {
        highSpells = new HashSet<>();
        midSpells = new HashSet<>();
        lowSpells = new HashSet<>();
        this.hasChanged = true;
    }
    
    public void setChange() {
        this.hasChanged = true;
    }
    
    public void clearChange() {
        this.hasChanged = false;
    }
    
    public int openCreature(File file) {
        // output values:
        // 0 : no problems
        // 1 : invalid file format (expecting an int, or not an sfnc file)
        // -1 : IOException

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            if (!reader.readLine().matches("sfnc file")) {
                reader.close();
                return 1;
            }
            this.name = reader.readLine();
            this.CR = ChallengeRating.valueOf(reader.readLine());
            this.alignment = Alignment.valueOf(reader.readLine());
            this.size = Size.valueOf(reader.readLine());
            this.longReach = !("false".equals(reader.readLine()));
            this.array = reader.readLine();
            this.type = reader.readLine();
            this.useTypeAdjustments = !("false".equals(reader.readLine()));
            this.typeOption = Integer.parseInt(reader.readLine());
            String subtypeString = reader.readLine();
            if(subtypeString.equals(""))
                this.generalSubtypes = new HashSet<>();
            else
                this.generalSubtypes = new HashSet<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.humanoidSubtypes = new HashSet<>();
            else
                this.humanoidSubtypes = new HashSet<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.outsiderSubtypes = new HashSet<>();
            else
                this.outsiderSubtypes = new HashSet<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.freeformSubtypes = new HashSet<>();
            else
                this.freeformSubtypes = new HashSet<>(Arrays.asList(subtypeString.split(",")));
            this.chosenAbilities = new HashSet<>();
            Integer n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Ability a = new Ability();
                a.makeFromLoadString(reader.readLine());
                chosenAbilities.add(a);
            }
            String[] skillString;
            skillString = reader.readLine().split("\\|");
            acrobatics = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            athletics = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            bluff = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            computers = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            culture = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            diplomacy = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            disguise = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            engineering = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            intimidate = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            lifeScience = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            medicine = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            mysticism = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            perception = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            physicalScience = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            piloting = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            profession = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            senseMotive = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            sleightOfHand = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            stealth = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            skillString = reader.readLine().split("\\|");
            survival = new Skill(SkillChoice.valueOf(skillString[0]),Integer.valueOf(skillString[1]));
            strength = new AbilityModifier(reader.readLine());
            dexterity = new AbilityModifier(reader.readLine());
            constitution = new AbilityModifier(reader.readLine());
            intelligence = new AbilityModifier(reader.readLine());
            wisdom = new AbilityModifier(reader.readLine());
            charisma = new AbilityModifier(reader.readLine());
            this.groundSpeed = Integer.valueOf(reader.readLine());
            this.burrowSpeed = Integer.valueOf(reader.readLine());
            this.climbSpeed = Integer.valueOf(reader.readLine());
            this.flySpeed = Integer.valueOf(reader.readLine());
            this.flyType = reader.readLine();
            this.flyManeuverability = reader.readLine();
            this.swimSpeed = Integer.valueOf(reader.readLine());
            this.meleeAttacks = new ArrayList<>();
            n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Attack a = new Attack();
                a.makeFromLoadString(reader.readLine());
                meleeAttacks.add(a);
            }
            this.rangedAttacks = new ArrayList<>();
            n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Attack a = new Attack();
                a.makeFromLoadString(reader.readLine());
                rangedAttacks.add(a);
            }
            this.usesSLAs = !("false".equals(reader.readLine()));
            this.spellType = reader.readLine();
            n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Spell s = new Spell();
                s.makeFromLoadString(reader.readLine());
                highSpells.add(s);
            }
            n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Spell s = new Spell();
                s.makeFromLoadString(reader.readLine());
                midSpells.add(s);
            }
            n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Spell s = new Spell();
                s.makeFromLoadString(reader.readLine());
                lowSpells.add(s);
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (opening sfnc file)");
            return -1;
        }

        this.hasChanged = false;
        return 0;
    }
    
    public void saveCreature(File file) {
        try {
            PrintWriter writer = new PrintWriter(file.getPath(),"UTF-8");
            writer.println("sfnc file");
            writer.println(name);
            writer.println(CR.name());
            writer.println(alignment.name());
            writer.println(size.name());
            writer.println(longReach);
            writer.println(array);
            writer.println(type);
            writer.println(useTypeAdjustments);
            writer.println(typeOption);
            writer.println(String.join(",",generalSubtypes));
            writer.println(String.join(",",humanoidSubtypes));
            writer.println(String.join(",",outsiderSubtypes));
            writer.println(String.join(",",freeformSubtypes));
            writer.println(chosenAbilities.size());
            for (Ability a : chosenAbilities) {
                writer.println(a.makeSaveString());
            }
            writer.println(acrobatics.getSkillChoice()+"|"+acrobatics.getCustomValue());
            writer.println(athletics.getSkillChoice()+"|"+athletics.getCustomValue());
            writer.println(bluff.getSkillChoice()+"|"+bluff.getCustomValue());
            writer.println(computers.getSkillChoice()+"|"+computers.getCustomValue());
            writer.println(culture.getSkillChoice()+"|"+culture.getCustomValue());
            writer.println(diplomacy.getSkillChoice()+"|"+diplomacy.getCustomValue());
            writer.println(disguise.getSkillChoice()+"|"+disguise.getCustomValue());
            writer.println(engineering.getSkillChoice()+"|"+engineering.getCustomValue());
            writer.println(intimidate.getSkillChoice()+"|"+intimidate.getCustomValue());
            writer.println(lifeScience.getSkillChoice()+"|"+lifeScience.getCustomValue());
            writer.println(medicine.getSkillChoice()+"|"+medicine.getCustomValue());
            writer.println(mysticism.getSkillChoice()+"|"+mysticism.getCustomValue());
            writer.println(perception.getSkillChoice()+"|"+perception.getCustomValue());
            writer.println(physicalScience.getSkillChoice()+"|"+physicalScience.getCustomValue());
            writer.println(piloting.getSkillChoice()+"|"+piloting.getCustomValue());
            writer.println(profession.getSkillChoice()+"|"+profession.getCustomValue());
            writer.println(senseMotive.getSkillChoice()+"|"+senseMotive.getCustomValue());
            writer.println(sleightOfHand.getSkillChoice()+"|"+sleightOfHand.getCustomValue());
            writer.println(stealth.getSkillChoice()+"|"+stealth.getCustomValue());
            writer.println(survival.getSkillChoice()+"|"+survival.getCustomValue());
            writer.println(strength.getSaveString());
            writer.println(dexterity.getSaveString());
            writer.println(constitution.getSaveString());
            writer.println(intelligence.getSaveString());
            writer.println(wisdom.getSaveString());
            writer.println(charisma.getSaveString());
            writer.println(groundSpeed);
            writer.println(burrowSpeed);
            writer.println(climbSpeed);
            writer.println(flySpeed);
            writer.println(flyType);
            writer.println(flyManeuverability);
            writer.println(swimSpeed);
            writer.println(meleeAttacks.size());
            for (Attack a : meleeAttacks) {
                writer.println(a.makeSaveString());
            }
            writer.println(rangedAttacks.size());
            for (Attack a : rangedAttacks) {
                writer.println(a.makeSaveString());
            }
            writer.println(usesSLAs);
            writer.println(spellType);
            writer.println(highSpells.size());
            for (Spell s: highSpells) {
                writer.println(s.makeSaveString());
            }            
            writer.println(midSpells.size());
            for (Spell s: midSpells) {
                writer.println(s.makeSaveString());
            }            
            writer.println(lowSpells.size());
            for (Spell s: lowSpells) {
                writer.println(s.makeSaveString());
            }            
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (saving to file)");
        }
        hasChanged = false;
    }

}
