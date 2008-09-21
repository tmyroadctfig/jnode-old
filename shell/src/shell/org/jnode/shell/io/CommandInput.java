/*
 * $Id: ShellManager.java 3571 2007-10-26 21:30:12Z lsantha $
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
package org.jnode.shell.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.jnode.util.IOUtils;
import org.jnode.util.ReaderInputStream;

/**
 * This CommandIO implementation supports input streams.
 * 
 * @author crawley@jnode.org
 */
public class CommandInput extends BaseCommandIO {
    
    private Reader reader;
    private InputStream inputStream;

    public CommandInput(Reader reader) {
        super(reader);
        this.reader = reader;
    }

    public CommandInput(InputStream inputStream) {
        super(inputStream);
        this.inputStream = inputStream;
    }

    public synchronized InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new ReaderInputStream(reader, getEncoding());
        }
        return inputStream;
    }

    public Reader getReader() throws CommandIOException {
        if (reader == null) {
            try {
                reader = new InputStreamReader(inputStream, getEncoding());
            } catch (UnsupportedEncodingException ex) {
                throw new CommandIOException("Cannot get reader", ex);
            }
        }
        return reader;
    }

    @Override
    public final int getDirection() {
        return DIRECTION_IN;
    }

    @Override
    public boolean isTTY() {
        Object obj = getSystemObject();
        if (obj instanceof Reader) {
            return IOUtils.isTTY((Reader) obj);
        } else {
            return IOUtils.isTTY((InputStream) obj);
        }
    }

    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (reader != null) {
            reader.close();
        }
    }

    @Override
    public OutputStream getOutputStream() {
        throw new CommandIOException("Output not supported");
    }

    @Override
    public PrintStream getPrintStream() {
        throw new CommandIOException("Output not supported");
    }

    @Override
    public PrintWriter getPrintWriter() {
        throw new CommandIOException("Output not supported");
    }

    @Override
    public Writer getWriter() throws CommandIOException {
        throw new CommandIOException("Output not supported");
    }
}
