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
package de.grit.gdi.daemon.utils.sax;

import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.grit.gdi.daemon.data.sensor.BoundingBox;
import de.grit.gdi.daemon.data.sensor.LatLonBoundingBox;
import de.grit.gdi.daemon.data.sensor.WMSCapabilities;
import de.grit.gdi.daemon.data.sensor.WmsLayer;

public class WMSCapabilitiesDefSaxHandler extends ExtDefaultHandler {
    private WMSCapabilities cap = new WMSCapabilities();

    private Hashtable<String, WmsLayer> ht = new Hashtable<String, WmsLayer>();

    /**
     * Creates a new CapabilitiesDefSaxHandler object.
     */
    public WMSCapabilitiesDefSaxHandler() {
    }

    /**
     * Capabilities zur\u00fcckliefern
     */
    public WMSCapabilities getCapabilites() {
        return cap;
    }

    public void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException {
        String full = getPath();

        if ( full.toLowerCase().endsWith( "/layer" ) ) {
            /*
             * Layer begin - neues Layerobjekt erzeugen - \u00fcbergeordneten Layer zuweisen
             */
            WmsLayer wmsl = new WmsLayer();

            String ref = full.substring( 0, full.length() - 6 );

            if ( ref.toLowerCase().endsWith( "/layer" ) ) {
                WmsLayer rwmsl = (WmsLayer) ht.get( ref );
                wmsl.setParent( rwmsl );
            }

            ht.put( full, wmsl );
        }
    }

    public void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr, StringBuffer value )
                            throws SAXException {
        String full = getPath() + "/" + localName;

        if ( full.toLowerCase().endsWith( "/capability/request/getmap/format" ) ) {
            // Formatinformation (Global)
            cap.getFormat().add( value.toString().trim() );
        } else if ( full.toLowerCase().endsWith( "/capability/request/getmap/dcptype/http/get/onlineresource" ) ) {
            // Adressinformation (Global)
            String url = attr.getValueByQName( "xlink:href" ).trim();
            // System.err.println("getMap href: " + url + " fp=" + full);
            cap.setUrl( url );
        } else if ( full.toLowerCase().endsWith( "/layer/name" ) ) {
            // Layername
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 5 ) );
            wmsl.setName( value.toString().trim() );
        } else if ( full.toLowerCase().endsWith( "/layer/title" ) ) {
            // Layerbezeichnung (Titel)
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 6 ) );
            wmsl.setTitle( value.toString().trim() );
        } else if ( full.toLowerCase().endsWith( "/layer/latlonboundingbox" ) ) {
            // Bounding Box nach Lat/Lon
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 18 ) );
            LatLonBoundingBox llbb = new LatLonBoundingBox();
            llbb.setMinx( Float.parseFloat( attr.getValueByLName( "minx" ) ) );
            llbb.setMaxx( Float.parseFloat( attr.getValueByLName( "maxx" ) ) );
            llbb.setMiny( Float.parseFloat( attr.getValueByLName( "miny" ) ) );
            llbb.setMaxy( Float.parseFloat( attr.getValueByLName( "maxy" ) ) );
            wmsl.setLatLonBoundingBox( llbb );
        } else if ( full.toLowerCase().endsWith( "/layer/boundingbox" ) ) {
            /*
             * Boundingbox mit Systemangabe
             */
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 12 ) );

            String srs;

            if ( wmsl.getBoundingBox() == null ) {
                BoundingBox bb = new BoundingBox();
                if ( attr.getValueByLName( "CRS" ) != null ) {
                    srs = attr.getValueByLName( "CRS" );
                } else {
                    srs = attr.getValueByLName( "SRS" );
                }
                bb.setSrs( srs.toUpperCase() );
                bb.setMinx( Float.parseFloat( attr.getValueByLName( "minx" ) ) );
                bb.setMaxx( Float.parseFloat( attr.getValueByLName( "maxx" ) ) );
                bb.setMiny( Float.parseFloat( attr.getValueByLName( "miny" ) ) );
                bb.setMaxy( Float.parseFloat( attr.getValueByLName( "maxy" ) ) );
                wmsl.addBoundingBox( bb );
            }
        } else if ( full.toLowerCase().endsWith( "/layer/style/name" ) ) {
            /*
             * Stylename - Nur der erste wird Gespeichert
             */
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 11 ) );
            String tmpStyleName = value.toString().trim();

            if ( ( tmpStyleName.length() > 0 ) && ( wmsl.getStyleName() == null ) ) {
                wmsl.setStyleName( tmpStyleName );
            }
        } else if ( full.toLowerCase().endsWith( "/layer/scalehint" ) ) {
            /*
             * Min- / Max- Skallierung
             */
            WmsLayer wmsl = (WmsLayer) ht.get( full.substring( 0, full.length() - 10 ) );
            wmsl.setScaleHintMin( Float.parseFloat( attr.getValueByLName( "min" ) ) );
            wmsl.setScaleHintMax( Float.parseFloat( attr.getValueByLName( "max" ) ) );
        } else if ( full.toLowerCase().endsWith( "/layer" ) ) {
            /*
             * Ende eines Layerelements - Element wird in Capabilities aufgenommen
             */
            WmsLayer wmsl = (WmsLayer) ht.remove( full );

            // System.err.println("==> " + wmsl.toString());
            if ( wmsl.getName() != null ) {
                cap.getLayer().put( wmsl.getName(), wmsl );
            }
        }
    }
}