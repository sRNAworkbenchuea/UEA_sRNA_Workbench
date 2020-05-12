/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javafx.scene.Cursor;
import uk.ac.uea.cmp.srnaworkbench.tools.paresnip2.fileinput.Paresnip2Configuration;

/**
 * User defined rule set
 *
 * @author rew13hpu
 */
public final class RuleSet {

    public static RuleSet rules;

    double maxScore;
    int maxAdjacentMM;
    int coreRegionStart;
    int coreRegionEnd;
    int maxMM;
    int maxGaps;
    int maxGUWobbles;
    int maxAdjacentMMCoreRegion;
    int maxMMCoreRegion;
    int maxGUCoreRegion;
    double wobbleScore;
    boolean allowMM10;
    boolean allowMM11;
    double scoreMM10;
    double scoreMM11;
    boolean GUCountAsMM;
    boolean GapCountAsMM;
    boolean allowGap10_11;
    double scoreGUWobble;
    double scoreMM;
    double coreRegionMultiplier;
    double scoreGap;
    boolean[] noMMAllowed;
    boolean[] mmAllowed;

    public int getMaxMMCoreRegion() {
        return maxMMCoreRegion;
    }

    public int getMaxAdjacentMMCoreRegion() {
        return maxAdjacentMMCoreRegion;
    }

    public double getGUWobbleScore() {
        return scoreGUWobble;
    }

    public double getMMScore() {
        return scoreMM;
    }

    public double getScoreMM10() {
        return scoreMM10;
    }

    public double getScoreMM11() {
        return scoreMM11;
    }

    public int getCoreRegionStart() {
        return coreRegionStart;
    }

    public int getCoreRegionEnd() {
        return coreRegionEnd;
    }

    public int getMaxMM() {
        return maxMM;
    }

    public double getCoreRegionMultiplier() {
        return coreRegionMultiplier;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public int getMaxAdjacentMM() {
        return maxAdjacentMM;
    }

    public int getMaxGaps() {
        return maxGaps;
    }

    public int getMaxGUWobbles() {
        return maxGUWobbles;
    }

    public boolean isAllowedMM10() {
        return allowMM10;
    }

    public boolean isAllowedMM11() {
        return allowMM11;
    }

    public boolean isAllowedGap10_11() {
        return allowGap10_11;
    }

    public double getGapScore() {
        return scoreGap;
    }

    public boolean setAllowedMM(int position) {
        mmAllowed[position] = !mmAllowed[position];
        return mmAllowed[position];

    }

    public boolean setNotAllowedMM(int position) {
        noMMAllowed[position] = !noMMAllowed[position];
        return noMMAllowed[position];

    }

    public boolean allowedMM(int position) {

        if (position < mmAllowed.length && position >= 0) {
            return mmAllowed[position];
        }

        return true;
    }

    public boolean notAllowedMM(int position) {
        if (position < noMMAllowed.length && position >= 0) {
            return noMMAllowed[position];
        }
        return false;
    }

    public boolean isGUCountAsMM() {
        return GUCountAsMM;
    }

    public boolean isGapCountAsMM() {
        return GapCountAsMM;
    }

    public void setMaxScore(double MAX_SCORE) {
        this.maxScore = MAX_SCORE;
    }

    public void setMaxAdjacentMM(int MAX_ADJACENT_MM) {
        this.maxAdjacentMM = MAX_ADJACENT_MM;
    }

    public void setCoreRegionStart(int CORE_REGION_START) {
        this.coreRegionStart = CORE_REGION_START;
    }

    public void setCoreRegionEnd(int CORE_REGION_END) {
        this.coreRegionEnd = CORE_REGION_END;
    }

    public void setMaxMM(int MAX_MM) {
        this.maxMM = MAX_MM;
    }

    public void setMaxGaps(int MAX_GAPS) {
        this.maxGaps = MAX_GAPS;
    }

    public void setMaxGUWobbles(int MAX_GU_WOBBLES) {
        this.maxGUWobbles = MAX_GU_WOBBLES;
    }

    public void setMaxAdjacentMMCoreRegion(int MAX_ADJACENT_MM_CORE_REGION) {
        this.maxAdjacentMMCoreRegion = MAX_ADJACENT_MM_CORE_REGION;
    }

    public void setMaxMMCoreRegion(int MAX_MM_CORE_REGION) {
        this.maxMMCoreRegion = MAX_MM_CORE_REGION;
    }

    public void setGUScore(double GU_MM_SCORE) {
        this.wobbleScore = GU_MM_SCORE;
    }

    public void setAllowMM10(boolean ALLOW_MM_10) {
        this.allowMM10 = ALLOW_MM_10;
    }

    public void setAllowMM11(boolean ALLOW_MM_11) {
        this.allowMM11 = ALLOW_MM_11;
    }

    public void setScoreMM10(double SCORE_MM_10) {
        this.scoreMM10 = SCORE_MM_10;
    }

    public void setScoreMM11(double SCORE_MM_11) {
        this.scoreMM11 = SCORE_MM_11;
    }

    public void setGUCountAsMM(boolean GUCountAsMM) {
        this.GUCountAsMM = GUCountAsMM;
    }

    public void setGapCountAsMM(boolean GapCountAsMM) {
        this.GapCountAsMM = GapCountAsMM;
    }

    public void setAllowGap10_11(boolean ALLOW_GAP_10_11) {
        this.allowGap10_11 = ALLOW_GAP_10_11;
    }

    public void setScoreGUWobble(double SCORE_GU_WOBBLE) {
        this.scoreGUWobble = SCORE_GU_WOBBLE;
    }

    public void setScoreMM(double SCORE_MM) {
        this.scoreMM = SCORE_MM;
    }

    public void setCoreRegionMultiplier(double CORE_REGION_MULTIPLIER) {
        this.coreRegionMultiplier = CORE_REGION_MULTIPLIER;
    }

    public void setGapScore(double SCORE_GAP) {
        this.scoreGap = SCORE_GAP;
    }

    public static RuleSet getRuleSet() {
        if (rules != null) {
            return rules;
        } else {
            rules = new RuleSet();
        }

        return rules;
    }

    public static void reset() {
        rules = null;
    }

    public void loadRules(File rulesFile) throws IOException, NumberFormatException {

        BufferedReader reader = new BufferedReader(new FileReader(rulesFile));

        try {
            this.setAllowMM10(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));
            this.setScoreMM10(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setAllowMM11(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));
            this.setScoreMM11(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setGapCountAsMM(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));
            this.setGUCountAsMM(Boolean.parseBoolean(reader.readLine().split("=")[1].trim()));
            this.setCoreRegionStart(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setCoreRegionEnd(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setCoreRegionMultiplier(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setMaxAdjacentMMCoreRegion(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxMMCoreRegion(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setScoreMM(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setGapScore(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setScoreGUWobble(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setMaxScore(Double.parseDouble(reader.readLine().split("=")[1].trim()));
            this.setMaxMM(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxGUWobbles(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxGaps(Integer.parseInt(reader.readLine().split("=")[1].trim()));
            this.setMaxAdjacentMM(Integer.parseInt(reader.readLine().split("=")[1].trim()));

            //set permissible and mismatches      
            this.noMMAllowed = new boolean[Paresnip2Configuration.getInstance().getMaxSmallRNALenth()];
            this.mmAllowed = new boolean[Paresnip2Configuration.getInstance().getMaxSmallRNALenth()];
            String splits[] = reader.readLine().split("=");
            if (splits.length > 1) {
                splits = splits[1].split(",");

                for (String s : splits) {
                    int pos = Integer.parseInt(s.trim());
                    //set it to the array index
                    pos = pos - 1;

                    rules.mmAllowed[pos] = true;
                }
            }

            //set non-permissible and mismatches      
            splits = reader.readLine().split("=");
            if (splits.length > 1) {
                splits = splits[1].split(",");

                for (String s : splits) {
                    int pos = Integer.parseInt(s.trim());
                    //set it to the array index
                    pos = pos - 1;

                    rules.noMMAllowed[pos] = true;
                }
            }
        } catch (IOException | NumberFormatException ex) {
            throw new IOException("There was an error reading the targeting rules file. Please generate a new one or use the default file provided.");
        }

    }

    private RuleSet() {

        //BUILD THIS TABLE WITH THE USERS NOT PERMITTED MM
        Paresnip2Configuration config = Paresnip2Configuration.getInstance();
        noMMAllowed = new boolean[config.getMaxSmallRNALenth()];

        //BUILD THIS TABLE WITH THE USERS PERMITTED MM
        mmAllowed = new boolean[config.getMaxSmallRNALenth()];
        setDefaultAllen();
    }

    public void setParameterSearchRules() {
        maxScore = 6;
        maxAdjacentMM = 3;
        maxAdjacentMMCoreRegion = 2;
        maxGaps = 1;
        //MAX_TOTAL_MM = 4.5;
        maxGUWobbles = 5;
        allowGap10_11 = false;
        allowMM10 = true;
        allowMM11 = true;
        scoreGap = 1;
        scoreGUWobble = 0.5;
        scoreMM = 1;
        scoreMM10 = 1;
        scoreMM11 = 1;
        coreRegionMultiplier = 2;
        maxMMCoreRegion = 3;
        maxMM = 6;
        GUCountAsMM = false;
        GapCountAsMM = true;
        wobbleScore = 0.5;
        coreRegionStart = 2;
        coreRegionEnd = 13;
    }

    public boolean isSetUp() {

        //check that the rules are sucessfully set up
        return true;

    }

    public String getPermissibleMM() {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 24; i++) {

            if (mmAllowed[i]) {
                sb.append((i + 1)).append(",");
            }

        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();

    }

    public String getNonPermissibleMM() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            if (noMMAllowed[i]) {
                sb.append((i + 1)).append(",");
            }

        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();

    }

    public void setDefaultCarrington() {
        maxScore = 4;
        maxAdjacentMM = 2;
        maxAdjacentMMCoreRegion = 1;
        maxGaps = 1;
        //MAX_TOTAL_MM = 4.5;
        maxGUWobbles = 4;
        allowGap10_11 = false;
        allowMM10 = true;
        allowMM11 = true;
        scoreGap = 1;
        scoreGUWobble = 0.5;
        scoreMM = 1;
        scoreMM10 = 1;
        scoreMM11 = 1;
        coreRegionMultiplier = 2;
        maxMMCoreRegion = 2;
        maxMM = 4;
        GUCountAsMM = false;
        GapCountAsMM = true;
        wobbleScore = 0.5;
        coreRegionStart = 2;
        coreRegionEnd = 13;
    }

    public void setDefaultAllen() {
        maxScore = 4;
        maxAdjacentMM = 2;
        maxAdjacentMMCoreRegion = 1;
        maxGaps = 1;
        //MAX_TOTAL_MM = 4.5;
        maxGUWobbles = 4;
        allowGap10_11 = false;
        allowMM10 = false;
        allowMM11 = false;
        scoreGap = 1;
        scoreGUWobble = 0.5;
        scoreMM = 1;
        scoreMM10 = 1;
        scoreMM11 = 1;
        coreRegionMultiplier = 2;
        maxMMCoreRegion = 2;
        maxMM = 4;
        GUCountAsMM = false;
        GapCountAsMM = true;
        wobbleScore = 0.5;
        coreRegionStart = 2;
        coreRegionEnd = 13;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("allow_mismatch_position_10=").append(rules.isAllowedMM10());
        sb.append(System.lineSeparator());
        sb.append("mismatch_position_10_penalty=").append(rules.getScoreMM10());
        sb.append(System.lineSeparator());
        sb.append("allow_mismatch_position_11=").append(rules.isAllowedMM11());
        sb.append(System.lineSeparator());
        sb.append("mismatch_position_11_penalty=").append(rules.getScoreMM11());
        sb.append(System.lineSeparator());
        sb.append("gaps_count_as_mismatch=").append(rules.isGapCountAsMM());
        sb.append(System.lineSeparator());
        sb.append("gu_count_as_mismatch=").append(rules.isGUCountAsMM());
        sb.append(System.lineSeparator());
        sb.append("core_region_start=").append(rules.getCoreRegionStart());
        sb.append(System.lineSeparator());
        sb.append("core_region_end=").append(rules.getCoreRegionEnd());
        sb.append(System.lineSeparator());
        sb.append("core_region_multiplier=").append(rules.getCoreRegionMultiplier());
        sb.append(System.lineSeparator());
        sb.append("max_adjacent_mismatches_core_region=").append(rules.getMaxAdjacentMMCoreRegion());
        sb.append(System.lineSeparator());
        sb.append("max_mismatches_core_region=").append(rules.getMaxMMCoreRegion());
        sb.append(System.lineSeparator());
        sb.append("mismatch_score=").append(rules.getMMScore());
        sb.append(System.lineSeparator());
        sb.append("gap_score=").append(rules.getGapScore());
        sb.append(System.lineSeparator());
        sb.append("gu_score=").append(rules.getGUWobbleScore());
        sb.append(System.lineSeparator());
        sb.append("max_score=").append(rules.getMaxScore());
        sb.append(System.lineSeparator());
        sb.append("max_mismatches=").append(rules.getMaxMM());
        sb.append(System.lineSeparator());
        sb.append("max_gu_pairs=").append(rules.getMaxGUWobbles());
        sb.append(System.lineSeparator());
        sb.append("max_gaps=").append(rules.getMaxGaps());
        sb.append(System.lineSeparator());
        sb.append("max_adjacent_mismatches=").append(rules.getMaxAdjacentMM());
        sb.append(System.lineSeparator());
        sb.append("permissible_mismatch_positions=").append(rules.getPermissibleMM());
        sb.append(System.lineSeparator());
        sb.append("non_permissible_mismatch_positions=").append(rules.getNonPermissibleMM());

        return sb.toString();
    }

    public void resetMMArrays(int maxSmallRNALength) {

        boolean newMMAllowed[] = new boolean[maxSmallRNALength];
        boolean newMMNotAllowed[] = new boolean[maxSmallRNALength];

        for (int i = 0; i < maxSmallRNALength; i++) {
            if (i < mmAllowed.length) {
                newMMAllowed[i] = mmAllowed[i];
            }
        }

        mmAllowed = newMMAllowed;

        for (int i = 0; i < maxSmallRNALength; i++) {
            if (i < noMMAllowed.length) {
                newMMNotAllowed[i] = noMMAllowed[i];
            }
        }

        noMMAllowed = newMMNotAllowed;
    }
}
