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
package de.grit.gdi.daemon.data.sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * F\u00fcr eine Sensorpr\u00fcfung ben\u00f6tigten Informationen
 * 
 * @version 1.1.0
 * @see OGC WMS Implementation Specification Version 1.1.0
 */
public class WmsLayer {
    /** Minimale Ausdehnung eines Pixels in der Einheit des Koordinatensystems */
    private double scaleHintMin;

    /** Maximale Ausdehnung eines Pixels in der Einheit des Koordinatensystems */
    private double scaleHintMax;

    /** Name des ersten Styles eines Layers */
    private String styleName;

    /** Name des Layers (techn.) */
    private String name;

    /** Name des Layers (Beschriftung) */
    private String title;

    /** LatLonBoundingBox */
    private LatLonBoundingBox latLonBoundingBox;

    /** Alle BoundingBoxex */
    private List<BoundingBox> boundingBoxes;

    /** \u00dcbergeordneter Layer (null = root) */
    private WmsLayer parent = null;

    /**
     * Creates a new WmsLayer object.
     */
    public WmsLayer() {
        boundingBoxes = new ArrayList<BoundingBox>();
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getScaleHintMin() {
        return scaleHintMin;
    }

    /**
     * DOCUMENT_ME
     *
     * @param scaleHintMin
     *            DOCUMENT_ME
     */
    public void setScaleHintMin( double scaleHintMin ) {
        this.scaleHintMin = scaleHintMin;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getScaleHintMax() {
        return scaleHintMax;
    }

    /**
     * DOCUMENT_ME
     *
     * @param scaleHintMax
     *            DOCUMENT_ME
     */
    public void setScaleHintMax( double scaleHintMax ) {
        this.scaleHintMax = scaleHintMax;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * DOCUMENT_ME
     *
     * @param styleName
     *            DOCUMENT_ME
     */
    public void setStyleName( String styleName ) {
        this.styleName = styleName;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getName() {
        return name;
    }

    /**
     * DOCUMENT_ME
     *
     * @param name
     *            DOCUMENT_ME
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getTitle() {
        return title;
    }

    /**
     * DOCUMENT_ME
     *
     * @param title
     *            DOCUMENT_ME
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public LatLonBoundingBox getLatLonBoundingBox() {
        return latLonBoundingBox;
    }

    /**
     * DOCUMENT_ME
     *
     * @param latLonBoundingBox
     *            DOCUMENT_ME
     */
    public void setLatLonBoundingBox( LatLonBoundingBox latLonBoundingBox ) {
        this.latLonBoundingBox = latLonBoundingBox;
    }

    public BoundingBox[] getBoundingBoxes() {
        if ( boundingBoxes == null || boundingBoxes.size() == 0 )
            return null;
        else
            return boundingBoxes.toArray( new BoundingBox[boundingBoxes.size()] );
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public BoundingBox getBoundingBox() {
        if ( boundingBoxes == null || boundingBoxes.size() == 0 )
            return null;
        else
            return boundingBoxes.get( 0 );
    }

    /**
     * DOCUMENT_ME
     *
     * @param boundingBox
     *            DOCUMENT_ME
     */
    public void addBoundingBox( BoundingBox boundingBox ) {
        boundingBoxes.add( boundingBox );
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCategory() {
        return ( ( this.name == null ) || ( this.name.length() == 0 ) );
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "(name=" + name );
        sb.append( "|title=" + title );
        sb.append( "|scaleHintMin=" + scaleHintMin );
        sb.append( "|scaleHintMax=" + scaleHintMax );
        sb.append( "|styleName=" + styleName );
        sb.append( "|boundingBox=" );
        if ( boundingBoxes != null && boundingBoxes.size() >= 0 ) {
            sb.append( boundingBoxes.get( 0 ).toString() );
            for ( int i = 1, j = boundingBoxes.size(); i < j; i++ ) {
                sb.append( "," ).append( boundingBoxes.get( 0 ).toString() );
            }
        }
        sb.append( "|latLonBoundingBox=" + ( ( latLonBoundingBox != null ) ? latLonBoundingBox.toString() : "" ) );
        sb.append( "|parent=" + ( ( parent != null ) ? parent.toString() : "" ) );
        sb.append( "@WmsLayer)" );

        return sb.toString();
    }

    /**
     * Pr\u00fcft b ein \u00dcbergeordnetes Element vorhanden ist
     *
     * @return DOCUMENT_ME
     */
    public boolean hasParent() {
        return ( this.parent != null );
    }

    /**
     * DOCUMENT_ME
     *
     * @param parent
     *            DOCUMENT_ME
     */
    public void setParent( WmsLayer parent ) {
        this.parent = parent;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public WmsLayer getParent() {
        return parent;
    }

    /**
     * Ermittelt die geltende Boundingbox
     * <p>
     * Es wird zuerst versucht in der Hierarchie eine Boundingbox zu finden. Ist dies nicht m\u00f6glich so wird versucht
     * eine LatLonBoundingBox in der Hierarchie zu finden.
     * <p>
     * Qualitativ ist die BoundingBox zu bevorzugen, da die Umrechnung des vorh. Koordinatensystem in EPSG:4326 (WGS1984)
     * nicht von jeden Mapserver korrekt durchgef\u00fchrt wird.
     * 
     * @return Geltende BoundingBox oder null im Fehlerfall
     */
    public BoundingBox getLayerBBox() {
        WmsLayer wtmp = this;

        while ( wtmp.getBoundingBox() == null && wtmp.parent != null ) {
            wtmp = wtmp.parent;
        }

        if ( wtmp.getBoundingBox() != null ) {
            // BBo gefunden
            return wtmp.getBoundingBox();
        }

        // In Lat/Lon BBox suchen
        wtmp = this;

        while ( wtmp.latLonBoundingBox == null && wtmp.parent != null ) {
            wtmp = wtmp.parent;
        }

        if ( wtmp.latLonBoundingBox != null ) {
            return wtmp.latLonBoundingBox.getCopyAsBBox();
        } else {
            return null;
        }
    }

    /**
     * BBox fuer angegebenes SRS suchen
     */
    public BoundingBox getLayerBBox( String srs ) {
        WmsLayer wtmp = this;

        if ( "EPSG:4326".equalsIgnoreCase( srs ) ) {
            // lookup in latlonbbox

            while ( wtmp.latLonBoundingBox == null && wtmp.parent != null ) {
                wtmp = wtmp.parent;
            }

            if ( wtmp.latLonBoundingBox != null ) {
                return wtmp.latLonBoundingBox.getCopyAsBBox();
            }

            // nothing found -> null
        } else {
            // suche in normalen bboxen

            while ( ( wtmp.boundingBoxes != null && wtmp.boundingBoxes.size() > 0 ) || wtmp.parent != null ) {

                if ( wtmp.boundingBoxes != null && wtmp.boundingBoxes.size() > 0 ) {
                    for ( BoundingBox b : wtmp.boundingBoxes ) {

                        // check for box-srs
                        if ( srs.equalsIgnoreCase( b.getSrs() ) )
                            return b;
                    }
                }

                // in this layer nothing found go upper or break if no upper available
                if ( wtmp.parent != null )
                    wtmp = wtmp.parent;
                else
                    break;
            }

            // nothing found -> null
        }

        return null;
    }

    /**
     * Ermittelt den Min- und Max- Scale Hint
     * 
     * @return Scatel Hints oder leeres Array
     */
    public double[] getLayerScaleHintMinMax() {
        WmsLayer wtmp = this;
        while ( wtmp != null && wtmp.scaleHintMin == 0 && wtmp.scaleHintMax == 0 ) {
            wtmp = wtmp.parent;
        }

        if ( wtmp == null || ( wtmp.scaleHintMin == 0 && wtmp.scaleHintMax == 0 ) ) {
            double[] res = {};
            return res;
        } else {
            double[] res = { wtmp.scaleHintMin, wtmp.scaleHintMax };
            return res;
        }
    }

    /**
     * Liefert den passenden Style zur\u00fcck
     * 
     * @return Stylename oder null
     */
    public String getLayerStyle() {
        WmsLayer wtmp = this;
        while ( wtmp != null && wtmp.styleName == null ) {
            wtmp = wtmp.parent;
        }

        if ( wtmp != null )
            return ( wtmp.styleName == null ? "" : wtmp.styleName );
        else
            return "";
    }
}