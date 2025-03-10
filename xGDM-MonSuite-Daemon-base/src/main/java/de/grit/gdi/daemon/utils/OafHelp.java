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

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OafHelp {

    private static final Logger LOG = LoggerFactory.getLogger( "monitor.sensor.oafsensor" );

    /**
     * funktion um oafURL f\u00fcr zu erstellen
     * 
     * @return HttpGet: die erstellte url
     */
    private HttpGet formOafUrl( String url, String responseTyp ) {

        if ( url.indexOf( "json" ) < 0 ) {
            // loescht den schraeger, wenn die url dieses enthaelt
            url = ( url.endsWith( "/" ) ) ? url.substring( 0, url.length() - 1 ) : url;

            url = url + "?f=json";
        }

        HttpGet method = new HttpGet();
        method.setURI( URI.create( url ) );
        method.setHeader( "accept", responseTyp ); // Typ von Response "application/json" oder "application/geo+json"

        return method;
    }

    private JSONObject response( HttpGet url, HttpWrapper httpwrp ) {

        // nimmt die Httpresponse an und speichert in einer integer
        int res = httpwrp.doGet( url );

        // nimmt die byte Daten aus der Http response an und speichert in einem Array von typ byte
        byte[] response = httpwrp.getLastData();

        JSONObject value = new JSONObject();

        if ( res != HttpWrapper.RESULT_OK ) {
            value.put( "Response", res );
            value.put( "Data", response );
        } else {
            value.put( "Response", res );
            value.put( "Data", new String( response ) );
        }

        return value;
    }

    /** Gibt ein JSONArray, die Collections enthaelt */
    public JSONArray collectionsArray( String url, HttpWrapper httpwrp ) {

        JSONArray service = null;

        HttpGet method = formOafUrl( url, "application/json" );

        // Landing Page
        JSONObject LandingPage = response( method, httpwrp );

        if ( LandingPage.getInt( "Response" ) == HttpWrapper.RESULT_OK ) {

            // nimmt jsonArray der Links aus der Landing page
            service = new JSONObject( LandingPage.getString( "Data" ) ).getJSONArray( "links" ); // optString(key)

            // filterung zur erhalt der Links zur collection
            List<JSONObject> listUrl = toStream( service ).filter( link -> ( ( link.get( "rel" ).equals( "data" )
                                                                               && !link.has( "type" ) )
                                                                             || link.has( "type" )
                                                                                && link.get( "rel" ).equals( "data" )
                                                                                && link.get( "type" ).equals( "application/json" ) ) )

                                                          .collect( Collectors.toList() );

            String urlToCollection = (String) listUrl.get( 0 ).get( "href" );

            // JsonArray von gefragte Collection
            service = new JSONObject( new String( (String) response( formOafUrl( urlToCollection, "application/json" ),
                                                                     httpwrp ).get( "Data" ) ) ).getJSONArray( "collections" );

        }

        return service;
    }

    /** Gibt eine Collection und sein Inhalt zur\u00fcck */
    public JSONObject jsonCollection( JSONObject featurecollection, HttpWrapper httpwrp ) {

        String url = collectionUrl( featurecollection );

        return new JSONObject( new String( (String) response( formOafUrl( url, "application/geo+json" ),
                                                              httpwrp ).get( "Data" ) ) );
    }

    /** Gibt ein Feature und sein Inhalt zur\u00fcck */
    public JSONObject jsonFeature( JSONObject featurecollection, HttpWrapper httpwrp, String feature ) {

        JSONObject ft;
        String featureUrl = featureUrl( collectionUrl( featurecollection ), feature );
        ft = response( formOafUrl( featureUrl, "application/geo+json" ), httpwrp );

        if ( ft.getInt( "Response" ) != HttpWrapper.RESULT_OK ) {
            ft = null;
        } else {
            ft = new JSONObject( new String( (String) response( formOafUrl( featureUrl, "application/geo+json" ),
                                                                httpwrp ).get( "Data" ) ) );
        }

        return ft;
    }

    /** Gibt die url zum Feature einer Collection */
    private String collectionUrl( JSONObject featurecollection ) {

        String url = (String) toStream( featurecollection.getJSONArray( "links" ) ).filter( l -> l.get( "rel" ).equals( "items" )
                                                                                                 && l.get( "type" ).equals( "application/geo+json" ) ).collect( Collectors.toList() ).get( 0 ).get( "href" );
        return url;
    }

    /** bildet die url zur eine Feature in einer Collection */
    private String featureUrl( String collectionUrl, String feature ) {
        String fture = "";
        if ( collectionUrl.indexOf( "json" ) > 0 ) {
            fture = collectionUrl.substring( 0, collectionUrl.indexOf( "?" ) ) + "/" + feature
                    + collectionUrl.substring( collectionUrl.indexOf( "?" ) );
        } else {
            fture = collectionUrl + "/" + feature;
        }
        return fture;
    }

    /** Wandeln ein JSonArray in einer Stream */
    public Stream<JSONObject> toStream( JSONArray arr ) {
        return StreamSupport.stream( arr.spliterator(), false ).map( JSONObject.class::cast );
    }

}