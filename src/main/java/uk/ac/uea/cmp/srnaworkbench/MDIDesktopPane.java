package uk.ac.uea.cmp.srnaworkbench;

import uk.ac.uea.cmp.srnaworkbench.tools.ToolManager;

import javax.swing.*;
import java.awt.*;
import java.beans.*;

/**
 * An extension of WDesktopPane that supports often used MDI functionality. This
 * class also handles setting scroll bars for when windows move too far to the left or
 * bottom, providing the MDIDesktopPane is in a ScrollPane.
 */
public class MDIDesktopPane extends JDesktopPane {
    private static final int FRAME_OFFSET=20;
    private static final int PARAM_SIZE = 0;

    private final MDIDesktopManager manager;

    private JScrollPane mainScrollPane = null;

    JSeparator jSeparator1 = new javax.swing.JSeparator();

    private javax.swing.JPanel paramsPanel;// = new javax.swing.JPanel();

    JScrollPane paramScroll = null;
    Container mainContentPane = null;
    /**
     *
     */
    public MDIDesktopPane() {
        manager=new MDIDesktopManager(this);

        setDesktopManager(manager);
        setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);


        //initComponents();




        //this.addMouseListener(new MDIDesktopMouseListener());


        //this.setExtendedState(this.getExtendedState()|MainMDIWindow.MAXIMIZED_BOTH);



    }

    public void setRefs(JPanel newParamsPanel, JScrollPane newParamScroll, JScrollPane newMainScroll, Container contentPane)
    {
         paramsPanel = newParamsPanel;
         paramScroll = newParamScroll;
         mainScrollPane = newMainScroll;
         mainContentPane = contentPane;
         manager.setLayouts(paramsPanel, jSeparator1, newParamScroll);

    }

    public void showParams(GUIInterface topFrame) {

//
//        JPanel toAdd = manager.getTopParams();
//        if (toAdd != null)
//        {
//            toAdd.setVisible(true);
//            System.out.println("params are not null");
//
//        }
        //add(paramScroll);
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(mainContentPane);
            mainContentPane.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(paramScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE).addComponent(paramScroll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE));
        paramScroll.setVisible(true);

        manager.activateFrame((JInternalFrame) topFrame);
        manager.setParamOffset(PARAM_SIZE);


    }

    public void hideParamsActionPerformed() {
        // TODO add your handling code here:
        //remove(paramScroll);
       hideParams();
    }
    private void hideParams()
    {
         paramsPanel.setVisible(false);
        paramScroll.setVisible(false);
        paramsPanel.removeAll();
        jSeparator1.setVisible(false);

        manager.setParamOffset(0);
        manager.resizeDesktop();
    }
    @Override
    public void setBounds(int x, int y, int w, int h) {
        //w-=245;
        //System.out.println("setting bounds : " + x + " " + y + " " + w + " " + h);
        super.setBounds(x,y,w,h);
        checkDesktopSize();
    }
    /**
     *
     * @param frame
     * @return
     */
    public Component add(JInternalFrame frame) {
        JInternalFrame[] array = getAllFrames();
        Point p;
        int w;
        int h;

        Component retval=super.add(frame);
        checkDesktopSize();
        if (array.length > 0) {
            p = array[0].getLocation();
            p.x = p.x + FRAME_OFFSET;
            p.y = p.y + FRAME_OFFSET;
        }
        else {
            p = new Point(0, 0);
        }
        frame.setLocation(p.x, p.y);
//        if (frame.isResizable()) {
//            w = getWidth() - (getWidth()/3);
//            h = getHeight() - (getHeight()/3);
//            if (w < frame.getMinimumSize().getWidth()) w = (int)frame.getMinimumSize().getWidth();
//            if (h < frame.getMinimumSize().getHeight()) h = (int)frame.getMinimumSize().getHeight();
//            frame.setSize(w, h);
//            //frame.setMaximumSize(new Dimension(getWidth(), getHeight()));
//        }
        moveToFront(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException e) {
            frame.toBack();
        }
        return retval;
    }

    @Override
    public void remove(Component c)
    {
        super.remove(c);

        if ( c instanceof GUIInterface )
        {
            GUIInterface gi = (GUIInterface)c;

            ToolManager.getInstance().removeTool( gi );

            if ( ! gi.getShowingParams() )
            {
                hideParams();

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(mainContentPane);
                mainContentPane.setLayout(layout);

                layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 818, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                      )
                );

                layout.setVerticalGroup(
                  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                );
            }
        }

        checkDesktopSize();
    }

    /**
     * Cascade all internal frames
     */
    public void cascadeFrames() {
        int x = 0;
        int y = 0;
        JInternalFrame allFrames[] = getAllFrames();

        manager.setNormalSize();
        //this.setBounds(x, y, y, y);
        int frameHeight = (getBounds().height - 5) - allFrames.length * FRAME_OFFSET;
        int frameWidth = (getBounds().width - 5) - allFrames.length * FRAME_OFFSET;
        for (int i = allFrames.length - 1; i >= 0; i--) {
            allFrames[i].setSize(frameWidth-PARAM_SIZE,frameHeight);

            allFrames[i].setLocation(x,y);
            x = x + FRAME_OFFSET;
            y = y + FRAME_OFFSET;
        }
    }

    /**
     * Tile all internal frames
     */
    public void tileFrames() {
        java.awt.Component allFrames[] = getAllFrames();
        manager.setNormalSize();
        int frameHeight = getBounds().height/allFrames.length;
        int y = 0;
      for ( Component allFrame : allFrames )
      {
        allFrame.setSize( getBounds().width-PARAM_SIZE, frameHeight );
        allFrame.setLocation( 0, y );
        y = y + frameHeight;
      }
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given dimension.
     * @param d
     */
    public void setAllSize(Dimension d){
        setMinimumSize(d);

        setPreferredSize(d);
        //System.out.println("max size " + d);

        setMaximumSize(d);


    }
    public void activateFrame(JInternalFrame f)
    {
        this.manager.activateFrame(f);
    }

    /**
     * Sets all component size properties ( maximum, minimum, preferred)
     * to the given width and height.
     * @param width
     * @param height
     */
    public void setAllSize(int width, int height){
        setAllSize(new Dimension(width,height));
    }

    private void checkDesktopSize() {
        if (getParent()!=null&&isVisible()) manager.resizeDesktop();
    }
}

/**
 * Private class used to replace the standard DesktopManager for JDesktopPane.
 * Used to provide scrollbar functionality.
 */
class MDIDesktopManager extends DefaultDesktopManager {
    private final MDIDesktopPane desktop;
    private static int PARAM_OFFSET = 0;
    private JPanel paramsPanel = null;
    private JSeparator jSeparator1;
    private JScrollPane paramScroll;


    public MDIDesktopManager(MDIDesktopPane desktop) {
        this.desktop = desktop;
    }
    public void setLayouts(JPanel newParamsPanel, JSeparator newJSeparator1, JScrollPane newParamScroll )
    {
        jSeparator1 = newJSeparator1;
        paramsPanel = newParamsPanel;
        paramScroll = newParamScroll;

    }

    @Override
    public void endResizingFrame(JComponent f) {
        super.endResizingFrame(f);
        resizeDesktop();
    }
    public void setParamOffset(int newValue)
    {
        PARAM_OFFSET = newValue;
    }
    @Override
    public void activateFrame(JInternalFrame f)
    {
        //System.out.println("activating");
        Container p = f.getParent();
        Component[] c;
	JDesktopPane d = f.getDesktopPane();
	JInternalFrame currentlyActiveFrame =
	  (d == null) ? null : d.getSelectedFrame();
	// fix for bug: 4162443
        if(p == null) {
            // If the frame is not in parent, its icon maybe, check it
            p = f.getDesktopIcon().getParent();
            if(p == null)
                return;
        }
	// we only need to keep track of the currentActive InternalFrame, if any
	if (currentlyActiveFrame == null){
	  if (d != null) { d.setSelectedFrame(f);}
	} else if (currentlyActiveFrame != f) {
	  // if not the same frame as the current active
	  // we deactivate the current
	  if (currentlyActiveFrame.isSelected()) {
	    try {
	      currentlyActiveFrame.setSelected(false);
	    }
	    catch(PropertyVetoException e2) {}
	  }
	  if (d != null) { d.setSelectedFrame(f);}
	}
        f.moveToFront();


        GUIInterface topPanel = (GUIInterface) f;

        if (topPanel.getShowingParams())
        {
             paramsPanel.setVisible(false);
            paramsPanel.removeAll();
            jSeparator1.setVisible(false);

            JPanel toAdd = topPanel.getParamsPanel();
            if (toAdd != null)
            {

                toAdd.setVisible(true);





                javax.swing.GroupLayout paramsPanelLayout = new javax.swing.GroupLayout(paramsPanel);
                paramsPanel.setLayout(paramsPanelLayout);
                paramsPanelLayout.setHorizontalGroup(
                        paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(toAdd).addGap(0, 215, Short.MAX_VALUE));
                paramsPanelLayout.setVerticalGroup(
                        paramsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(toAdd).addGap(0, 647, Short.MAX_VALUE));
                paramsPanel.setVisible(true);

                jSeparator1.setVisible(true);
                resizeDesktop();
            }
        }
        else
        {
            paramsPanel.setVisible(false);

            paramsPanel.removeAll();
            jSeparator1.setVisible(false);

            setParamOffset(0);
            resizeDesktop();
        }

    }


    @Override
    public void endDraggingFrame(JComponent f) {
//        Point pos = new Point();
//        f.getLocation(pos);
//        JScrollPane scrollPane=getScrollPane();
//        Dimension d = scrollPane.getVisibleRect().getSize();
//        if(pos.x > d.width - 245)
//        {
//            System.out.println("here");
//        }
        super.endDraggingFrame(f);
        resizeDesktop();
    }

    public void setNormalSize() {
        JScrollPane scrollPane=getScrollPane();
        int x = 0;
        int y = 0;
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {
            Dimension d = scrollPane.getVisibleRect().getSize();

            if (scrollPane.getBorder() != null) {
               d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                         d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            d.setSize(d.getWidth() - 20, d.getHeight() - 20);
            desktop.setAllSize(x,y);
            scrollPane.invalidate();
            scrollPane.validate();
        }
    }

    private Insets getScrollPaneInsets() {
        JScrollPane scrollPane=getScrollPane();
        if (scrollPane==null) return new Insets(0,0,0,0);
        else return getScrollPane().getBorder().getBorderInsets(scrollPane);
    }

    private JScrollPane getScrollPane() {
        if (desktop.getParent() instanceof JViewport) {
            JViewport viewPort = (JViewport)desktop.getParent();
            if (viewPort.getParent() instanceof JScrollPane)
                return (JScrollPane)viewPort.getParent();
        }
        return null;
    }

    protected void resizeDesktop() {
        int x = 0;
        int y = 0;
        JScrollPane scrollPane = getScrollPane();
        Insets scrollInsets = getScrollPaneInsets();

        if (scrollPane != null) {

            JInternalFrame allFrames[] = desktop.getAllFrames();
          for ( JInternalFrame allFrame : allFrames )
          {
            if ( allFrame.getX() + allFrame.getWidth() > x )
            {
              x = allFrame.getX() + allFrame.getWidth();
            }
            if ( allFrame.getY() + allFrame.getHeight() > y )
            {
              y = allFrame.getY() + allFrame.getHeight();
            }
          }
            x += PARAM_OFFSET;
            Dimension d=scrollPane.getVisibleRect().getSize();
            //d.width -= 245;
            if (scrollPane.getBorder() != null) {
               d.setSize(d.getWidth() - scrollInsets.left - scrollInsets.right,
                         d.getHeight() - scrollInsets.top - scrollInsets.bottom);
            }

            if (x <= d.getWidth()) x = ((int)d.getWidth()) - 20;
            if (y <= d.getHeight()) y = ((int)d.getHeight()) - 20;
            desktop.setAllSize(x,y);
//            for (int i = 0; i < allFrames.length; i++) {
//                allFrames[i].setMaximumSize(new Dimension(desktop.getWidth()-245, desktop.getHeight()));
//
//
//            }
            scrollPane.invalidate();
            scrollPane.validate();
            //pack();
        }
    }
}