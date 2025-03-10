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

import org.xml.sax.InputSource;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.WfsSensorConfig;
import de.grit.gdi.daemon.data.config.WmsSensorConfig;
import de.grit.gdi.daemon.data.config.OafSensorConfig;
import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.sax.OGCExceptionDefSaxHandler;

public abstract class AbstractOGCSensor extends AbstractSensor {
    private static final int AUTH_TYPE_DEEGRE_WAS_2_1 = 2;

    // private static final int AUTH_TYPE_HTTP = 1;

    /** HttpWrapper */
    protected HttpWrapper httpwrp;

    private int authType = -1;

    private String authUrl = null;

    private String authUser = null;

    private String authPass = null;

    private String sessionId = null;

    // private long lastSessionTime = 0L;

    private StringBuffer fetchLastMessages = null;

    public AbstractOGCSensor() {
        super();
        fetchLastMessages = new StringBuffer( 50 );
    }

    /**
     * Initialisierung
     */
    public void init( CheckTaskConfig ctConfig ) {
        super.init( ctConfig );

        String auth = null;
        String url = null;
        String proxy = null;

        if ( ctConfig.getSensor() instanceof WmsSensorConfig ) {
            authUser = ( (WmsSensorConfig) ctConfig.getSensor() ).getAuthUser();
            authPass = ( (WmsSensorConfig) ctConfig.getSensor() ).getAuthPass();
            auth = ( (WmsSensorConfig) ctConfig.getSensor() ).getAuthType();
            authUrl = ( (WmsSensorConfig) ctConfig.getSensor() ).getAuthUrl();
            url = ( (WmsSensorConfig) ctConfig.getSensor() ).getUrl();
            proxy = ( (WmsSensorConfig) ctConfig.getSensor() ).getProxyStr();
        } else if ( ctConfig.getSensor() instanceof WfsSensorConfig ) {
            authUser = ( (WfsSensorConfig) ctConfig.getSensor() ).getAuthUser();
            authPass = ( (WfsSensorConfig) ctConfig.getSensor() ).getAuthPass();
            auth = ( (WfsSensorConfig) ctConfig.getSensor() ).getAuthType();
            authUrl = ( (WfsSensorConfig) ctConfig.getSensor() ).getAuthUrl();
            url = ( (WfsSensorConfig) ctConfig.getSensor() ).getUrl();
            proxy = ( (WfsSensorConfig) ctConfig.getSensor() ).getProxyStr();
        } else if ( ctConfig.getSensor() instanceof OafSensorConfig ) {

            /* fuellt Logindaten, wenn der Sensor von typ OAF ist */
            authUser = ( (OafSensorConfig) ctConfig.getSensor() ).getAuthUser();
            authPass = ( (OafSensorConfig) ctConfig.getSensor() ).getAuthPass();
            auth = ( (OafSensorConfig) ctConfig.getSensor() ).getAuthType();
            authUrl = ( (OafSensorConfig) ctConfig.getSensor() ).getAuthUrl();
            proxy = ( (OafSensorConfig) ctConfig.getSensor() ).getProxyStr();
        }

        // HttpWrapper vorbereiten
        httpwrp = new HttpWrapper( ctConfig.getTimeout(), proxy );

        if ( ( auth == null ) || ( auth.length() == 0 ) ) {
            auth = "http";
        }

        // TODO: Implement load && get of AuthType && AuthUrl
        if ( authUser != null && url != null && authUser.startsWith( "DeegreeWAS:" ) && authUser.length() > 11 ) {
            authUser = authUser.substring( 11 );
            auth = "deegree-was";

            if ( url.indexOf( "?" ) == -1 )
                authUrl = url;
            else
                authUrl = url.substring( 0, url.indexOf( "?" ) );
        }

        if ( ( authUser != null ) && ( authUser.length() > 0 ) && ( authPass != null ) && ( authPass.length() > 0 ) ) {
            // Optionale Authentifizierung setzen
            if ( auth.equalsIgnoreCase( "http" ) ) {
                httpwrp.setAuth( authUser, authPass );
            } else if ( auth.equalsIgnoreCase( "deegree-was" ) && ( authUrl != null ) && ( authUrl.length() > 0 ) ) {
                authType = AUTH_TYPE_DEEGRE_WAS_2_1;
            }
        }
    }

    protected void triggerPreRun()
                            throws Exception {
        if ( authType == AUTH_TYPE_DEEGRE_WAS_2_1 ) {
            sessionId = fetchSessionId();
            if ( sessionId == null )
                throw new Exception( fetchLastMessages.toString() );
        }
    }

    protected String triggerGetUrl( String getUrl ) {
        if ( ( sessionId != null ) && ( sessionId.length() > 0 ) && ( getUrl != null ) && ( getUrl.length() > 3 ) ) {
            String oldUrl = getUrl;
            String pre;

            if ( getUrl.indexOf( "?" ) == -1 ) {
                pre = "?";
            } else {
                pre = "&";
            }

            if ( oldUrl.endsWith( "/" ) ) {
                getUrl = oldUrl.substring( 0, oldUrl.length() - 1 ) + pre + "SESSIONID=" + sessionId;
            } else {
                getUrl = oldUrl + pre + "SESSIONID=" + sessionId;
            }
        }

        return getUrl;
    }

    protected String triggerPostVSPasText() {
        if ( ( sessionId != null ) && ( sessionId.length() > 0 ) ) {
            return " sessionID=\"" + sessionId + "\"";
        }
        return "";
    }

    private String fetchSessionId() {
        String res = null;

        // clear last message
        fetchLastMessages.setLength( 0 );

        StringBuffer sb = new StringBuffer( 500 );
        sb.append( endUrl( authUrl ) );
        sb.append( "SERVICE=WAS&VERSION=1.0.0&REQUEST=GetSession&" );
        sb.append( "AUTHMETHOD=urn:x-gdi-nrw:authnMethod:1.0:password&CREDENTIALS=" );
        sb.append( authUser ).append( ',' ).append( authPass );

        int ires = httpwrp.doGet( sb.toString() );
        byte[] response = httpwrp.getLastData();
        String tmpSesId;

        String exceptionTxt = null;

        if ( ( ires == HttpWrapper.RESULT_OK ) && ( response != null ) && ( response.length > 0 ) ) {
            // msg.appendText("GetCapabilities-Anfrage ausgef\u00fchrt, " + response.length + " byte empfangen");
            // Fehlermeldung setzen, sofern noch nicht vorhanden

            if ( response != null && response.length < 200 ) {
                tmpSesId = new String( response );
                if ( tmpSesId != null && tmpSesId.length() > 3 && tmpSesId.trim().toLowerCase().startsWith( "id" ) ) {
                    res = tmpSesId;
                }
            }

            if ( res == null ) {
                try {
                    exceptionTxt = parseOGCException( response );
                } catch ( Exception ex ) {
                    // ignore
                }
                if ( exceptionTxt == null ) {
                    try {
                        exceptionTxt = parseSecurityMessage( response );
                    } catch ( Exception ex ) {
                        // ignore
                    }
                }
            }

            // Free memory
            response = null;
        } else {
            res = null;
        }

        switch ( ires ) {
        case HttpWrapper.RESULT_OK:
            break;

        case HttpWrapper.RESULT_MAX_SIZE_EXCEEDED:
        case HttpWrapper.RESULT_WRONG_RESULT:
            fetchLastMessages.append( "Session konnte nicht angefordert werden (Ergebnis nicht als SessionID erkannt)" );
            break;
        case HttpWrapper.RESULT_CONNECTION_REFUSED:
        case HttpWrapper.RESULT_CONNECTION_TIMEOUT:
        case HttpWrapper.RESULT_NO_ROUTE:
        case HttpWrapper.RESULT_READ_TIMEOUT:
        case HttpWrapper.RESULT_UNKNOWN_HOST:
            fetchLastMessages.append( "Session konnte nicht angefordert werden (Netzwerk oder Verbindungsproblem)" );
            break;
        default:
            fetchLastMessages.append( "Session konnte nicht angefordert werden (Keine Details vorh.)" );
            break;
        }

        if ( exceptionTxt != null && exceptionTxt.length() > 0 )
            fetchLastMessages.append( "Details: " ).append( exceptionTxt );

        httpwrp.cleanup();

        return res;
    }

    protected String parseSecurityMessage( byte[] data ) {
        if ( ( data == null ) || ( data.length > 1000 ) ) {
            // INFO: iGeoSecurity erzeugt meisten nur weniger als 1000 zeichen, so das davon auszugehen ist das es keine
            // Security Meldung ist.
            return null;
        }

        String tmp = new String( data );

        // if (tmp == null) {
        // return null;
        // }

        String trim = tmp.trim();
        tmp = null;

        if ( !( trim == null || trim.length() <= 10 ) || trim.toLowerCase().startsWith( "message: " ) ) {
            return trim.substring( 9 );
        }

        return null;
    }

    /**
     * try parsing Result as OGC-Exception
     * 
     * @return Formatted Error form OGC-Exception or null Text not parsable
     */
    protected String parseOGCException( byte[] data ) {
        // String res = null;
        OGCExceptionDefSaxHandler defhandler = new OGCExceptionDefSaxHandler();

        // SAX
        try {
            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( defhandler );
            xr.setErrorHandler( defhandler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );
        } catch ( Exception e ) {
            return null;
        }

        return defhandler.getFormatedMessag();
    }

    /**
     * HttpWrapper Fehlercodes auf RC Fehlercodes ummappen
     *
     * @param res
     *            HttpWrapper.RESULT_*
     *
     * @return RESULT_CODE_*
     */
    protected int getResultCodeForHttpError( int httpres ) {
        int res;

        switch ( httpres ) {
        case HttpWrapper.RESULT_CONNECTION_REFUSED:
        case HttpWrapper.RESULT_UNKNOWN_HOST:
        case HttpWrapper.RESULT_CONNECTION_TIMEOUT:
        case HttpWrapper.RESULT_NO_ROUTE:
            res = Constants.RESULT_CODE_CONNECTION_TIMEOUT;

            break;

        case HttpWrapper.RESULT_READ_TIMEOUT:
            res = Constants.RESULT_CODE_DATA_TIMEOUT;

            break;

        case HttpWrapper.RESULT_WRONG_RESULT:
            res = Constants.RESULT_CODE_FAILED_WRONG_DATA;

            break;

        case HttpWrapper.RESULT_UNKNOW_ERROR:
        case HttpWrapper.RESULT_MAX_SIZE_EXCEEDED:
            res = Constants.RESULT_CODE_FAILED_UNKNOWN;

            break;

        default:
            res = Constants.RESULT_CODE_FAILED_UNKNOWN;

            break;
        }

        return res;
    }

    private String endUrl( String pUrl ) {
        if ( pUrl == null ) {
            pUrl = "";

            return pUrl;
        }

        while ( ( pUrl.length() > 0 ) && pUrl.endsWith( "/" ) ) {
            pUrl = pUrl.substring( 0, pUrl.length() - 1 );
        }

        if ( !pUrl.endsWith( "/" ) ) {
            if ( pUrl.indexOf( '?' ) < 0 ) {
                pUrl = pUrl + '?';
            } else if ( !pUrl.endsWith( "&" ) && !pUrl.endsWith( "?" ) ) {
                pUrl = pUrl + '&';
            }
        }

        return pUrl;
    }

}