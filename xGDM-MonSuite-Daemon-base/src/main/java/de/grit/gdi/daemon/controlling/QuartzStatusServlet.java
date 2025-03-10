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
import static de.grit.gdi.daemon.monitoring.CheckTaskJob.TRIGGER_VERSION;
import static de.grit.gdi.daemon.utils.SchedulerHolder.CONTEXT_KEY;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.utils.SchedulerHolder;
import de.grit.gdi.daemon.utils.Utilities;

public class QuartzStatusServlet extends HttpServlet {

    private static final long serialVersionUID = -4541932627491293991L;

    private static final String NL = "\n";

    private static final Logger LOG = LoggerFactory.getLogger( QuartzStatusServlet.class );

    @Override
    protected void doPost( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        handle( req, resp );
    }

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp )
                            throws ServletException, IOException {
        handle( req, resp );
    }

    private void handle( HttpServletRequest req, HttpServletResponse resp )
                            throws IOException {
        resp.setContentType( "text/plain" );
        PrintWriter w = resp.getWriter();
        w.append( "Monitoring Suite Daemon v" );
        w.append( Utilities.getPomVersion( Utilities.POM_BASE_GROUPID, Utilities.POM_BASE_ARTIFACTID ) );
        w.append( NL ).append( NL );

        Date currentTime = new Date();
        Object o = getServletContext().getAttribute( CONTEXT_KEY );
        Scheduler s = null;
        if ( o instanceof SchedulerHolder )
            s = ((SchedulerHolder) o).getScheduler();

        String token = req.getParameter( "token" );

        if ( s != null && "snoopy".equalsIgnoreCase( token ) ) {
            try {
                w.append( s.getMetaData().getSummary() );
                w.append( NL );

                w.append( "Running:" ).append( NL );
                w.append( "^^^^^^^^" ).append( NL );

                for ( JobExecutionContext ctx : s.getCurrentlyExecutingJobs() ) {
                    w.append( "  " + ctx.getJobDetail().getKey() ).append( NL );
                }

                w.append( NL );
                w.append( "All Jobs:" ).append( NL );
                w.append( "^^^^^^^^^" ).append( NL );
                for ( JobKey jobKey : s.getJobKeys( GroupMatcher.anyJobGroup() ) ) {
                    w.append( "Job: " + jobKey );
                    JobDetail jd = s.getJobDetail( jobKey );
                    if ( jd.getDescription() != null ) {
                        w.append( "(" ).append( jd.getDescription() ).append( ")" );
                    }
                    if ( jd.getJobDataMap().containsKey( CHECKTASK_VERSION  ) ) {
                        w.append( " v" + jd.getJobDataMap().getLong( CHECKTASK_VERSION ) );
                    }
                    w.append( NL );
                    for ( Trigger t : s.getTriggersOfJob( jobKey ) ) {
                        w.append( "   [T] Status: " + s.getTriggerState( t.getKey() ) );
                        if ( t.getDescription() != null ) {
                            w.append( "(" ).append( t.getDescription() ).append( ")" );
                        }
                        if ( t.getJobDataMap().containsKey( TRIGGER_VERSION  ) ) {
                            w.append( " v" + t.getJobDataMap().getLong( TRIGGER_VERSION ) );
                        }
                        w.append( NL );
                        w.append( "       PrevFireTime:   " + t.getPreviousFireTime() ).append( NL );
                        w.append( "       NextFireTime:   " + t.getNextFireTime() ).append( NL );
                        w.append( "       FireTimeAfter:  " + t.getFireTimeAfter( currentTime ) ).append( NL );
                        w.append( NL );
                    }
                }
            } catch ( SchedulerException e ) {
                w.append( "Failed to print Status: " ).append( e.getMessage() );
                LOG.info( "Failed to response with status: {}", e.getMessage() );
            }
        } else if ( s != null ) {
            try {
                w.append( "Status: " );
                if ( s.isStarted() )
                    w.append( "STARTED" );
                else if ( s.isShutdown() )
                    w.append( "SHUTDOWN" );
                else if ( s.isInStandbyMode() )
                    w.append( "STANDBY" );
                else
                    w.append( "UNKNOWN" );
                w.append( NL );

            } catch ( SchedulerException e ) {
                w.append( "Failed to print Status: " ).append( e.getMessage() );
                LOG.info( "Failed to response with status: {}", e.getMessage() );
            }
        } else {
            w.append( "- no scheduler found -" );
        }

        w.append( NL ).append( NL ).append( "Finished at " ).append( ( new Date() ).toString() );

    }
}