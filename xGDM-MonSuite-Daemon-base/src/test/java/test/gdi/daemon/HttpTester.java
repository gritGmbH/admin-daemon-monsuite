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
package test.gdi.daemon;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import de.grit.gdi.daemon.data.sensor.BoundingBox;
import de.grit.gdi.daemon.data.sensor.WMSCapabilities;
import de.grit.gdi.daemon.data.sensor.WmsLayer;
import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.ImageAnalyser;
import de.grit.gdi.daemon.utils.ImageAnalyserResult;
import de.grit.gdi.daemon.utils.sax.WMSCapabilitiesDefSaxHandler;

/**
 * DOCUMENT ME
 */
public class HttpTester {
    /** DOCUMENT_ME */
    private Random rand = new Random();

    /**
     * Creates a new HttpTester object.
     */
    public HttpTester() {
        HttpWrapper wrp = new HttpWrapper( 5, null );

        wrp.setAuth( "auskunft", "***REMOVED***" );

        wrp.doGet( "http://***REMOVED***/GetCapabilities.pl?REQUEST=capabilities&"
                   + "VERSION=1.1.0&WMTVER=1.1.0&SERVICE=WMS&datasource=test" );

        System.out.println( "Resultcode: " + wrp.getLastResult() );
        System.out.println( "Charset: " + wrp.getLastCharset() );

        byte[] data = wrp.getLastData();

        if ( data != null ) {
            System.out.println( "L\u00e4nge: " + data.length );
        }

        System.out.println( "Fertig" );

        // Handler vorbereiten
        // SAX2 - Xerces
        DefaultHandler defhandler = new WMSCapabilitiesDefSaxHandler();

        WMSCapabilities cap = ( (WMSCapabilitiesDefSaxHandler) defhandler ).getCapabilites();

        // SAX
        try {
            long start = System.currentTimeMillis();

            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( defhandler );
            xr.setErrorHandler( defhandler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );

            long end = System.currentTimeMillis();
            System.err.println( "Duration = " + ( end - start ) + " ms" );

            /*
             * for (Enumeration enkey = cap.getLayer().keys(); enkey.hasMoreElements() ; ) { Object key =
             * enkey.nextElement(); System.out.println("Found Layer: " + key); } for (int i = 0, j = avail_lay.length; i
             * < j; i++) { if (cap.getLayer().containsKey(avail_lay[i])) { // OK System.out.println("OK: " +
             * avail_lay[i]); } else { // Failed System.err.println("Failed: " + avail_lay[i]); } } //
             */

        } catch ( Exception e ) {
            e.printStackTrace();
        }

        // Wrapper Cleanup
        wrp.cleanup();

        String tLayerName;
        tLayerName = "C"; // geht

        // tLayerName = "001"; // geht auch
        WmsLayer tLayer = (WmsLayer) cap.getLayer().get( tLayerName );

        // */

        /*
         * Gr\u00f6\u00dfenberechnung
         * 
         * Basis 100 px x 100 px 15 m x 15 m
         */
        int sizepx = 150; // 100x100 px

        try {
            String imgurl = buildGetMapUrl( tLayer, cap, sizepx );
            System.err.println( imgurl );
            wrp.doGet( imgurl );

            byte[] idata = wrp.getLastData();

            if ( idata != null ) {
                System.out.println( "L\u00e4nge: " + idata.length );

                InputStream is = new ByteArrayInputStream( idata );

                ImageAnalyserResult ires = ImageAnalyser.analyse( is );

                System.err.println( "Bild" );
                System.err.println( "ergebnis  = " + ires.isResult() );
                System.err.println( "warnungen = " + ires.getMessage() );
                System.err.println( "type      = " + ires.getImgtype() );
                System.err.println( "height    = " + ires.getHeight() );
                System.err.println( "width     = " + ires.getWidth() );

                is.close();

                /*
                 * Debug to File
                 */
                try {
                    StringBuffer filename = new StringBuffer();
                    filename.append( "C:\\imgtest_" );
                    filename.append( System.currentTimeMillis() );
                    filename.append( "." );
                    filename.append( ires.getImgtype() );
                    OutputStream out = new FileOutputStream( filename.toString() );
                    out.write( idata );
                    out.close();
                } catch ( IOException e ) {
                }

                // System.out.println("Daten");
                // String dat = HttpConstants.getContentString(data, wrp.getLastCharset());
                // System.out.println(dat);
            }
        } catch ( Exception ex1 ) {
            System.err.println( "Fehler: " + ex1.getMessage() );
            ex1.printStackTrace();
        }
    }

    /**
     * Ermittelt eine Zuf\u00e4llige Posiotion f\u00fcr den Ausschnitt in der Box
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
    public double[] getRandomPosition( double boxy, double boxx, double party, double partx ) {
        double randy = 0f;
        double randx = 0f;

        double qbby = boxy / 4;
        double qbbx = boxx / 4;

        // Bild gr\u00f6\u00dfer als 1/4 => keine verschiebung
        if ( ( party > qbby ) || ( partx > qbbx ) ) {
            return new double[] { randy, randx };
        }

        // Abweichung Y/X vom Mittelpunkt aus max. 0.99 * 1/4 Y/X
        double dy = rand.nextFloat() * qbby * ( rand.nextBoolean() ? 1 : ( -1 ) );
        double dx = rand.nextFloat() * qbbx * ( rand.nextBoolean() ? 1 : ( -1 ) );

        randy = ( 0 + ( boxy / 2 ) ) - ( party / 2 ) + dy;
        randx = ( 0 + ( boxx / 2 ) ) - ( partx / 2 ) + dx;

        return new double[] { randy, randx };
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
     *             DOCUMENT_ME
     */
    private String buildGetMapUrl( WmsLayer layer, WMSCapabilities cap, int resultsize )
                            throws IllegalArgumentException {
        if ( layer == null ) {
            throw new IllegalArgumentException( "Layerdaten nicht gefunden" );
        }

        if ( cap == null ) {
            throw new IllegalArgumentException( "Capabilities unvollst\u00e4ndig" );
        }

        BoundingBox bbox = layer.getLayerBBox();

        if ( bbox == null ) {
            throw new IllegalArgumentException( "Keine BoundingBox in Layerinformationen gefunden" );
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

        double[] scaleHintMinMax = layer.getLayerScaleHintMinMax();

        if ( scaleHintMinMax.length == 2 ) {
            if ( ( scaleHint < scaleHintMinMax[0] ) && ( scaleHint < scaleHintMinMax[1] ) ) {
                System.err.println( "Skallierung an Min => " + scaleHintMinMax[0] );
                scaleHint = scaleHintMinMax[0];
            } else if ( ( scaleHint > scaleHintMinMax[1] ) && ( scaleHint > scaleHintMinMax[0] ) ) {
                System.err.println( "Skallierung an Max => " + scaleHintMinMax[1] );
                scaleHint = scaleHintMinMax[1];
            }
        } else {
            System.err.println( "Keine Skallierung gefunden" );
        }

        sizerl = scaleHint * resultsize;
        System.err.println( "Ausdehnung=" + sizerl + " Pixel=" + resultsize );

        // *** Zusammenbauen der URL ***
        StringBuffer sb = new StringBuffer();
        String url = cap.getUrl();
        sb.append( url );

        if ( url.indexOf( "?" ) == -1 ) {
            sb.append( "?" );
        } else if ( !( url.endsWith( "&" ) || url.endsWith( "&amp;" ) ) ) {
            sb.append( "&" );
        }

        sb.append( "VERSION=1.1.0" );
        sb.append( "&REQUEST=GetMap" );
        sb.append( "&WIDTH=" + resultsize );
        sb.append( "&HEIGHT=" + resultsize );

        double[] deltas = getRandomPosition( bbox.getMaxy() - bbox.getMiny(), bbox.getMaxx() - bbox.getMinx(), sizerl,
                                             sizerl );
        System.err.println( "dy=" + deltas[0] + " dx=" + deltas[1] );

        sb.append( "&SRS=" );
        sb.append( bbox.getSrs() );
        sb.append( "&BBOX=" );
        sb.append( bbox.getMinx() + deltas[0] );
        sb.append( "," );
        sb.append( bbox.getMiny() + deltas[1] );
        sb.append( "," );
        sb.append( bbox.getMinx() + deltas[0] + sizerl );
        sb.append( "," );
        sb.append( bbox.getMiny() + deltas[1] + sizerl );

        sb.append( "&LAYERS=" );
        sb.append( layer.getName() );

        sb.append( "&STYLES=" );
        sb.append( layer.getLayerStyle() );

        sb.append( "&FORMAT=" );
        sb.append( cap.getPrefferdFormat() );

        sb.append( "&TRANSPARENT=FALSE" );
        sb.append( "&BGCOLOR=0xFFFFFF" );

        return sb.toString();
    }

    /**
     * DOCUMENT ME
     *
     * @param args
     *            DOCUMENT ME
     */
    public static void main( String[] args ) {
        new HttpTester();
    }
}