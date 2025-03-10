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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Erweiterter Sax DefaultHandler
 */
public abstract class ExtDefaultHandler extends DefaultHandler {
    public class NodeAttrs {
        private Hashtable<String, String> htQn;

        private Hashtable<String, String> htLn;

        @SuppressWarnings("unused")
        private NodeAttrs() {
            // Noop
        }

        public NodeAttrs( Attributes atts ) {
            int len = atts.getLength();

            htQn = new Hashtable<String, String>( len );
            htLn = new Hashtable<String, String>( len );

            for ( int i = 0; i < len; i++ ) {
                htQn.put( atts.getQName( i ), atts.getValue( i ) );
                htLn.put( atts.getLocalName( i ), atts.getValue( i ) );
            }
        }

        public String[] getLocalNames() {
            return (String[]) htLn.keySet().toArray( new String[htLn.keySet().size()] );
        }

        public String[] getQlNames() {
            return htQn.keySet().toArray( new String[htLn.keySet().size()] );
        }

        public String getValueByQName( String qName ) {
            return htQn.get( qName );
        }

        public String getValueByLName( String lName ) {
            return htLn.get( lName );
        }
    }

    public class NsNode {
        private NsNode parent;

        private Hashtable<String, String> ht;

        public NsNode( NsNode ref ) {
            this.parent = ref;
            this.ht = new Hashtable<String, String>();
        }

        public void setNS( String prefix, String uri ) {
            if ( prefix == null || uri == null )
                return;

            ht.put( prefix, uri );
        }

        public String getNS( String prefix ) {
            if ( ht.containsKey( prefix ) ) {
                return ht.get( prefix );
            } else if ( parent != null ) {
                return parent.getNS( prefix );
            }

            return null;
        }

        public void dump( Vector<String> res, int level ) {
            Iterator<String> keys = ht.keySet().iterator();
            while ( keys.hasNext() ) {
                String key = keys.next();
                res.add( key + " " + ht.get( key ) );
            }
            res.add( "Level " + level );

            if ( parent != null )
                parent.dump( res, --level );
        }

        public void clear() {
            ht.clear();
        }
    }

    /** Log4J Logger */
    private static final Logger log = LoggerFactory.getLogger( "utilities.xml" );

    /** Defaultgr\u00f6\u00dfte f\u00fcr ArrayListen */
    private static final int DEFAULT_ELEMENT_DEPTH = 10;

    /** ArrayList mit dem Element Pfad */
    protected ArrayList<String> elemPath = new ArrayList<String>( DEFAULT_ELEMENT_DEPTH );

    /** ArrayList mit dem Element Values (erst bei endElement vollst\u00e4ndig) */
    protected ArrayList<StringBuffer> elemValues = new ArrayList<StringBuffer>( DEFAULT_ELEMENT_DEPTH );

    /** ArrayList mit den Element Attributen */
    protected ArrayList<NodeAttrs> elemAttributes = new ArrayList<NodeAttrs>( DEFAULT_ELEMENT_DEPTH );

    /** ArrayList mit den Namespaces */
    protected ArrayList<NsNode> elemNS = new ArrayList<NsNode>( DEFAULT_ELEMENT_DEPTH );

    /**
     * Creates a new ExtendedDefaultSaxHandler object.
     */
    public ExtDefaultHandler() {
    }

    /**
     * Aktuellen Pfad im XML-Baum zur\u00fcckliefern
     *
     * @return Pfad in /elem/elem Notation
     */
    protected String getPath() {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0, j = elemPath.size(); i < j; i++ ) {
            sb.append( "/" );
            sb.append( elemPath.get( i ) );
        }

        return sb.toString();
    }

    protected String getParentPath() {
        StringBuffer sb = new StringBuffer();

        for ( int i = 0, j = elemPath.size() - 1; i < j; i++ ) {
            sb.append( "/" );
            sb.append( elemPath.get( i ) );
        }

        return sb.toString();
    }

    protected NsNode getNamespace() {
        NsNode ns = elemNS.get( elemNS.size() - 2 );

        if ( ns == null )
            ns = new NsNode( null );

        return ns;
    }

    /**
     * Receive notification of the beginning of the document.
     *
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument()
                            throws SAXException {
        if ( log.isDebugEnabled() ) {
            log.debug( "Start des Dokuments" );
        }

        elemPath.clear();

        // root ns node
        elemNS.clear();
        elemNS.add( new NsNode( null ) );
    }

    /**
     * Receive notification of the start of an element.
     *
     * @param name
     *            The element type name.
     * @param attributes
     *            The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException {
        // Element hinzuf\u00fcgen
        elemPath.add( localName );
        elemValues.add( new StringBuffer() );

        NodeAttrs na = new NodeAttrs( atts );
        elemAttributes.add( na );

        // NS objekte
        NsNode par = elemNS.get( elemNS.size() - 1 );
        elemNS.add( new NsNode( par ) );

        if ( log.isDebugEnabled() ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "S " + getPath() + " " );
            sb.append( "(#=" + atts.getLength() + " " );

            for ( int i = 0; i < atts.getLength(); i++ )
                sb.append( atts.getQName( i ) + "=" + atts.getValue( i ) + " " );

            log.debug( sb.toString() + ")" );
        }

        elementStart( namespaceURI, localName, qName, atts );
    }

    public abstract void elementStart( String namespaceURI, String localName, String qName, Attributes atts )
                            throws SAXException;

    public abstract void elementEnd( String namespaceURI, String localName, String qName, NodeAttrs attr,
                                     StringBuffer value )
                            throws SAXException;

    /**
     * Receive notification of the end of an element.
     *
     * @param name
     *            The element type name.
     * @param attributes
     *            The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement( String namespaceURI, String localName, String qName )
                            throws SAXException {
        String last = null;
        StringBuffer value = null;
        NodeAttrs attr = null;

        try {
            last = elemPath.remove( elemPath.size() - 1 );
            value = elemValues.remove( elemValues.size() - 1 );
            attr = elemAttributes.remove( elemAttributes.size() - 1 );
        } catch ( Exception e ) {
            throw new SAXException( "XML-Fehler at Path " + getPath() + "/" + last );
        }

        if ( !last.equals( localName ) ) {
            throw new SAXException( "XML-Fehler at Path " + getPath() + "/" + last );
        }

        if ( log.isDebugEnabled() ) {
            log.debug( "E " + getPath() + "/" + last + " - " + localName + " value=" + value.toString().trim() );
        }

        elementEnd( namespaceURI, localName, qName, attr, value );

        try {
            // remove last ns
            ( elemNS.get( elemNS.size() - 2 ) ).clear();
            elemNS.remove( elemNS.size() - 1 );
        } catch ( Exception e ) {
            throw new SAXException( "XML-Fehler at Path " + getPath() + "/" + last );
        }
    }

    /**
     * Receive notification of the end of the document.
     *
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument()
                            throws SAXException {
        log.debug( "Ende des Dokuments" );
        elemPath.clear();
    }

    /**
     * Receive notification of character data inside an element.
     *
     * @param ch
     *            The characters.
     * @param start
     *            The start position in the character array.
     * @param length
     *            The number of characters to use from the character array.
     * @exception org.xml.sax.SAXException
     *                Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters( char[] ch, int start, int length )
                            throws SAXException {
        try {
            StringBuffer sb = elemValues.get( elemValues.size() - 1 );
            sb.append( ch, start, length );
            elemValues.set( elemValues.size() - 1, sb );
        } catch ( Exception e ) {
            throw new SAXException( "Fehler bei character handling" );
        }
    }

    public void startPrefixMapping( String prefix, String uri )
                            throws SAXException {
        NsNode nsn = elemNS.get( elemNS.size() - 1 );
        nsn.setNS( prefix, uri );
    }
}