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

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.MonitorConfig;
import de.grit.vaadin.common.EbeanUtilsCommon;

public class Utilities extends EbeanUtilsCommon {

    private static final Logger LOG = LoggerFactory.getLogger( Utilities.class );

    public static String POM_BASE_GROUPID = "de.grit";

    public static String POM_BASE_ARTIFACTID = "xgdm-monsuite-daemon-base";

    private Utilities() {
    }

    /**
     * Primaere IP-Adresse des Rechners als String ermitteln Format: 000.000.000.000
     *
     * @return IP-Adresse im obigen Format
     */
    public static String getIpAsString() {
        String res = "";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            res = addr.getHostAddress();
        } catch ( UnknownHostException e ) {
        }

        return res;
    }

    /**
     * Sting im Zeitformat Stunden: Minuten in Minuten (int) umwandeln
     *
     * @param str_time
     *            Zeit im Format Stunden:Minuten
     *
     * @return Zeit in Minuten
     */
    public static int strTimeToMin( String str_time ) {
        int res = 0;

        if ( str_time == null || str_time.equals( "" ) ) {
            return res;
        }

        int pos = str_time.indexOf( ":" );

        if ( pos >= 0 ) {
            int std = Integer.parseInt( str_time.substring( 0, pos ) );
            int min = Integer.parseInt( str_time.substring( pos + 1 ) );
            res = ( std * 60 ) + min;
        } else {
            res = Integer.parseInt( str_time );
        }

        return res;
    }

    /**
     * Liste mit Monitoren in HashMap (Key = ID) umwandeln
     *
     * @param monitorList
     *            Liste von MonitorConfig Objekten
     *
     * @return HashMap von MonitorConfig Objekten
     */
    @Deprecated
    public static Map<String, MonitorConfig> monitorListToHashMap( List<MonitorConfig> monitorList ) {
        if ( monitorList == null ) {
            return new HashMap<String, MonitorConfig>( 1 );
        }

        HashMap<String, MonitorConfig> moniutorHashMap = new HashMap<String, MonitorConfig>( monitorList.size() + 2 );

        for ( int i = 0, ilen = monitorList.size(); i < ilen; i++ ) {
            MonitorConfig tmp = (MonitorConfig) monitorList.get( i );
            moniutorHashMap.put( "" + tmp.getId(), tmp );
        }

        return moniutorHashMap;
    }

    /**
     * Liste mit CechekcTaskConfig in HashMap (Key = ID) umwandeln
     *
     * @param monitorList
     *            Liste von CechekcTaskConfig Objekten
     *
     * @return HashMap von CechekcTaskConfig Objekten
     */
    @Deprecated
    public static HashMap<String, CheckTaskConfig> checkTaskListToHashMap( List<CheckTaskConfig> checkTaskList ) {
        HashMap<String, CheckTaskConfig> checkTaskHashMap = new HashMap<String, CheckTaskConfig>( checkTaskList.size()
                                                                                                  + 2 );

        for ( int i = 0, ilen = checkTaskList.size(); i < ilen; i++ ) {
            CheckTaskConfig tmp = checkTaskList.get( i );
            checkTaskHashMap.put( "" + tmp.getTaskId(), tmp );
        }

        return checkTaskHashMap;
    }

    public static float[] strToFloatArray( String source, String separatorChars )
                            throws NumberFormatException {
        StringTokenizer st = new StringTokenizer( source, separatorChars );

        ArrayList<String> list = new ArrayList<String>( st.countTokens() );

        while ( st.hasMoreTokens() ) {
            String t = st.nextToken().replace( ' ', '+' );

            if ( ( t != null ) && ( t.length() > 0 ) ) {
                list.add( t.trim().replace( ',', '.' ) );
            }
        }

        float[] array = new float[list.size()];

        for ( int i = 0; i < list.size(); i++ ) {
            array[i] = Float.parseFloat( list.get( i ) );
        }

        return array;
    }

    public static double[] strToDoubleArray( String source, String separatorChars )
                            throws NumberFormatException {
        StringTokenizer st = new StringTokenizer( source, separatorChars );

        ArrayList<String> list = new ArrayList<String>( st.countTokens() );

        while ( st.hasMoreTokens() ) {
            String t = st.nextToken().replace( ' ', '+' );

            if ( ( t != null ) && ( t.length() > 0 ) ) {
                list.add( t.trim().replace( ',', '.' ) );
            }
        }

        double[] array = new double[list.size()];

        for ( int i = 0; i < list.size(); i++ ) {
            array[i] = Double.parseDouble( list.get( i ) );
        }

        return array;
    }

    public static String getImageFormatShort( String longFmt ) {
        if ( longFmt == null || longFmt.length() == 0 )
            return null;
        else if ( longFmt.contains( "image/png" ) )
            return "png";
        else if ( longFmt.contains( "image/gif" ) )
            return "gif";
        else if ( longFmt.contains( "image/jpg" ) )
            return "jpg";
        else if ( longFmt.contains( "image/jpeg" ) )
            return "jpg";
        else if ( longFmt.indexOf( '/' ) != -1 ) {
            String tmp = longFmt.substring( longFmt.indexOf( '/' ) + 1 );
            if ( tmp != null && tmp.length() > 1 )
                return tmp;
        }

        // else
        return null;
    }

    public static void closeSilent( Object c ) {
        try {
            if ( c instanceof Closeable )
                ( (Closeable) c ).close();
            else if ( c instanceof Connection )
                ( (Connection) c ).close();
        } catch ( Exception ex ) {
            // ignore
        }
    }

    public static void closeSilentN( Object c ) {
        closeSilent( c );
        c = null;
    }

    public static int getPropAsInt( String key, int defaultValue ) {
        try {
            String val = getProp( key, null );
            if ( val != null )
                return Integer.parseInt( val );
        } catch ( NumberFormatException nfe ) {
            LOG.warn( "Can't read property {} as integer", key );
        }
        return defaultValue;
    }

    public static String urlEnocde( String msg ) {
        String encodeCode = getProp( "url.encode", null );

        if ( msg == null || msg.length() == 0 )
            return "";
        else {
            try {
                if ( encodeCode != null ) {
                    msg = URLEncoder.encode( msg, encodeCode );
                } else {
                    LOG.debug( "url.encode not configured using UTF-8" );
                    msg = URLEncoder.encode( msg, "UTF-8" );
                }

                return msg;
            } catch ( UnsupportedEncodingException e ) {
                LOG.warn( "failed encoding: ", e );
                return "";
            }
        }
    }

    public static Properties readPropertiesWithPrefix( String key )
                            throws IOException {
        final int extra = 50;
        final String NL = System.getProperty( "line.separator", "\n" );
        final StringBuilder sb = new StringBuilder();

        for ( int i = 0, max = extra; i < max; i++ ) {
            String line = getProp( key + i, null );
            if ( line != null ) {
                sb.append( line ).append( NL );
                max = i + extra; // read max 25 extra
            }
        }
        Properties prop = new Properties();
        prop.load( new StringReader( sb.toString() ) );
        return prop;
    }

    public static boolean isNullOrEmpty( String val ) {
        return ( val == null || val.isEmpty() );
    }
}