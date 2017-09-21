/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.utils;

import uk.ac.uea.cmp.srnaworkbench.exceptions.NotJavaFXThreadException;
import uk.ac.uea.cmp.srnaworkbench.exceptions.NoSQLException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.ac.uea.cmp.srnaworkbench.database.WF.DatabaseWorkflowModule;
import uk.ac.uea.cmp.srnaworkbench.database.servicelayers.SequenceServiceImpl;
import uk.ac.uea.cmp.srnaworkbench.exceptions.IDDoesNotExistException;
import uk.ac.uea.cmp.srnaworkbench.utils.HQLQuery.HQL_DIR;

/**
 *
 * @author chris applegate
 */
public class WFQueryResultsTable {

    // id of the result set (must be the same name as the javascript div id and javascript variable
    private final String id;
    // list of resulting records (each row contains map of key -> value pairs)
    private List<Map<String, Object>> output;
    // SQL statement
    private HQLQueryComplex query;
    // the number of records returned in the last query
    private int nRecords;
    // the first record to display
    private int start;
    // the maximum number of records to display
    private int maxResults;
    // flag storing whether query is being executed
    private boolean inUpdate;
    // flag storing whether the resultset is out of date
    private boolean sqlUpdated;
    // flag storing whether the window of the resultset is out of date
    private boolean windowUpdated;
    // search condition string
    private String searchCondition;

    /*
     * @param id: id of the result set (must be the same name as the javascript div id and javascript variable
     */
    public WFQueryResultsTable(String id) {

        // initialise variables
        this.id = id;
        this.output = null;
        this.query = null;
        this.nRecords = 0;
        this.start = 0;
        this.maxResults = 0;
        this.inUpdate = false;
        this.sqlUpdated = false;
        this.windowUpdated = false;
        this.searchCondition = "";
    }

    public HQLQuery getQuery() {
        return this.query;
    }

    /*
     * @param sql: the sql statement to execute
     */
    public void setSQL(HQLQueryComplex query) {
        if (this.query == null || !this.query.eval().equals(query.eval())) {
            this.query = query;
            this.sqlUpdated = true;
        }
    }

    public void setSearch(String str) {
        this.searchCondition = "";

        List<String> aliases = this.query.getSelectAliases();
        int counter = 0;
        for (String alias : aliases) {
            if (counter++ > 0) {
                this.searchCondition += " OR ";
            }
            this.searchCondition += String.format("%s = '%%%s%%' ", alias, str);
        }
        this.sqlUpdated = true;
    }

    public void clearOrder() throws HQLQuery.HQLQueryLockedException {
        this.query.clearOrder();
    }

    public void toggleOrder(int index) throws HQLQuery.HQLQueryLockedException {
        String str = this.query.getSelect(index);
        if (str != null) {
            this.query.toggleOrder(str);
            this.windowUpdated = true;
        }
    }

    public void addOrder(int index, HQL_DIR dir) throws HQLQuery.HQLQueryLockedException {
        String str = this.query.getSelect(index);
        addOrder(str, dir);
    }

    public void addOrder(String s, HQL_DIR dir) throws HQLQuery.HQLQueryLockedException {
        this.query.addOrder(s, dir);
        this.windowUpdated = true;
    }


    /*
     * @param start: the first record to show (index starts at 0)
     * @param maxResults: the number of records to show
     */
    public void setWindow(int start, int maxResults) {
        if (start >= 0 && maxResults >= 0) {
            if (this.start != start || this.maxResults != maxResults) {
                this.start = start;
                this.maxResults = maxResults;
                this.windowUpdated = true;
            }
        } else {
            Logger.getLogger(WFQueryResultsTable.class.getName()).log(Level.WARNING, "Query result window invalid. Ignoring window frame.");
        }
    }

    /*
     * @param engine: the web engine instance
     * IMPORTANT: this function must be run in javaFX thread
     */
    public synchronized void updateTable(WebEngine engine) throws NotJavaFXThreadException, NoSQLException {
        System.out.println("Updating TABLE: " + id);
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'updateTable' must be run from a JavaFX Thread.");
        }

        SequenceServiceImpl sequence_service = (SequenceServiceImpl) DatabaseWorkflowModule.getInstance().getContext().getBean("SequenceService");

        if (!this.inUpdate) {
            if (this.query != null && !this.query.eval().isEmpty()) {
                this.inUpdate = true;
                // run the update in standard Java Thread

                final Task<Integer> task = new Task<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        HQLQuery searchQuery = new HQLQueryComplex(query);
                        if (sqlUpdated) {

                            if (!searchCondition.isEmpty()) {
                                searchQuery.addWhere(searchCondition);
                            }

                            nRecords = sequence_service.getNumRecords(searchQuery.eval());
                        }
                        if (sqlUpdated || windowUpdated) {
                            output = sequence_service.executeGenericSQL(searchQuery.eval(), start, maxResults);
                        }
                        // run JavaFX thread when standard Java thread has completed
                        //Platform.runLater(() -> {
                        //});
                        return 0;
                    }
                };

                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent event) {
                        try {
                            //Integer result = task.getValue(); // result of computation
                            // update UI with result
                            if (sqlUpdated || windowUpdated) {
                                show(engine);
                            }
                            stopLoading(engine);
                            inUpdate = false;
                            sqlUpdated = false;
                            windowUpdated = false;
                        } catch (NotJavaFXThreadException | IDDoesNotExistException ex) {
                            Logger.getLogger(WFQueryResultsTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                Thread t = new Thread(task);
                t.setDaemon(true); // thread will not prevent application shutdown
                t.start(); // start the thread
            } else {
                throw new NoSQLException("WFQuery: " + this.id);
            }
        }

    }

    /*
     * @param index: index of the result set to query
     * @param key: the name of the variable to query
     */
    public Object getOutput(int index, String key) {
        return this.output.get(index).get(key);
    }

    /*
     * @param engine: the web engine instance
     * IMPORTANT: this function must be run in javaFX thread
     */
    private void show(WebEngine engine) throws IDDoesNotExistException, NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'show' must be run from a JavaFX Thread.");
        }

        Document doc = engine.getDocument();
        clear(engine);
        if (!output.isEmpty()) {
            Set<String> col_names = this.output.get(0).keySet();
            //setNRowsCols(engine, output.size(), col_names.size());
            setNCols(engine, col_names.size());
            int col = 0;
            for (String col_name : col_names) {
                String colID = id + "_table_header_col_" + (col);
                String dirSymbol = "";
                HQL_DIR orderDir = this.query.getOrder(col_name);
                if (orderDir == HQL_DIR.ASC) {
                    dirSymbol = "&#65514;";
                } else if (orderDir == HQL_DIR.DESC) {
                    dirSymbol = "&#65516;";
                }
                Element headerCellElement = doc.getElementById(colID);
                if (headerCellElement != null) {
                    // replace all underscores with spaces
                    col_name = col_name.replaceAll("_", " ");
                    engine.executeScript(String.format("setInnerHTML('%s', '%s %s');", colID, col_name, dirSymbol));
                    // headerCellElement.setTextContent(col_name + " " + dirSymbol);
                } else {
                    throw new IDDoesNotExistException("Could not find DOM element with ID: '" + colID + "'. Ensure that this method call is running from JavFX Thread.");
                }
                col++;
            }
            for (int r = 0; r < output.size(); r++) {
                int c = 0;
                for (String col_name : col_names) {
                    String cellID = id + "_table_row_" + r + "_col_" + c;
                    Element cellElement = doc.getElementById(cellID);
                    if (cellElement != null) {
                        Object obj = output.get(r).get(col_name);
                        cellElement.setTextContent(obj.toString());
                    } else {
                        throw new IDDoesNotExistException("Could not find DOM element with ID: '" + cellID + "'. Ensure that this method call is running from JavFX Thread.");
                    }
                    c++;
                }
            }
            
            
            String cols = "[";
            int colNum = 0;
            for (String col_name : col_names) {
                if(colNum++ > 0)
                    cols += ", ";
                cols += "'" + col_name + "'";    
            }
            cols += "]";
                    
             engine.executeScript("createDataTable(" + cols + ", 'example_container');");
            
            for (int r = 0; r < output.size(); r++) {
                String rowStr = "[";
                int c = 0;
                for (String col_name : col_names) {
                    if(c > 0)
                    {
                        rowStr +=", ";
                    }
                    rowStr += String.format("'%s'" , output.get(r).get(col_name));
                    c++;
                }
                rowStr += "]";
                engine.executeScript("addToTable(" + rowStr + ", 'example_container');");
            }
            
                    // addToTable(['AGAATCTTGATGATGCTGCAT', '1491', '317'], "example_container");
              //  drawTable("example_container");
                
 engine.executeScript("drawTable('example_container');");      

           

        }
        setNRecords(engine);

    }

    /*
     * @param engine: the web engine instance
     * IMPORTANT: this function must be run in javaFX thread
     */
    private void clear(WebEngine engine) throws NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'clear' must be run from a JavaFX Thread.");
        }
        engine.executeScript(this.id + ".clear();");
    }

    /*
     * @param engine: the web engine instance
     * @col: the number of columns in the table
     * IMPORTANT: this function must be run in javaFX thread
     */
    private void setNCols(WebEngine engine, int cols) throws NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'setNCols' must be run from a JavaFX Thread.");
        }
        engine.executeScript(this.id + ".setNCols(" + cols + ");");
    }

    private void setNRowsCols(WebEngine engine, int rows, int cols) throws NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'setNCols' must be run from a JavaFX Thread.");
        }
        engine.executeScript(this.id + ".setNRowsCols(" + rows + ", " + cols + ");");
    }

    /*
     * @param engine: the web engine instance
     * IMPORTANT: this function must be run in javaFX thread
     */
    private void setNRecords(WebEngine engine) throws NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'setNRecords' must be run from a JavaFX Thread.");
        }
        engine.executeScript(this.id + ".setNRecords(" + this.nRecords + ");");
    }

    /*
     * @param engine: the web engine instance
     * IMPORTANT: this function must be run in javaFX thread
     */
    private void stopLoading(WebEngine engine) throws NotJavaFXThreadException {
        if (!Platform.isFxApplicationThread()) {
            throw new NotJavaFXThreadException("This function 'stopLoading' must be run from a JavaFX Thread.");
        }
        engine.executeScript(this.id + ".stopLoading();");
    }
}
