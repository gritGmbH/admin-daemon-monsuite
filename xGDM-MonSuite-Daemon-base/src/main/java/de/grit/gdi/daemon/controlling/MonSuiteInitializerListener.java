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
package de.grit.gdi.daemon.controlling;

import static de.grit.gdi.daemon.utils.SchedulerHolder.CONTEXT_KEY;
import static de.grit.gdi.daemon.utils.Utilities.getPropAsInt;
import static de.grit.gdi.daemon.utils.Utilities.readPropertiesWithPrefix;
import static de.grit.vaadin.common.EbeanUtilsCommon.getProp;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.messaging.MessageHandlerJob;
import de.grit.gdi.daemon.utils.SchedulerHolder;
import de.grit.gdi.daemon.utils.Utilities;

public class MonSuiteInitializerListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger( MonSuiteInitializerListener.class );

    private static final int QUARTZ_MAX_PRIORTIY = 10;

    private static final int QUARTZ_2ND_PRIORTIY = 9;

    private boolean performShutdown = true;

    private boolean waitOnShutdown = false;

    private Scheduler scheduler = null;

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        LOG.info( "Monitoring Suite {} loaded, initializing Scheduler...",
                  Utilities.getPomVersion( Utilities.POM_BASE_GROUPID, Utilities.POM_BASE_ARTIFACTID ) );

        try {
            // Loading defaults
            performShutdown = "true".equalsIgnoreCase( getProp( "quartz.shutdown.perfom", "true" ) );
            waitOnShutdown = "true".equalsIgnoreCase( getProp( "quartz.shutdown.wait", "false" ) );

            Properties prop = readPropertiesWithPrefix( "quartz." );
            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize( prop );
            scheduler = sf.getScheduler();
            if ( sce != null && sce.getServletContext() != null ) {
                try {
                    sce.getServletContext().setAttribute( CONTEXT_KEY, new SchedulerHolder( scheduler ) );
                } catch ( Exception ign ) {
                    // ignored
                }
            }

            if ( "false".equalsIgnoreCase( getProp( "quartz.dbloader.autostart", "true" ) ) ) {
                LOG.warn( "Startup of DB-Loader job was disabled !" );
            } else {
                scheduleDbLoaderJob();
            }

            if ( "false".equalsIgnoreCase( getProp( "quartz.msghandler.autostart", "true" ) ) ) {
                LOG.warn( "Startup of Message job was disabled !" );
            } else {
                scheduleMessageJob();
            }

            int delay = getPropAsInt( "quartz.startup.delay", -1 );
            if ( delay > 3600 ) {
                LOG.warn( "Limited startup delay to 3600s was: {}s", delay );
                delay = 3600;
            }

            if ( delay > 0 ) {
                scheduler.startDelayed( delay );
                LOG.info( "Scheduler delayed for {}s", delay );
            } else {
                scheduler.start();
                LOG.info( "Scheduler started" );
            }

            LOG.info( "Initialisation complete" );
        } catch ( Exception sx ) {
            LOG.error( "Failed to initialize MonSuite", sx );
            throw new RuntimeException( "Failed to initialize MonSuite, see logs." );
        }
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
        LOG.info( "Monitoring Suite is shutting down ..." );

        if ( sce != null && sce.getServletContext() != null ) {
            try {
                sce.getServletContext().removeAttribute( CONTEXT_KEY );
            } catch ( Exception ign ) {
                // ignored
            }
        }

        if ( !performShutdown ) {
            return;
        }

        if ( waitOnShutdown ) {
            interruptRunningJobs();
        }
        try {
            if ( scheduler != null ) {
                scheduler.shutdown( waitOnShutdown );
            }
        } catch ( Exception e ) {
            LOG.error( "Quartz Scheduler failed to shutdown cleanly: {}", e.getMessage() );
            LOG.error( "Stacktrace", e );
        }
        if ( !waitOnShutdown ) {
            interruptRunningJobs();
        }

        LOG.info( "Monitoring Suite,Scheduler successful shutdown." );
    }

    private void interruptRunningJobs() {
        if ( scheduler == null )
            return;

        LOG.debug( "Trying to interrupt current jobs" );
        try {
            for ( JobExecutionContext jec : scheduler.getCurrentlyExecutingJobs() ) {
                JobDetail jd = jec.getJobDetail();
                try {
                    scheduler.interrupt( jd.getKey() );
                } catch ( Exception iex ) {
                    LOG.info( "Job {} could not be interrupted", jd.getKey() );
                }
            }
        } catch ( Exception ex ) {
            LOG.info( "Interrupting currently running jobs failed: ", ex.getMessage() );
            LOG.trace( "Exception", ex );
        }
    }

    private void scheduleDbLoaderJob()
                            throws SchedulerException {
        JobBuilder jb = newJob( DbLoaderJob.class );
        jb.withIdentity( DbLoaderJob.JOB_NAME, DbLoaderJob.JOB_GROUP );
        jb.withDescription( "Database Loader Job" );
        JobDetail job = jb.build();

        SimpleScheduleBuilder ssb = simpleSchedule();
        ssb.withIntervalInSeconds( getPropAsInt( "quartz.dbloader.interval", 30 ) );

        ssb.repeatForever();
        ssb.withMisfireHandlingInstructionNextWithExistingCount();

        Trigger trig = newTrigger().forJob( job ).withSchedule( ssb ) //
        .withPriority( QUARTZ_2ND_PRIORTIY ).startNow().build();

        scheduler.scheduleJob( job, trig );
    }

    private void scheduleMessageJob()
                            throws SchedulerException {
        JobBuilder jb = newJob( MessageHandlerJob.class );
        jb.withIdentity( MessageHandlerJob.JOB_NAME, MessageHandlerJob.JOB_GROUP );
        jb.withDescription( "Message Sender Job" );
        JobDetail job = jb.build();

        SimpleScheduleBuilder ssb = simpleSchedule();
        ssb.withIntervalInSeconds( getPropAsInt( "quartz.msghandler.interval", 5 ) );

        ssb.repeatForever();
        ssb.withMisfireHandlingInstructionNextWithExistingCount();

        Trigger trig = newTrigger().forJob( job ).withSchedule( ssb ) //
        .withPriority( QUARTZ_MAX_PRIORTIY ).startNow().build();

        scheduler.scheduleJob( job, trig );
    }
}