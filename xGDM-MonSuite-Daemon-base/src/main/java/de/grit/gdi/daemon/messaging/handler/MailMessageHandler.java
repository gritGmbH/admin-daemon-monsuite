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
package de.grit.gdi.daemon.messaging.handler;

import static de.grit.gdi.daemon.utils.Utilities.getPropAsInt;
import static de.grit.vaadin.common.EbeanUtilsCommon.getProp;

import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger( MailMessageHandler.class );

    private static Object[] getReplacements( Map<String, Object> tokens ) {
        String[] tokenn = getProp( "messagehandler.defaults.mail.tokens", "" ).split( "," );

        Object[] args = new Object[tokenn.length];

        for ( int i = 0, j = tokenn.length; i < j; i++ ) {
            Object ob = tokens.get( tokenn[i] );

            if ( ob == null )
                ob = resolveProp( tokens, tokenn[i] );

            if ( ob != null ) {
                args[i] = ob;
            } else {
                args[i] = "";
            }
        }

        return args;
    }

    private static Object resolveProp( Map<String, Object> tokens, String key ) {
        int pos = key != null ? key.indexOf( '.' ) : -1;
        if ( pos == -1 )
            return null;
        String obj = key.substring( 0, pos );
        String ref = key.substring( pos + 1 );

        Object o = tokens.get( obj );
        try {
            if ( o != null )
                return PropertyUtils.getNestedProperty( o, ref );
        } catch ( Exception ex ) {
            LOG.info( "Trying to access porperty {} for notification throws {}", key, ex.getMessage() );
            LOG.trace( "Exception on resolving property", ex );
        }

        return null;
    }

    public static void sendMail( MailMessageParameter mp ) {
        StringBuffer mailinfo = new StringBuffer();

        try {
            SimpleEmail email = new SimpleEmail();
            email.setHostName( getProp( "handler.mail.mailserver.host", "127.0.0.1" ) );
            email.setSmtpPort( getPropAsInt( "handler.mail.mailserver.port", 25 ) );

            email.setFrom( mp.getFromAddr() );

            Object[] args = getReplacements( mp.getTokens() );

            email.setSubject( MessageFormat.format( mp.getSubject(), args ) );
            mailinfo.append( "mail subj: " ).append( email.getSubject() );

            for ( int i = 0, j = mp.getToAddr().length; i < j; i++ ) {
                email.addTo( mp.getToAddr()[i] );
                mailinfo.append( " to:" ).append( mp.getToAddr()[i] );
            }

            email.setMsg( MessageFormat.format( mp.getMessage(), args ) );

            String mailMsgID = email.send();

            if ( mailMsgID == null || mailMsgID.length() == 0 ) {
                mailinfo.append( " result: no msgid returned" );
                LOG.error( "Mail senden Fehlgeschlagen (Keine MSGID zur\u00fcckerhalten)" );
            } else {
                mailinfo.append( " result: " ).append( mailMsgID );
                LOG.debug( "Mail gesendet MSGID: {}", mailMsgID );
            }
            LOG.info( mailinfo.toString() );
        } catch ( Exception e ) {
            LOG.error( "Mail senden Fehlgeschlagen (Details: " + e.getMessage() + ")", e );
        }
    }
}