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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.WmsSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.data.sensor.BoundingBox;
import de.grit.gdi.daemon.data.sensor.WMSCapabilities;
import de.grit.gdi.daemon.data.sensor.WmsLayer;
import de.grit.gdi.daemon.utils.ExceptionAnalyser;
import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.ImageAnalyser;
import de.grit.gdi.daemon.utils.ImageAnalyserResult;
import de.grit.gdi.daemon.utils.Utilities;
import de.grit.gdi.daemon.utils.sax.WMSCapabilitiesDefSaxHandler;

/**
 * DOCUMENT_ME
 */
public class WmsSensor2 extends AbstractOGCSensor {
    /** Log4J Logger */
    private static final Logger LOG = LoggerFactory.getLogger( "monitor.sensor.wmssensor" );

    /** Sensorkonfiguration */
    private WmsSensorConfig config;

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    /** Nummerngenerator f\u00fcr Zufallspositionierung */
    private Random rand = new Random();

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
        if ( !( ctConfig.getSensor() instanceof WmsSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.config = (WmsSensorConfig) ctConfig.getSensor();

        this.msgStatus = true;
    }

    /**
     * Logger an den Worker zur\u00fcckgeben
     * 
     * @return Logger Log4J Logger
     */
    public Logger getLogger() {
        return LOG;
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

    /**
     * DOCUMENT_ME
     * 
     * @param cap
     *            DOCUMENT_ME
     * @param msg
     *            DOCUMENT_ME
     */
    private void validateLayerGetMap( WMSCapabilities cap, CheckTaskMessage msg ) {
        String[] chkMapLayers = {};
        String[] chkMapStyles = {};

        if ( ( config.getLayerImgcheck() != null ) && ( config.getLayerImgcheck().length() > 0 ) ) {
            chkMapLayers = config.getLayerImgcheck().split( "\\|" );
        }

        if ( ( config.getImgcheckStyles() != null ) && ( config.getImgcheckStyles().length() > 0 ) ) {
            chkMapStyles = config.getImgcheckStyles().split( "\\|" );
        }

        if ( ( cap == null ) || ( cap.getLayer() == null ) ) {
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( "Capabilites unvollst\u00e4ndig" );

            return;
        }

        if ( chkMapStyles.length > 0 && chkMapLayers.length != chkMapStyles.length ) {
            msg.setCode( Constants.RESULT_CODE_WRONG_PARAMETER );
            msg.setText( "Liste der vorgegebenen Styles stimmt nicht mit der Anzahl der Layer \u00fcberein." );
        }

        // Nagegebene Layer pr\u00fcfen
        StringBuffer combmsg = new StringBuffer();
        combmsg.append( "Detailpr\u00fcfung: " );

        int deterrcnt = 0;

        for ( int i = 0, j = chkMapLayers.length; i < j; i++ ) {
            String curLayerName = chkMapLayers[i];
            WmsLayer curLayer = (WmsLayer) cap.getLayer().get( chkMapLayers[i] );
            String styleOverride = ( chkMapLayers.length == chkMapStyles.length ) ? chkMapStyles[i] : null;

            try {
                boolean logurl = false;
                // 0 => image_url, 1=short_format (png)
                int picsize = 150;

                String[] getmapdat = buildGetMapUrl( curLayer, styleOverride, cap, picsize );

                int biresult = httpwrp.doGet( triggerGetUrl( getmapdat[0] ) );
                byte[] idata = httpwrp.getLastData();

                if ( biresult != HttpWrapper.RESULT_OK ) {
                    if ( deterrcnt > 0 ) {
                        combmsg.append( ", " );
                    }

                    deterrcnt++;
                    combmsg.append( curLayerName );
                    combmsg.append( " (" );
                    combmsg.append( Utilities.getProp( "config.httwrapper." + biresult, "HTTP-Fehler" ) );
                    combmsg.append( " [" );
                    combmsg.append( httpwrp.getLastHttpStatusCode() );
                    combmsg.append( "]" );
                    combmsg.append( ")" );

                    logurl = true;
                } else if ( idata != null ) {
                    LOG.debug( "Bilddatenlaenge: " + idata.length );

                    InputStream is = new ByteArrayInputStream( idata );

                    ImageAnalyserResult ires = ImageAnalyser.analyse( is );

                    if ( !ires.isResult() ) {
                        if ( deterrcnt > 0 ) {
                            combmsg.append( ", " );
                        }

                        ExceptionAnalyser exa = new ExceptionAnalyser( idata );

                        deterrcnt++;
                        combmsg.append( curLayerName );

                        if ( exa.isException() ) {
                            combmsg.append( " (Exception: " ).append( exa ).append( ")" );
                        } else {
                            combmsg.append( " (Bilddaten unvollst\u00e4ndig)" );
                        }
                        logurl = true;
                    } else if ( ( ires.getWidth() != picsize ) || ( ires.getHeight() != picsize ) ) {
                        if ( deterrcnt > 0 ) {
                            combmsg.append( ", " );
                        }

                        deterrcnt++;
                        combmsg.append( curLayerName );
                        combmsg.append( " (Bildgr\u00f6\u00dfe weicht von Anfrage ab)" );
                        logurl = true;
                    } else if ( ( ires.getImgtype() == null ) || ( getmapdat[1] == null )
                                || ( !ires.getImgtype().equalsIgnoreCase( getmapdat[1] ) ) ) {
                        /*
                         * jpeg wird in jpg umgesetzt
                         * 
                         * @see de.grit.gdi.daemon.data.sensor.Capabilities
                         * 
                         * @see de.grit.gdi.daemon.utils.ImageAnalyser
                         */
                        if ( deterrcnt > 0 ) {
                            combmsg.append( ", " );
                        }

                        deterrcnt++;
                        combmsg.append( curLayerName );
                        combmsg.append( " (Bildtyp weicht von Anfrage ab)" );
                        logurl = true;
                    }

                    LOG.debug( "Bild (" + ires.isResult() + "-" + ires.getMessage() + ")" + " t=" + ires.getImgtype()
                               + " s=" + ires.getWidth() + "x" + ires.getHeight() );

                    is.close();

                    /*
                     * Debug to File
                     */

                    if ( Utilities.getProp( "config.WmsSensor2.debug", "0" ).equals( "1" ) ) {
                        try {
                            StringBuffer filename = new StringBuffer();
                            filename.append( Utilities.getProp( "config.WmsSensor2.debug.imgpath", "0" ) );
                            filename.append( "ct" );
                            filename.append( ctConfig.getTaskId() );
                            filename.append( "_s" );
                            filename.append( config.getId() );
                            filename.append( "_t" );
                            filename.append( System.currentTimeMillis() );
                            filename.append( "." );
                            filename.append( ires.getImgtype() );

                            OutputStream out = new FileOutputStream( filename.toString() );
                            out.write( idata );
                            out.close();
                        } catch ( IOException e ) {
                        }
                    }
                } else {
                    // Bilddaten == null
                    if ( deterrcnt > 0 ) {
                        combmsg.append( ", " );
                    }

                    deterrcnt++;
                    combmsg.append( curLayerName );
                    combmsg.append( " (Daten unvollst\u00e4ndig)" );
                    logurl = true;
                }

                if ( logurl ) {
                    // Log Request-URL
                    StringBuffer requrlmsg = new StringBuffer( "ct" );
                    requrlmsg.append( ctConfig.getTaskId() ).append( "_s" );
                    requrlmsg.append( config.getId() ).append( " url: " );
                    requrlmsg.append( getmapdat[0] ).append( " type:" ).append( getmapdat[1] );
                    LOG.info( requrlmsg.toString() );
                }

                httpwrp.cleanup();
            } catch ( Exception ex1 ) {
                if ( deterrcnt > 0 ) {
                    combmsg.append( ", " );
                }

                deterrcnt++;
                combmsg.append( curLayerName );
                combmsg.append( " (" );
                combmsg.append( ex1.getMessage() );
                combmsg.append( ")" );
            }
        } // for layer schleife

        if ( deterrcnt > 0 ) {
            LOG.warn( "" );
            LOG.warn( "EEE Sensor ValidateGetMapFailed" );
            LOG.warn( "" );
            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
            msg.setText( combmsg.toString() );
        } else {
            LOG.warn( "" );
            LOG.warn( "OK OK Sensor ValidateGetMap" );
            LOG.warn( "" );
        }
    }

    /**
     * Capabilities \u00fcberpr\u00fcfen ob konfigurierte Layer verf\u00fcgbar sind
     * 
     * @param cap
     *            Capabilities Objekt
     * 
     * @return Pr\u00fcfungsergebnis Ok / Fehler
     */
    private boolean validateLayerAvail( WMSCapabilities cap, CheckTaskMessage msg ) {
        boolean result = true;
        String[] avail_check = {};

        if ( ( config.getLayerAvail() != null ) && ( config.getLayerAvail().length() > 0 ) ) {
            avail_check = config.getLayerAvail().split( "\\|" );
        }

        if ( ( cap == null ) || ( cap.getLayer() == null ) ) {
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( "Capabilites unvollst\u00e4ndig" );

            return false;
        }

        // Auswertung ergebnisse
        if ( avail_check.length > 0 ) {
            // Pr\u00fcfen ob Layer aus liste verf\u00fcgbar
            StringBuffer failedLayer = new StringBuffer();

            for ( int i = 0, j = avail_check.length; i < j; i++ ) {
                if ( !cap.getLayer().containsKey( avail_check[i] ) ) {
                    if ( failedLayer.length() > 0 ) {
                        failedLayer.append( ", " );
                    }

                    failedLayer.append( avail_check[i] );
                }
            }

            if ( failedLayer.length() > 0 ) {
                // Fehler
                result = false;
                msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                msg.setText( "Layer " + failedLayer.toString() + " nicht gefunden" );
            }
        } else {
            // min. 1 Layer muss vorh. sein
            if ( !( cap.getLayer().size() > 0 ) ) {
                // Fehler
                result = false;
                msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                msg.setText( "Es muss mindesten ein Layer verf\u00fcgbar sein" );
            }
        }

        return result;
    }

    /**
     * Pr\u00fcffunktion des Sensors ausf\u00fchren
     */
    public void run() {
        long start = System.currentTimeMillis();
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );

        try {
            triggerPreRun();
        } catch ( Exception e ) {
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( e.getMessage() );
        }

        /*
         * Capabilities holen
         */
        WMSCapabilities cap = null;
        if ( canNext( msg ) ) {
            int res = httpwrp.doGet( triggerGetUrl( config.getUrl() ) );
            byte[] response = httpwrp.getLastData();

            if ( ( res == HttpWrapper.RESULT_OK ) && ( response != null ) && ( response.length > 0 ) ) {
                // Daten geholt -> Capabilities extrahieren
                StringBuffer parseDet = new StringBuffer( 50 );
                cap = parseCapabilities( response, parseDet );
                response = null;

                // Fehlermeldung setzen, sofern noch nicht vorhanden
                if ( ( cap == null ) && ( msg.getCode() == Constants.RESULT_CODE_UNDEFIEND ) ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                    msg.setText( "Capabilities konnten nicht verarbeitet werden (" + parseDet.toString().trim() + ")" );
                }
            } else {
                // Keine Daten => Fehlercode mappen
                msg.setCode( getResultCodeForHttpError( res ) );
            }

            httpwrp.cleanup();
        }

        /*
         * Layerverf\u00fcgbarkeit pr\u00fcfen
         */
        if ( canNext( msg ) ) {
            validateLayerAvail( cap, msg );
        }

        /*
         * Bildverf\u00fcgbarkeit pr\u00fcfen
         */
        if ( canNext( msg ) ) {
            long endtime = System.currentTimeMillis() + 5000;
            long tmp = 0;
            while ( System.currentTimeMillis() < endtime ) {
                tmp += 1l;
            }
            LOG.trace( "waited for {} iterations", tmp );

            validateLayerGetMap( cap, msg );
        }

        // handle result / message
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
    }

    /**
     * Capabilities parsen
     * 
     * @param data
     *            DOCUMENT_ME
     * 
     * @return Capabilities oder null bei Fehlern
     */
    private WMSCapabilities parseCapabilities( byte[] data, StringBuffer details ) {
        // Handler holen
        DefaultHandler defhandler = new WMSCapabilitiesDefSaxHandler();

        WMSCapabilities cap = ( (WMSCapabilitiesDefSaxHandler) defhandler ).getCapabilites();

        if ( data == null || data.length == 0 ) {
            details.append( "0 bytes " );
        } else if ( data.length > 1048576 ) {
            details.append( (int) Math.round( ( ( (double) data.length ) / 1048576d ) ) ).append( " mb " );
        } else if ( data.length > 1024 ) {
            details.append( (int) Math.round( ( ( (double) data.length ) / 1024d ) ) ).append( " kb " );
        } else {
            details.append( data.length ).append( " bytes " );
        }

        // SAX
        try {
            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( defhandler );
            xr.setErrorHandler( defhandler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );
        } catch ( SAXParseException e ) {
            int line = e.getLineNumber();
            int colum = e.getColumnNumber();
            if ( data != null && ( line != -1 || colum != -1 ) ) {
                if ( line != -1 )
                    details.append( "Zeile " ).append( line ).append( " " );
                if ( colum != -1 )
                    details.append( "Spalte " ).append( colum ).append( " " );
            }
            LOG.error( "Fehler beim SAX parsen der Capabilities (l:" + line + " c:" + colum + "): " + e.getMessage() );
            LOG.trace( "", e );
            cap = null;
        } catch ( Exception e ) {
            LOG.error( "Fehler beim SAX parsen der Capabilities: " + e.getMessage() );
            LOG.trace( "", e );
            cap = null;
        }

        return cap;
    }

    /**
     * Ermittelt eine Zuf\u00e4llige Position f\u00fcr den Ausschnitt in der Box
     * 
     * @param boxy
     *            Breite der zur Verf\u00fcgung stehenden Box
     * @param boxx
     *            H\u00f6he der zur Verf\u00fcgung stehenden Box
     * @param party
     *            Breit des Ausschnitts
     * @param partx
     *            H\u00f6he des Ausschnitts
     * 
     * @return Y, X f\u00fcr die linke untere Ecke des Ausschnitts in der Box
     */
    public double[] getRandomPosition( double boxy, double boxx, double party, double partx )
                            throws InterruptedException {
        double randy = 0f;
        double randx = 0f;

        double qbby = boxy / 4;
        double qbbx = boxx / 4;

        // Bild gr\u00f6\u00dfer als 1/4 => keine verschiebung
        if ( ( party > qbby ) || ( partx > qbbx ) ) {
            return new double[] { randy, randx, party, partx };
        }

        // Abweichung Y/X vom Mittelpunkt aus max. 0.99 * 1/4 Y/X
        double dy = rand.nextDouble() * qbby * ( rand.nextBoolean() ? 1 : ( -1 ) );
        double dx = rand.nextDouble() * qbbx * ( rand.nextBoolean() ? 1 : ( -1 ) );

        randy = ( 0 + ( boxy / 2 ) ) - ( party / 2 ) + dy;
        randx = ( 0 + ( boxx / 2 ) ) - ( partx / 2 ) + dx;

        return new double[] { randy, randx, party, partx };
    }

    public double[] getRandomPositionLL( double boxy, double boxx, double party, double partx )
                            throws InterruptedException {

        if ( boxy > 1500 || boxx > 1500 ) {
            // Assume wrong srs -> go metric
            return getRandomPosition( boxy, boxx, party, partx );
        }

        double wi = boxx;
        double he = boxy;

        while ( wi < 0 )
            wi += 360;
        while ( he < 0 )
            he += 360;

        // Bessel-Ellipsoid 1841 6.377.397,155 m 6.356.078,962 m Mittel 6.370.283 m
        // 1 Grad (mittel)
        float onegrad = 111182.4216f;

        double[] tmp = getRandomPosition( he * onegrad, wi * onegrad, party, partx );

        return new double[] { tmp[0] / onegrad, tmp[1] / onegrad, party / onegrad, partx / onegrad };
    }

    /**
     * DOCUMENT_ME
     * 
     * @param layer
     *            DOCUMENT_ME
     * @param cap
     *            DOCUMENT_ME
     * @param resultsize
     *            DOCUMENT_ME
     * 
     * @return DOCUMENT_ME
     * 
     * @throws IllegalArgumentException
     *             Fehlermeldung mit Detailinformationen
     */
    private String[] buildGetMapUrl( WmsLayer layer, String styleOverride, WMSCapabilities cap, int resultsize )
                            throws IllegalArgumentException,
                            InterruptedException {
        if ( layer == null ) {
            throw new IllegalArgumentException( "Layer nicht verf\u00fcgbar" );
        }

        if ( cap == null ) {
            throw new IllegalArgumentException( "Capabilities unvollst\u00e4ndig" );
        }

        BoundingBox bbox = null;
        if ( config.getImgcheckSrs() != null && config.getImgcheckSrs().length() > 0 && config.getImgcheckBbox() != null
             && config.getImgcheckBbox().length() > 0 ) {
            // bbox and srs set
            // no check if srs or bbox is allowed in capabilities
            bbox = new BoundingBox( config.getImgcheckSrs(), config.getImgcheckBbox() );

        } else if ( config.getImgcheckSrs() != null && config.getImgcheckSrs().length() > 0 ) {
            // only srs set, get matching bbox from capabilities
            bbox = layer.getLayerBBox( config.getImgcheckSrs().trim() );
            if ( bbox == null )
                throw new IllegalArgumentException( "Keine BoundingBox zu dem angegebenen SRS in den Capabilities gefunden" );

        } else {
            bbox = layer.getLayerBBox();
        }

        if ( bbox == null ) {
            throw new IllegalArgumentException( "Keine BoundingBox in den Capabilities gefunden" );
        }

        /*
         * Skalierung
         * 
         * 100 px x 100 px 15 m x 15 m
         * 
         * ==> 0.15
         */
        double scaleHint = 0.15f;
        double sizerl;
        String version = config.getVersion();

        double[] scaleHintMinMax = layer.getLayerScaleHintMinMax();

        if ( scaleHintMinMax.length == 2 ) {
            if ( ( scaleHint < scaleHintMinMax[0] ) && ( scaleHint < scaleHintMinMax[1] ) ) {
                LOG.debug( "Skallierung an Min => " + scaleHintMinMax[0] );
                scaleHint = scaleHintMinMax[0];
            } else if ( ( scaleHint > scaleHintMinMax[1] ) && ( scaleHint > scaleHintMinMax[0] ) ) {
                LOG.debug( "Skallierung an Max => " + scaleHintMinMax[1] );
                scaleHint = scaleHintMinMax[1];
            }
        } else {
            LOG.debug( "Keine Skalierungsinformationen gefunden" );
        }

        sizerl = scaleHint * resultsize;
        LOG.debug( "Ausdehnung=" + sizerl + " Pixel=" + resultsize );

        // Format der Anfrage
        String queryFmt = cap.getPrefferdFormat();
        if ( config.getImgcheckFormat() != null && config.getImgcheckFormat().length() > 0 )
            queryFmt = config.getImgcheckFormat();

        String queryFmtShort = cap.getPrefferdFormatShort();
        if ( config.getImgcheckFormat() != null && config.getImgcheckFormat().length() > 0 )
            queryFmtShort = Utilities.getImageFormatShort( config.getImgcheckFormat() );

        // *** Zusammenbauen der URL ***
        StringBuffer sb = new StringBuffer();
        String url = cap.getUrl();
        sb.append( url );

        if ( url.indexOf( "?" ) == -1 ) {
            sb.append( "?" );
        } else if ( !( url.endsWith( "&" ) || url.endsWith( "&amp;" ) ) ) {
            sb.append( "&" );
        }

        sb.append( "VERSION=" );

        if ( ( version == null ) || ( version.length() == 0 ) ) {
            sb.append( "1.1.0" );
            LOG.debug( "Using version 1.1.0" );
        } else {
            sb.append( config.getVersion() );
            LOG.debug( "Using version " + version );
        }

        sb.append( "&REQUEST=GetMap" );
        sb.append( "&WIDTH=" + resultsize );
        sb.append( "&HEIGHT=" + resultsize );

        double[] deltas;
        if ( bbox.getSrs() != null && bbox.getSrs().endsWith( ":4326" ) )
            deltas = getRandomPositionLL( bbox.getMaxy() - bbox.getMiny(), bbox.getMaxx() - bbox.getMinx(), sizerl,
                                          sizerl );
        else
            deltas = getRandomPosition( bbox.getMaxy() - bbox.getMiny(), bbox.getMaxx() - bbox.getMinx(), sizerl,
                                        sizerl );

        // System.err.println("dy=" + deltas[0] + " dx=" + deltas[1]);
        if ( "1.3.0".equals( version ) )
            sb.append( "&CRS=" );
        else
            sb.append( "&SRS=" );
        sb.append( bbox.getSrs() );

        sb.append( "&BBOX=" );
        if ( "1.3.0".equals( version ) && //
             ( config.getImgcheckBbox() == null || config.getImgcheckBbox().length() == 0 ) //
             && config.getRotation() == 1 ) {
            LOG.debug( "change direction of axes" );
            sb.append( bbox.getMiny() + deltas[1] );
            sb.append( "," );
            sb.append( bbox.getMinx() + deltas[0] );
            sb.append( "," );
            sb.append( bbox.getMiny() + deltas[1] + deltas[3] );
            sb.append( "," );
            sb.append( bbox.getMinx() + deltas[0] + deltas[2] );
        } else {
            sb.append( bbox.getMinx() + deltas[0] );
            sb.append( "," );
            sb.append( bbox.getMiny() + deltas[1] );
            sb.append( "," );
            sb.append( bbox.getMinx() + deltas[0] + deltas[2] );
            sb.append( "," );
            sb.append( bbox.getMiny() + deltas[1] + deltas[3] );
        }

        sb.append( "&LAYERS=" );
        sb.append( layer.getName() );

        sb.append( "&STYLES=" );
        if ( styleOverride != null )
            sb.append( styleOverride );
        else
            sb.append( layer.getLayerStyle() );

        sb.append( "&FORMAT=" ).append( queryFmt );

        sb.append( "&TRANSPARENT=FALSE" );
        sb.append( "&BGCOLOR=0xFFFFFF" );

        LOG.info( "GetMap String: {}", sb.toString() );

        return new String[] { sb.toString(), queryFmtShort };
    }
}