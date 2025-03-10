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
package de.grit.gdi.daemon.messaging.handler;

import de.grit.gdi.daemon.data.message.CheckTaskMessage;

/**
 * Parameterklasse f\u00fcr SnmpMessageHandler
 */
public class SnmpMessageParameter extends AbstractMessageParameter {
    public static final int VERSION_V1_TRAP = 0;

    public static final int VERSION_V2C_NOTIFY = 1;

    /** Empf\u00e4ngeradresse */
    private String destination;

    /** SNMPv1 / SNMPv2 Community */
    private String comunity;

    /** Version Trap / */
    private int version;

    /** Pr\u00fcfauftragsnummer */
    private int checkTaskNr;

    /** Zeitstempel der Meldung */
    private long timeStamp;

    /** Dauer in Sekunden */
    private int duration;

    /** Ergebnis-Code */
    private int resultCode;

    /** Ergebnis-Code-Meldung */
    private String resultText;

    /**
     * Creates a new SnmpMessageParameter object.
     */
    public SnmpMessageParameter() {
        super();
        init();
    }

    public SnmpMessageParameter( CheckTaskMessage ctm ) {
        super();
        init();

        this.resultCode = ctm.getCode();
        this.timeStamp = ctm.getTerm();
        this.duration = ctm.getDuration();
        this.checkTaskNr = ctm.getCheckTaksId();
    }

    private void init() {
        // Defaultwerte
        comunity = "public";
        version = VERSION_V1_TRAP;
    }

    /* Getter & Setter */

    public String getDestination() {
        return destination;
    }

    public void setDestination( String destination ) {
        this.destination = destination;
    }

    public String getComunity() {
        return comunity;
    }

    public void setComunity( String comunity ) {
        this.comunity = comunity;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion( int version ) {
        this.version = version;
    }

    public int getCheckTaskNr() {
        return checkTaskNr;
    }

    public void setCheckTaskNr( int checkTaskNr ) {
        this.checkTaskNr = checkTaskNr;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp( long timeStamp ) {
        this.timeStamp = timeStamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration( int duration ) {
        this.duration = duration;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode( int resultCode ) {
        this.resultCode = resultCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText( String resultText ) {
        this.resultText = resultText;
    }
}