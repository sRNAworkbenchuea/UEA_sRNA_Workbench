package uk.ac.uea.cmp.srnaworkbench.tools.mirprof;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RowModel;
import uk.ac.uea.cmp.srnaworkbench.utils.CollectionUtils;
import uk.ac.uea.cmp.srnaworkbench.utils.Tools;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 *
 * @author w0445959
 */
final class MirprofTableRowModel implements RowModel
{
    private List<String> columns;

    public MirprofTableRowModel()
    {
        this( new ArrayList<File>() );
    }

   /**
     * Creates a two model based on the number of samples present
     * @return Row model
     */
    public MirprofTableRowModel( List<File> input_files )
    {
      this.columns = CollectionUtils.newArrayList();
      
//      for ( File f : input_files )
//      {
//        addColumn( f.getName() + LINE_SEPARATOR + "Seqs" );
//      }
      addColumn("Genome Matches");
      for ( File f : input_files )
      {
        addColumn( f.getName() + LINE_SEPARATOR + "Raw" );
      }
//      for ( File f : input_files )
//      {
//        addColumn( f.getName() + LINE_SEPARATOR + "Seqs" );
//      }
      for ( File f : input_files )
      {
        addColumn( f.getName() + LINE_SEPARATOR + "Norm" );
      }
      
    }

    @Override
    public Class getColumnClass(int column)
    {
        return String.class;
    }

    @Override
    public int getColumnCount()
    {
        return this.columns.size();
    }

    public void addColumn(String name)
    {
        this.columns.add(name);
    }

    @Override
    public String getColumnName(int column)
    {
        return columns.get(column);
    }

    @Override
    public Object getValueFor(Object node, int column)
    {
        // This can only be an arraylist as that's all we've put in
        @SuppressWarnings("unchecked")
        List<String> f = (List<String>)(((DefaultMutableTreeNode)node).getUserObject());

        // Skip the first column
        int nextColumn = column+1;

        if (f.size() > 1 && nextColumn < f.size())
        {
            return f.get(nextColumn);
        }
        else
        {
            return "";
        }
    }

    @Override
    public boolean isCellEditable(Object node, int column)
    {
        return false;
    }

    @Override
    public void setValueFor(Object node, int column, Object value)
    {

    }
}