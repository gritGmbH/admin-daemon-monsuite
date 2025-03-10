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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.grit.gdi.daemon.data.sensor.WFSCapabilities;

public class WFSCapabilitiesDefSaxHandler extends ExtDefaultHandler {
    /*
     * /root/second/third/ ^ \- elemNS.size() - 2
     * 
     * NsNode nsn = (NsNode)elemNS.get(elemNS.size() - 2); Vector v = new Vector( 150 ); nsn.dump(v, elemNS.size()-1);
     * System.out.println(getPath()); for (int i = v.size()-1; i >= 0; i-- ) { System.out.println( v.get( i ) ); }
     */

    public WFSCapabilitiesDefSaxHandler() {
    }

    private WFSCapabilities cap = new WFSCapabilities();

    public void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException {
        String full = getPath().toLowerCase();

        /*
         * if (full.toLowerCase().endsWith("/wgs84boundingbox/lowercorner")) {
         * System.out.println("LN: "+localName+" QN: "+qName+" #AT: "+atts.getLength()+"  NS: "+namespaceURI ); }
         * 
         * if (full.startsWith("/wfs_capabilities/operationsmetadata")) { //noop }
         */

        if ( full.endsWith( "/wfs_capabilities" ) ) {
            cap.setWfsVersion( atts.getValue( "", "version" ) );
        }
    }

    public void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr, StringBuffer value )
                            throws SAXException {
        String full = getPath() + "/" + localName;

        if ( full.toLowerCase().endsWith( "/featuretype/name" ) ) {
            String fname = value.toString().trim();
            String namespace = "";

            if ( fname.indexOf( ':' ) >= 0 ) {
                String tmpNS = fname.substring( 0, fname.indexOf( ':' ) );
                String tmpLN = fname.substring( fname.indexOf( ':' ) + 1 );
                String uri = getNamespace().getNS( tmpNS );

                // System.out.println("tmp: "+tmpNS + " " + tmpLN+ " -> "+uri);
                if ( ( uri != null ) && ( uri.length() > 0 ) ) {
                    namespace = uri;
                    fname = tmpLN;
                }
            }

            String res = "{" + namespace + "}" + fname;
            cap.getFeatureTypeNames().add( res );
        } else if ( full.toLowerCase().endsWith( "operationsmetadata/operation/parameter/value" ) ) {
            String aName = ( (NodeAttrs) elemAttributes.get( elemAttributes.size() - 1 ) ).getValueByLName( "name" );
            String aOpName = ( (NodeAttrs) elemAttributes.get( elemAttributes.size() - 2 ) ).getValueByLName( "name" );

            if ( ( aName != null ) && aName.equalsIgnoreCase( "outputFormat" ) && aOpName != null
                 && aOpName.equalsIgnoreCase( "GetFeature" ) ) {
                cap.getGetFeatureOutputFormats().add( value.toString() );
                // System.out.println("Opertation: " + aOpName + " outputFormat=" + value.toString());
                // System.out.println("LN: " + localName + "\t" + elemAttributes.size() + "\t\t" + getPath());
            }
        } else if ( full.toLowerCase().endsWith( "operation/dcp/http/get" ) ) {
            String aOpName = ( (NodeAttrs) elemAttributes.get( elemAttributes.size() - 3 ) ).getValueByLName( "name" );
            if ( aOpName != null && aOpName.equalsIgnoreCase( "GetFeature" ) ) {
                cap.setGetFeatureHttpGetUri( attr.getValueByQName( "xlink:href" ) );
            }
        } else if ( full.toLowerCase().endsWith( "operation/dcp/http/post" ) ) {
            String aOpName = ( (NodeAttrs) elemAttributes.get( elemAttributes.size() - 3 ) ).getValueByLName( "name" );
            if ( aOpName != null && aOpName.equalsIgnoreCase( "GetFeature" ) ) {
                cap.setGetFeatureHttpPostUri( attr.getValueByQName( "xlink:href" ) );
            }
        }
    }

    public WFSCapabilities getWfsCapabilities() {
        return cap;
    }
}