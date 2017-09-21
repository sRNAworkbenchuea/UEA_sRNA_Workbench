package uk.ac.uea.cmp.srnaworkbench.database;

import org.hibernate.Session;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;

/**
 * A simple class to simplify and clean up typical batching code
 * when iterating through a query result and updating the database.
 * 
 * Typical use-case is:
 * 
 * Session session = getSession();
 * Batcher batcher = new Batcher(session); // create a batcher by passing it a session
 * 
 * result = query.scroll();
 * while(result.next())
 * {
 *  Entity e = result.get(0)
 *  e.setStuff(stuff);
 *  batcher.batchFlush(); // this will flush the session every batchSize, saving on processing power
 * }
 * 
 * batcher.finish(); // gives a final flush and clear.
 * session.close();
 *  
 * @author mka07yyu
 */
public class Batcher {
    public static int DEFAULT_BATCH_SIZE = 500;
    private int batchSize;
    private Session session = null;
    private GenericDaoImpl dao = null;
    private int incrementor = 0;
        
    public Batcher(Session session)
    {
        this(session, DEFAULT_BATCH_SIZE);
    }
    
    public Batcher(GenericDaoImpl dao) {
        this.dao = dao;
        this.batchSize = DEFAULT_BATCH_SIZE;
    }
    
    public Batcher(Session session, int batchSize)
    {
        this.session = session;
        this.batchSize = batchSize;
    }
    
    public void batchFlush()
    {
        incrementor++;
        if(incrementor % batchSize == 0)
        {
            flushAndClear();
        }
    }
    
    public void batchClear()
    {
        incrementor++;
        if (incrementor % batchSize == 0) {
            if (session != null) {
                session.clear();
            } else {
                dao.clear();
            }
        }
    }
        
    public void finish()
    {
        incrementor = 0;
        flushAndClear();
    }
    
    private void flushAndClear()
    {
        if(session != null)
        {
            session.flush();
            session.clear();
        }
        else
        {
            dao.flush();
            dao.clear();
        }
    }
}
