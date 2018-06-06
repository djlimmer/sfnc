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
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Doug
 */
public class Creature {
    String name;
    ChallengeRating CR;
    Alignment alignment;
    Size size;
    String array;
    String type;
    Boolean useTypeAdjustments;
    Integer typeOption;
    // typeOption: 0 - no choice, 1 - first choice, 2 - second choice, 3 - third choice
    // int for Animals, save bonus for Humanoid and Outsider.
    List<String> generalSubtypes;
    List<String> humanoidSubtypes;
    List<String> outsiderSubtypes;
    List<String> freeformSubtypes;
    List<Ability> chosenAbilities;
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
    
    // temporary status variables
    Boolean hasChanged;
    
    Creature() {
        this.name = "";
        this.CR = ChallengeRating.NONE;
        this.alignment = Alignment.NEUTRAL;
        this.size = Size.MEDIUM;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.typeOption = 0;
        this.hasChanged = true;
        this.generalSubtypes = new ArrayList<String>();
        this.humanoidSubtypes = new ArrayList<String>();
        this.outsiderSubtypes = new ArrayList<String>();
        this.freeformSubtypes = new ArrayList<String>();
        this.chosenAbilities = new ArrayList<Ability>();
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
    
    List<Ability> getChosenAbilities() {
        return chosenAbilities;
    }
    
    public Boolean useTypeAdjustments() {
        return useTypeAdjustments;
    }

    public Integer getTypeOption() {
        return typeOption;
    }
    
    public List<String> getGeneralSubtypes() {
        return generalSubtypes;
    }
    
    public List<String> getHumanoidSubtypes() {
        return humanoidSubtypes;
    }
    
    public List<String> getOutsiderSubtypes() {
        return outsiderSubtypes;
    }
    
    public List<String> getFreeformSubtypes() {
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
        this.generalSubtypes = new ArrayList<>();
        this.generalSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setHumanoidSubtypes(List<String> s) {
        this.humanoidSubtypes = new ArrayList<>();
        this.humanoidSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setOutsiderSubtypes(List<String> s) {
        this.outsiderSubtypes = new ArrayList<>();
        this.outsiderSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setFreeformSubtypes(List<String> s) {
        this.freeformSubtypes = new ArrayList<>();
        this.freeformSubtypes.addAll(s);
        this.hasChanged = true;
    }
    
    public void setChosenAbilities(List<String> a) {
        this.chosenAbilities = new ArrayList<>();
        a.stream().forEach((String s) -> {
            if (Ability.getAbility(s)!=null)
                this.chosenAbilities.add(new Ability(Ability.getAbility(s)));
        });
        this.hasChanged = true;
    }
    
    public void addAbility(Ability a) {
        Ability aa = new Ability(a);
        this.chosenAbilities.add(aa);
        this.hasChanged = true;
    }
    
    public void dropAbility(Ability a) {
        this.hasChanged = this.chosenAbilities.remove(a);
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
        // 1 : invalid file format (expecting an int)
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
            this.array = reader.readLine();
            this.type = reader.readLine();
            this.useTypeAdjustments = !("false".equals(reader.readLine()));
            this.typeOption = Integer.parseInt(reader.readLine());
            String subtypeString = reader.readLine();
            if(subtypeString.equals(""))
                this.generalSubtypes = new ArrayList<>();
            else
                this.generalSubtypes = new ArrayList<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.humanoidSubtypes = new ArrayList<>();
            else
                this.humanoidSubtypes = new ArrayList<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.outsiderSubtypes = new ArrayList<>();
            else
                this.outsiderSubtypes = new ArrayList<>(Arrays.asList(subtypeString.split(",")));
            if((subtypeString = reader.readLine()).equals(""))
                this.freeformSubtypes = new ArrayList<>();
            else
                this.freeformSubtypes = new ArrayList<>(Arrays.asList(subtypeString.split(",")));
            this.chosenAbilities = new ArrayList<>();
            Integer n = Integer.parseInt(reader.readLine());
            for (Integer i = 0; i < n; i++) {
                Ability a = new Ability();
                a.loadString(reader.readLine());
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
                writer.println(a.saveString());
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
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (saving to file)");
        }
        hasChanged = false;
    }

}
