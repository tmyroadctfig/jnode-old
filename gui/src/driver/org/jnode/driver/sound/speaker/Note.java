
package org.jnode.driver.sound.speaker;

/** A class to represent notes that a speaker can play.
 *  @author Matt Paine
 **/
public class Note
{

//**********  constants  **********//

	public static final int NOTE_C3 = 131;
	public static final int NOTE_D3 = 147;
	public static final int NOTE_E3 = 165;
	public static final int NOTE_F3 = 175;
	public static final int NOTE_G3 = 196;
	public static final int NOTE_A3 = 220;
	public static final int NOTE_B3 = 247;
	public static final int NOTE_C4 = 262;
	public static final int NOTE_D4 = 294;
	public static final int NOTE_E4 = 330;
	public static final int NOTE_F4 = 350;
	public static final int NOTE_G4 = 392;
	public static final int NOTE_A4 = 440;
	public static final int NOTE_B4 = 494;
	public static final int NOTE_C5 = 523;
	public static final int NOTE_D5 = 587;
	public static final int NOTE_E5 = 659;
	public static final int NOTE_F5 = 698;
	public static final int NOTE_G5 = 784;
	public static final int NOTE_A5 = 880;
	public static final int NOTE_B5 = 988;
	public static final int NOTE_C6 = 1047;


//**********  private variables  **********//

	private int note;
	private int length;


//**********  constructor  **********//

	/** Constructs a Note object.
	 *  @param note The frequency of the note to play.
	 *  @param length The length of the note to play (in milliseconds).
	 **/
	public Note (int note, int length)
	{
		this.note = note;
		this.length = length;
	}

//**********  public methods  **********//

	/** Getter method.
	 *  @return The frequency of the note to play.
	 **/
	public int getNote () { return note; }

	/** Getter method.
	 *  @return The length of time to play the note.
	 **/
	public int getLength () { return length; }



}

