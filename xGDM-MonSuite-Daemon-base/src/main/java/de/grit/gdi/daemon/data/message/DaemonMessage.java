/*-
 * #%L
 * xGDM-MonSuite Daemon (Base)
 * %%
 * Copyright (C) 2022 - 2025 grit GmbH
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package de.grit.gdi.daemon.data.message;

/**
 * Einfache Nachricht eines Daemon
 */
public class DaemonMessage extends SimpleMessage {
    /** Konstante fuer origin */
    public static final int MONITOR_CONTROLLER = 1;

    /** Konstante fuer origin */
    public static final int MONITOR = 2;

    /** Herkunft MonitorController / Monitor */
    private int origin;

    /**
     * Creates a new DaemonMessage object.
     */
    public DaemonMessage( int origin ) {
        setOrigin( origin );
    }

    /**
     * Creates a new DaemonMessage object.
     */
    @SuppressWarnings("unused")
    private DaemonMessage() {
    }

    /**
     * Message in String umwandeln
     *
     * @return String Objektinhalt als String
     */
    public String toString() {
        return "(" + toStringBasic() + "-" + origin + "@DaemonMessage)";
    }

    /**
     * Herkunft setzen
     *
     * @param origin
     *            DOCUMENT ME
     */
    public void setOrigin( int origin ) {
        if ( origin < 1 || origin > 2 )
            throw new IllegalArgumentException();

        this.origin = origin;
    }

    /**
     * Herkunft zur\u00fcckliefern
     *
     * @return DOCUMENT ME
     */
    public int getOrigin() {
        return origin;
    }
}