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

public class OGCExceptionDefSaxHandler extends ExtDefaultHandler {
    private String errMessage = null;

    private String errCode = null;

    private String errLocation = null;

    public OGCExceptionDefSaxHandler() {
    }

    public String getFormatedMessag() {
        StringBuffer msg = new StringBuffer();

        if ( ( errLocation != null ) && ( errLocation.length() > 0 ) ) {
            if ( ( errCode != null ) && ( errCode.length() > 0 ) ) {
                msg.append( "Fehler " ).append( errCode ).append( " in " );
                msg.append( errLocation );
            } else {
                msg.append( "Fehler in " ).append( errLocation );
            }
        }

        if ( ( errMessage != null ) && ( errMessage.length() > 0 ) ) {
            if ( msg.length() > 0 ) {
                msg.append( " " );
            }

            msg.append( " Meldung: " ).append( errMessage );
        }

        if ( msg.length() == 0 ) {
            msg.append( "Kein OGC-Fehler oder Format unbekant" );
        }

        return msg.toString();
    }

    public boolean isOgcErrorMessage() {
        return ( ( errMessage != null ) || ( errCode != null ) || ( errLocation != null ) );
    }

    public void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr, StringBuffer value )
                            throws SAXException {
        String full = getPath() + "/" + localName;

        if ( full.toLowerCase().endsWith( "/serviceexception" ) ) {
            String[] lnames = attr.getLocalNames();

            for ( int i = 0, j = lnames.length; i < j; i++ ) {
                String nam = lnames[i];

                if ( ( nam != null ) && nam.equalsIgnoreCase( "code" ) ) {
                    errCode = attr.getValueByLName( nam );
                } else if ( ( nam != null )
                            && ( nam.equalsIgnoreCase( "locator" ) || nam.equalsIgnoreCase( "location" ) ) ) {
                    errLocation = attr.getValueByLName( nam );
                }
            }

            errMessage = value.toString();

            if ( errMessage != null ) {
                errMessage = errMessage.trim();
            }
        }
    }

    public void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException {
        // NOOP
    }
}