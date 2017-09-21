package uk.ac.uea.cmp.srnaworkbench.tools.tasi;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Matthew Stocks and Dan Mapleson
 */
final class TasiRowModel implements RowModel
{
    private List<String> columns;
        
    public TasiRowModel()
    {
        this.columns = new ArrayList<String>();
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
    
    /**
     * Creates a two model based on the number of samples present
     * @return Row model
     */
    public static TasiRowModel newTasiRowModel()
    {
        TasiRowModel rowModel = new TasiRowModel();

        rowModel.addColumn("Sequence");
        rowModel.addColumn("Abundance");
        rowModel.addColumn("Chromosome");
        rowModel.addColumn("Start");
        rowModel.addColumn("End");
        rowModel.addColumn("Strand");
        
        return rowModel;
    }
}