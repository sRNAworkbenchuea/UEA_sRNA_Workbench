/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.uea.cmp.srnaworkbench.database.DAO;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.uea.cmp.srnaworkbench.database.DAO.abstraction.GenericDaoImpl;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Interaction_Entity;

/**
 *
 * @author Chris Applegate
 */
@Repository("InteractionDAO")
public class InteractionDAOImpl extends GenericDaoImpl<Interaction_Entity, Long> {

    public static int interactionID = 0;

    @Autowired
    public InteractionDAOImpl(SessionFactory sf) {
        super(sf);
    }

    /*public void save(Interaction_Entity i) {
        try {
            Session session = getSessionFactory().openSession();
            session.beginTransaction();
            session.doWork(
                    new Work() {
                        @Override
                        public void execute(Connection connection) throws SQLException {
                            Statement s = connection.createStatement();
                            String sql = String.format("INSERT INTO INTERACTION ("
                                    + "PRECURSOR_ID, "
                                    + "GENE, "
                                    + "CATEGORY, "
                                    + "CLEAVAGE_POS, "
                                    + "P_VAL, FRAGMENT_ABUNDANCE, "
                                    + "WEIGHTED_FRAGMENT_ABUNDANCE, "
                                    + "NORMALISED_WEIGHTED_FRAGMENT_ABUNDANCE, "
                                    + "DUPLEX, "
                                    + "ALIGNMENT_SCORE, "
                                    + "SHORT_READ_ID, "
                                    + "NORMALISED_SHORT_READ_ABUNDANCE, "
                                    + "SRNA) "
                                    + "VALUES(%d, \"%s\", %d, %d, %f, %d, %f, %f, \"%s\", %f, \"%s\", %d, %f, \"%s\");",
                                    interactionID++,
                                    i.getGene(),
                                    i.getCategory(),
                                    i.getCleavagePos(),
                                    i.getPVal(),
                                    i.getFragmentAbundance(),
                                    i.getWeightedFragmentAbundance(),
                                    i.getNormalisedWeightedFragmentAbundance(),
                                    i.getDuplex(),
                                    i.getAlignmentScore(),
                                    i.getShortReadID(),
                                    i.getShortReadAbundane(),
                                    i.getNormalisedShortReadAbundance(),
                                    i.get_sRNA());
                            System.out.println("SQL: " + sql);
                            s.execute(sql);
                            s.close();
                            connection.close();
                        }
                    }
            );
            session.clear();
            session.close();
        } catch (Exception ex) {
            System.err.println("EXCEPTION: " + ex);
        }
    }*/
    
    
   /* public void getAll()
    {
        Session session = getSessionFactory().openSession();
            session.beginTransaction();
            session.doWork(
                    new Work() {
                        @Override
                        public void execute(Connection connection) throws SQLException {
                            Statement s = connection.createStatement();
                            String sql = "SELECT * FROM INTERACTIONS";
                            s.execute(sql);
                            s.close();
                            connection.close();
                        }
                    }
            );
            session.clear();
            session.close();

    }*/

    @Override
    protected Class<Interaction_Entity> getEntityClass() {
        return Interaction_Entity.class;
    }

   /* public void executeSQL(String sql) {
        System.out.println("ATTEMPTING TO EXECUTE: " + sql);
        Session session = getSessionFactory().openSession();
        session.beginTransaction();
        session.doWork(
                new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        Statement s = connection.createStatement();
                        s.execute(sql);
                        s.close();
                        connection.close();
                    }
                }
        );
        session.clear();
        session.close();
        System.out.println("done");
    }*/

}
