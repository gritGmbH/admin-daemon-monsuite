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
package de.grit.gdi.daemon.monitoring.sensor;

import static de.grit.gdi.daemon.utils.Utilities.closeSilentN;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.SqlPostgresSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;

public class SqlPostgresSensor extends AbstractSensor {

    public SqlPostgresSensor() {

    }

    /** Log4J Logger */
    private static final Logger log = LoggerFactory.getLogger( "monitor.sensor.sqlpostgressensor" );

    private SqlPostgresSensorConfig config;

    private boolean msgStatus;

    public void init( CheckTaskConfig ctConfig ) {
        super.init( ctConfig );

        if ( !( ctConfig.getSensor() instanceof SqlPostgresSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.config = (SqlPostgresSensorConfig) ctConfig.getSensor();
        this.msgStatus = true;
    }

    public String getThreadName() {
        return config.getId() + "-" + config.getName();
    }

    public boolean getMessageStatus() {
        return this.msgStatus;
    }

    public Logger getLogger() {
        return log;
    }

    public CheckTaskConfig getCheckTaskConfig() {
        return ctConfig;
    }

    public void run() {
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );
        long start = System.currentTimeMillis();

        Class<?> tmp = null;

        try {
            tmp = Class.forName( "org.postgresql.Driver" );
        } catch ( Exception ex ) {
            tmp = null;
        }

        if ( tmp == null ) {
            msg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            msg.setText( "PostgreSQL Treiber konnte nicht geladen werden" );
        }

        Connection tstConn = null;
        try {
            if ( canNext( msg ) ) {
                // TODO
                StringBuffer sb = new StringBuffer( 150 );
                sb.append( "jdbc:postgresql://" ).append( config.getUrlServer() );
                sb.append( ":" ).append( config.getUrlPort() );
                String inst = config.getUrlInstance();
                // if ( inst != null && inst.indexOf( '.' ) != -1 || inst.length() > 16 ) {
                // // oracle service name
                // if ( inst.endsWith( "." ) ) {
                // // remove ending . (if it was used to force service name naming)
                // inst = inst.substring( 0, inst.length() - 1 );
                // }
                // sb.append( "/" ).append( inst );
                // } else {
                // classic oracle SID
                sb.append( "/" ).append( inst );
                // }
                try {
                    DriverManager.setLoginTimeout( ctConfig.getTimeout() );
                    tstConn = DriverManager.getConnection( sb.toString(), config.getAppUser(), config.getAppPass() );
                } catch ( Exception ex ) {
                    msg.setCode( Constants.RESULT_CODE_NOT_REACHABLE );
                    msg.setText( "Verbindungsparameter falsch oder DB nicht erreichbar.\nDetails: " + ex.getMessage() );
                }
            }

            PreparedStatement tstPstmt = null;
            if ( canNext( msg ) ) {
                try {
                    tstPstmt = tstConn.prepareStatement( config.getSqlCmd() );
                } catch ( Exception ex ) {
                    msg.setCode( Constants.RESULT_CODE_WRONG_PARAMETER );
                    msg.setText( "SQL-Kommando fehlerhaft.\nDetails: " + ex.getMessage() );
                }
            }

            if ( canNext( msg ) ) {
                try {
                    ResultSet rs = tstPstmt.executeQuery();
                    if ( !rs.next() )
                        throw new Exception( "SQL Kommando lieferte keine Daten zur\u00fcck." );

                    if ( rs.getObject( 1 ) == null )
                        throw new Exception( "SQL Kommando lieferte leere Zeile zur\u00fcck." );

                } catch ( SQLException ex ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                    msg.setText( "SQL-Fehler: " + ex.getMessage() );
                } catch ( Exception ex ) {
                    msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                    msg.setText( ex.getMessage() );
                }
            }
        } finally {
            closeSilentN( tstConn );
        }

        // handle result
        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            msgStatus = handleMsg( msg, start, stop, true );
        }
    }

}