/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import uk.ac.uea.cmp.srnaworkbench.tools.ToolBox;
import uk.ac.uea.cmp.srnaworkbench.utils.AppUtils;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;
import uk.ac.uea.cmp.srnaworkbench.utils.StringUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;

/**
 *
 * @author rew13hpu
 */
public class ServerApp {

    public static void main(String args[]) {
        try {
            Tools.getInstance();
            startWorkbench(parseArgs(args));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.err.println("WORKENCH ERROR: Unrecoverable exception occured: " + ex.toString());
            System.exit(1);
        }
    }

    /**
     * Parse the arguments and load them into a map.<br/>
     * Arguments are expected to start with a single '-'.<br/>
     * Key-value pairs are formatted as '-key value'.<br/>
     * Switches are formatted as '-switch' and placed in the map according to
     * their function.<br/>
     *
     * @param args
     * @return
     */
    private static Map<String, String> parseArgs(String[] args) {
        HashMap<String, String> argmap = new HashMap<String, String>();

        int i = 0;

        //paresnip2 allows the user to input multiple files for each option
        //this means we have to split the args in a slightly different way....
        //this is a tempory fix and a better solution should be found
        if (args.length > 1 && args[0].equals("-tool") && args[1].toLowerCase().equals("paresnip2")) {

            String option = null;
            for (i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    option = args[i].split("-")[1];
                    argmap.put(option, "");
                } else {
                    argmap.put(option, argmap.get(option) + " " + args[i]);
                }
            }

            //remove any leading and trailing spaces
            for (String s : argmap.keySet()) {
                argmap.put(s, argmap.get(s).trim());
            }

        } else {

            while (i < args.length) {
                String arg = args[i++];

                if ("-help".equals(arg) || "--help".equals(arg)) {
                    //TO_DO goToHelp some new help output here
                    ToolBox.UNKNOWN_TOOL.printUsage();
                    System.exit(0);
                }
                if ("-verbose".equals(arg) || "--verbose".equals(arg)) {
                    // verbose flag has special treatment
                    AppUtils.INSTANCE.setVerbose(true);
                    argmap.put("verbose", Boolean.TRUE.toString());
                } else if (arg.indexOf('-') == 0) {
                    // argument starts with '-' so need to work out if it's a flag or a key-value pair

                    // Remove the prefixes '-' or '--'
                    if (arg.indexOf("--") == 0) {
                        arg = StringUtils.safeSubstring(arg, 2, "");
                    }
                    if (arg.indexOf('-') == 0) {
                        arg = StringUtils.safeSubstring(arg, 1, "");
                    }

                    if (i < args.length) {
                        String nextarg = args[i];

                        if (nextarg.indexOf('-') == 0) {
                            // 'nextarg' is an argument (not a value) so store 'arg' in the map and move on
                            argmap.put(arg, "");
                        } else {
                            // key-value pair
                            argmap.put(arg, nextarg);
                            i++;
                        }
                    } else {
                        // 'arg' is the last argument so it's a flag on its own
                        argmap.put(arg, "");
                    }
                } else {
                    throw new IllegalArgumentException("CLI Error: Illegal argument syntax near: " + arg);
                }
            }
        }

        return argmap;
    }

    private static void startWorkbench(Map<String, String> argmap) throws IOException {
        // Make sure directories and files are present for both the command-line tools and the gui
        Tools.checkUserDirectoryData();

        // Assume that if the user has selected a particular tool they want to run in CLI mode.
        if (argmap.containsKey("tool")) {
            AppUtils.INSTANCE.setCommandLine(true);

            if (argmap.containsKey("basespace")) {
                AppUtils.INSTANCE.setBaseSpace(true);
            }

            //Tools.initialCLISetup();
            // Get the tool
            String toolName = argmap.get("tool");
            ToolBox tool = ToolBox.getToolForName(toolName);

            try {
                tool.startTool(argmap);

                System.exit(0);
            } catch (IllegalArgumentException e) {
                System.out.println("------------------------------------------------------------------------------------------");
                System.out.println(e.getMessage());
                tool.printUsage();
                System.out.println("------------------------------------------------------------------------------------------");

                System.exit(1);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, null, e);
                System.err.println("WORKENCH ERROR: Unrecoverable exception occured: " + e.toString());
                System.exit(1);
            }
        }
    }

}
