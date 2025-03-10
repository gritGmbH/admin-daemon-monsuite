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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Klasse zum Speicher der Capabilities Informationen
 */
public class WMSCapabilities {
    private Map<String, WmsLayer> layer = new Hashtable<String, WmsLayer>();

    private List<String> format = new ArrayList<String>();

    private String url = "";

    public WMSCapabilities() {
    }

    public Map<String, WmsLayer> getLayer() {
        return layer;
    }

    public void setLayer( Map<String, WmsLayer> layer ) {
        this.layer = layer;
    }

    public String getPrefferdFormat() {
        if ( format.contains( "image/png" ) )
            return "image/png";
        else if ( format.contains( "image/gif" ) )
            return "image/gif";
        else if ( format.contains( "image/jpg" ) )
            return "image/jpg";
        else if ( format.contains( "image/jpeg" ) )
            return "image/jpeg";
        else
            return null;
    }

    public String getPrefferdFormatShort() {
        if ( format.contains( "image/png" ) )
            return "png";
        else if ( format.contains( "image/gif" ) )
            return "gif";
        else if ( format.contains( "image/jpg" ) )
            return "jpg";
        else if ( format.contains( "image/jpeg" ) )
            return "jpg";
        else
            return null;
    }

    public List<String> getFormat() {
        return format;
    }

    public void setFormat( List<String> format ) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl( String url ) {
        this.url = url;
    }
}