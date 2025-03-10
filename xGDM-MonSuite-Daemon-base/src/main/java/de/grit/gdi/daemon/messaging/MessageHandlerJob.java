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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.message.SimpleMessage;
import de.grit.gdi.daemon.utils.Utilities;

@DisallowConcurrentExecution
// PersistJobDataAfterExecution
public class MessageHandlerJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger( MessageHandlerJob.class );

    public static final String JOB_GROUP = "MonSuiteCtl";

    public static final String JOB_NAME = "MessageHandler";

    @Override
    public void execute( JobExecutionContext context )
                            throws JobExecutionException {
        LOG.debug( "Starting Message processing ..." );
        ExecutorService executor = null;
        try {
            int timeoutSeconds = Utilities.getPropAsInt( "messagehandler.timeout", 30 );
            executor = Executors.newCachedThreadPool();
            SimpleMessage msg = null;
            while ( ( msg = MessageQueue.poll() ) != null ) {
                Future<?> future = executor.submit( new MessageRunner( msg ) );
                try {
                    future.get( timeoutSeconds, TimeUnit.SECONDS );
                } catch ( TimeoutException tx ) {
                    LOG.warn( "Timeout on handling message occurred, logging to logfile" );
                    logFailed( msg );
                } catch ( Exception ex ) {
                    LOG.warn( "Handling message was " );
                    logFailed( msg );
                }
            }
        } finally {
            if ( executor != null ) {
                executor.shutdownNow();
            }
        }

        LOG.debug( "Message processing finished" );
    }

    private void logFailed( SimpleMessage msg ) {
        // Failed Message to logfile
        Logger log = LoggerFactory.getLogger( "messages.failed" );
        log.error( msg != null ? msg.toString() : "-empty message-" );
    }
}