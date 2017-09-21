/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.uea.cmp.srnaworkbench.apple;

/**
 *
 * @author w0445959
 */

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import javax.swing.JOptionPane;
import uk.ac.uea.cmp.srnaworkbench.MainMDIWindow;
import uk.ac.uea.cmp.srnaworkbench.help.AboutInternal;
 

public class AppleApplicationHandler extends ApplicationAdapter
{
   
  public AppleApplicationHandler()
  {

  }
 
  @Override
  public void handleQuit(ApplicationEvent e)
  {
    System.exit(0);
  }
 
  @Override
  public void handleAbout(ApplicationEvent e)
  {
    // tell the system we're handling this, so it won't display
    // the default system "about" dialog after ours is shown.
     AboutInternal aboutFrame = AboutInternal.getInstance();
    aboutFrame.setVisible( true );
  }
 
  @Override
  public void handlePreferences(ApplicationEvent e)
  {
    JOptionPane.showMessageDialog(null, "Please view preferences per tool");
  }
}
