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

import de.grit.gdi.daemon.data.Constants;

/**
 * Basisnachricht
 */
public abstract class SimpleMessage {
    /** Result Code */
    private int code = 0;

    /** Zeitpunkt */
    private long term;

    /** Optional: Dauer (in s) */
    private int duration = 0;

    /** Optional: Meldungstext */
    private String text = "";

    /** Quellhost (IP-Adresse) */
    private String host;

    /**
     * Creates a new SimpleMessage object.
     */
    SimpleMessage() {
    }

    /**
     * Message in String umwandeln
     *
     * @return String Objektinhalt als String
     */
    protected String toStringBasic() {
        return "(" + term + "-" + code + "-" + host + "-" + duration + "-" + text + "@SimpleMessage)";
    }

    /**
     * Message in String umwandeln
     *
     * @return String Objektinhalt als String
     */
    public abstract String toString();

    public boolean isCodeSuccessfull() {
        return getCode() == Constants.RESULT_CODE_SUCCESSFUL;
    }

    /**
     * Result Code setzen
     *
     * @param code
     *            Result Code
     */
    public void setCode( int code ) {
        this.code = code;
    }

    /**
     * Result Code zur\u00fcckliefern
     *
     * @return Result Code
     */
    public int getCode() {
        return code;
    }

    /**
     * Zeitpunkt setzen
     *
     * @param term
     *            Zeitpunkt in ms seit 0:00 01.01.1970 GMT
     */
    public void setTerm( long term ) {
        this.term = term;
    }

    /**
     * Zeitpunkt zur\u00fcckgeben
     *
     * @return Zeitpunkt in ms seit 0:00 01.01.1970 GMT
     */
    public long getTerm() {
        return term;
    }

    /**
     * Optionale Dauer setzen
     * 
     * @param duration
     *            Dauer (in s)
     */
    public void setDuration( int duration ) {
        this.duration = duration;
    }

    /**
     * Optionale Dauer zur\u00fcckliefern
     *
     * @return Dauer (in s)
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Optionaler Meldungstext setzen
     *
     * @param text
     *            Meldungstext
     */
    public void setText( String text ) {
        this.text = text;
    }

    /**
     * Optionalen Meldungstext zur\u00fcckliefern
     *
     * @return Meldungstext
     */
    public String getText() {
        return text;
    }

    /**
     * Nachrichtenquelle setzen
     *
     * @param host
     *            Quellhost (IP-Adresse)
     */
    public void setHost( String host ) {
        this.host = host;
    }

    /**
     * Nachrichtenquelle zur\u00fcckliefern
     *
     * @return Quellhost (IP-Adresse)
     */
    public String getHost() {
        return host;
    }

    /**
     * Meldungstext um Zeile erweitern (wird von setText \u00fcberschrieben)
     */
    public void appendText( String msg ) {
        if ( text != null && text.length() > 0 )
            this.text += "\n" + msg;
        else
            this.text = msg;
    }
}