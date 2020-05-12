/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.web.WebEngine;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import static uk.ac.uea.cmp.srnaworkbench.utils.LOGGERS.WorkbenchLogger.LOGGER;

/**
 *
 * @author ujy06jau
 */
public class HQLDataTable {

    private final String id;
    private HQLQuery query;
    private boolean inUpdate;
    public boolean sqlUpdated;
    private List<Map<String, Object>> output;
    private Map<String, String> initialisationFlags;
    private HashSet<String> preTagCols;
    private boolean selectable;
    private String selectableFunction;
    private String tableHeader;
    private List<Integer> columnAlignment;
    public static final String FIELD_SEPARATOR = "<FIELD_SEP>";

    public HQLDataTable(String containerID) {
        this.id = containerID;
        this.inUpdate = false;
        this.sqlUpdated = false;
        this.selectable = false;
        this.preTagCols = new HashSet<>();
        this.initialisationFlags = new HashMap<>();
        this.initialisationFlags.put("dom", "'T<\"clear\">lfrtip'");
        //colDef = "[{\"targets\": 0,\"searchable\": false,\"orderable\": false}]";
        this.initialisationFlags.put("columnDefs", "[{\"targets\": 0,\"searchable\": false,\"orderable\": false}]");
        this.initialisationFlags.put("order", "[[ 1, 'asc' ]]");
        this.initialisationFlags.put("scrollX", "true");

        this.columnAlignment = new ArrayList<>();

        this.tableHeader = "";
    }

    public void addRightColumn(int index) {
        columnAlignment.add(index);
        String colDef = "[{\"targets\": 0,\"searchable\": false,\"orderable\": false},{\"className\": \"dt-right\", \"targets\": [";
        for (int i = 0; i < columnAlignment.size(); i++) {
            if (i > 0) {
                colDef += ",";
            }
            int cIndex = columnAlignment.get(i);
            colDef += cIndex;

        }
        colDef += "]}]";
        this.initialisationFlags.put("columnDefs", colDef);
    }

    public void setPreTagCol(String name) {
        this.preTagCols.add(name);
    }

    public void setSelectableFunction(String selectableFunction) {
        this.selectable = true;
        this.selectableFunction = selectableFunction;
    }

    public void setQuery(HQLQuery query) {
        this.query = query;
        this.sqlUpdated = true;
    }

    public void setTableHeader(String str) {
        //System.out.println(str);
        this.tableHeader = str;
    }

    public void update(WebEngine engine) throws NotJavaFXThreadException, NoSQLException {
        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");

        if (!this.inUpdate) {
            if (this.query != null && !this.query.eval().isEmpty()) {
                this.inUpdate = true;
                if (!Platform.isFxApplicationThread()) {
                    if (sqlUpdated) {
                        output = sequence_service.executeGenericSQL(query.eval());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (sqlUpdated) {
                                        show(engine);
                                    }
                                    inUpdate = false;
                                    sqlUpdated = false;
                                } catch (Exception ex) {
                                    Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                                    System.out.println("error:" + ex);
                                }
                            }
                        });
                    }
                } // if we are on javafx thread
                else {
                    final Task<Integer> task = new Task<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            if (sqlUpdated) {
                                output = sequence_service.executeGenericSQL(query.eval());
                            }
                            return 0;
                        }
                    };
                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent event) {
                            try {
                                if (sqlUpdated) {
                                    show(engine);
                                }
                                inUpdate = false;
                                sqlUpdated = false;
                            } catch (IDDoesNotExistException ex) {
                                Logger.getLogger(WFQueryResultsTable.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NotJavaFXThreadException ex) {
                                Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception ex) {
                                Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });

                    Thread t = new Thread(task);
                    t.setDaemon(true); // thread will not prevent application shutdown
                    t.start(); // start the thread
                }
            } else {
                //System.out.println("no SQL");
            }
        }

        // throw new NotJavaFXThreadException("This function 'update' must be run from a JavaFX Thread.");
        //if (!this.inUpdate
//    
//        ) {
//            if (this.query != null && !this.query.eval().isEmpty()) {
//            this.inUpdate = true;
//            // run the update in standard Java Thread
//
//            final Task<Integer> task = new Task<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    if (sqlUpdated) {
//                        output = sequence_service.executeGenericSQL(query.eval());
//                    }
//                    return 0;
//                }
//            };
//
//            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//                @Override
//                public void handle(WorkerStateEvent event) {
//                    try {
//                        if (sqlUpdated) {
//                            show(engine);
//                        }
//                        inUpdate = false;
//                        sqlUpdated = false;
//                    } catch (IDDoesNotExistException ex) {
//                        Logger.getLogger(WFQueryResultsTable.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (NotJavaFXThreadException ex) {
//                        Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (Exception ex) {
//                        Logger.getLogger(HQLDataTable.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });
//
//            Thread t = new Thread(task);
//            t.setDaemon(true); // thread will not prevent application shutdown
//            t.start(); // start the thread
//        } else {
//            //throw new NoSQLException("");
//            System.out.println("no SQL");
//        }
        //}
    }

    public void show(WebEngine engine) throws IDDoesNotExistException, NotJavaFXThreadException, Exception {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'show' must be run from a JavaFX Thread.");
        }
        if (this.output == null || this.output.isEmpty()) {
            String func = "createDataTable('', '" + this.id + "', {});";
            engine.executeScript(func);
            return;
        }
        String thead;
        if (this.tableHeader.isEmpty()) {
            thead = generateDefaultTableHeader();
        } else {
            thead = this.tableHeader;
        }

        String initialisationStr = "{";

        int initialisationCounter = 0;
        for (String key : this.initialisationFlags.keySet()) {
            if (initialisationCounter++ > 0) {
                initialisationStr += ", ";
            }
            initialisationStr += "\"" + key + "\": " + this.initialisationFlags.get(key);
        }

        initialisationStr += "}";
        String func = "createDataTable(\"" + thead + "\", \"" + this.id + "\", " + initialisationStr + ");";
        //System.out.println("init string:" + initialisationStr);
        //System.out.println("id:" + this.id);
        engine.executeScript(func);

        Set<String> col_names = this.output.get(0).keySet();


        /* String cols = "[''";
         int colNum = 1;
         for (String col_name : col_names) {
         if (colNum++ > 0) {
         cols += ", ";
         }
         cols += "\"" + col_name + "\"";
         }
         cols += "]";*/
        //System.out.println(cols);
        // engine.executeScript("createDataTable(" + cols + ", \"" + this.id + "\", " + initialisationStr + ");");
        if (this.selectable) {
            engine.executeScript("setSelectableDataTable('" + this.id + "', " + this.selectableFunction + ");");
        }
        for (int r = 0; r < output.size(); r++) {
            String rowStr = "[''";
            int c = 1;
            for (String col_name : col_names) {
                if (c > 0) {
                    rowStr += ", ";
                }
                //System.out.println("r: " + r);
               // System.out.println("col_name: " + col_name);
              //  System.out.println(output.get(r).get(col_name).toString()) ;
             //   System.out.println("----");
                String content = output.get(r).get(col_name).toString();
                if(Tools.isWindows()){
                    if(col_name.equalsIgnoreCase("Duplex") || col_name.equalsIgnoreCase("Hairpin")){ // it cause error in Windows when using \r
                        content = content.replaceAll("\r", "");
                    }
                }
                rowStr = rowStr.replaceAll("\n", "</br>");
                if (this.preTagCols.contains(col_name)) {
                    rowStr += String.format("\"<pre>%s</pre>\"", content);
                } else {
                    rowStr += String.format("\"%s\"", content);
                }
                c++;
            }
            rowStr += "]";
            try {
                engine.executeScript("addToTable(" + rowStr + ", \"" + this.id + "\");");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Could not add to table: " + rowStr, ex);
                throw new Exception(ex);
            }
        }

        engine.executeScript("drawTable('" + this.id + "');");
    }

    public String generateDefaultTableHeader() {
        if (!this.output.isEmpty()) {
            Set<String> col_names = this.output.get(0).keySet();
            String cols = "<tr><th></th>";
            for (String col_name : col_names) {
                String outColName = col_name.replaceAll("[_]", " ");
                cols += "<th>" + outColName + "</th>";
            }
            cols += "</tr>";
            return cols;
        }
        return "<tr><th></th><tr>";
    }

    public void addInitialisationFlag(String key, String value) {
        this.initialisationFlags.put(key, value);
    }

}
