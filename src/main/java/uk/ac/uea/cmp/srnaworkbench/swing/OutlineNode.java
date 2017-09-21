package uk.ac.uea.cmp.srnaworkbench.swing;

import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author w0445959
 */
public class OutlineNode extends DefaultMutableTreeNode
{
    public OutlineNode(List<String> userObject)
    {
        super(userObject);
    }
    
    /**
     * Retrieves the user object.  We know that it will be of type List<String>
     * because that's what we put in the constructor.
     * @return User object as List<String>
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserObject()
    {
        return (List<String>)this.userObject;
    }

    @Override
    public String toString()
    {
        List<String> list = getUserObject();

        if (list.isEmpty())
        {
            return "";
        }
        else
        {
            return list.get(0);
        }
    }
}