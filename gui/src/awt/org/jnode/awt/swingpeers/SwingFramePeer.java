/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.awt.swingpeers;

import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuBar;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.peer.FramePeer;
import java.beans.PropertyVetoException;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JRootPane;

final class SwingFrame extends SwingBaseWindow<Frame, SwingFrame> {

    public SwingFrame(Frame awtFrame, String title) {
        super(awtFrame, title);
    }

    /*
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        super.repaint(tm, x, y, width, height);
        if(target instanceof JFrame && isVisible())
            ((JFrame)target).getRootPane().repaint(tm, x, y, width, height);
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
        if(target instanceof JFrame && isVisible())
            ((JFrame)target).getRootPane().update(g);
    }
    */

    @Override
    public void setMaximum(boolean b) throws PropertyVetoException {
        super.setMaximum(b);
        target.setBounds(this.getBounds());
    }

    @Override
    public void setIcon(boolean b) throws PropertyVetoException {
        super.setIcon(b);
        target.setBounds(this.getBounds());
    }        

    @Override
    public void paintAll(Graphics g) {
        super.paintAll(g);
      //  if(target instanceof JFrame && isVisible())
        //    ((JFrame)target).getRootPane().paintAll(g);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(target instanceof JFrame && isVisible()){
            JRootPane rp = ((JFrame) target).getRootPane();
            int x = rp.getX(), y = rp.getY();
            g.translate(x,y);
            rp.paint(g);
            g.translate(-x, -y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /**
     * @see javax.swing.JInternalFrame#createRootPane()
     */
    protected JRootPane createRootPane() {
        return new NoContentRootPane();
    }

    @Override
    protected void validateTree() {
        super.validateTree();
        if(target instanceof JFrame)
            ((JFrame)target).getRootPane().validate();
    }

    boolean requestingFocus = false;
    @Override
    public void requestFocus() {
        if(target instanceof JFrame){
            if(!requestingFocus){
                requestingFocus = true;
                target.requestFocus();
                requestingFocus = false;
            }
        } else
            super.requestFocus();
    }

    @Override
    public boolean requestFocus(boolean temporary) {
        if(target instanceof JFrame){
            if(!requestingFocus){
                requestingFocus = true;
                target.requestFocus();
                requestingFocus = false;
            }
            return true;
        }else
            return super.requestFocus(temporary);
    }

    @Override
    public boolean requestFocusInWindow() {
        if(target instanceof JFrame) {
            if(!requestingFocus){
                requestingFocus = true;
                boolean ret = target.requestFocusInWindow();
                requestingFocus = false;
                return ret;
            }
            return true;
        } else
            return super.requestFocusInWindow();
    }

    @Override
    protected boolean requestFocusInWindow(boolean temporary) {
        if(target instanceof JFrame) {
            if(!requestingFocus){
                requestingFocus = true;
                boolean ret = target.requestFocusInWindow();
                requestingFocus = false;
                return ret;
            }
            return true;
        } else
            return super.requestFocusInWindow(temporary);
    }

    boolean settingCursor;
    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        if(!settingCursor){
            settingCursor = true;
            target.setCursor(cursor);
            settingCursor = false;
        }
    }
}

final class NullContentPane extends JComponent {
    @Override
    public void update(Graphics g) {
        //empty
    }

    @Override
    public void paint(Graphics g) {

    }

    @Override
    public void repaint() {

    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {

    }
}

final class NoContentRootPane extends JRootPane {
        /**
         * @see javax.swing.JRootPane#createContentPane()
         */
        protected Container createContentPane() {
            return new NullContentPane();
        }
    }

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
final class SwingFramePeer extends SwingBaseWindowPeer<Frame, SwingFrame>
        implements FramePeer, ISwingContainerPeer {

    /**
     * Initialize this instance.
     */
    public SwingFramePeer(SwingToolkit toolkit, Frame target) {
        super(toolkit, target, new SwingFrame(target, target.getTitle()));
        setResizable(target.isResizable());
        peerComponent.setIconifiable(true);
        peerComponent.setMaximizable(true);
        peerComponent.setClosable(true);
        try {
            peerComponent.setIcon(target.getState() == Frame.ICONIFIED);
        } catch (PropertyVetoException x) {
        }
        setState(target.getState());
        peerComponent.setTitle(target.getTitle());
        // frame.setIconImage(awtFrame.getIconImage());
        MenuBar mb = target.getMenuBar();
        if (mb != null) {
            setMenuBar(mb);
        }

        addToDesktop();
    }

    /**
     * @see java.awt.peer.FramePeer#getState()
     */
    public int getState() {
        int ret = Frame.NORMAL;
        if(peerComponent.isIcon())
            ret = ret | Frame.ICONIFIED;
        if(peerComponent.isMaximum())
            ret = ret | Frame.MAXIMIZED_BOTH;
        return ret;
    }

    /**
     * @see java.awt.peer.FramePeer#setIconImage(java.awt.Image)
     */
    public void setIconImage(Image image) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMaximizedBounds(java.awt.Rectangle)
     */
    public void setMaximizedBounds(Rectangle r) {
    }

    /**
     * @see java.awt.peer.FramePeer#setMenuBar(java.awt.MenuBar)
     */
    @SuppressWarnings("deprecation")
    public void setMenuBar(final MenuBar mb) {
        SwingToolkit.invokeNowOrLater(new Runnable() {
            public void run() {
                mb.addNotify();
                peerComponent
                        .setJMenuBar(((SwingMenuBarPeer) mb.getPeer()).jComponent);
                targetComponent.invalidate();
            }
        });
    }
    /**
     * @see java.awt.peer.FramePeer#setState(int)
     */
    public void setState(int state) {
        if((state & Frame.ICONIFIED) == Frame.ICONIFIED) {
            try {
                peerComponent.setIcon(true);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }

        if((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            try {
                peerComponent.setMaximum(true);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }

        if(state == Frame.NORMAL) {
            try {
                peerComponent.setMaximum(false);
                peerComponent.setIcon(false);
            } catch (PropertyVetoException x){
                log.warn(x);
            }
        }
    }

    public void setBoundsPrivate(int x, int y, int width, int height) {

    }


    //jnode openjdk
    public Rectangle getBoundsPrivate() {
        //TODO implement it
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
