/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.exceptions;

/**
 *
 * @author prb07qmu
 */
public class WorkbenchException extends Exception
{
  public WorkbenchException( Throwable cause )
  {
    super( cause );
  }

  public WorkbenchException( String message, Throwable cause )
  {
    super( message, cause );
  }

  public WorkbenchException( String message )
  {
    super( message );
  }

  public WorkbenchException()
  {
  }
}
