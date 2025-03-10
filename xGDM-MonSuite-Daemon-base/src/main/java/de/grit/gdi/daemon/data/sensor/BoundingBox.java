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

import de.grit.gdi.daemon.utils.Utilities;

/**
 * BoundingBox nach OGC
 * 
 * @version 1.1.0
 */
public class BoundingBox {
    /** DOCUMENT_ME */
    private double minx;

    /** DOCUMENT_ME */
    private double maxx;

    /** DOCUMENT_ME */
    private double miny;

    /** DOCUMENT_ME */
    private double maxy;

    /** DOCUMENT_ME */
    private String srs;

    /**
     * Creates a new BoundingBox object.
     */
    public BoundingBox() {
    }

    public BoundingBox( String srs, String bbox ) {
        try {
            this.srs = srs;
            double[] fa = Utilities.strToDoubleArray( bbox, "," );

            this.minx = fa[0];
            this.miny = fa[1];
            this.maxx = fa[2];
            this.maxy = fa[3];
        } catch ( Exception ex ) {
            throw new IllegalArgumentException( "Der angegebene Koordinatenbereich (BBox) konnte nicht geparst werden." );
        }
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getMinx() {
        return minx;
    }

    /**
     * DOCUMENT_ME
     *
     * @param minx
     *            DOCUMENT_ME
     */
    public void setMinx( double minx ) {
        this.minx = minx;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getMaxx() {
        return maxx;
    }

    /**
     * DOCUMENT_ME
     *
     * @param maxx
     *            DOCUMENT_ME
     */
    public void setMaxx( double maxx ) {
        this.maxx = maxx;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getMiny() {
        return miny;
    }

    /**
     * DOCUMENT_ME
     *
     * @param miny
     *            DOCUMENT_ME
     */
    public void setMiny( double miny ) {
        this.miny = miny;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public double getMaxy() {
        return maxy;
    }

    /**
     * DOCUMENT_ME
     *
     * @param maxy
     *            DOCUMENT_ME
     */
    public void setMaxy( double maxy ) {
        this.maxy = maxy;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getSrs() {
        return srs;
    }

    /**
     * DOCUMENT_ME
     *
     * @param srs
     *            DOCUMENT_ME
     */
    public void setSrs( String srs ) {
        this.srs = srs;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "(srs=" + srs );
        sb.append( "|minx=" + minx );
        sb.append( "|maxx=" + maxx );
        sb.append( "|miny=" + miny );
        sb.append( "|maxy=" + maxy );
        sb.append( "@BoundingBox)" );
        return sb.toString();
    }
}