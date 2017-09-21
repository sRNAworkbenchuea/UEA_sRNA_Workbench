
package uk.ac.uea.cmp.srnaworkbench.tools.paresnip;

import java.util.Stack;

/**
 * @author Leighton Folkes (l.folkes@uea.ac.uk)
 */
final class SearchTreeThreadPoolManager extends Thread {

    /** The number of threads used to search trees.**/
    private final int numThreads;
    /** The stack of results.**/
    public final Stack<String> results;
    /** The stack of queries/candidates to be searched for.**/
    public final Stack<Query> queries;
    /**Array of search tree thread objects.**/
    public final Tree[] threadPool;
    /** Data object */
    private final Data _data;

    /**
     * Constructs a new instance of SearchTreeThreadPoolManager.
     * @param num The number of threads to be used.
     */
    public SearchTreeThreadPoolManager(int num, Data data){
        this.numThreads = num;
        _data = data;
        results = new Stack<String>();
        queries = new Stack<Query>();
      //  done = false;
        threadPool = new Tree[num];
    }//end constructor.

    public int getQueryCount(){
        return queries.size();
    }

    /**
     * All queries must be pushed before starting the search.
     * @param q The query to be pushed onto the stack.
     */
    public void pushQuery(Query q){
        queries.push(q);
    }//end method.

    /**
     * Gets the next query on the stack. If the stack is empty null will be returned;
     * @return Query or null.
     */
    public synchronized Query nextQuery(){
        if(queries.isEmpty()){
            //this.isDone(true);
            return null;
        }else{
            _data.accessCountDown(true);
            return queries.pop();
        }
    }//end method.

    public boolean isFinishedRunning(){
        for(int i = 0; i < threadPool.length; i++){
            if(threadPool[i].isAlive()){
                return false;
            }
        }
        return true;
    }

    public void startSearching(Tree originalSRNAs){
        threadPool[0] = originalSRNAs;
        threadPool[0].setThreadIndex(0);
        for(int i = 1; i < this.numThreads; i++){
            threadPool[i] = originalSRNAs.getTreeCopy();
            threadPool[i].setThreadIndex(i);
        }
        for(int i = 0; i < threadPool.length; i++){
            threadPool[i].start();
        }
    }

    /**
     * Thread safe method for accessing the results stack.
     * @param set The string to be pushed onto the stack.
     * @param get True if popping, false if pushing.
     * @return String if getting and result is available, null if getting and no result, null setting.
     */
    public synchronized String accessResults(String set, boolean get){
        if(get){
            if(results.isEmpty()){
                return null;
            }else{
                return results.pop();
            }
        }else{
            results.push(set);
            return null;
        }
    }

    /**
     * Gets the number of threads used to search he trees.
     * @return Number of threads used to search the trees.
     */
    public synchronized int getNumThreads(){
        return numThreads;
    }//end method.

    /**
     * Notifies the threads that they need to stop running.
     */
    public void requestStop(){
        for(int i = 0; i < threadPool.length; i++){
            if(threadPool[i] != null && threadPool[i].isAlive()){
                threadPool[i].interrupt();
            }
        }


    }//end method

}//end class.
