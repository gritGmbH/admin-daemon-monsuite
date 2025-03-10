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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.WfsSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.data.sensor.WFSCapabilities;
import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.Utilities;
import de.grit.gdi.daemon.utils.sax.WFSCapabilitiesDefSaxHandler;
import de.grit.gdi.daemon.utils.sax.WFSFeatureDefSaxHandler;

/**
 * Implementierung des WFS-Sensor (WFS Version 1.1.0)
 */
public class WfsSensor extends AbstractOGCSensor {

    public static final String[] NAMESPACES_WFS_1_1_0 = new String[] { "xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\"",
                                                                       "xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                                                                       "xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\"" };

    public static final String[] NAMESPACES_WFS_2_0_0 = new String[] { "xmlns=\"http://www.opengis.net/wfs/2.0\"",
                                                                       "xmlns:gml=\"http://www.opengis.net/gml/3.2\"",
                                                                       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"",
                                                                       "xsi:schemaLocation=\"http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd\"" };

    /** Sensorkonfiguration */
    private WfsSensorConfig config;

    /** Log4J Logger */
    private final Logger LOG = LoggerFactory.getLogger( "monitor.sensor.wfssensor" );

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    /**
     * Initialisierung des Sensors
     *
     * @param config
     *            DOCUMENT ME
     */
    public void init( CheckTaskConfig ctConfig ) {
        super.init( ctConfig );

        if ( !( ctConfig.getSensor() instanceof WfsSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.config = (WfsSensorConfig) ctConfig.getSensor();

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
        WFSCapabilities cap = null;
        int res;
        byte[] response;

        if ( canNext( msg ) ) {
            res = httpwrp.doGet( triggerGetUrl( config.getUrl() ) );
            response = httpwrp.getLastData();

            if ( ( res == HttpWrapper.RESULT_OK ) && ( response != null ) && ( response.length > 0 ) ) {
                msg.appendText( "GetCapabilities-Anfrage ausgef\u00fchrt, " + response.length + " byte empfangen" );

                if ( canNext( msg ) ) {
                    // Daten geholt -> Capabilities extrahieren
                    cap = parseWfsCapabilities( response );

                    // Fehlermeldung setzen, sofern noch nicht vorhanden
                    if ( ( cap == null ) && ( msg.getCode() == Constants.RESULT_CODE_UNDEFIEND ) ) {
                        try {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            String exceptionTxt = parseOGCException( response );

                            if ( exceptionTxt == null )
                                exceptionTxt = parseSecurityMessage( response );

                            if ( exceptionTxt == null )
                                exceptionTxt = "Unbekannter Fehler";
                            msg.appendText( exceptionTxt );
                        } catch ( Exception ex ) {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            msg.appendText( "Capabilities konnten nicht verarbeitet werden (Fehlermeldung unvollst\u00e4ndig oder unbekannt)" );
                        }
                    }

                    if ( !( "".equals( cap.getWfsVersion() ) || //
                            "1.1.0".equals( cap.getWfsVersion() ) || //
                            "2.0".equals( cap.getWfsVersion() ) || //
                            "2.0.0".equals( cap.getWfsVersion() ) ) ) {
                        msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                        msg.appendText( "Capabilities Version " + cap.getWfsVersion() + " nicht unterstuetzt." );
                    }

                    // Free memory
                    response = null;
                }
            } else {
                // Keine Daten => Fehlercode mappen
                msg.setCode( getResultCodeForHttpError( res ) );
            }

            httpwrp.cleanup();
        }

        /*
         * Feature verf\u00fcgbarkeit pr\u00fcfen
         */
        if ( canNext( msg ) ) {
            // validateLayerAvail(cap, msg);
            String[] avail_check = {};

            if ( ( config.getFeatureCapabilities() != null ) && ( config.getFeatureCapabilities().length() > 0 ) ) {
                avail_check = config.getFeatureCapabilities().split( " " );
            }

            List<String> features = cap.getFeatureTypeNames();

            if ( ( ( features == null ) || ( features.size() == 0 ) ) && ( avail_check.length == 0 ) ) {
                // Fehler
                msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                msg.appendText( "Es muss mindesten ein Feature verf\u00fcgbar sein" );
            }

            StringBuffer failedFeatures = new StringBuffer();

            for ( int i = 0, j = avail_check.length; i < j; i++ ) {
                if ( !features.contains( avail_check[i] ) ) {
                    if ( failedFeatures.length() > 0 ) {
                        failedFeatures.append( ", " );
                    }

                    failedFeatures.append( avail_check[i] );
                }
            }

            if ( failedFeatures.length() > 0 ) {
                // Fehler
                msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                msg.setText( "Feature(s) " + failedFeatures.toString() + " nicht gefunden" );
            }
        } // End Capabilities Check

        /*
         * GetFeature-Pr\u00fcfung
         */
        if ( canNext( msg ) && config.getFeatureGetFeature() != null && config.getFeatureGetFeature().length() > 0 ) {
            // validateLayerGetMap(cap, msg);
            String[] features = config.getFeatureGetFeature().split( " " );

            for ( int i = 0; i < features.length; i++ ) {
                if ( ( features[i] == null ) || ( features[i].length() == 0 ) ) {
                    continue;
                }

                if ( !cap.getFeatureTypeNames().contains( features[i]/* .toLowerCase() */ ) ) {
                    // Feature not found
                    msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
                    msg.setText( "Feature " + features[i]
                                 + " kann nicht mit einer GetFature-Anfrage getestet werden, da es nicht in den Capabilities enthalten ist." );
                }

                String[] spl = features[i].substring( 1 ).split( "}", 2 );

                // Version in der Config auf den Wert aus den Capabilities setzen, um Nullpointer zu verhindern
                if ( config.getVersion() == null ) {
                    config.setVersion( cap.getWfsVersion() );
                }

                config.setVersion( config.getVersion().trim() );

                if ( config.getVersion().length() < 5 ) {
                    config.setVersion( config.getVersion() + ".0" );
                }

                String reqbody = buildGetFeatureRequest( config.getVersion(), "featurens", spl[0], spl[1],
                                                         ( config.getVersion().startsWith( "2.0" ) ? NAMESPACES_WFS_2_0_0
                                                                                                   : NAMESPACES_WFS_1_1_0 ) );
                res = httpwrp.doPost( cap.getGetFeatureHttpPostUri(), reqbody );
                response = httpwrp.getLastData();

                if ( ( res == HttpWrapper.RESULT_OK ) && ( response != null ) && ( response.length > 0 ) ) {
                    msg.appendText( "GetFeature-Anfrage fuer " + features[i] + " ausgef\u00fchrt, " + response.length
                                    + " byte empfangen" );

                    // +++***
                    int ifeat = parseWfsFeatures( response, features[i] );

                    if ( ifeat == 0 ) {
                        // Not found
                        msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                        msg.appendText( "GetFeature-Anfrage lieferte kein Feature fuer " + features[i] + " zurueck." );

                        LOG.debug( "Suche {} in\n{}", features[i], new String( response ) );

                        break;
                    } else if ( ifeat == -1 ) {
                        // Parser error
                        try {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            String errDet = null;
                            errDet = parseOGCException( response );
                            if ( errDet == null )
                                errDet = parseSecurityMessage( response );

                            if ( errDet == null )
                                msg.appendText( "GetFeature-Anfrage lieferte fuer " + features[i] + " einen Fehler." );
                            else
                                msg.appendText( "GetFeature-Anfrage lieferte fuer " + features[i] + " den Fehler: "
                                                + errDet );
                        } catch ( Exception ex ) {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            msg.appendText( "Die Fehlermeldung der GetFeature-Anfrage konnten nicht verarbeitet werden (Fehlermeldung unvollst\u00e4ndig oder unbekannt)" );
                        }

                        break;
                    }
                } else {
                    // HTTP-Fehler
                    msg.setCode( getResultCodeForHttpError( res ) );
                    msg.appendText( "Fehler bei GetFeature-Anfrage (siehe Fehlertyp)" );
                }
            }
        } // End GetMap

        // handle result / message
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
    }

    /**
     * @return Capabilities or null on parse error
     * @param data
     */
    private WFSCapabilities parseWfsCapabilities( byte[] data ) {
        WFSCapabilitiesDefSaxHandler defhandler = new WFSCapabilitiesDefSaxHandler();

        // SAX
        try {
            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( defhandler );
            xr.setErrorHandler( defhandler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );
        } catch ( Exception e ) {
            LOG.warn( "Fehler beim SAX parsen der Capabilities {}", e.getMessage() );
            LOG.trace( "Exception", e );

            return null;
        }

        return defhandler.getWfsCapabilities();
    }

    /**
     * @param data
     *            Result of GetFeature-Request
     * @param reqFeatureTypeName
     *            Name of Feature in Syntax: {namespace}Name
     * @return result of parsing; -1 = parse error, 0 = not found, 1 = found
     */
    private int parseWfsFeatures( byte[] data, String reqFeatureTypeName ) {
        WFSFeatureDefSaxHandler defhandler = new WFSFeatureDefSaxHandler();

        // SAX
        try {
            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( defhandler );
            xr.setErrorHandler( defhandler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );
        } catch ( Exception e ) {
            LOG.warn( "Fehler beim SAX parsen der Feature Collection {}", e.getMessage() );
            LOG.trace( "Exception", e );

            return -1;
        }

        return defhandler.getFeatureTypes().contains( reqFeatureTypeName ) ? 1 : 0;
    }

    private String buildGetFeatureRequest( String version, String fNS, String fNSURI, String fName, String[] xmlns ) {
        // TODO template verwenden, damit es WFS 2.0 kompatibel ist
        String template = Utilities.getProp( "sensor.template.wfs." + version, null );
        if ( template == null ) {
            StringBuffer req = new StringBuffer( 1000 );
            req.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
            req.append( "<wfs:GetFeature version=\"" ).append( version ).append( "\" maxFeatures=\"1\" " );

            req.append( " xmlns:" ).append( fNS ).append( "=\"" ).append( fNSURI ).append( "\"" );

            // xmlns:abc="http://www.***REMOVED***.demo.grit.de/***REMOVED***"
            for ( int i = 0; i < xmlns.length; i++ ) {
                req.append( " " ).append( xmlns[i] );
            }

            req.append( triggerPostVSPasText() );
            req.append( ">\n" );

            req.append( "  <wfs:Query typeName=\"" ).append( fNS ).append( ":" ).append( fName ).append( "\">" );
            req.append( "  </wfs:Query>" );
            req.append( "</wfs:GetFeature>" );

            return req.toString();
        } else {
            template = template.replaceAll( "@version@", version ) //
                               .replaceAll( "@xmlns@", " xmlns:" + fNS + "=\"" + fNSURI + "\"" ) //
                               .replaceAll( "@namespaces_wfs@", String.join( " ", xmlns ) ) //
                               .replaceAll( "@postvs@", triggerPostVSPasText() ) //
                               .replaceAll( "@type_name@", fNS + ":" + fName );

            return template;
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