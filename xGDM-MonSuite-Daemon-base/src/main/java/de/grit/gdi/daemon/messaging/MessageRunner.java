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
package de.grit.gdi.daemon.messaging;

import static de.grit.vaadin.common.EbeanUtilsCommon.getSQLProp;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.data.message.DaemonMessage;
import de.grit.gdi.daemon.data.message.SimpleMessage;
import de.grit.gdi.daemon.messaging.handler.MailMessageHandler;
import de.grit.gdi.daemon.messaging.handler.MailMessageParameter;
import de.grit.gdi.daemon.messaging.handler.SnmpMessageHandler;
import de.grit.gdi.daemon.messaging.handler.SnmpMessageParameter;
import de.grit.xgdm.monsuite.data.CheckTask;
import de.grit.xgdm.monsuite.data.Notification;
import de.grit.xgdm.monsuite.data.ResultCode;

public class MessageRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger( MessageRunner.class );

    private DateFormat fmtDbDate = new SimpleDateFormat( "yyyyMMddHHmmss" );

    private final SimpleMessage msg;

    public MessageRunner( SimpleMessage msg ) {
        this.msg = msg;
    }

    @Override
    /**
     * Handle current message
     * <p>
     * <code>
     * Ablauf: 
     * 1) Logging zur DB 
     * 2) Konfiguration aus DB Laden 
     * 3) An den passenden MessageHandler uebergeben
     * </code>
     */
    public void run() {
        try {
            if ( !logToDatabase( msg ) ) {
                // Fehler konnte nicht in DB geschrieben werden
                logToLogfile( true, msg );
            }
        } catch ( SQLException e ) {
            LOG.error( "Error writing Log to Database", e );
        }

        // 2 / 3
        Hashtable<String, Object> httokens = new Hashtable<String, Object>();

        httokens.put( "MESSAGE", msg );
        httokens.put( "MESSAGE_TEXT",
                      ( msg.getText() == null || msg.getText().length() == 0 ) ? "-" : "" + msg.getText() );

        Date tmp_date = new Date( msg.getTerm() );
        httokens.put( "MESSAGE_TERM", "" + tmp_date );
        httokens.put( "MESSAGE_DURATION", "" + msg.getDuration() );
        httokens.put( "MESSAGE_HOST",
                      ( msg.getHost() == null || msg.getHost().length() == 0 ) ? "-" : "" + msg.getHost() );
        httokens.put( "MESSAGE_CODE", "" + msg.getCode() );

        if ( msg instanceof CheckTaskMessage ) {
            // Standardnachricht
            CheckTaskMessage sp = (CheckTaskMessage) msg;

            try {
                CheckTask ct = Ebean.find( CheckTask.class ) //
                                    .setId( sp.getCheckTaksId() ) //
                                    .fetch( "sensor" ) //
                                    .fetch( "sensor.config" ) //
                                    .fetch( "notifications" ) //
                                    .fetch( "notifications.person" ) //
                                    .findUnique();

                httokens.put( "CHECKTASK", ct );

                ResultCode rc = Ebean.find( ResultCode.class, sp.getCode() );
                httokens.put( "RESULTCODE", rc );

                for ( Notification n : ct.getNotifications() ) {
                    httokens.put( "NOTIFICATION", n );
                    int rs_msg_typ = n.getType() != null ? n.getType().intValue() : -1;

                    switch ( rs_msg_typ ) {
                    case 1:
                        // E-Mail (1) (nur fehlgeschlagene Nachrichten \u00fcbermitteln)
                        if ( !msg.isCodeSuccessfull() ) {
                            MailMessageParameter mmp = new MailMessageParameter();
                            mmp.setToAddr( ( n.getPerson().getMail() != null ? n.getPerson().getMail()
                                                                             : "" ).split( " " ) );
                            mmp.getTokens().put( "PERSON_NAME", n.getPerson().getName() );
                            mmp.getTokens().putAll( httokens );

                            MailMessageHandler.sendMail( mmp );
                        }
                        break;

                    case 4:
                    case 5:
                        // SNMPv1 (4), SNMPv2c (5)
                        SnmpMessageParameter smp = new SnmpMessageParameter( sp );

                        if ( rs_msg_typ == 5 )
                            smp.setVersion( SnmpMessageParameter.VERSION_V2C_NOTIFY );
                        else
                            smp.setVersion( SnmpMessageParameter.VERSION_V1_TRAP );

                        String dest = n.getPerson().getSnmp();
                        if ( dest == null )
                            dest = "";

                        int destRef = dest.indexOf( '#' );
                        if ( destRef != -1 ) {
                            smp.setDestination( dest.substring( 0, destRef ) );
                            smp.setComunity( dest.substring( destRef + 1 ) );
                        } else {
                            smp.setDestination( dest );
                            smp.setComunity( "public" );
                        }

                        // SnmpMessageHandler.getInstance().handleJob( smp );
                        SnmpMessageHandler.sendMessage( smp );
                        break;

                    case 2:
                    case 3:
                    default:
                        // Fax (2), SMS (3), nicht implementiert
                        logToLogfile( true, msg );
                        break;
                    }

                }
            }

            catch ( Exception ex ) {
                // TODO:Exception implementieren
                LOG.error( "Fehler beim benachrichtigen der Nachrichtenziele: {}", ex.getMessage() );
                LOG.trace( "Exception", ex );
                logToLogfile( true, msg );
            }
        } // Ende CheckTaskMessage
        else if ( msg.getClass().equals( DaemonMessage.class ) ) {
            /*
             * Ignoriere DaemonMessages -> Nur in DB schreiben
             */
        } else {
            // Message not know
            logToLogfile( false, msg );
        }
    }

    private void logToLogfile( boolean known, SimpleMessage msg ) {
        Logger log;

        if ( known ) {
            log = LoggerFactory.getLogger( "messages.failed" );
        } else {
            log = LoggerFactory.getLogger( "messages.unknown" );
        }

        log.error( msg.toString() );
    }

    boolean logToDatabase( SimpleMessage msg )
                            throws SQLException {

        SqlUpdate upd = null;
        boolean result = true;

        if ( msg instanceof CheckTaskMessage ) {
            // Standardnachricht
            CheckTaskMessage sp = (CheckTaskMessage) msg;
            LOG.info( "CheckTaskMessage: {} res: {} txt: {}", sp.getCheckTaksId(), sp.getCode(), sp.getText() );

            upd = Ebean.createSqlUpdate( getSQLProp( "config.sql.logging.sensor", null ) );

            upd.setParameter( 1, sp.getCheckTaksId() );
            upd.setParameter( 2, sp.getCode() );
            if ( "true".equalsIgnoreCase( getSQLProp( "config.sql.logging.termAsString", "false" ) ) ) {
                upd.setParameter( 3, fmtDbDate.format( new java.util.Date( sp.getTerm() ) ) );
            } else {
                upd.setParameter( 3, new java.sql.Timestamp( sp.getTerm() ) );
            }
            upd.setParameter( 4, sp.getHost() );
            upd.setParameter( 5, sp.getDuration() );
            upd.setParameter( 6, sp.getText() );

            int modifiedCount = Ebean.execute( upd );

            if ( modifiedCount != 1 ) {
                LOG.warn( "Failed to message {} ({})", msg, modifiedCount );
                result = false;
            }
        } else if ( msg instanceof DaemonMessage ) {
            DaemonMessage sp = (DaemonMessage) msg;
            LOG.info( "DaemonMessage: {} res: {} txt: {}", sp.getOrigin(), sp.getCode(), sp.getText() );
            String sql;
            if ( sp.getOrigin() == DaemonMessage.MONITOR_CONTROLLER ) {
                sql = getSQLProp( "config.sql.logging.controller", null );
            } else {
                sql = getSQLProp( "config.sql.logging.monitor", null );
            }
            if ( sql == null ) {
                LOG.error( "Check config config.sql.logging.controller or config.sql.logging.monitor missing" );
                return false;
            }
            upd = Ebean.createSqlUpdate( sql );
            upd.setParameter( 1, sp.getCode() );

            if ( "true".equalsIgnoreCase( getSQLProp( "config.sql.logging.termAsString", "false" ) ) ) {
                upd.setParameter( 2, fmtDbDate.format( new java.util.Date( sp.getTerm() ) ) );
            } else {
                upd.setParameter( 2, new java.sql.Timestamp( sp.getTerm() ) );
            }

            // prep.setDate(2, new java.sql.Date( sp.getTerm() ) );
            upd.setParameter( 3, sp.getHost() );
            upd.setParameter( 4, sp.getDuration() );
            upd.setParameter( 5, sp.getText() );

            int modifiedCount = Ebean.execute( upd );

            if ( modifiedCount != 1 ) {
                LOG.warn( "Failed to message {} ({})", msg, modifiedCount );
                result = false;
            }
        } else {
            LOG.error( "Unknown message {}", msg );
            result = false;
        }

        return result;
    }

}