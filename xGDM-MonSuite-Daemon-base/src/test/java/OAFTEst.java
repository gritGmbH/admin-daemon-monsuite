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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.JOptionPane;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class OAFTEst {
    public static void main( String[] args ) {

        String url = "http://***REMOVED***/rest/services/";
        System.out.println( url.length() );
        System.out.println( url.substring( 0, url.length() - 1 ) );
    }

    /** \u00e4berpr\u00e4ft, ob eine collection in der Api vorhanden ist, und gibt ein boolean zur\u00e4ck */
    public static boolean pruefeCollectionIds( String service, String collectionid ) {

        // Aufruf der Collection der Service

        JSONArray arre = jsonRequest( "http://***REMOVED***/***REMOVED***/services/" + service
                                      + "/collections?f=json" ).getJSONArray( "collections" );

        // Sucht collectionid im Stream und gibt booelan zur\u00e4ckt

        boolean b = toStream( arre ).anyMatch( c -> collectionid.equals( c.get( "title" ) ) );

        // Gibt eine Nachricht, falls die Collection kein Feature enthaelt

        if ( b && jsonRequest( "http://***REMOVED***/***REMOVED***/services/" + service + "/collections/" + collectionid
                               + "/items?f=json" ).getJSONArray( "features" ).length() == 0 ) {
            String message = collectionid + " ist vorhanden aber besitzt keine features." + collectionid
                             + " ist nicht andwendbar";
            JOptionPane.showMessageDialog( null, message );
        }

        return b;
    }

    /** wandelt ein JsonArray in einem Stream */
    static Stream<JSONObject> toStream( JSONArray arr ) {
        return StreamSupport.stream( arr.spliterator(), false ).map( JSONObject.class::cast );
    }

    /**
     * \u00e4berpr\u00e4ft, ob eine feature in der Api vorhanden ist, gibt ein boolean zur\u00e4ck
     */
    public static boolean featureIds( String service, String featureid ) {

        // Aufruf der Collection der Service

        JSONArray arre = jsonRequest( "http://***REMOVED***/***REMOVED***/services" + "/" + service
                                      + "/collections?f=json" ).getJSONArray( "collections" );

        // Schleife zur Pruefung "featureid" in jeder collection

        for ( int i = 0; i < arre.length(); i++ ) {
            JSONObject o = (JSONObject) arre.get( i );
            String title = (String) o.get( "title" );
            String url = "http://***REMOVED***/***REMOVED***/services/" + service + "/collections/" + title + "/items/"
                         + featureid + "?f=json";

            System.out.println( url );

            JSONObject collection = jsonRequest( url );

            // gibt True zurueck, falls "featureid" in der jeweiligen collection ist

            if ( collection != null && featureid.equals( collection.get( "id" ) ) ) {
                return true;
            }
        }

        // gibt False zurueck, falls "featureid" nicht in der service ist
        return false;
    }

    /**
     * ruft daten aus einer Url auf, und gibt ein Jsonobject oder null zur\u00fcck
     */

    public static JSONObject jsonRequest( String urlQueryString ) {

        String json = null;

        // Erstellung eine Url und auslesen diese per Http mit dem Mode GET

        try {
            URL url = new URL( urlQueryString );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod( "GET" );
            connection.connect();

            // Liest die HTTPresponse mit einem InputStream und wandelt diese in einer String, falls die Response 200
            // ist

            if ( connection.getResponseCode() == 200 ) {
                InputStream inStream = connection.getInputStream();
                json = IOUtils.toString( inStream, StandardCharsets.UTF_8 );
                inStream.close();
            }

            // Benachicht den Anwender, falls die Url falsch geschrieben wurde

            else if ( connection.getResponseCode() == 400 ) {
                String message = "\u00dcberpr\u00fcfen Sie Ihre URL";
                JOptionPane.showMessageDialog( null, message );
            }

            // Benachicht den Anwender, falls der Server nicht verfuegbar ist

            else if ( connection.getResponseCode() == 500 ) {
                String message = "Server momentan nicht verf\u00fcgbar";
                JOptionPane.showMessageDialog( null, message );
            }

        } catch ( IOException ex ) {
            ex.printStackTrace();
        }

        // return null, falls json null ist, sonst wird ein JSONObject erstellt with json

        return ( json != null ) ? new JSONObject( json ) : null;
    }

    public static boolean featureId1s( String service, String collectionsid, String featureid ) {

        String url = "http://***REMOVED***/***REMOVED***/services" + "/" + service + "/collections/" + collectionsid
                     + "/items/" + featureid + "?f=json";

        JSONObject feature = jsonRequest( url );

        if ( feature != null && featureid.equals( feature.get( "id" ) ) ) {
            return true;
        }

        return false;
    }

}