/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.tools.firepat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author a019349
 */
public class HTMLutil {
    String docHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n"+
"<HTML><HEAD><TITLE>FiRePat</TITLE><META content=\"IE=5.0000\" http-equiv=\"X-UA-Compatible\">"+
"<META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">"+
"</HEAD><BODY style='font-family=\"Arial,Helvertica,sans-serif\"'>"+
"<TABLE id=\"table_FiRePat\" style=\"border-collapse: collapse;\" border=\"1\" bordercolor=";
    String startTableHead = " <THEAD>";
    String endTableHead = " </THEAD>";
    String startTableBody = " <TBODY>";
    String endTableBody = " </TBODY>";
    String startTableRow = "  <TR>";
    String endTableRow = "  </TR>";
    String endDoc = "</TABLE></BODY></HTML>";
    String same_colour = "000000";
    String same_foreground_colour = "ffffff";
    String up_colour = "00ff00";
    String up_foreground_colour = "ffffff";
    String down_colour = "ff0000";
    String down_foreground_colour = "ffffff";
    String bordercolour = "d3d3d3";
    String RED = "ff0000";
    String GREEN = "00ff00";
    String BLUE = "0000ff";
    String CYAN = "00ffff";
    String MAGENTA = "ff00ff";
    String ORANGE = "ffa500";
    String BLACK = "000000";   
    String WHITE = "ffffff";
    String LIGHTGREY = "d3d3d3";
    String DARKGREY = "a9a9a9";
    double max_first_up; // to record largest increase in expression for seqs from 1st dataset
    double max_first_down; // ditto decrease
    double max_second_up; // to record largest increase in expression for seqs from 2nd dataset
    double max_second_down; // ditto decrease
    String[][] first_up; // to hold colour values for different levels of OFC for increased expression, 1st dataset
    String[][] first_down; // ditto decreased 
    String[][] second_up; // to hold colour values for different levels of OFC for increased expression, 2nd dataset
    String[][] second_down; // ditto decreased 
    String first_dataset_name = "First"; // identifier for the first set of data in output files
    String second_dataset_name = "Second"; // identifier for the second set of data in output files
    
    String latexDocHeader = "\\documentclass[10pt, a3paper]{article}\n" + 
        "\\usepackage[margin=1cm, bottom=2cm, landscape]{geometry}\n" +
        "\\usepackage{graphicx}\n\\usepackage{longtable}\n\\usepackage{multirow}\n\\usepackage[HTML]{xcolor}" +
        "\n\\usepackage{colortbl}\n\\usepackage{array}\n\n\\begin{document}\n" +
        "\\newcommand{\\evtablecell}[2]{#2}\n" +
        "\\newcommand{\\evzerotablecell}[1]{#1}\n" +
        "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
        "%                                                                          %\n" +
        "%   TO MAKE TABLE PLAIN BLACK AND WHITE COMMENT OUT THE NEXT THREE LINES   %\n" +
        "%                                                                          %\n" +
        "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
        "\\renewcommand{\\evtablecell}[2]{\\cellcolor{#1}{\\textcolor{white}{#2}}}\n" +
        "\\renewcommand{\\evzerotablecell}[1]{\\cellcolor{black}{\\textcolor{white}{#1}}}\n" +
        "\\arrayrulecolor{lightgray}\n\n\\sffamily\\small\n";

    String endLatexDoc = "\\end{longtable}\n\n\\end{document}\n";
   
    String[][] latex_colour_codes; // to hold latex colour definitions
    
    HTMLutil()
    {   // default values, will be reset when printing results
        max_first_up = 1;
        max_first_down = 1;
        max_second_up = 1;
        max_second_down = 1;      
    }
    // methods to return the text of the start and end of the document
    public String startDoc()
    {        
        return(docHeader+bordercolour+">");
    }
    public String startLatexDoc()
    {        
        return(latexDocHeader);
    }
    public String endDoc()
    {
        return(endDoc);
    }
    public String endLatexDoc()
    {
        return(endLatexDoc);
    }
    // methods to return the start and ending of table header
    public String startTableHead()
    {
        return(startTableHead);
    }
    public String startLatexTable(int nsamples, int ncolsout, boolean includeTransformedPattern, boolean output_first_dataset_first, String[] sample_names)
    {
        int num_dset_output_cells = nsamples+1;//number of columns for all data for one dataset
        if(includeTransformedPattern) num_dset_output_cells = num_dset_output_cells+1;
        int both_dset_outputcells = num_dset_output_cells * 2;
        String output = "\n\\begin{longtable}{|l";
        if(includeTransformedPattern) output = output+ "|l";
        for(int i=0; i<nsamples; i++) output = output+"|r";
        if(includeTransformedPattern) output = output+ "|l";
        output = output+ "|l";
        for(int i=0; i<nsamples; i++) output = output+"|r";
        output = output+"|l|}\n\\caption[FiRePat]{FiRePat} \\label{table:firepat} \\\\\n\\hline ";
        String header = "";
        
        if(output_first_dataset_first)
        {
            header = "\\multicolumn{"+num_dset_output_cells+"}{|c|}{"+first_dataset_name+"}";
            header = header + " & \\multicolumn{"+num_dset_output_cells+"}{c|}{"+second_dataset_name+"} & \\multicolumn{1}{c|}{\\multirow{2}{*}{Correlation}}";
            header = header + "\\\\\n\\cline{1-"+both_dset_outputcells+"} ";
        }
        else
        {
            header = "\\multicolumn{"+num_dset_output_cells+"}{|c|}{"+second_dataset_name+"}";
            header = header + " & \\multicolumn{"+num_dset_output_cells+"}{c|}{"+first_dataset_name+"} & \\multicolumn{1}{c|}{\\multirow{2}{*}{Correlation}}";
            header = header + "\\\\\n\\cline{1-"+both_dset_outputcells+"} ";
        }
        
        // following can be simplified
        if(output_first_dataset_first) header = header +  "\\multicolumn{1}{|c|}{ID}";
        else header = header + "\\multicolumn{1}{|c|}{ID}";
        if(includeTransformedPattern) header = header+ " & Pattern";
        for(int i=0; i<sample_names.length; i++) header = header+" & "+toLatex(sample_names[i]);
        if(output_first_dataset_first) header = header+" & \\multicolumn{1}{c|}{ID}";
        else header = header+" & \\multicolumn{1}{c|}{ID}";
        if(includeTransformedPattern) header = header+ " & Pattern";
        for(int i=0; i<sample_names.length; i++) header = header+" & "+toLatex(sample_names[i]);
        header = header+" & ";
        output = output+header +" \\\\ \\hline\\hline\n\\endfirsthead\n\\multicolumn{";   
        output = output+ncolsout+"}{c}{\\tablename\\ \\thetable{} -- continued from previous page} \\\\ \\hline\n";
        output = output+header+" \\\\ \\hline\\hline\n\\endhead\n\\hline \\multicolumn{";
        output = output+ncolsout+"}{r}{{Continued on next page}} \\\\\n\\endfoot \\hline \\endlastfoot\n";
        return(output);
    }
    // method to replace reserved characters etc with LaTeX commands for them.
    // NB order of checking is critical: \ first, then {, } and $, then others.
    // this is only a basic check, it checks all symbols on the standard UK QWERTY keyboard,
    // but it does not check unusual characters, eg non-ascii, accented, etc.
    private String toLatex(String s)
    { 
        String[] reserved_in = new String[]{"\\", "{", "}", "$", "#", "%", "^", "&", "_", "~"};
        String[] reserved_out = new String[] {"\\textbackslash{}", "\\{", "\\}", "\\$", "\\#", "\\%", "\\^{}", "\\&", "\\_", "\\~{}"};
        for(int i=0; i<reserved_in.length; i++) s = s.replace(reserved_in[i],reserved_out[i]); 
        String[] symbols_in = new String[]{"<", ">", "@", "§", "|", "±", "£"};
        String[] symbols_out = new String[]{"$<$", "$>$", "$@$", "\\S", "$|$", "\\pm", "\\pounds"};
        for(int i=0; i<symbols_in.length; i++) s = s.replace(symbols_in[i],symbols_out[i]); 
        return(s);
    }
    public String endTableHead()
    {
        return(endTableHead);
    }
    // methods to return the start and ending of BODY header
    public String startTableBody()
    {
        return(startTableBody);
    }
    public String endTableBody()
    {
        return(endTableBody);
    }
    // methods to return the start and ending of table row
    public String startTableRow()
    {
        return(startTableRow);
    }
    public String endTableRow()
    {
        return(endTableRow);
    }    
    // methods to return code for cell contents
    // for all these methods, input contents = contents of cell: id, pattern or numeric value
    // colnum = column number in output table
    // alignment = left/center/right; in practice always left blank, ie left, except header which is
    // set to "left" (default for html header is centre)
    public String plainHeaderCell(String contents, int colnum, String alignment)
    {
        if(alignment!="") alignment = " style=\"text-align: "+alignment+"\"";
        String plainHeaderCell = "   <TH class=\"header_"+colnum+"\""+alignment+">";        
        plainHeaderCell = plainHeaderCell + contents + "</TH>";
        return(plainHeaderCell);
    } 
    // plain black on white table cell
    public String plainTableCell(String contents, int colnum, String alignment)
    {
        if(alignment!="") alignment = " style=\"text-align: "+alignment+"\"";
        String plainTableCell = "   <TD class=\"column_"+colnum+"\""+alignment+">";        
        plainTableCell = plainTableCell + contents + "</TD>";
        return(plainTableCell);
    }
    // get html for table cell at start of series, white text on black background
    public String startTableCell(String contents, int colnum, String alignment)
    {
        if(alignment!="") alignment = " style=\"text-align: "+alignment+"\"";
        String sameTableCell = "   <TD style=\"background-color:"+same_colour+"; color:"+same_foreground_colour+"\" class=\"column_"+colnum+"\""+alignment+">";        
        sameTableCell = sameTableCell + contents + "</TD>";
        return(sameTableCell);
    }
    // get html for table cell at start of series, white text on black background
    public String startLatexTableCell(String contents)
    {
        String sameTableCell = " & \\evzerotablecell{" + contents + "}";
        return(sameTableCell);
    }
    // get html for main table cell, white text on coloured background, colour intensity according to value
    // THIS METHOD NEEDS REWRITING
    public String patternTableCell(String contents, int colnum, String alignment, boolean is_second)
    {
        String[] colours = getColours(contents, "U", is_second);
        String bkd = colours[0];
        String col = colours[1];
        if(alignment!="") alignment = " style=\"text-align: "+alignment+"\"";
        String upTableCell = "   <TD style=\"background-color:"+bkd+"; color:"+col+"\" class=\"column_"+colnum+"\""+alignment+">";        
        upTableCell = upTableCell + contents + "</TD>";
        return(upTableCell);
    }
    public String patternLatexTableCell(String contents,  boolean is_second)
    {
        String[] colours = getColours(contents, "U", is_second);
        String bkd = getLatexColour(colours[0]);
        String col = colours[1];// always white at present, may change, not used
        String tableCell = " & \\evtablecell{"+bkd+ "}{" + contents + "}";
        return(tableCell);
    }
    private String getLatexColour(String hexval)// returns predefined latex name for colour
    {
        String[] hexcols = latex_colour_codes[0];
        for(int i=0; i<hexcols.length; i++)
        {
            if(hexcols[i].equals(hexval)) return latex_colour_codes[1][i];
        }     
        return ("black");// gets here if hexval=000000, i.e. black
    }
    // method to adjust colour intensity according to expression value
    public String adjColour(int colour, double factor)
    {// multiply hex value by factor and ensure output has 2 digits (eg 05 not 5)
        String output = "";
        String[] val = Integer.toHexString((int) (colour*factor)).split("");
        if(val.length==1) output = "0"+val[0];
        else output = val[0]+val[1];        
        return(output);
    }
    // commands for setting colours of html output table
    public void setUpColour(int i)
    {
        String col = getColour(i);
        if(col!=null) up_colour = col;
    }
    public void setDownColour(int i)
    {
        String col = getColour(i);
        if(col!=null) down_colour = col;
    }
    public void setBorderColour(int i)
    {
        String col = getColour(i);
        if(col!=null) bordercolour = col;
    }
    // returns string of hex colour code for a given colour
    private String getColour(int i)
    {
        switch(i)
        {
            case 0: return(RED);
            case 1: return(GREEN);
            case 2: return(BLUE);
            case 3: return(CYAN);
            case 4: return(MAGENTA);
            case 5: return(ORANGE);
            case 6: return(BLACK);
            case 7: return(WHITE);
            case 8: return(LIGHTGREY); 
            case 9: return(DARKGREY); 
        }
        return(null);// should never get here
    }
    public void setMaxUpDown(double second_up_max, double second_down_max, double first_up_max, double first_down_max)
    {
        max_first_up = first_up_max;
        max_first_down = first_down_max;
        max_second_up = second_up_max;
        max_second_down = second_down_max;
    }
    // method to bin output OFC values for seqs for setting colour intensities
    public void setOutputColourIntensities(String[][] patternsNamesCorrs)
    {
        int nrows = patternsNamesCorrs.length;
        int ncols = patternsNamesCorrs[0].length;
        ArrayList<Double> first_up_binvals = new ArrayList<>();
        ArrayList<Double> first_down_binvals = new ArrayList<>();
        ArrayList<Double> second_up_binvals = new ArrayList<>();
        ArrayList<Double> second_down_binvals = new ArrayList<>();
        for(int i = 0; i<nrows; i++)
        {// get output for this row of table
            String[] rowi = patternsNamesCorrs[i];
            // now get OFC values
            int j = 3;// input index to start at first OFC value from 2nd dataset
            int x = (ncols-5)/2+2;// stop at last OFC value
            // second dataset
            while(j<x)
            {// add +ve and -ve values to appropriate ArrayList, ignore zeros
                double ij = Double.parseDouble(rowi[j]);
                if(ij>0) second_up_binvals.add(ij);
                if(ij<0) second_down_binvals.add(ij);
                j++;
            }
            // first dataset
            j = j + 3; // restart at first OFC value from 1st dataset                     
            x = ncols-1;// stopping at last OFC value (before correlation value)
            while(j<x)
            {// add +ve and -ve values to appropriate ArrayList, ignore zeros
                double ij = Double.parseDouble(rowi[j]);
                if(ij>0) first_up_binvals.add(ij);
                if(ij<0) first_down_binvals.add(ij);
                j++;
            }
        }
        first_up_binvals = ofcBinValues(first_up_binvals);
        first_down_binvals = ofcBinValues(first_down_binvals);
        second_up_binvals = ofcBinValues(second_up_binvals);
        second_down_binvals = ofcBinValues(second_down_binvals);
        first_up = setColValues(first_up_binvals, up_colour);
        first_down = setColValues(first_down_binvals, down_colour);
        second_up = setColValues(second_up_binvals, up_colour);
        second_down = setColValues(second_down_binvals, down_colour);        
    }
    private ArrayList<Double> ofcBinValues(ArrayList<Double> values)
    {        
        ArrayList<Double> vals = new ArrayList<>();
        vals.add((double)0);
        int imax = values.size();
        if(imax>0)// values to process
        {
            Double[] allvals = new Double[values.size()];
            Double[] allvalsabs = new Double[values.size()];
            values.toArray(allvals);
            // first make values absolute
            for(int j= 0; j<allvals.length; j++)
            {
                allvalsabs[j] = Math.abs(allvals[j]);
            }
            // now sort 
            Arrays.sort(allvalsabs);
            int j = 0;
            double lastvali = 0;
            for(int i = 1; i<imax; i++)
            {
                double vali = Math.abs(allvalsabs[i]);
                if(vali>lastvali)
                {
                    vals.add(vali);
                    lastvali = vali;
                }
            }
        }
        return vals;
    }
    String[][] setColValues(ArrayList<Double> input, String hexcolmax)
    {   // convert hex strings to ints
      //  System.out.println("setColValues");
        String[] allmax = hexcolmax.split("");
        int red = Integer.parseInt(allmax[0]+allmax[1], 16);
        int green = Integer.parseInt(allmax[2]+allmax[3], 16);
        int blue = Integer.parseInt(allmax[4]+allmax[5], 16);
        // for copy of input
        ArrayList<Double> values = new ArrayList<>();
        // check/adjust number of colour intensities
        int imax = input.size();
        int jmax = input.size();
        if(jmax>256) // copy input to values, but maximum possible length of values is 256
        {// so copy 256 'evenly' spaced elements from input to values
            jmax = 256; 
            values.add(input.get(0));
            double kncrement = (double)imax/(double)jmax;
            double kval = 0;
            for(int k =1; k<jmax-1; k++)
            {
                kval = kval + kncrement;
                int kvalint = (int)Math.round(kval);
                values.add(input.get(kvalint));
            }
            values.add(input.get(jmax-1));
        }
        else // just copy it
        {
            for(int i = 0; i<imax; i++) values.add(input.get(i));
        }
        // output[0] will contain OFC values, output[1] corresponding colour hex strings
        String[][] output = new String[2][jmax];
        int j = 0;
        double increment = 1;
        if(jmax<256) increment = 256/(double)jmax;
        double jval = 0;
        while(j<jmax)
        {   // now set level of colour and convert back to hex
            jval = jval + increment;
            int jvalint = (int)Math.round(jval);
            String hexj = adjColour(red, jval) + adjColour(green, jval) + adjColour(blue, jval);
            String valj  = Double.toString(values.get(j));
            output[1][j] = adjColour(red, jval) + adjColour(green, jval) + adjColour(blue, jval);
            output[0][j] = Double.toString(values.get(j));
            j++;            
        }      
        return output;
    }
    String getHexCol(double absvalue, String[][] colours)
    {// return the colour corresponding to the absvalue
        String output = "000000";// for safety, should not occur
        int i = colours[0].length;
        while(i>0)
        {
            i--;
            double ofci = Double.parseDouble(colours[0][i]);
            if(absvalue>=ofci) return(colours[1][i]); 
        }
        return output;
    }
    // method to adjust colour intensity according to OFC value
    public String[] getColours(String value, String type, boolean is_second)
    {
        String[] output = new String[2];
        String bkdcol = "";
        output[0] = same_colour;
        output[1] = same_foreground_colour;
        // this next line may need changing, because a small OFC may be classed as "S" even though there is some change
        if(type.equals("S")) return(output); // return white on black           
        double ofc = Double.parseDouble(value); // OFC value
        if(ofc==0) return(output); // no change, return white on black
        
        if(ofc>0) // increase
        {
            output[1] = up_foreground_colour;
            if(is_second)  bkdcol = getHexCol(ofc, second_up);
            else  bkdcol = getHexCol(ofc, first_up);     
        }
        else // decrease
        {
            output[1] = up_foreground_colour;
            ofc = Math.abs(ofc);
            if(is_second)  bkdcol = getHexCol(ofc, second_down);
            else  bkdcol = getHexCol(ofc, first_down);
        }
        output[0] = bkdcol; // background colour
        return(output);
    }
    public String[][] makeLatexColorCommands()
    {// returns array[][] containing all hex values used, latex names for them and latex code for these colours
        // first get ArrayList of all colours used
        ArrayList<String>all_colours = new ArrayList<>();
        String[] first_up_values = first_up[1];
        all_colours.add(first_up_values[0]);
        for(String s : first_up_values)
        {
            if(!all_colours.contains(s)) all_colours.add(s);
        }
        String[] second_up_values = second_up[1];
        for(String s : second_up_values)
        {
            if(!all_colours.contains(s)) all_colours.add(s);
        }
        String[] first_down_values = first_down[1];
        for(String s : first_down_values)
        {
            if(!all_colours.contains(s)) all_colours.add(s);
        }
        String[] second_down_values = second_down[1];
        for(String s : second_down_values)
        {
            if(!all_colours.contains(s)) all_colours.add(s);
        }
        // arrays to store values, names and latex code for them
        String[] hex_vals = new String[all_colours.size()];
        String[] colour_names = new String[hex_vals.length];
        String[] latex_code = new String[hex_vals.length];
        // put values, names and latex code in the arrays
        for(int i=0; i<hex_vals.length; i++)
        {
            String h = all_colours.get(i);
            hex_vals[i] = h;
            String n = makeLatexColourName(h);
            colour_names[i] = n;
            latex_code[i] = makeLatexColourCode(h, n);
        }    
        // store all in String[][]
        latex_colour_codes = new String[3][];
        latex_colour_codes[0] = hex_vals;// hex values
        latex_colour_codes[1] = colour_names;// names for these colours
        latex_colour_codes[2] = latex_code;// latex code for them
        return(latex_colour_codes);
    }
    private String makeLatexColourName(String h)
    {   // split hex value into r g and b
        String[] hchars = h.split("");
        // convert each to roman so that it can be used as a latex name for the colour
        String R = hexToRoman(Arrays.copyOfRange(hchars, 0, 2));
        String G = hexToRoman(Arrays.copyOfRange(hchars, 2, 4));
        String B = hexToRoman(Arrays.copyOfRange(hchars, 4, 6));        
        // make name from this (will be unique)
        String name = "R"+R+"G"+G+"B"+B;
        return(name);
    }
    private String hexToRoman(String[] s)
    {   // convert hex string to roman numeral
        String h = s[0]+s[1];
        if(h.equals("00")) return("");
        int a = Integer.parseInt(h, 16);        
        String output = String.valueOf(new char[a]).replace('\0', 'i')
            .replace("iiiii", "v").replace("iiii", "iv")
            .replace("vv", "x").replace("viv", "ix")
            .replace("xxxxx", "l").replace("xxxx", "xl")
            .replace("ll", "c").replace("lxl", "xc");// no need to go higher
        return(output);
    }
    private String makeLatexColourCode(String h, String n)
    {   // use n as colour name for hex value h
        String colour = "\\definecolor{"+n+"}{HTML}{"+h+"}";
        return(colour);
    }
    // names of datasets
    public void setFirstDatasetName(String s)
    {
        first_dataset_name = s;          
    }
    public void setSecondDatasetName(String s)
    {
        second_dataset_name = s;
    }        
}
