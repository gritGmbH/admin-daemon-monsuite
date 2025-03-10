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
package de.grit.gdi.daemon.utils;

import static de.grit.gdi.daemon.utils.Utilities.closeSilent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;

/**
 * Http(Client) Wrapper Klasse
 * 
 * Aufschl\u00fcsselung der Fehler in GDI Resultcodes:
 * <ul>
 * <li>0 = RESULT_UNDEFIEND
 * <li>1 = RESULT_OK
 * <li>2 = RESULT_READ_TIMEOUT
 * <li>3 = RESULT_CONNECTION_REFUSED, RESULT_UNKNOWN_HOST, <br/>
 * RESULT_CONNECTION_TIMEOUT, RESULT_NO_ROUTE
 * <li>4 = RESULT_UNKNOW_ERROR, RESULT_MAX_SIZE_EXCEEDED
 * <li>5 = RESULT_WRONG_RESULT
 * </ul>
 */
public class HttpWrapper {
    private static final Logger LOG = LoggerFactory.getLogger( HttpWrapper.class );

    /** Maximale Response gr\u00f6\u00dfe in Bytes (16 * 1024 * 1024 Bytes = 16 MB) */
    private static final int MAX_RESPONSE_SIZE = 16 * 1024 * 1024;

    /** Anfangsbuffer f\u00fcr Anfragen (50 KB) */
    private static final int DEF_BUFFER_START_SIZE = 50 * 1024;

    /** Ergebnis: Unbekannt */
    public static final int RESULT_UNDEFIEND = 0;

    /** Ergebnis: OK */
    public static final int RESULT_OK = 1;

    /** Ergebnis: Connection Refused */
    public static final int RESULT_CONNECTION_REFUSED = 2;

    /** Ergebnis: Unknow Host */
    public static final int RESULT_UNKNOWN_HOST = 3;

    /** Ergebnis: Connection Timeout */
    public static final int RESULT_CONNECTION_TIMEOUT = 4;

    /** Ergebnis: No Route To Host */
    public static final int RESULT_NO_ROUTE = 5;

    /** Ergebnis: Read Timeout */
    public static final int RESULT_READ_TIMEOUT = 6;

    /** Ergebnis: Fahlsche antwort */
    public static final int RESULT_WRONG_RESULT = 7;

    /** Ergebnis: Anderer Fehler */
    public static final int RESULT_UNKNOW_ERROR = 8;

    /** Ergebnis: Zu gro\u00dfe datenmenge */
    public static final int RESULT_MAX_SIZE_EXCEEDED = 9;

    /** HttpClient Instanz */
    private CloseableHttpClient hclient;

    /** Timeout in ms */
    private int timeout;

    /** Resultcode der letzten Abfrage */
    private int lastResult = RESULT_UNDEFIEND;

    /** Charset der letzten Abfrage */
    private String lastCharset = "";

    /** Daten der letzten Abfrage */
    private byte[] lastData = null;

    /** Letzter HTTP Statuscode */
    private int lastHttpStatusCode = 0;

    /** Proxy Port (Standard: 8080) */
    private int proxyPort = 8080;

    /** Proxy Host */
    private String proxyHost = null;

    private HttpClientContext context = HttpClientContext.create();

    /**
     * Creates a new HttpWrapper object.
     */
    public HttpWrapper( String proxyString ) {
        timeout = 10;
        parseProxy( proxyString );
        init();
    }

    /**
     * Creates a new HttpWrapper object.
     */
    public HttpWrapper( int timeout, String proxyString ) {
        this.timeout = timeout;
        parseProxy( proxyString );
        init();
    }

    private void parseProxy( String proxyStr ) {
        if ( proxyStr != null && proxyStr.length() >= 2 ) {
            if ( proxyStr.lastIndexOf( ":" ) != -1 ) {
                int pos = proxyStr.lastIndexOf( ':' );
                proxyHost = proxyStr.substring( 0, pos );
                try {
                    proxyPort = Integer.valueOf( proxyStr.substring( pos + 1 ) );
                } catch ( Exception ex ) {
                    // TRICKY ignore wrong port (handeld in UI)
                }
            } else
                proxyHost = proxyStr;
        }
    }

    /**
     * Initialisierung des HTTPClient
     */
    private void init() {
        RequestConfig.Builder reqBuild = RequestConfig.custom();

        reqBuild.setSocketTimeout( timeout * 1000 );
        reqBuild.setConnectTimeout( timeout * 1000 );
        reqBuild.setConnectionRequestTimeout( timeout * 1000 );

        // disable redirects
        reqBuild.setRedirectsEnabled( false );
        reqBuild.setRelativeRedirectsAllowed( false );
        reqBuild.setCircularRedirectsAllowed( false );

        RequestConfig reqConfig = reqBuild.build();

        HttpClientBuilder cliBuild = HttpClients.custom();
        cliBuild.setDefaultRequestConfig( reqConfig );
        if ( proxyHost != null ) {
            cliBuild.setProxy( new HttpHost( proxyHost, proxyPort ) );
        }
        // no keep alive
        cliBuild.setConnectionReuseStrategy( NoConnectionReuseStrategy.INSTANCE );

        // only one retry
        cliBuild.setRetryHandler( new StandardHttpRequestRetryHandler( 1, false ) );

        // SSL allow all
        SSLContext sslContext;

        try {
            sslContext = SSLContexts.custom().loadTrustMaterial( new TrustStrategy() {

                @Override
                public boolean isTrusted( X509Certificate[] chain, String authType )
                                        throws CertificateException {
                    return true;
                }
            } ).build();
        } catch ( Exception ex ) {
            LOG.warn( "Could not create SSLContext for HttpWrapper that allows any certificate, fallback to default" );
            sslContext = SSLContexts.createSystemDefault();
        }

        // disable hostname cert checks
        HostnameVerifier verify = NoopHostnameVerifier.INSTANCE;

        // String hostname = url != null && url.getHost() != null ? url.getHost() : "default";
        // String protOverride = Conf.get( "httpclient.ssl.protocol." + hostname, null );
        //
        // String[] protocols = SSL_PROTOCOLS;
        // if ( protOverride != null ) {
        // LOG.logInfo( "Using ssl protocol override for host: " + hostname + " with enabled protocols: "
        // + protOverride );
        // protocols = split( protOverride );
        // }

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory( sslContext, //
                                                                           null, // SSL_PROTOCOLS
                                                                           null, // SSL_CIPHER_SUITES
                                                                           verify );
        cliBuild.setSSLSocketFactory( sslsf );

        hclient = cliBuild.build();
    }

    /**
     * L\u00f6schen der zuletzt abgefragten Daten
     */
    public void cleanup() {
        this.lastResult = RESULT_UNDEFIEND;
        this.lastCharset = "";
        this.lastData = null;
    }

    /**
     * Authorisierungsparameter setzen (f\u00fcr HTTP-Auth)
     * 
     * @param user
     *            Benutzername
     * @param pass
     *            Benutzerpasswort
     */
    public void setAuth( String user, String pass ) {
        UsernamePasswordCredentials cred = null;
        cred = new UsernamePasswordCredentials( user, pass );

        BasicCredentialsProvider prov = new BasicCredentialsProvider();
        prov.setCredentials( AuthScope.ANY, cred );
        context.setCredentialsProvider( prov );
    }

    /**
     * Authorisierungsparameter entfernen (f\u00fcr HTTP-Auth)
     */
    public void removeAuth() {
        context.removeAttribute( HttpClientContext.CREDS_PROVIDER );
    }

    private int doRequest( HttpRequestBase method ) {
        int result;
        CloseableHttpResponse resp = null;
        InputStream is = null;
        try {
            LOG.info( "Executing HTTP Request " + method.getRequestLine() );
            // Execute the method.
            resp = hclient.execute( method, context );
            int statusCode = resp.getStatusLine().getStatusCode();
            lastHttpStatusCode = statusCode;
            HttpEntity entity = resp.getEntity();
            if ( statusCode != HttpStatus.SC_OK ) {
                LOG.debug( "Execude failed: {} : {} (Entity available: {})", statusCode, resp.getStatusLine(),
                           entity != null );
                result = RESULT_WRONG_RESULT;
            } else {
                result = RESULT_OK;
                int rxbytes = 0;

                ByteArrayOutputStream outstream;
                outstream = new ByteArrayOutputStream( DEF_BUFFER_START_SIZE );

                is = entity.getContent();
                if ( is != null ) {
                    byte[] buffer = new byte[4096]; // 4k
                    int len = 0;

                    while ( ( ( len = is.read( buffer ) ) > 0 ) && ( rxbytes <= MAX_RESPONSE_SIZE ) ) {
                        rxbytes += len;
                        outstream.write( buffer, 0, len );
                    }
                }

                if ( rxbytes > MAX_RESPONSE_SIZE ) {
                    result = RESULT_MAX_SIZE_EXCEEDED;
                } else if ( rxbytes > 0 ) {
                    // Alle Daten erfolgreich empfangen
                    LOG.debug( "OK {} recieved", rxbytes );
                    this.lastData = outstream.toByteArray();
                    ContentType ct = null;
                    try {
                        ContentType.getOrDefault( entity );
                    } catch ( Exception ex ) {
                        LOG.debug( "Could not get content-type from response: {}", ex.getMessage() );
                        LOG.trace( "Exception", ex );
                    }
                    this.lastCharset = ( ct != null && ct.getCharset() != null ) ? ct.getCharset().name() : "";
                }
            }
        } catch ( ConnectException e ) {
            // java.net.ConnectException
            // Fatal transport error: Connection refused: connect
            result = RESULT_CONNECTION_REFUSED;
            LOG.debug( e.getMessage() );
        } catch ( UnknownHostException e ) {
            // java.net.UnknownHostException
            // Fatal transport error
            result = RESULT_UNKNOWN_HOST;
            LOG.debug( e.getMessage() );
        } catch ( NoRouteToHostException e ) {
            // java.net.NoRouteToHostException Fatal transport error: No route to host: connect
            result = RESULT_NO_ROUTE;
            LOG.debug( e.getMessage() );
            // } catch ( HttpConnection.ConnectionTimeoutException e ) {
            // org.apache.commons.httpclient.HttpConnection$ConnectionTimeoutException
            // -> Fatal transport error: null
            // result = RESULT_CONNECTION_TIMEOUT;
            // LOG.debug( e.getMessage() );
        } catch ( SocketTimeoutException e ) {
            // java.net.SocketTimeoutException IO-Fehler: Read timed out (beim lesen)
            result = RESULT_READ_TIMEOUT;
            LOG.debug( e.getMessage() );
        } catch ( IOException e ) {
            LOG.warn( "Unknown Exception: " + e.getMessage() );
            result = RESULT_UNKNOW_ERROR;
        } finally {
            closeSilent( is );
            closeSilent( resp );
        }

        return result;
    }

    /**
     * HTTP-Get Abfrage durchf\u00fchren
     * 
     * @param uri
     *            Adresse die abgefragt werden soll
     */
    public int doGet( String uri ) {
        cleanup();

        int result = RESULT_UNDEFIEND;
        this.lastHttpStatusCode = 0;

        HttpGet method = new HttpGet( uri );

        // HTTP Redirects nicht folgen
        // see #init()

        // Bei HTTP Fehler 1x erneut versuchen
        // see #init()

        result = doRequest( method );

        // Daten hinterlegen
        this.lastResult = result;

        return result;
    }
    
    public int doGet(HttpGet method) {
        cleanup();
        int result = doRequest( method );
        this.lastResult = result;

        return result;
        
    }

    public int doPost( String uri, String xml ) {
        cleanup();

        int result = RESULT_UNDEFIEND;
        this.lastHttpStatusCode = 0;

        HttpPost method = new HttpPost( uri );
        method.setEntity( new StringEntity( xml, ContentType.create( "text/xml" ) ) );

        // HTTP Redirects nicht folgen
        // see #init()

        // Bei HTTP Fehler 1x erneut versuchen
        // see #init()

        result = doRequest( method );

        // Daten hinterlegen
        this.lastResult = result;

        // Release the connection.
        method.releaseConnection();

        return result;
    }

    /**
     * Charset der letzten Abfrage zur\u00fcckliefern
     * 
     * @return Charset
     */
    public String getLastCharset() {
        return this.lastCharset;
    }

    /**
     * Ergebniscode der letzten Abfrage zur\u00fcckliefern
     * 
     * @return Resultcode
     */
    public int getLastResult() {
        return this.lastResult;
    }

    /**
     * Daten der letzten Abfrage zur\u00fcckliefern
     * 
     * @return Daten (Body)
     */
    public byte[] getLastData() {
        return this.lastData;
    }

    /**
     * Den letzten HTTP Status Code zur\u00fcckgeben
     * 
     * @return HTTP_Status Code
     */
    public int getLastHttpStatusCode() {
        return lastHttpStatusCode;
    }

    public static int mapResultToConstantResult( int httpWrapperCode ) {
        int res;

        switch ( httpWrapperCode ) {
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
}