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
    
    // temporary status variables
    Boolean hasChanged;
    
    Creature() {
        this.name = "";
        this.CR = ChallengeRating.NONE;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.hasChanged = true;
    }
    
    Creature(String n, ChallengeRating c) {
        this.name = n;
        this.CR = c;
        this.array = "";
        this.type = "";
        this.useTypeAdjustments = true;
        this.hasChanged = true;
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
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong (saving to file)");
        }
        hasChanged = false;
    }

}
