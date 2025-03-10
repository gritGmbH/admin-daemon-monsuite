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
package de.grit.gdi.daemon.monitoring;

import static de.grit.gdi.daemon.utils.Utilities.getIpAsString;
import static de.grit.vaadin.common.EbeanUtilsCommon.getProp;
import static java.lang.Thread.currentThread;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.avaje.ebean.Ebean;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.SensorFactory;
import de.grit.gdi.daemon.data.UnknownSensorTypeException;
import de.grit.gdi.daemon.data.config.AbstractSensorConfig;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.messaging.MessageQueue;
import de.grit.gdi.daemon.monitoring.sensor.IBasicSensor;
import de.grit.xgdm.monsuite.data.CheckTask;

/**
 * Quartz Bases Job Handler
 * 
 * @since X.Y
 * @author reichhelm
 *
 */
@DisallowConcurrentExecution
// PersistJobDataAfterExecution
public class CheckTaskJob implements InterruptableJob {

    public static final String JOB_GROUP = "MonSuiteJobs";

    public static final String JOB_NAME_PREFIX = "CT_";

    public static final String CHECKTASK_ID = "CHECKTASK_ID";

    public static final String CHECKTASK_VERSION = "CHECKTASK_VERSION";

    public static final String TRIGGER_VERSION = "TRIGGER_VERSION";

    public static final String TIMEOUT_SECONDS = "TIMEOUT_SEC";

    public static final String PAUSE_SECONDS = "PAUSE_SEC";

    private static final Logger LOG = LoggerFactory.getLogger( CheckTaskJob.class );

    private String host_ident;

    private Thread sleepingThread = null;

    private boolean isInterrupted = false;

    public CheckTaskJob() {
        host_ident = getProp( "cfg.bindto", getIpAsString() );
        LOG.debug( "Job for Ident: {}", host_ident );
    }

    @Override
    public void execute( JobExecutionContext context )
                            throws JobExecutionException {
        JobKey key = context.getJobDetail().getKey();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        if ( !dataMap.containsKey( CHECKTASK_ID ) ) {
            LOG.error( "Invalid Job Configuration not CheckTask set in JobDataMap" );
            return;
        }

        Long taskId = dataMap.getLongValue( CHECKTASK_ID );

        int sleepSec = 600; // default to 600 seconds => 5 minute
        if ( dataMap.containsKey( PAUSE_SECONDS ) )
            sleepSec = dataMap.getIntValue( PAUSE_SECONDS );

        int timeoutSec = 60; // Default to 60 seconds => 1 minute
        if ( dataMap.containsKey( TIMEOUT_SECONDS ) )
            sleepSec = dataMap.getIntValue( TIMEOUT_SECONDS );

        Thread run = null;
        try {
            MDC.put( "task", taskId.toString() );
            CheckTask ct = Ebean.find( CheckTask.class, taskId );
            if ( ct == null ) {
                LOG.info( "Checktask with id {} not found, removing", taskId );
                context.getScheduler().deleteJob( key );
                return;
            } else if ( !ct.isValid() ) {
                LOG.info( "Checktask {} ({}) is invalid, removing", taskId, ct.getName() );
                context.getScheduler().deleteJob( key );
                return;
            }

            // Recreate old style class/config
            String configType = ct.getSensor().getType();
            try {
                CheckTaskConfig tctc = new CheckTaskConfig();
                tctc.setTaskId( taskId.intValue() );
                tctc.setTimeout( timeoutSec );
                tctc.setPause( sleepSec );

                AbstractSensorConfig tbsc = SensorFactory.createConfig( configType );
                tbsc.load( ct );
                tctc.setSensor( tbsc );
                IBasicSensor sen = SensorFactory.createSensor( configType );
                sen.init( tctc );

                // run Job in sub thread
                run = new Thread( sen, currentThread().getName() + "-run" );
                run.start();

                run.join( timeoutSec * 1000 );

                if ( run.isAlive() ) {
                    stopThreadAndWarn( run, ct.getId().intValue(), true );
                    run = null;
                }
            } catch ( UnknownSensorTypeException ign ) {
                LOG.warn( "Unknown sensor type '{}', ignored", configType );
            }

            // Sleep
            if ( sleepSec > 0 && !isInterrupted ) {
                LOG.warn( "Job run complete, sleeping for {}s", sleepSec );
                try {
                    sleepingThread = Thread.currentThread();
                    Thread.sleep( 1000L * sleepSec );
                    LOG.trace( "Sleep complete" );
                } catch ( InterruptedException e ) {
                    LOG.warn( "Sleep interrupted" );
                } finally {
                    sleepingThread = null;
                }
            }
        } catch ( InterruptedException ire ) {
            LOG.warn( "Job got interrupted: {}", ire.getMessage() );
            LOG.trace( "InterruptedException", ire );
        } catch ( SchedulerException e ) {
            LOG.warn( "Job created Scheduler exception: {}", e.getMessage() );
            LOG.trace( "SchedulerException", e );
        } catch ( Exception ex ) {
            LOG.warn( "Job threw Exception: {}", ex.getMessage() );
            LOG.warn( "Exception in Job", ex );
        } finally {
            LOG.info( "Finished", key );
            MDC.remove( "task" );
        }
    }

    /**
     * Try to stop thread
     * <p>
     * ported from @see MonitoringWorker
     */
    @SuppressWarnings("deprecation")
    private void stopThreadAndWarn( Thread run, int checkTaskId, boolean first ) {

        if ( run == null )
            return;

        // try interrupt
        if ( run.isAlive() && !run.isInterrupted() ) {
            try {
                run.interrupt();

                try {
                    run.join( 10000 ); // wait 10 sek for termination
                } catch ( Exception exign ) {
                    // ignore
                }
            } catch ( Exception ex ) {
                // ignore
            }
        }

        // try stop
        if ( run.isAlive() ) {
            try {
                run.stop();

                try {
                    run.join( 10000 ); // wait 10 sek for termination
                } catch ( Exception exign ) {
                    // ignore
                }
            } catch ( Exception ex ) {
                // ignore
            }
        }

        if ( run.isAlive() ) {
            CheckTaskMessage cm = new CheckTaskMessage();
            cm.setHost( host_ident );
            cm.setCheckTaksId( checkTaskId );
            cm.setDuration( 0 );
            cm.setTerm( System.currentTimeMillis() );
            cm.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );

            if ( first )
                cm.setText( "WARNUNG: Der Sensor konnte nach dem Ablauf des Timeouts nicht gestoppt werden. (Operation blockiert)" );
            else
                cm.setText( "WARNUNG: Der Sensor laueft noch und konnte nicht gestoppt werden. (Operation blockiert)" );

            LOG.warn( "Thread Running ({})", cm.getText() );
            // MessageListner.getInstance().handleMessage( cm );
            MessageQueue.enqueue( cm );
        }
    }

    @Override
    public void interrupt()
                            throws UnableToInterruptJobException {
        isInterrupted = true;
        if ( sleepingThread != null ) {
            sleepingThread.interrupt();
        }
    }
}