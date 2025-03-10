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
package de.grit.gdi.daemon.monitoring.sensor;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.HttpSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.utils.HttpWrapper;

public class HttpSensor extends AbstractSensor {

    /** Log4J Logger */
    private static final Logger log = LoggerFactory.getLogger( "monitor.sensor.wmssensor" );

    private HttpSensorConfig config;

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    /** HttpWrapper */
    private HttpWrapper httpwrp;

    /**
     * Initialisierung des Sensors
     * 
     * @param config
     *            Sensorkonfiguration
     * @param timeout
     *            Timeout
     * @param pause
     *            Pause
     */
    public void init( CheckTaskConfig ctConfig ) {
        super.init( ctConfig );

        if ( !( ctConfig.getSensor() instanceof HttpSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.ctConfig = ctConfig;
        this.config = (HttpSensorConfig) ctConfig.getSensor();

        this.msgStatus = true;

        // HttpWrapper vorbereiten
        httpwrp = new HttpWrapper( ctConfig.getTimeout(), config.getProxyStr() );

        // Optionale Authentifizierung setzen
        if ( ( config.getAuthUser() != null ) && ( config.getAuthUser().length() > 0 )
             && ( config.getAuthPass() != null ) && ( config.getAuthPass().length() > 0 ) ) {
            httpwrp.setAuth( config.getAuthUser(), config.getAuthPass() );
        }
    }

    /**
     * Logger an den Worker zur\u00fcckgeben
     * 
     * @return Logger Log4J Logger
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * ThreadNamen an den Worker zur\u00fcckgeben
     * 
     * @return String Threadnamen
     */
    public String getThreadName() {
        return config.getId() + "-" + config.getName();
    }

    /**
     * Nachrichtenstatus zur\u00fcckliefern
     * 
     * @return Nachrichten erfolgreich zugestellt
     */
    public boolean getMessageStatus() {
        return this.msgStatus;
    }

    /**
     * Pr\u00fcfauftrag Konfiguration zur\u00fcckgeben
     * 
     * @return Konfiguration des Pr\u00fcfauftrags
     */
    public CheckTaskConfig getCheckTaskConfig() {
        return ctConfig;
    }

    public void run() {
        long start = System.currentTimeMillis();
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );

        // ToDo
        int res = httpwrp.doGet( config.getUrl() );
        int httpCode = httpwrp.getLastHttpStatusCode();
        byte[] response = httpwrp.getLastData();
        String charset = httpwrp.getLastCharset();

        String resString = null;

        List<Integer> validCodes = config.getValidCodeAsList();

        if ( validCodes.size() > 0 && validCodes.contains( new Integer( httpCode ) ) ) {
            // go ahead
            msg.appendText( "Adresse erfolgreich abgerufen und Ergebniscode g\u00fcltig." );
        } else if ( validCodes.size() == 0 && res == HttpWrapper.RESULT_OK && response != null && response.length > 0 ) {
            // go ahead
            msg.appendText( "Adresse erfolgreich abgerufen (" + response.length + " bytes)" );
        } else {
            // Keine Daten verf\u00fcgbar
            // Fehlermeldung auf Msg-RC mappen
            msg.setCode( HttpWrapper.mapResultToConstantResult( res ) );
            msg.setText( "Die angeforderte Adresse konnte nicht abgerufen werden oder der Ergebniscode war nicht definiert (Code: "
                         + httpCode + ")" );
        }

        if ( canNext( msg ) ) {
            try {
                if ( response == null || response.length == 0 )
                    resString = "";
                else if ( charset != null && charset.length() > 0 )
                    resString = new String( response, charset );
                else
                    resString = new String( response );
            } catch ( UnsupportedEncodingException e ) {
                // exception
                msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                msg.appendText( "Das Charset des Ergebnisdokuments ist ung\u00fcltig (Charset: " + charset
                                + " Fehler: " + e.getMessage() + ")" );
            }
        }

        if ( canNext( msg ) ) {
            // ceheck for fail pattern
            String notRegexp = config.getRegexpNot();
            if ( notRegexp != null && notRegexp.length() > 0 ) {
                if ( resString == null || resString.length() == 0 ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                    msg.appendText( "Ergebnisdokument ist ung\u00fcltig (Not-Regexp)" );
                } else {
                    try {
                        if ( Pattern.compile( notRegexp, Pattern.DOTALL | Pattern.MULTILINE ).matcher( resString ).matches() ) {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            msg.appendText( "Ergebnisdokument ist ung\u00fcltig (Not-Regexp)" );
                        } else {
                            msg.appendText( "Pr\u00fcfung auf ung\u00fcltige Daten lieferte keinen Treffer (Not-Regexp)" );
                        }
                    } catch ( PatternSyntaxException pse ) {
                        // fehlermeldung das pattern nicht ok
                        msg.setCode( Constants.RESULT_CODE_WRONG_PARAMETER );
                        msg.appendText( "Regul\u00e4rer Ausdruck f\u00fcr ein ung\u00fcltiges Ergebnis ist fehlerhaft (Not-Regexp)" );
                    }
                }
            }
        }

        if ( canNext( msg ) ) {
            // ceheck for fail pattern
            String reqRegexp = config.getRegexpReq();
            if ( reqRegexp != null && reqRegexp.length() > 0 ) {
                if ( resString == null || resString.length() == 0 ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                    msg.appendText( "Ergebnisdokument ist ung\u00fcltig (Req-Regexp)" );
                } else {
                    try {
                        if ( !Pattern.compile( reqRegexp, Pattern.DOTALL | Pattern.MULTILINE ).matcher( resString ).matches() ) {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            msg.appendText( "Ergebnisdokument ist ung\u00fcltig (Req-Regexp)" );
                        } else {
                            msg.appendText( "Pr\u00fcfung auf g\u00fcltige Daten lieferte einen Treffer (Req-Regexp)" );
                        }
                    } catch ( PatternSyntaxException pse ) {
                        // fehlermeldung das pattern nicht ok
                        msg.setCode( Constants.RESULT_CODE_WRONG_PARAMETER );
                        msg.appendText( "Regul\u00e4rer Ausdruck f\u00fcr ein g\u00fcltiges Ergebnis ist fehlerhaft (Req-Regexp)" );
                    }
                }
            }
        }

        // report result
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
    }
}