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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.ArcGisSensorConfig;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.ImageAnalyser;
import de.grit.gdi.daemon.utils.ImageAnalyserResult;
import de.grit.gdi.daemon.utils.Utilities;

public class ArcGisSensor extends AbstractSensor {
    /** Log4J Logger */
    private static final Logger log = LoggerFactory.getLogger( "monitor.sensor.arcgissensor" );

    /** Sensorkonfiguration */
    private ArcGisSensorConfig config;

    /** HttpWrapper */
    private HttpWrapper httpwrp;

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    public ArcGisSensor() {
        super();
    }

    public Logger getLogger() {
        return log;
    }

    public String getThreadName() {
        return config.getId() + "-" + config.getName();
    }

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

        if ( !( ctConfig.getSensor() instanceof ArcGisSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.config = (ArcGisSensorConfig) ctConfig.getSensor();

        // HttpWrapper vorbereiten
        httpwrp = new HttpWrapper( ctConfig.getTimeout(), null );

        this.msgStatus = true;
    }

    /**
     * Pr\u00fcffunktion des Sensors ausf\u00fchren
     */
    public void run() {
        // Anfangszeit
        long start = System.currentTimeMillis();
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );

        String type = "";
        String urlLayer = "";
        int mapServiceExist = 0;
        // int layerExist = 0;

        log.info( config.getUrl() + " / " + config.getServiceName() + " / " + config.getLayerName() );

        if ( config.getUrl() != null && !"".equals( config.getUrl() ) ) {
            urlLayer = config.getUrl().concat( "?f=pjson" );
            type = "MapService";
            mapServiceExist = urlExist( msg, urlLayer, type );
        } else {
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( "kein MapService uebergeben" );
        }

        /*
         * wenn MapService auf Fehler gelaufen ist, dann Layer nicht mehr ueberpruefen
         */
        if ( mapServiceExist != 0 ) {
            /*
             * wenn kein Layer Name vorhanden, dann keine Pruefung
             */
            if ( config.getLayerName() != null && !"".equals( config.getLayerName() ) ) {
                type = "Layer";
                urlLayer = config.getUrl() + "/" + config.getServiceName() + "/MapServer?f=pjson";
                log.info( "Service Url: " + urlLayer );
                // layerExist = urlExist(msg, urlLayer, type);
            }

            /*
             * Bildpruefung wenn keine Bbox vorhanden, dann keine Pruefung
             */
            if ( config.getBbox() != null && !"".equals( config.getBbox() ) ) {

                String urlRest = "&bboxSR=&layers=&layerdefs=&size=&imageSR=&format=png&transparent=false&dpi=&f=pjson";
                String urlImageExport = config.getUrl() + "/" + config.getServiceName() + "/MapServer/export?bbox="
                                        + config.getBbox() + urlRest;
                log.info( urlImageExport );
                // auslesen der BildHref aus Json
                int iCheck = urlExist( msg, urlImageExport, "ImageCheck" );
                if ( iCheck != 0 ) {
                    checkMap( config.getHref(), msg );
                }

            }
        }

        // handle result / message
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
    }

    /**
     * Bild ueberpruefen
     * 
     * @param url
     * @param msg
     */
    private void checkMap( String url, CheckTaskMessage msg ) {
        int deterrcnt = 0;
        StringBuffer combmsg = new StringBuffer();
        combmsg.append( "Detailpruefung: " );

        try {
            int picsize = config.getImgSizeWidth();
            int biresult = httpwrp.doGet( url );
            byte[] idata = httpwrp.getLastData();

            if ( biresult != HttpWrapper.RESULT_OK ) {
                deterrcnt++;

                combmsg.append( url + " " );
                combmsg.append( Utilities.getProp( "config.httwrapper." + biresult, "HTTP-Fehler" ) );

            } else if ( idata != null ) {
                log.debug( "Bilddatenlaenge: " + idata.length );
                InputStream is = new ByteArrayInputStream( idata );

                ImageAnalyserResult ires = ImageAnalyser.analyse( is );

                if ( !ires.isResult() ) {
                    if ( deterrcnt > 0 ) {
                        combmsg.append( ", " );
                    }

                    deterrcnt++;
                    combmsg.append( " (Bilddaten unvollstaendig)" );
                } else if ( ( ires.getWidth() != picsize ) || ( ires.getHeight() != picsize ) ) {
                    if ( deterrcnt > 0 ) {
                        combmsg.append( ", " );
                    }
                    deterrcnt++;

                    combmsg.append( " (Bildgroesse weicht von Anfrage ab)" );
                }
                log.debug( "Bild (" + ires.isResult() + "-" + ires.getMessage() + ")" + " t=" + ires.getImgtype()
                           + " s=" + ires.getWidth() + "x" + ires.getHeight() );

                is.close();
            } else {
                // Bilddaten == null
                if ( deterrcnt > 0 ) {
                    combmsg.append( ", " );
                }

                deterrcnt++;
                combmsg.append( " (Daten unvollstaendig)" );
            }

            httpwrp.cleanup();

        } catch ( Exception ex1 ) {
            log.error( ex1.getMessage() );
            deterrcnt++;
            combmsg.append( ", " + ex1 );
        }

        if ( deterrcnt > 0 ) {
            log.warn( "" );
            log.warn( "EEE Sensor ValidateGetMapFailed" );
            log.warn( "" );
            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
            msg.setText( combmsg.toString() );
        } else {
            log.info( "" );
            log.info( "ArcGIS Sensor: Bildpruefung - OK OK Sensor ValidateGetMap" );
            log.info( "" );
            msg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
            msg.setText( "Bildpr\u00fcfung erfolgreich" );
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

    /**
     * 
     * Ueberpruefung, ob Service oder Layer vorhanden sind
     * 
     * @param msg
     * @param urlStr
     * @param type
     */
    private int urlExist( CheckTaskMessage msg, String urlStr, String type ) {

        try {
            URL url = new URL( urlStr );
            URLConnection con = url.openConnection();
            String cline;
            int len = con.getContentLength();
            if ( len > 0 ) {
                InputStream input = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( input ) );
                int mapServiceExist = 0;
                int layerExist = 0;
                int imageExist = 0;
                while ( ( cline = bufferedReader.readLine() ) != null ) {
                    if ( "MapService".equals( type ) ) {
                        if ( config.getServiceName() != null && cline.indexOf( config.getServiceName() ) != -1 ) {
                            mapServiceExist = serviceFromJson( cline, config.getServiceName() );
                        }
                    }
                    if ( "Layer".equals( type ) ) {
                        if ( config.getLayerName() != null && cline.indexOf( config.getLayerName() ) != -1 ) {
                            layerExist = serviceFromJson( cline, config.getLayerName() );
                        }
                    }
                    if ( "ImageCheck".equals( type ) ) {
                        if ( cline.indexOf( "href" ) != -1 ) {
                            String[] urlHref = cline.trim().split( "\"" );
                            config.setHref( urlHref[3] );
                            imageExist = 1;
                        }
                    }
                }
                bufferedReader.close();
                if ( mapServiceExist == 1 ) {
                    log.info( "MapService '" + config.getServiceName() + "' vorhanden" );
                    msg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
                    msg.setText( "MapService '" + config.getServiceName() + "' vorhanden" );
                    return 1;
                } else if ( layerExist == 1 ) {
                    log.info( "Layer '" + config.getLayerName() + "' vorhanden" );
                    msg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
                    msg.setText( "Layer '" + config.getLayerName() + "' vorhanden" );
                    return 1;
                } else if ( imageExist == 1 ) {

                    return 1;
                } else {
                    msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                    String str = "";
                    if ( "MapService".equals( type ) )
                        str = config.getServiceName();
                    else if ( "Layer".equals( type ) )
                        str = config.getLayerName();
                    else
                        str = "Bild";
                    msg.setText( type + " " + str + " nicht vorhanden" );
                    log.info( type + " " + str + " nicht vorhanden" );
                    return 0;
                }
            } else {
                msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                msg.setText( "No Content Available" );
                return 0;
            }
        } catch ( IOException e ) {
            e.printStackTrace();
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( "No Content Available" );
            return 0;
        }
    }

    /**
     * exakte ueberpruefung der Layer und MapService Namen
     * 
     * @param cline
     * @param str
     * @return
     */
    private int serviceFromJson( String cline, String str ) {
        int i = 0;
        String service = cline.trim();
        String[] rService;
        rService = service.split( "\"" );
        for ( String s : rService ) {
            if ( str.equals( s ) ) {
                i = 1;
            }
        }
        return i;
    }
}