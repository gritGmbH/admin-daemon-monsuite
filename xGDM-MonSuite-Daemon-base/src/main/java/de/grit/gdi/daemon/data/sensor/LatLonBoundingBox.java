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

/**
 * LatLonBoundingBox nach OGC
 * 
 * @version 1.1.0
 */
public class LatLonBoundingBox {
    /** DOCUMENT_ME */
    private float minx;

    /** DOCUMENT_ME */
    private float maxx;

    /** DOCUMENT_ME */
    private float miny;

    /** DOCUMENT_ME */
    private float maxy;

    /**
     * Creates a new LatLonBoundingBox object.
     */
    public LatLonBoundingBox() {
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public float getMinx() {
        return minx;
    }

    /**
     * DOCUMENT_ME
     *
     * @param minx
     *            DOCUMENT_ME
     */
    public void setMinx( float minx ) {
        this.minx = minx;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public float getMaxx() {
        return maxx;
    }

    /**
     * DOCUMENT_ME
     *
     * @param maxx
     *            DOCUMENT_ME
     */
    public void setMaxx( float maxx ) {
        this.maxx = maxx;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public float getMiny() {
        return miny;
    }

    /**
     * DOCUMENT_ME
     *
     * @param miny
     *            DOCUMENT_ME
     */
    public void setMiny( float miny ) {
        this.miny = miny;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public float getMaxy() {
        return maxy;
    }

    /**
     * DOCUMENT_ME
     *
     * @param maxy
     *            DOCUMENT_ME
     */
    public void setMaxy( float maxy ) {
        this.maxy = maxy;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "(minx=" + minx );
        sb.append( "|maxx=" + maxx );
        sb.append( "|miny=" + miny );
        sb.append( "|maxy=" + maxy );
        sb.append( "@LatLonBoundingBox)" );
        return sb.toString();
    }

    public BoundingBox getCopyAsBBox() {
        BoundingBox tmp = new BoundingBox();
        tmp.setSrs( "EPSG:4326" );
        tmp.setMinx( this.minx );
        tmp.setMaxx( this.maxx );
        tmp.setMiny( this.miny );
        tmp.setMaxy( this.maxy );
        return tmp;
    }
}