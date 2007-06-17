/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */



package sun.misc;

import java.util.Vector;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class is intended to be a central place for the jdk to 
 * log timing events of interest.  There is pre-defined event
 * of startTime, as well as a general
 * mechanism of setting aribtrary times in an array.
 * All unreserved times in the array can be used by callers
 * in application-defined situations.  The caller is responsible
 * for setting and getting all times and for doing whatever 
 * analysis is interesting; this class is merely a central container
 * for those timing values.
 * Note that, due to the variables in this class being static,
 * use of particular time values by multiple applets will cause
 * confusing results.  For example, if plugin runs two applets 
 * simultaneously, the initTime for those applets will collide
 * and the results may be undefined.
 * <P>
 * To automatically track startup performance in an app or applet,
 * use the command-line parameter sun.perflog as follows:<BR>
 *     -Dsun.perflog[=file:<filename>]
 * <BR>
 * where simply using the parameter with no value will enable output
 * to the console and a value of "file:<filename>" will cause 
 * that given filename to be created and used for all output.
 * <P>
 * <B>Warning: Use at your own risk!</B> 
 * This class is intended for internal testing 
 * purposes only and may be removed at any time.  More
 * permanent monitoring and profiling APIs are expected to be
 * developed for future releases and this class will cease to
 * exist once those APIs are in place.
 * @author Chet Haase
 */
public class PerformanceLogger {

    // Timing values of global interest
    private static final int START_INDEX    = 0;    // VM start
    private static final int LAST_RESERVED  = START_INDEX;

    private static boolean perfLoggingOn = false;
    private static Vector times;
    private static String logFileName = null;
    private static Writer logWriter = null;

    static {
        String perfLoggingProp = 
	    (String) java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.perflog"));
	if (perfLoggingProp != null) {
	    perfLoggingOn = true;
	    // Now, figure out what the user wants to do with the data
	    if (perfLoggingProp.regionMatches(true, 0, "file:", 0, 5)) {
		logFileName = perfLoggingProp.substring(5);
	    }
	    if (logFileName != null) {
		if (logWriter == null) {
		    java.security.AccessController.doPrivileged(
		    new java.security.PrivilegedAction() {
			public Object run() {
			    try {
				File logFile = new File(logFileName);
				logFile.createNewFile();
				logWriter = new FileWriter(logFile);
			    } catch (Exception e) {
				System.out.println(e + ": Creating logfile " +
						   logFileName + 
						   ".  Log to console");
			    }
			    return null;
			}
		    });
		}
	    }
	    if (logWriter == null) {
		logWriter = new OutputStreamWriter(System.out);
	    }
	}
	times = new Vector(10);
	// Reserve predefined slots
	for (int i = 0; i <= LAST_RESERVED; ++i) {
	    times.add(new TimeData("Time " + i + " not set", 0));
	}
    }

    /**
     * Returns status of whether logging is enabled or not.  This is
     * provided as a convenience method so that users do not have to
     * perform the same GetPropertyAction check as above to determine whether
     * to enable performance logging.
     */
    public static boolean loggingEnabled() {
	return perfLoggingOn;
    }


    /**
     * Internal class used to store time/message data together.
     */
    static class TimeData {
	String message;
	long time;

	TimeData(String message, long time) {
	    this.message = message;
	    this.time = time;
	}

	String getMessage() {
	    return message;
	}

	long getTime() {
	    return time;
	}
    }

    /**
     * Sets the start time.  Ideally, this is the earliest time available
     * during the startup of a Java applet or application.  This time is
     * later used to analyze the difference between the initial startup 
     * time and other events in the system (such as an applet's init time).
     */
    public static void setStartTime(String message) {
	if (loggingEnabled()) {
	    long nowTime = System.currentTimeMillis();
	    setStartTime(message, nowTime);
	}
    }

    /**
     * Sets the start time.  
     * This version of the method is
     * given the time to log, instead of expecting this method to
     * get the time itself.  This is done in case the time was
     * recorded much earlier than this method was called.
     */
    public static void setStartTime(String message, long time) {
	if (loggingEnabled()) {
	    times.set(START_INDEX, new TimeData(message, time));
	}
    }

    /**
     * Gets the start time, which should be the time when
     * the java process started, prior to the VM actually being
     * loaded.
     */
    public static long getStartTime() {
	if (loggingEnabled()) {
	    return ((TimeData)times.get(START_INDEX)).getTime();
	} else {
	    return 0;
	}
    }

    /** 
     * Sets the value of a given time and returns the index of the
     * slot that that time was stored in.
     */
    public static int setTime(String message) {
	if (loggingEnabled()) {
	    long nowTime = System.currentTimeMillis();
	    return setTime(message, nowTime);
	} else {
	    return 0;
	}
    }

    /** 
     * Sets the value of a given time and returns the index of the
     * slot that that time was stored in.
     * This version of the method is
     * given the time to log, instead of expecting this method to
     * get the time itself.  This is done in case the time was
     * recorded much earlier than this method was called.
     */
    public static int setTime(String message, long time) {
	if (loggingEnabled()) {
	    // times is already synchronized, but we need to ensure that
	    // the size used in times.set() is the same used when returning
	    // the index of that operation.
	    synchronized (times) {
		times.add(new TimeData(message, time));
		return (times.size() - 1);
	    }
	} else {
	    return 0;
	}
    }

    /**
     * Returns time at given index.
     */
    public static long getTimeAtIndex(int index) {
	if (loggingEnabled()) {
	    return ((TimeData)times.get(index)).getTime();
	} else {
	    return 0;
	}
    }

    /**
     * Returns time at given index.
     */
    public static String getMessageAtIndex(int index) {
	if (loggingEnabled()) {
	    return ((TimeData)times.get(index)).getMessage();
	} else {
	    return null;
	}
    }

    /**
     * Outputs all data to parameter-specified Writer object
     */
    public static void outputLog(Writer writer) {
	if (loggingEnabled()) {
	    try {
		synchronized(times) {
		    for (int i = 0; i < times.size(); ++i) {
			TimeData td = (TimeData)times.get(i);
			if (td != null) {
			    writer.write(i + " " + td.getMessage() + ": " + 
					 td.getTime() + "\n");
			}
		    }
		}
		writer.flush();
	    } catch (Exception e) {
		System.out.println(e + ": Writing performance log to " + 
				   writer);
	    }
	}
    }
    	
    /**
     * Outputs all data to whatever location the user specified
     * via sun.perflog command-line parameter.
     */
    public static void outputLog() {
	outputLog(logWriter);
    }
}
