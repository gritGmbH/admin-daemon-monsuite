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

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpMessageHandler implements SchedulerPlugin {

    private static final Logger LOG = LoggerFactory.getLogger( SnmpMessageHandler.class );

    private static Snmp snmp;

    public SnmpMessageHandler() {
    }

    @Override
    public void initialize( String name, Scheduler scheduler, ClassLoadHelper loadHelper )
                            throws SchedulerException {

    }

    @Override
    public void start() {
        LOG.info( "Starting SNMP Sender" );
    }

    @Override
    public void shutdown() {
        LOG.info( "Shutting down SNMP Sender" );
        try {
            if ( snmp != null ) {
                snmp.close();
            }
        } catch ( IOException e ) {
            LOG.warn( "Shutting down SNMP gave error: {}", e.getMessage() );
            LOG.trace( "IOException", e );
        }

        snmp = null;
    }

    public static synchronized boolean sendMessage( SnmpMessageParameter param ) {
        SnmpMessageParameter mp = param;

        try {
            // build snmp-pdu
            PDU pdu = null;

            if ( mp.getVersion() == SnmpMessageParameter.VERSION_V2C_NOTIFY )
                pdu = createTrapV2( mp.getCheckTaskNr(), mp.getResultCode(), mp.getResultText(), mp.getDuration(),
                                    mp.getTimeStamp() );
            else
                pdu = createTrapV1( mp.getCheckTaskNr(), mp.getResultCode(), mp.getResultText(), mp.getDuration(),
                                    mp.getTimeStamp() );

            // build target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity( new OctetString( mp.getComunity() ) );
            target.setAddress( GenericAddress.parse( mp.getDestination() ) );
            target.setTimeout( 2000 ); // 2 sekunden

            // check object, send message
            if ( snmp == null )
                snmp = new Snmp( new DefaultUdpTransportMapping() );

            snmp.listen();

            if ( pdu.getType() == PDU.V1TRAP ) {
                target.setVersion( SnmpConstants.version1 );
                snmp.trap( (PDUv1) pdu, target );
            } else {
                target.setVersion( SnmpConstants.version2c );
                snmp.notify( pdu, target );
            }

            return true;
        } catch ( Exception e ) {
            LOG.error( "SNMP Trap/Notify senden Fehlgeschlagen " + e.getMessage() );
        }

        return false;
    }

    private static PDUv1 createTrapV1( int checkTaskId, int resCode, String resTxt, int duration, long timeStamp ) {
        PDUv1 pdu = new PDUv1();
        pdu.setType( PDUv1.V1TRAP );

        pdu.setGenericTrap( PDUv1.ENTERPRISE_SPECIFIC );

        pdu.setEnterprise( SnmpGritConsts.xgdmMonSuiteTraps );

        // timestamp (Java-VM Uptime)
        pdu.setTimestamp( ManagementFactory.getRuntimeMXBean().getUptime() / 10L );

        if ( resCode == 1 ) {
            // OK
            // .0.1 => checkSuccesResult
            pdu.setSpecificTrap( 1 );

            // checktasknr
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkTaskNr, new Integer32( checkTaskId ) ) );

            // timestamp
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_timeStamp,
                                          new Counter32( timeStamp / 1000L ) ) );
        } else {
            // failed

            // .0.2 => checkFailedResult
            pdu.setSpecificTrap( 2 );

            // checktasknr
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkTaskNr, new Integer32( checkTaskId ) ) );

            // timestamp
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_timeStamp,
                                          new Counter32( timeStamp / 1000L ) ) );

            // resultCode
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_resultCode, new Integer32( resCode ) ) );

            // resultText
            String txt = ( resTxt == null || resTxt.length() == 0 ) ? "-" : resTxt;
            if ( txt.length() > 250 )
                txt = txt.substring( 0, 250 );

            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_resultText, new OctetString( txt ) ) );
        }

        // duration
        pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkDuration, new TimeTicks( duration * 100L ) ) );

        return pdu;
    }

    private static PDU createTrapV2( int checkTaskId, int resCode, String resTxt, int duration, long timeStamp ) {
        PDU pdu = new PDU();
        pdu.setType( PDU.NOTIFICATION );

        // timestamp (Java-VM Uptime)
        pdu.add( new VariableBinding( SnmpConstants.sysUpTime,
                                      new TimeTicks( ManagementFactory.getRuntimeMXBean().getUptime() / 10L ) ) );

        // 1256811778 s (heute)
        // 4294967295L

        if ( resCode == 1 ) {
            // OK

            // set Type/Enterprise
            pdu.add( new VariableBinding( SnmpConstants.snmpTrapOID, SnmpGritConsts.notifyCheckSuccesResult ) );

            // checktasknr
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkTaskNr, new Integer32( checkTaskId ) ) );

            // timestamp
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_timeStamp,
                                          new Counter32( timeStamp / 1000L ) ) );
        } else {
            // failed

            // set Type/Enterprise
            pdu.add( new VariableBinding( SnmpConstants.snmpTrapOID, SnmpGritConsts.notifyCheckFailedResult ) );

            // checktasknr
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkTaskNr, new Integer32( checkTaskId ) ) );

            // timestamp
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_timeStamp,
                                          new Counter32( timeStamp / 1000L ) ) );

            // resultCode
            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_resultCode, new Integer32( resCode ) ) );

            // resultText
            String txt = ( resTxt == null || resTxt.length() == 0 ) ? "-" : resTxt;
            if ( txt.length() > 250 )
                txt = txt.substring( 0, 250 );

            pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_resultText, new OctetString( txt ) ) );
        }

        // duration
        pdu.add( new VariableBinding( SnmpGritConsts.gritmgmt_mtrapargs_checkDuration, new TimeTicks( duration * 100L ) ) );

        return pdu;
    }
}