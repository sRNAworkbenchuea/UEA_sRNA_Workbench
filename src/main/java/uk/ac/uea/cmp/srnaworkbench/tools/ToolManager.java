/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.tools;

import java.util.ArrayList;
import uk.ac.uea.cmp.srnaworkbench.GUIInterface;
import java.util.Iterator;

/**
 *
 * @author w0445959
 */
public class ToolManager implements Iterable<GUIInterface>
{
  private final ArrayList<GUIInterface> toolList = new ArrayList<GUIInterface>();
  private static ToolManager instance;

  private ToolManager()
  {
  }

  public static synchronized ToolManager getInstance()
  {
    if ( instance == null )
    {
      instance = new ToolManager();
    }
    return instance;
  }

  @Override
  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }

  public void addTool( GUIInterface toAdd )
  {
    toolList.add( toAdd );
  }

  public int searchTools( String identifier )
  {
    int amountOfInstances = 0;
    for ( GUIInterface tool : toolList )
    {
      if ( tool.toString().equals( identifier ) )
      {
        amountOfInstances++;
      }
    }
    return amountOfInstances;
  }

  public int getTotalToolCount()
  {
    return toolList.size();
  }

  public GUIInterface getTool( String identifier )
  {
    GUIInterface toolToFind = null;
    for ( GUIInterface tool : toolList )
    {
      if ( tool.toString().equals( identifier ) )
      {
        toolToFind = tool;
      }
    }
    return toolToFind;
  }

  @Override
  public Iterator<GUIInterface> iterator()
  {
    return toolList.iterator();
  }

  public boolean removeTool( GUIInterface tool )
  {
    return toolList.remove( tool );
  }

  public void closeTools()
  {
    for ( GUIInterface tool : toolList )
    {
     tool.shutdown();
    }
  }
}
