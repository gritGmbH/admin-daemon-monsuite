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
package de.grit.gdi.daemon;

import static de.grit.gdi.daemon.utils.Utilities.readPropertiesWithPrefix;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.controlling.DbLoaderJob;
import de.grit.gdi.daemon.monitoring.CheckTaskJob;

public class QuarzStarter {

    private static final Logger LOG = LoggerFactory.getLogger( QuarzStarter.class );

    private Scheduler scheduler;

    @Test
    @Ignore
    public void testScheduling()
                            throws Exception {

        Properties prop = readPropertiesWithPrefix( "quartz." );
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize( prop );
        scheduler = sf.getScheduler();
        scheduler.start();

        JobBuilder jb1 = newJob( CheckTaskJob.class );
        jb1.withIdentity( CheckTaskJob.JOB_NAME_PREFIX + 1, CheckTaskJob.JOB_GROUP );
        jb1.usingJobData( CheckTaskJob.CHECKTASK_ID, 1L );
        jb1.usingJobData( CheckTaskJob.PAUSE_SECONDS, 30L );
        jb1.withDescription( "Job 1 Name" );
        JobDetail job1 = jb1.build();

        JobBuilder jb2 = newJob( CheckTaskJob.class );
        jb2.withIdentity( CheckTaskJob.JOB_NAME_PREFIX + 2, CheckTaskJob.JOB_GROUP );
        jb2.usingJobData( CheckTaskJob.CHECKTASK_ID, 2L );
        jb2.usingJobData( CheckTaskJob.PAUSE_SECONDS, 5L );
        jb2.withDescription( "Job 1 Name" );
        JobDetail job2 = jb2.build();

        SimpleScheduleBuilder ssb1 = simpleSchedule();
        ssb1.withIntervalInSeconds( 15 );
        ssb1.repeatForever();
        ssb1.withMisfireHandlingInstructionNextWithExistingCount();

        Trigger trig1 = newTrigger().forJob( job1 ).withSchedule( ssb1 ).startNow().build();

        SimpleScheduleBuilder ssb2 = simpleSchedule();
        ssb2.withIntervalInSeconds( 15 );
        ssb2.repeatForever();
        ssb2.withMisfireHandlingInstructionNextWithExistingCount();

        Trigger trig2 = newTrigger().forJob( job2 ).withSchedule( ssb2 ).startNow().build();

        scheduler.scheduleJob( job1, trig1 );
        scheduler.scheduleJob( job2, trig2 );

        listJobs();
        wait( 60 * 3 );

        // shutdown() does not return until executing Jobs complete execution
        scheduler.shutdown( true );
    }

    private void wait( int seconds ) {
        try {
            Thread.sleep( 1000L * seconds );
        } catch ( Exception ex ) {
            LOG.warn( "Waiting sleep of {}s interrupted with: {}", seconds, ex.getMessage() );
        }
    }

    private void listJobs()
                            throws SchedulerException {
        // List Monitoring Suite Jobs
        for ( JobKey jobKey : scheduler.getJobKeys( jobGroupEquals( CheckTaskJob.JOB_GROUP ) ) ) {
            LOG.info( "Found job identified by: {}", jobKey );
        }
    }

    @Test
    @Ignore
    public void testStartDBLoader()
                            throws Exception {
        Properties prop = readPropertiesWithPrefix( "quartz." );
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize( prop );
        scheduler = sf.getScheduler();
        scheduler.start();

        JobBuilder jb = newJob( DbLoaderJob.class );
        jb.withIdentity( DbLoaderJob.JOB_NAME, DbLoaderJob.JOB_GROUP );
        jb.withDescription( "Database Loader Job" );
        JobDetail job = jb.build();

        SimpleScheduleBuilder ssb = simpleSchedule();
        ssb.withIntervalInSeconds( 30 );
        ssb.repeatForever();
        ssb.withMisfireHandlingInstructionNextWithExistingCount();

        Trigger trig = newTrigger().forJob( job ).withSchedule( ssb ).startNow().build();

        scheduler.scheduleJob( job, trig );

        wait( 60 * 3 );
        LOG.error( "********************************************************************************" );
        LOG.error( "********************************************************************************" );
        LOG.error( "*****                                                                      *****" );
        LOG.error( "*****                   DEV STARTER ENDED - STOPPING NOW                   *****" );
        LOG.error( "*****                                                                      *****" );
        LOG.error( "********************************************************************************" );
        LOG.error( "********************************************************************************" );
        LOG.error( "********************************************************************************" );
        // shutdown() does not return until executing Jobs complete execution
        scheduler.shutdown( true );
    }
}