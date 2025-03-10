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

public class WFSCapabilities {
    private ArrayList<String> featureTypeNames = new ArrayList<String>();

    private ArrayList<String> getFeatureOutputFormats = new ArrayList<String>();

    private String getFeatureHttpPostUri = "";

    private String getFeatureHttpGetUri = "";

    private String wfsVersion = "";

    public WFSCapabilities() {
    }

    /**
     * Achtung die komplette Liste ist in lowercase!
     * 
     * @return
     */
    public List<String> getFeatureTypeNames() {
        return featureTypeNames;
    }

    public List<String> getGetFeatureOutputFormats() {
        return getFeatureOutputFormats;
    }

    public String getWfsVersion() {
        return wfsVersion;
    }

    public String getGetFeatureHttpPostUri() {
        return getFeatureHttpPostUri;
    }

    public String getGetFeatureHttpGetUri() {
        return getFeatureHttpGetUri;
    }

    public void setGetFeatureHttpPostUri( String getFeatureHttpPostUri ) {
        this.getFeatureHttpPostUri = getFeatureHttpPostUri;
    }

    public void setGetFeatureHttpGetUri( String getFeatureHttpGetUri ) {
        this.getFeatureHttpGetUri = getFeatureHttpGetUri;
    }

    public void setWfsVersion( String wfsVersion ) {
        this.wfsVersion = wfsVersion;
    }
}