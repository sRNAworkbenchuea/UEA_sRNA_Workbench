/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.rfam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author w0445959
 */
public class RFAM_DatabaseAccess
{
    public static void main(String[] args)
    {
        
        getAccessions();
    }
    
    public static ArrayList<String> getAccessions()
    {
        // creates Connection objects
        Connection conn1 = null;
        
        ArrayList<String> accessions = new ArrayList<>();
       
        try {
            // connect to the RFAM Database
            String url1 = "jdbc:mysql://mysql-rfam-public.ebi.ac.uk:4497/Rfam";
            String user = "rfamro";
            Properties info = new Properties();
            info.put("user", user);
            //String password = "secret";
 
            conn1 = DriverManager.getConnection(url1, info);
            if (conn1 != null) {
                System.out.println("Connected to the RFAM database");
                //LOGGER.log(Level.INFO, "Connected to the RFAM database");
                
                Statement stmt = conn1.createStatement();
                ResultSet rs = stmt.executeQuery("select rfam_acc from family " +
                                    "where type like '%rRNA%' " +
                                    "or type like '%tRNA%'" + 
                                    "ORDER BY rfam_acc");
                
                while (rs.next())
                {
                    accessions.add(rs.getString("rfam_acc") + ".fa.gz");
                }
            }
            
            return accessions;
            
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            LOGGER.log(Level.WARNING, "{0} No RFAM Data was retrieved", ex.getMessage());
            //send back empty array to prevent null pointer in the event of failure 

            return accessions;
        }
        
    }
}
