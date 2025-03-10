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
package de.grit.gdi.daemon.utils;

import static de.grit.gdi.daemon.utils.Utilities.isNullOrEmpty;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.grit.gdi.daemon.utils.sax.ExtDefaultHandler;

public class ExceptionAnalyser {

    private static final long MAX_TEST_LENGTH = 1048576L; // 1 MB

    private static final Logger LOG = LoggerFactory.getLogger( ExceptionAnalyser.class );

    class ExceptionParser extends ExtDefaultHandler {

        @Override
        public void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                                throws SAXException {
            LOG.trace( "start tag {}:{}", namespaceURI, localName );
        }

        @Override
        public void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr,
                                StringBuffer value )
                                throws SAXException {
            LOG.trace( "end tag {}:{}: text: {}", namespaceURI, localName, value );

            if ( localName != null && localName.toLowerCase().endsWith( "exception" ) ) {
                String tmpCode = attr.getValueByLName( "code" );
                String tmpExtCode = attr.getValueByLName( "exceptionCode" );
                String tmpLoc = attr.getValueByLName( "locator" );
                if ( code == null && !isNullOrEmpty( tmpCode ) )
                    code = tmpCode;
                else if ( code == null && !isNullOrEmpty( tmpExtCode ) )
                    code = tmpCode;

                if ( locator == null && !isNullOrEmpty( tmpLoc ) )
                    locator = tmpLoc;

                message += value.toString();

                isException = true;
            }
        }
    }

    boolean isException = false;

    String code = null;

    String locator = null;

    String message = "";

    public ExceptionAnalyser( byte[] data ) {
        if ( data == null || data.length > MAX_TEST_LENGTH || data.length == 0 ) {
            return;
        }

        ExceptionParser handler = new ExceptionParser();
        try {
            org.apache.xerces.parsers.SAXParser xr = new org.apache.xerces.parsers.SAXParser();

            xr.setFeature( "http://xml.org/sax/features/validation", false );
            xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

            xr.setContentHandler( handler );
            xr.setErrorHandler( handler );

            xr.parse( new InputSource( new ByteArrayInputStream( data ) ) );
        } catch ( SAXParseException e ) {
            int line = e.getLineNumber();
            int colum = e.getColumnNumber();
            LOG.debug( "Fehler beim versuch zu pruefen ob eine Exception vorliegt (l:{} c:{}): {}", line, colum,
                       e.getMessage() );
            LOG.trace( "Exception", e );
            isException = false;
        } catch ( Exception e ) {
            LOG.debug( "Fehler beim versuch zu pruefen ob eine Exception vorliegt: {}", e.getMessage() );
            LOG.trace( "Exception", e );
            isException = false;
        }
    }

    public boolean isException() {
        return isException;
    }

    public String getCode() {
        return code;
    }

    public String getLocator() {
        return locator;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if ( !isNullOrEmpty( locator ) )
            sb.append( "[" ).append( locator ).append( "] " );

        sb.append( message );

        if ( !isNullOrEmpty( code ) )
            sb.append( " (" ).append( code ).append( ")" );

        return sb.toString();
    }
}