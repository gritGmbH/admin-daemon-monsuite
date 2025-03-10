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
package de.grit.gdi.daemon.data.config;

import java.sql.ResultSet;
import java.util.Objects;

import de.grit.xgdm.monsuite.data.CheckTask;
import de.grit.xgdm.monsuite.data.SensorConfig;

public class ArcGisSensorConfig extends AbstractSensorConfig {

    /**
     * Internetadresse des WMS-Services (f\u00fcr getCapabilities) (Alle anderen Adressen lassen sich hiervon ableiten)
     */
    private String url = "";

    /** BoundingBox fuer Image-Anfrage */
    private String bbox;

    /** Breite/Hoehe fuer Image-Anfrage */
    private String imgSize;

    private String serviceName;

    private String layerName;

    private String href;

    private static final String[] ROWIDsARCGISSC = new String[] { "URL", "MAP_MXD", "LAYER_AVAIL", "BBOX", "IMAGE_SIZE" };

    public static int DEFAULT_IMG_SIZE = 400;

    /**
     * Creates a new ArcGISSensorConfig object.
     */
    public ArcGisSensorConfig() {
        super();
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind
     *
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    public boolean equals( Object o ) {
        if ( !equalsBasicSensorConfig( o ) ) {
            return false;
        }

        ArcGisSensorConfig sp = (ArcGisSensorConfig) o;

        // Vergleich Direktattribute
        if ( Objects.equals( url, sp.getUrl() ) && Objects.equals( serviceName, sp.getServiceName() )
             && Objects.equals( layerName, sp.getLayerName() ) && Objects.equals( bbox, sp.getBbox() )
             && Objects.equals( imgSize, sp.getImgSize() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Laden eines SQL-Datensatzes (Erweiterte Attribute) in das Config Objekt
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     *
     * @return boolean erfolgreich / fehlgeschlagen
     */
    public boolean loadFromRow( ResultSet dbres ) {
        if ( !loadFromRowBasic( dbres ) ) {
            return false;
        }

        try {
            this.url = dbres.getString( ROWIDsARCGISSC[0] );
            this.serviceName = dbres.getString( ROWIDsARCGISSC[1] );
            this.layerName = dbres.getString( ROWIDsARCGISSC[2] );
            this.bbox = dbres.getString( ROWIDsARCGISSC[3] );
            this.imgSize = dbres.getString( ROWIDsARCGISSC[4] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.serviceName = sen.getMapMxd();
        this.layerName = sen.getLayerAvail();
        this.bbox = sen.getBbox();
        this.imgSize = sen.getImageSize();
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( ( bbox == null ) ? 0 : bbox.hashCode() ) );
        sb.append( ( ( imgSize == null ) ? 0 : imgSize.hashCode() ) );
        sb.append( ( ( url == null ) ? 0 : url.hashCode() ) );
        sb.append( ( ( serviceName == null ) ? 0 : serviceName.hashCode() ) );
        sb.append( ( ( layerName == null ) ? 0 : layerName.hashCode() ) );

        return sb.toString().hashCode();
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getBbox() {
        return bbox;
    }

    public String getImgSize() {
        return imgSize;
    }

    public void setBbox( String bbox ) {
        this.bbox = bbox;
    }

    public void setImgSize( String size ) {
        this.imgSize = size;
    }

    public int getImgSizeWidth() {
        if ( imgSize == null || imgSize.length() == 0 )
            return DEFAULT_IMG_SIZE;

        try {
            int pos = imgSize.indexOf( ',' );
            if ( pos == -1 )
                return Integer.parseInt( imgSize );
            else
                return Integer.parseInt( imgSize.substring( 0, pos ) );

        } catch ( Exception ex ) {
            return DEFAULT_IMG_SIZE;
        }
    }

    public int getImgSizeHeight() {
        if ( imgSize == null || imgSize.length() == 0 )
            return DEFAULT_IMG_SIZE;

        try {
            int pos = imgSize.indexOf( ',' );
            if ( pos == -1 )
                return Integer.parseInt( imgSize );
            else
                return Integer.parseInt( imgSize.substring( pos + 1 ) );

        } catch ( Exception ex ) {
            return DEFAULT_IMG_SIZE;
        }
    }

    public void setServiceName( String serviceName ) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setLayerName( String layerName ) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setHref( String href ) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }
}