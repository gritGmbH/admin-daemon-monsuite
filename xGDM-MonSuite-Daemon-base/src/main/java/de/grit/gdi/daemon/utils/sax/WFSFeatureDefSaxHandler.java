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

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class WFSFeatureDefSaxHandler extends ExtDefaultHandler {

    private static final Logger LOG = LoggerFactory.getLogger( WFSFeatureDefSaxHandler.class );

    private ArrayList<String> featureTypes = new ArrayList<String>();

    public WFSFeatureDefSaxHandler() {
    }

    public void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException {

    }

    public void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr, StringBuffer value )
                            throws SAXException {
        // WFS 1.1.0
        if ( getPath().equalsIgnoreCase( "/featurecollection/featuremember" ) ) {
            String featureName = "{" + namespaceURI + "}" + localName;

            if ( !featureTypes.contains( featureName ) ) {
                featureTypes.add( featureName );
                LOG.trace( "Found Featuretype {}", featureName );
            }
        }
        // WFS 2.0.0
        if ( getPath().equalsIgnoreCase( "/featurecollection/member" ) ) {
            String featureName = "{" + namespaceURI + "}" + localName;

            if ( !featureTypes.contains( featureName ) ) {
                featureTypes.add( featureName );
                LOG.trace( "Found Featuretype {}", featureName );
            }
        }
        // TODO: Eventuell weitere Varianten ermitteln und hier implementieren (andere Server etc.).
    }

    public ArrayList<String> getFeatureTypes() {
        return featureTypes;
    }
}