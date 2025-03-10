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

import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests trigger
 * <p>
 * this tests have to be updated to run automatically.
 * especially because of day of week handling.
 * 
 * @author reichhelm
 */
public class TriggerTester {

    private static final Logger LOG = LoggerFactory.getLogger( TriggerTester.class );

    private Trigger trigAll;

    private long trigDateNow;
    
    private Trigger trigOnlyTime;

    private Set<Integer> getDaysOfWeek( boolean even ) {
        Set<Integer> daysAsSet = new HashSet<Integer>( 12 );

        if ( even ) {
            //
            daysAsSet.add( Calendar.TUESDAY );
            daysAsSet.add( Calendar.THURSDAY );
            daysAsSet.add( Calendar.SATURDAY );
        } else {
            // odd
            daysAsSet.add( Calendar.MONDAY );
            daysAsSet.add( Calendar.WEDNESDAY );
            daysAsSet.add( Calendar.FRIDAY );
            daysAsSet.add( Calendar.SUNDAY );
        }
        return daysAsSet;
    }

    public Date getDate( long time, int deltaDays, int hours, int minutes, int seconds ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date( time ) );
        cal.set( Calendar.HOUR_OF_DAY, hours );
        cal.set( Calendar.MINUTE, minutes );
        cal.set( Calendar.SECOND, seconds );
        cal.add( Calendar.DAY_OF_YEAR, deltaDays );
        return cal.getTime();
    }

    @Before
    public void buildTrigger1() {
        DailyTimeIntervalScheduleBuilder isb = dailyTimeIntervalSchedule();
        trigDateNow = new Date().getTime();

        // Mo, Mi, Fr, So
        Set<Integer> weekdays = getDaysOfWeek( false );
        isb.onDaysOfTheWeek( weekdays );
        // 07:00 - 23:59
        isb.startingDailyAt( TimeOfDay.hourAndMinuteOfDay( 7, 0 ) );
        isb.endingDailyAt( TimeOfDay.hourAndMinuteOfDay( 23, 59 ) );
        isb.withIntervalInSeconds( 15 );
        isb.withMisfireHandlingInstructionIgnoreMisfires();

        TriggerBuilder<Trigger> tb = newTrigger();
        tb.withSchedule( isb );

        //tb.startAt( getDate( trigDateNow, -3, 0, 0, 0 ) );
        tb.startAt( new Date( 0L ) );

        //tb.endAt( getDate( trigDateNow, +3, 23, 59, 59 ) );
        tb.endAt( new Date( Long.MAX_VALUE ) );

        trigOnlyTime = tb.build();
    }
    
    @Before
    public void buildTrigger2() {
        DailyTimeIntervalScheduleBuilder isb = dailyTimeIntervalSchedule();
        trigDateNow = new Date().getTime();

        // Mo, Mi, Fr, So
        Set<Integer> weekdays = getDaysOfWeek( false );
        isb.onDaysOfTheWeek( weekdays );
        // 07:00 - 23:59
        isb.startingDailyAt( TimeOfDay.hourAndMinuteOfDay( 7, 0 ) );
        isb.endingDailyAt( TimeOfDay.hourAndMinuteOfDay( 23, 59 ) );
        isb.withIntervalInSeconds( 15 );
        isb.withMisfireHandlingInstructionIgnoreMisfires();

        TriggerBuilder<Trigger> tb = newTrigger();
        tb.withSchedule( isb );

        tb.startAt( getDate( trigDateNow, -3, 0, 0, 0 ) );
        // tb.startAt( new Date( 0L ) );

        tb.endAt( getDate( trigDateNow, +3, 23, 59, 59 ) );
        // tb.endAt( new Date( Long.MAX_VALUE ) );

        trigAll = tb.build();
    }

    @Test
    @Ignore
    public void testScheduling() {
        LOG.info( "Trigger: {}", trigAll );
        LOG.info( "PrevFireTime: {}", trigAll.getPreviousFireTime() );
        LOG.info( "NextFireTime: {}", trigAll.getNextFireTime() );

        Date testDate = getDate( trigDateNow, 0, 12, 0, 0 );
        Date nextDate = trigAll.getFireTimeAfter( testDate );
        LOG.info( "Test: {}", testDate );
        LOG.info( "Next: {}", nextDate );

        Date test1Date = getDate( trigDateNow, +1, 12, 0, 0 );
        Date next1Date = trigAll.getFireTimeAfter( test1Date );
        LOG.info( "Test: {}", test1Date );
        LOG.info( "Next: {}", next1Date );

        Date afterDate = getDate( trigDateNow, 4, 0, 0, 0 );
        Date expDate = trigAll.getFireTimeAfter( afterDate );
        LOG.info( "After: {}", afterDate );
        LOG.info( "Test: {}", expDate );

        Date beforeDate = getDate( trigDateNow, -4, 0, 0, 0 );
        Date firstDate = trigAll.getFireTimeAfter( beforeDate );
        LOG.info( "Before: {}", beforeDate );
        LOG.info( "Test: {}", firstDate );
    }
    
    @Test
    @Ignore
    public void testSchedulingOnlyTime() {
        LOG.info( "Trigger: {}", trigOnlyTime );
        LOG.info( "PrevFireTime: {}", trigOnlyTime.getPreviousFireTime() );
        LOG.info( "NextFireTime: {}", trigOnlyTime.getNextFireTime() );

        Date testDate = getDate( trigDateNow, 0, 12, 0, 0 );
        Date nextDate = trigOnlyTime.getFireTimeAfter( testDate );
        LOG.info( "Test: {}", testDate );
        LOG.info( "Next: {}", nextDate );

        Date test1Date = getDate( trigDateNow, +1, 12, 0, 0 );
        Date next1Date = trigOnlyTime.getFireTimeAfter( test1Date );
        LOG.info( "Test: {}", test1Date );
        LOG.info( "Next: {}", next1Date );

        Date afterDate = getDate( trigDateNow, 4, 0, 0, 0 );
        Date expDate = trigOnlyTime.getFireTimeAfter( afterDate );
        LOG.info( "After: {}", afterDate );
        LOG.info( "Test: {}", expDate );

        Date beforeDate = getDate( trigDateNow, -4, 0, 0, 0 );
        Date firstDate = trigOnlyTime.getFireTimeAfter( beforeDate );
        LOG.info( "Before: {}", beforeDate );
        LOG.info( "Test: {}", firstDate );
    }
}