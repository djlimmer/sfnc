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
    
    // temporary status variables
    Boolean hasChanged;
    
    Creature() {
        this.name = "";
        this.CR = ChallengeRating.NONE;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.typeOption = 0;
        this.hasChanged = true;
        this.generalSubtypes = new ArrayList<String>();
        this.humanoidSubtypes = new ArrayList<String>();
        this.outsiderSubtypes = new ArrayList<String>();
        this.freeformSubtypes = new ArrayList<String>();
    }
    
    Creature(String n, ChallengeRating c) {
        this.name = n;
        this.CR = c;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.typeOption = 0;
        this.hasChanged = true;
        this.generalSubtypes = new ArrayList<String>();
        this.humanoidSubtypes = new ArrayList<String>();
        this.outsiderSubtypes = new ArrayList<String>();
        this.freeformSubtypes = new ArrayList<String>();
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
            this.array = reader.readLine();
            this.type = reader.readLine();
            this.useTypeAdjustments = !("false".equals(reader.readLine()));
            this.typeOption = Integer.parseInt(reader.readLine());
            this.generalSubtypes = new ArrayList<>(Arrays.asList(reader.readLine().split(",")));
            this.humanoidSubtypes = new ArrayList<>(Arrays.asList(reader.readLine().split(",")));
            this.outsiderSubtypes = new ArrayList<>(Arrays.asList(reader.readLine().split(",")));
            this.freeformSubtypes = new ArrayList<>(Arrays.asList(reader.readLine().split(",")));
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
            writer.println(array);
            writer.println(type);
            writer.println(useTypeAdjustments);
            writer.println(typeOption);
            writer.println(String.join(",",generalSubtypes));
            writer.println(String.join(",",humanoidSubtypes));
            writer.println(String.join(",",outsiderSubtypes));
            writer.println(String.join(",",freeformSubtypes));
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (saving to file)");
        }
        hasChanged = false;
    }

}
