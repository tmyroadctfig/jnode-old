/*
 * $Id$
 */
package org.jnode.awt;

import gnu.java.awt.EventModifier;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.PointerAPI;
import org.jnode.driver.input.PointerEvent;
import org.jnode.driver.input.PointerListener;
import org.jnode.driver.video.HardwareCursorAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MouseHandler implements PointerListener {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	private int x;
	private int y;
	private final HardwareCursorAPI hwCursor;
	private final PointerAPI pointerAPI;
	private final Dimension screenSize;

	/**
	 * Create a new instance
	 * 
	 * @param fbDevice
	 * @param screenSize
	 */
	public MouseHandler(Device fbDevice, Dimension screenSize) {
		HardwareCursorAPI hwCursor = null;
		Device pointerDevice = null;
		PointerAPI pointerAPI = null;
		try {
			hwCursor = (HardwareCursorAPI) fbDevice.getAPI(HardwareCursorAPI.class);
		} catch (ApiNotFoundException ex) {
			log.info("No hardware-cursor found on device " + fbDevice.getId());
		}
		if (hwCursor != null) {
			try {
				final Collection pointers = DeviceUtils.getDevicesByAPI(PointerAPI.class);
				if (!pointers.isEmpty()) {
					pointerDevice = (Device) pointers.iterator().next();
					pointerAPI = (PointerAPI) pointerDevice.getAPI(PointerAPI.class);
				}
			} catch (ApiNotFoundException ex) {
				log.error("Strange...", ex);
			}
		}
		this.hwCursor = hwCursor;
		this.pointerAPI = pointerAPI;
		this.screenSize = screenSize;
		if (pointerAPI != null) {
			log.debug("Using PointerDevice " + pointerDevice.getId());
            hwCursor.setCursorImage(JNodeCursors.ARROW);
			hwCursor.setCursorVisible(true);
			hwCursor.setCursorPosition(x, y);
			pointerAPI.addPointerListener(this);
		}
	}

	/**
	 * Close this handler
	 */
	public void close() {
		if (pointerAPI != null) {
			pointerAPI.removePointerListener(this);
		}
	}

	/**
	 * @param event
	 * @see org.jnode.driver.input.PointerListener#pointerStateChanged(org.jnode.driver.input.PointerEvent)
	 */
/*	public void pointerStateChanged(PointerEvent event) {
		x = Math.min(screenSize.width - 1, Math.max(0, x + event.getX()));
		y = Math.min(screenSize.height - 1, Math.max(0, y + event.getY()));
		hwCursor.setCursorPosition(x, y);
	}*/

    public void pointerStateChanged(PointerEvent event) {
        x = Math.min(screenSize.width - 1, Math.max(0, x + event.getX()));
        y = Math.min(screenSize.height - 1, Math.max(0, y + event.getY()));
        hwCursor.setCursorPosition(x, y);

        final int buttons = event.getButtons();
        boolean eventFired = false;
        for (int i = 0; i < 3; i++) {
            final int mask = BUTTON_MASK[ i];
            final int nr = BUTTON_NUMBER[ i];
            if ((buttons & mask) != 0) {
                if (!buttonState[ i]) {
                    postEvent(MouseEvent.MOUSE_PRESSED, nr);
                    buttonState[ i] = true;
                    eventFired = true;
                }
            }else if (buttonState[ i]) {
                    postEvent(MouseEvent.MOUSE_RELEASED, nr);
                    postEvent(MouseEvent.MOUSE_CLICKED, nr);
                    buttonState[ i] = false;
                    eventFired = true;
            }
        }
        /* Must have been a drag or move. */
        if (!eventFired) {
            if (buttonState[ 0]) {
                postEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON1);
            } else if (buttonState[ 1]) {
                postEvent(MouseEvent.MOUSE_DRAGGED,MouseEvent.BUTTON2);
            } else if (buttonState[ 2]) {
                postEvent(MouseEvent.MOUSE_DRAGGED,MouseEvent.BUTTON3);
            } else {
                postEvent(MouseEvent.MOUSE_MOVED, MouseEvent.NOBUTTON);
            }
        }
    }

    private void postEvent(int id, int button) {
        JNodeToolkit tk = (JNodeToolkit) Toolkit.getDefaultToolkit();
        Component source = tk.getTop().getComponentAt(x, y);
        if( source == null) source = tk.getTop();
        //TODO full support for modifiers
        MouseEvent me = new MouseEvent(source, id, System.currentTimeMillis(), EventModifier.OLD_MASK|button, x, y, 1, false, button);
        JNodeGenericPeer.q.postEvent(me);
    }

    private static final int[] BUTTON_MASK = { PointerEvent.BUTTON_LEFT,
            PointerEvent.BUTTON_RIGHT, PointerEvent.BUTTON_MIDDLE};

    private static final int[] BUTTON_NUMBER = { 1, 2, 3};
    private boolean[] buttonState = new boolean[ 3];
}
