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
package de.grit.gdi.daemon.data;

/**
 * Konstanten f\u00fcr die GDI Monitoring Suite
 *
 * Result Codes => RESULT_CODE_*
 */
public class Constants {
    /** Ergebnis undefiniert (Interner Fehlercode) */
    public static final int RESULT_CODE_UNDEFIEND = 0;

    /** Aktion Erfolgreich */
    public static final int RESULT_CODE_SUCCESSFUL = 1;

    /** Zeit\u00fcberschreitung (Timeout) w\u00e4hrend einer Aktion aufgetreten (z.B. Netzwerkfehler) */
    public static final int RESULT_CODE_DATA_TIMEOUT = 2;

    /** Fehler beim Verbindungsaufbau zum Ziel (z.B. Dienst nicht verf\u00fcgbar) */
    public static final int RESULT_CODE_CONNECTION_TIMEOUT = 3;

    /** Aktion fehlgeschlagen (ohne weitere Angaben) */
    public static final int RESULT_CODE_FAILED_UNKNOWN = 4;

    /** Aktion fehlgeschlagen (Falsche Antwort des Ziels) */
    public static final int RESULT_CODE_FAILED_WRONG_DATA = 5;

    /** \u00dcbergabeparameter / Konfiguration Fehlerhaft */
    public static final int RESULT_CODE_WRONG_PARAMETER = 6;

    /** Sensor nicht erreichbar */
    public static final int RESULT_CODE_NOT_REACHABLE = 7;

    /** Nachrichtentransport nicht m\u00f6glich */
    public static final int RESULT_CODE_MSG_TRANSPORT_FAILED = 8;
    
    /** Collections enth\u00e4lt keine Feature*/
    public static final int RESULT_EMPTY_ARRAY = 9;

    /**
     * Creates a new Constants object.
     */
    private Constants() {
    }
}