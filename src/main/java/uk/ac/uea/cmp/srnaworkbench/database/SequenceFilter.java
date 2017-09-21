package uk.ac.uea.cmp.srnaworkbench.database;

import java.util.Collection;
import java.util.HashSet;
import java.util.StringJoiner;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import uk.ac.uea.cmp.srnaworkbench.database.entities.Sequence_Entity;

/**
 * Sets up a Sequences table filter based on criteria that exist in that table and
 * other tables. This can be passed to a method in the Sequences service layer that
 * wittles down that table to just the selected sequences
 * @author Matthew
 */
public class SequenceFilter {
    Collection<String> files = new HashSet<>();
    Collection<String> annotationTypes = new HashSet<>();
    Collection<Integer> sizes = new HashSet<>();
    public SequenceFilter(){
        
    }
    
    public SequenceFilter newFilter()
    {
        return new SequenceFilter();
    }
    
    public SequenceFilter addFiles(Collection<String> files)
    {
        this.files.addAll(files);
        return(this);
    }
    
    public SequenceFilter addAnnotationTypes(Collection<String> annotationTypes)
    {
        this.annotationTypes.addAll(annotationTypes);
        return(this);
    }
    
     public SequenceFilter addSizes(Collection<Integer> sizes)
     {
         this.sizes.addAll(sizes);
         return(this);
     }
     
//     public int generateHQL()
//     {
//         String hql = "from Sequences_Entity s ";
//         if (!this.sizes.isEmpty() || !this.annotationTypes.isEmpty()) {
//             hql += "join Unique_Sequences"
//         }
//         if(!this.files.isEmpty())
//         {
//             StringJoiner sj = new StringJoiner("('", "','", "')");
//             for(String file : this.files)
//                 sj.add(file);
//             hql += sj.toString() + " ";
//         }
//
//     }
     
    public DetachedCriteria createFilterCriteria() {
        DetachedCriteria filterCrit = DetachedCriteria.forClass(Sequence_Entity.class);
        if (!this.sizes.isEmpty() || !this.annotationTypes.isEmpty())
            filterCrit.createAlias("unique_sequence", "u");
        if(!this.files.isEmpty())
            filterCrit.add(Restrictions.in("fileID", files));
        if(!this.sizes.isEmpty())
            filterCrit.add(Restrictions.in("u.RNA_Size", sizes));
        if(!this.annotationTypes.isEmpty()){
            filterCrit.createAlias("u.type", "a");
            filterCrit.add(Restrictions.in("a.id.type", annotationTypes));
        }
        return filterCrit;
    }
    
}
