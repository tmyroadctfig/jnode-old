/*
 * $Id$
 */
package org.jnode.driver.sound.speaker.pc;
import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.sound.speaker.Note;
import org.jnode.driver.sound.speaker.SpeakerAPI;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;

/** A driver for the internal speaker of a IBM Compatable machine.
 *  @author Matt Paine
 **/
public class PCSpeakerDriver extends Driver implements SpeakerAPI
{

//**********  constants  **********//

	/** The port for the speaker **/
	public final static int SPEAKER_PORT = 0x61;

	/** The PIT Control Port **/
	public final static int CONTROL_PORT = 0x43;

	/** The PIT Channel 2 Port **/
	public final static int CHANNEL2_PORT = 0x42;

	/** The base frequency for the PIT **/
	public final static int BASE_FREQUENCY = 1193100;


//**********  private variables  **********//

	/** This holds the reference to the IOResource we need to manipulate. **/
	private IOResource speakIO;

	/** This holds the reference to the IOResource for the PIT **/
	private IOResource pitIO;


//**********  Driver implementation  **********//

	/** A routine that claims all the resources nessasary to run the PCSpeaker. **/
	public void startDevice() throws DriverException
	{
		try
		{
			final Device dev = getDevice();
			final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			speakIO = claimPorts(rm, dev, SPEAKER_PORT, 1);
			pitIO = claimPorts(rm, dev, CHANNEL2_PORT, 2);
			getDevice().registerAPI(SpeakerAPI.class, this);
			// do a test beep during startup
			//beep();
		}
		catch (NameNotFoundException nnfex) { throw new DriverException (nnfex); }
		catch (ResourceNotFreeException rnfex) { throw new DriverException (rnfex); }
	}

	/** A routine that releases all the resources back to the operating system. **/
	public void stopDevice() throws DriverException
	{
		getDevice().unregisterAPI(SpeakerAPI.class);
		pitIO.release();
		speakIO.release();
	}


//**********  Speaker implementation  **********//

	public void beep()
	{
		// backup the port, and start the beep
		int oldPort = speakIO.inPortByte(SPEAKER_PORT);
		int newValue = oldPort | 0x03;    // 0b0000_0011;
		speakIO.outPortByte(SPEAKER_PORT, newValue);

		// sleep for the duration of the beep
		try { Thread.sleep(125); }
		catch (InterruptedException iex) { }

		// restore the speaker port
		speakIO.outPortByte(SPEAKER_PORT, oldPort);
	}

	public void playNote (Note n)
	{
		pitIO.outPortByte (CONTROL_PORT, 0xb6);
		playNote (n.getNote(), n.getLength());
	}

	public void playNote (Note[] n)
	{
		pitIO.outPortByte (CONTROL_PORT, 0xb6);
		for (int x = 0; x < n.length; x++)
			playNote(n[x].getNote(), n[x].getLength());
	}

	public void playNote (int frequency, int length)
	{
		int freq = (BASE_FREQUENCY/frequency);
		pitIO.outPortByte(CHANNEL2_PORT, (byte)(freq & 0xff));
		pitIO.outPortByte(CHANNEL2_PORT, (byte)(freq >> 8));

		// backup the port, and start the beep
		int oldPort = speakIO.inPortByte(SPEAKER_PORT);
		int newValue = oldPort | 0x03;    // 0b0000_0011;
		speakIO.outPortByte(SPEAKER_PORT, newValue);

		// sleep for the duration of the beep
		try { Thread.sleep(length); }
		catch (InterruptedException iex) { }

		// restore the speaker port
		speakIO.outPortByte(SPEAKER_PORT, oldPort);
	}

	private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner, final int low, final int length) throws ResourceNotFreeException, DriverException {
		try {
            return (IOResource)AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ResourceNotFreeException {
            		return rm.claimIOResource(owner, low, length);
                    }});
		} catch (ResourceNotFreeException ex) {
		    throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }
	    
	}
}

