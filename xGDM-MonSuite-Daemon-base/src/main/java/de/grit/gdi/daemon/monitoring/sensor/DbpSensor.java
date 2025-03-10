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

import static de.grit.gdi.daemon.utils.Utilities.urlEnocde;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.DbpSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.utils.HttpWrapper;

/**
 * DOCUMENT_ME
 */
public class DbpSensor extends AbstractSensor {
    /** Log4J Logger */
    private static final Logger log = LoggerFactory.getLogger( "monitor.sensor.dbpsensor" );

    /** Sensorkonfiguration */
    private DbpSensorConfig config;

    /** HttpWrapper */
    private HttpWrapper httpwrp;

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    /**
     * Initialisierung des Sensors
     *
     * @param config
     *            AbstractSensorConfig Konfiguration des Sensors
     * @param timeout
     *            int Timeout bei Pr\u00fcfungen
     * @param pause
     *            int Pause zwischen den Pr\u00fcfungen
     */
    public void init( CheckTaskConfig ctConfig ) {
        super.init( ctConfig );

        if ( !( ctConfig.getSensor() instanceof DbpSensorConfig ) ) {
            throw new IllegalArgumentException();
        }
        this.config = (DbpSensorConfig) ctConfig.getSensor();
        // HttpWrapper vorbereiten
        httpwrp = new HttpWrapper( ctConfig.getTimeout(), null );

        // Optionale Authentifizierung setzen
        if ( ( config.getHttpUser() != null ) && ( config.getHttpUser().length() > 0 )
             && ( config.getHttpPass() != null ) && ( config.getHttpPass().length() > 0 ) ) {
            httpwrp.setAuth( config.getHttpUser(), config.getHttpPass() );
        }

        this.msgStatus = true;
    }

    /**
     * Logger, der vom PoolWorker verwendet werden soll
     *
     * @return Logger Log4J Logger f\u00fcr den PoolWorker
     */
    public Logger getLogger() {
        return log;
    }

    /**
     * Basisnamen des Threads zur\u00fcckliefern
     *
     * @return String Namesprefix f\u00fcr den Thread
     */
    public String getThreadName() {
        return config.getId() + "-" + config.getName();
    }

    /**
     * DOCUMENT_ME
     *
     * @param streamData
     *            DOCUMENT_ME
     * @param charset
     *            DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    private Object[] getPropertiesFromStream( byte[] streamData, String charset ) {
        Object[] res = new Object[2];

        try {
            BufferedReader br;
            if ( charset != null && charset.length() > 0 )
                br = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( streamData ), charset ) );
            else
                br = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( streamData ) ) );

            String rcode = "4";
            String duration = "0";

            for ( String line; ( line = br.readLine() ) != null; ) {
                // Daten vorhanden
                if ( line.startsWith( "ResultCode=" ) ) {
                    // Found
                    rcode = line.substring( line.indexOf( "=" ) + 1 );
                } else if ( line.startsWith( "Duration=" ) ) {
                    // Found
                    duration = line.substring( line.indexOf( "=" ) + 1 );
                }
            }

            res[0] = Integer.decode( rcode );
            res[1] = Integer.decode( duration );
        } catch ( UnsupportedEncodingException e ) {
            // Encoding Fehler
            log.warn( "Falscher Zeichensatz der R\u00fcckgabe" );
        } catch ( IOException e ) {
            log.warn( "E/A Fehler beim Lesen der Daten" );
        } catch ( Exception e ) {
            log.warn( "Fehler beim Lesen der Daten" + e.getMessage() );
        }

        if ( ( res.length == 2 ) && ( res[0] != null ) && ( res[1] != null ) ) {
            return res;
        } else {
            return new Object[0];
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's <code>run</code> method to be called in that separately executing thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        long start = System.currentTimeMillis();
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );

        String url = config.getUrl();
        url += "?dsn=" + config.getDatabase();
        url += "&uid=" + config.getAppUser();
        url += "&pwd=" + urlEnocde( config.getAppPass() );
        url += "&ActionID=" + config.getAction();
        url += "&SqlCmd=" + urlEnocde( config.getSqlCmd() );

        log.info( "URL FOR Sensor: " + url );

        if ( canNext( msg ) ) {
            int res = httpwrp.doGet( url );
            byte[] response = httpwrp.getLastData();
            String charset = httpwrp.getLastCharset();

            if ( ( res == HttpWrapper.RESULT_OK ) && ( response != null ) && ( response.length > 0 ) ) {
                // Daten Erfolgreich geholt
                // Weitere Pr\u00fcfungen veranlassen
                boolean vres = true;

                if ( vres ) {
                    Object[] resp = getPropertiesFromStream( response, charset );

                    if ( ( resp.length > 1 ) && ( resp[0] != null ) && ( resp[1] != null ) ) {
                        msg.setCode( ( (Integer) resp[0] ).intValue() );
                        msg.setDuration( ( (Integer) resp[1] ).intValue() / 1000 );
                    } else {
                        msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                    }

                    if ( !msg.isCodeSuccessfull() ) {
                        vres = false;
                    }
                }

                if ( vres ) {
                    msg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
                    msg.setText( "" );
                } else if ( msg.getCode() == 0 ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                }
            } else {
                // Keine Daten verf\u00fcgbar
                // Fehlermeldung auf Msg-RC mappen
                switch ( res ) {
                case HttpWrapper.RESULT_CONNECTION_REFUSED:
                case HttpWrapper.RESULT_UNKNOWN_HOST:
                case HttpWrapper.RESULT_CONNECTION_TIMEOUT:
                case HttpWrapper.RESULT_NO_ROUTE:
                    msg.setCode( Constants.RESULT_CODE_CONNECTION_TIMEOUT );

                    break;

                case HttpWrapper.RESULT_READ_TIMEOUT:
                    msg.setCode( Constants.RESULT_CODE_DATA_TIMEOUT );

                    break;

                case HttpWrapper.RESULT_WRONG_RESULT:
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );

                    break;

                case HttpWrapper.RESULT_UNKNOW_ERROR:
                case HttpWrapper.RESULT_MAX_SIZE_EXCEEDED:
                    msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );

                    break;

                default:
                    msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );

                    break;
                }
            }
        }

        // report result
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
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
}