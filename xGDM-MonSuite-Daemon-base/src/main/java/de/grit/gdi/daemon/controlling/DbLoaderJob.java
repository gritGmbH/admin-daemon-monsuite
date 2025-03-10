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

import static de.grit.gdi.daemon.monitoring.CheckTaskJob.CHECKTASK_VERSION;
import static de.grit.gdi.daemon.utils.Utilities.getIpAsString;
import static de.grit.vaadin.common.EbeanUtilsCommon.getProp;
import static java.util.Collections.singleton;
import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.message.DaemonMessage;
import de.grit.gdi.daemon.messaging.MessageQueue;
import de.grit.gdi.daemon.monitoring.CheckTaskJob;
import de.grit.xgdm.monsuite.data.CheckTask;

@DisallowConcurrentExecution
// PersistJobDataAfterExecution
public class DbLoaderJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger( DbLoaderJob.class );

    public static final String JOB_GROUP = "MonSuiteCtl";

    public static final String JOB_NAME = "DB-Loader";

    private String host_ident;

    public DbLoaderJob() {
        host_ident = getProp( "cfg.bindto", getIpAsString() );
        LOG.debug( "Job for Ident: {}", host_ident );
    }

    @Override
    public void execute( JobExecutionContext context )
                            throws JobExecutionException {
        Scheduler scheduler = context.getScheduler();
        long starttime, endtime;
        starttime = System.currentTimeMillis();

        DaemonMessage dmsg = new DaemonMessage( DaemonMessage.MONITOR_CONTROLLER );
        dmsg.setHost( host_ident );

        try {
            Map<String, JobKey> jobNames = new HashMap<String, JobKey>();
            for ( JobKey jobKey : scheduler.getJobKeys( jobGroupEquals( CheckTaskJob.JOB_GROUP ) ) ) {
                LOG.trace( "Found monsuite job identified by: {}", jobKey.getName() );
                jobNames.put( jobKey.getName(), jobKey );
            }

            for ( CheckTask ct : Ebean.find( CheckTask.class ).findList() ) {
                if ( ct.getId() == null ) {
                    LOG.warn( "Invalid CheckTask with null id and name: {}, ignored", ct.getName() );
                    continue;
                } else if ( !ct.isValid() ) {
                    LOG.debug( "Checktask {} ({}) is invalid, ignored", ct.getId(), ct.getName() );
                    continue;
                }

                String name = CheckTaskJob.JOB_NAME_PREFIX + ct.getId().longValue();
                if ( jobNames.containsKey( name ) ) {
                    // check if trigger is up to date
                    LOG.trace( "Job with name {} already exists, checking for update", name );
                    JobDetail jd = scheduler.getJobDetail( jobNames.get( name ) );
                    JobDataMap dataMap = jd.getJobDataMap();

                    long trigVer = dataMap.containsKey( CHECKTASK_VERSION ) ? dataMap.getLongValue( CHECKTASK_VERSION )
                                                                            : Long.MIN_VALUE;
                    if ( trigVer == Long.MIN_VALUE || ct.getVersion() == null
                         || trigVer != ct.getVersion().longValue() ) {
                        LOG.info( "Job with name {} is changed, renewing (old: {}, new: {})", name, trigVer,
                                  ct.getVersion() );

                        try {
                            scheduler.interrupt( jobNames.get( name ) );
                        } catch ( Exception ire ) {
                            LOG.debug( "Running thread not interrupted: {}", ire.getMessage() );
                        }
                        addOrReplaceJob( scheduler, ct.getId(), ct );
                    }
                } else {
                    LOG.info( "Createing Job for {} ({})", ct.getId(), ct.getName() );
                    addOrReplaceJob( scheduler, ct.getId(), ct );
                }
            }

        } catch ( SchedulerException e ) {
            LOG.warn( "Error scheduling Job: {}", e.getMessage() );
            LOG.trace( "SchedulerException", e );
            dmsg.setCode( Constants.RESULT_CODE_FAILED_UNKNOWN );
            dmsg.setText( e.getMessage() );
        }

        endtime = System.currentTimeMillis();
        long duration = endtime - starttime;
        LOG.info( "Checking for new or changed jobs finished in {} ms ( {} s)", duration, duration / 1000 );
        dmsg.setTerm( endtime );
        dmsg.setDuration( (int) ( duration / 1000 ) );

        if ( dmsg.getCode() == Constants.RESULT_CODE_UNDEFIEND ) {
            // Wenn nichts passiert dann OK
            dmsg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
        }
        // MessageListner.getInstance().handleMessage( dmsg );
        MessageQueue.enqueue( dmsg );

        try {
            // TODO SLEEP
            Thread.sleep( 1000L * 15 );
        } catch ( InterruptedException ire ) {
            LOG.warn( "Got interrupted", ire.getMessage() );
            LOG.trace( "InterruptedException", ire );
        }
    }

    private void addOrReplaceJob( Scheduler scheduler, Long taskId, CheckTask ct )
                            throws SchedulerException {
        Date now = new Date();

        JobBuilder jb = newJob( CheckTaskJob.class );
        // TRICKY Do not remove class name for this block
        jb.withIdentity( CheckTaskJob.JOB_NAME_PREFIX + taskId, CheckTaskJob.JOB_GROUP );
        jb.usingJobData( CheckTaskJob.CHECKTASK_ID, taskId );
        jb.usingJobData( CheckTaskJob.PAUSE_SECONDS, ct.getPause() != null ? ct.getPause() : 600 );
        jb.usingJobData( CheckTaskJob.CHECKTASK_VERSION, ct.getVersion() );
        jb.withDescription( ct.getName() );

        JobDetail job = jb.build();

        DailyTimeIntervalScheduleBuilder isb = dailyTimeIntervalSchedule();

        Set<Integer> weekdays = getDaysOfWeek( ct );
        isb.onDaysOfTheWeek( weekdays );

        final TimeOfDay startDailyAt;
        if ( ct.getTimeStartMinutes() >= 0 ) {
            startDailyAt = getTimeOfDay( ct.getTimeStartMinutes() );
            isb.startingDailyAt( startDailyAt );
        } else {
            startDailyAt = TimeOfDay.hourMinuteAndSecondOfDay( 0, 0, 0 );
        }

        final TimeOfDay endDailyAt;
        if ( ct.getTimeEndMinutes() >= 0 ) {
            endDailyAt = getTimeOfDay( ct.getTimeEndMinutes() );
            isb.endingDailyAt( endDailyAt );
        } else {
            endDailyAt = TimeOfDay.hourMinuteAndSecondOfDay( 23, 59, 59 );
        }
        isb.withIntervalInSeconds( 15 );
        isb.withMisfireHandlingInstructionDoNothing();

        TriggerBuilder<Trigger> tb = newTrigger();
        tb.withSchedule( isb );

        // tb.forJob( job );
        tb.withIdentity( CheckTaskJob.JOB_NAME_PREFIX + taskId, CheckTaskJob.JOB_GROUP );
        tb.usingJobData( CheckTaskJob.TRIGGER_VERSION, ct.getVersion() );

        Date startAt;
        if ( ct.getDateStart() != null ) {
            startAt = startDailyAt.getTimeOfDayForDate( ct.getDateStart() );
            // for newer ebean with LocalDate
            // startAt = startDailyAt.getTimeOfDayForDate( Date.from( ct.getDateStart().atZone( ZoneId.systemDefault()
            // ).toInstant() ) );
            tb.startAt( startAt );
        } else {
            // TRICKY start today at first possible time
            Date today = new Date();
            startAt = startDailyAt.getTimeOfDayForDate( today );
            tb.startAt( startAt );
        }
        if ( startAt.compareTo( now ) < 0 ) {
            // TRICKY prevent startTime in the past, as Quartz does not allow it
            tb.startNow();
        }

        if ( ct.getDateEnd() != null ) {
            tb.endAt( endDailyAt.getTimeOfDayForDate( ct.getDateEnd() ) );
            // for newer ebean with LocalDate
            // tb.endAt( endDailyAt.getTimeOfDayForDate( Date.from( ct.getDateEnd().atZone( ZoneId.systemDefault()
            // ).toInstant() ) ) );
        } else {
            tb.endAt( null );
        }
        Trigger trig = tb.build();

        // add or replace job
        // LOG.info( "*** Replacing job {}", job.getKey() );
        scheduler.scheduleJob( job, singleton( trig ), true );
    }

    private Set<Integer> getDaysOfWeek( CheckTask ct ) {
        Set<Integer> daysAsSet = new HashSet<Integer>( 12 );

        if ( ct.isCheckday0Sun() )
            daysAsSet.add( Calendar.SUNDAY );
        if ( ct.isCheckday1Mon() )
            daysAsSet.add( Calendar.MONDAY );
        if ( ct.isCheckday2Tue() )
            daysAsSet.add( Calendar.TUESDAY );
        if ( ct.isCheckday3Wed() )
            daysAsSet.add( Calendar.WEDNESDAY );
        if ( ct.isCheckday4Thu() )
            daysAsSet.add( Calendar.THURSDAY );
        if ( ct.isCheckday5Fri() )
            daysAsSet.add( Calendar.FRIDAY );
        if ( ct.isCheckday6Sat() )
            daysAsSet.add( Calendar.SATURDAY );

        return daysAsSet;
    }

    private TimeOfDay getTimeOfDay( int minutes ) {
        int minute = minutes % 60;
        int hour = minutes / 60;

        return TimeOfDay.hourAndMinuteOfDay( hour, minute );
    }
}