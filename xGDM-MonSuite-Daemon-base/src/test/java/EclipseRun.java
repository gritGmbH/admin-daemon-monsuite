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
import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.controlling.MonSuiteInitializerListener;

public class EclipseRun {

    private static final Logger LOG = LoggerFactory.getLogger( EclipseRun.class );

    public static void main( String[] args ) {
        try {
            MonSuiteInitializerListener mil = new MonSuiteInitializerListener();
            ServletContextEvent dummy = null;

            mil.contextInitialized( dummy );

            wait( 60 * 10 );
            LOG.error( "********************************************************************************" );
            LOG.error( "********************************************************************************" );
            LOG.error( "********************************************************************************" );
            LOG.error( "*****                                                                      *****" );
            LOG.error( "*****                   DEV STARTER ENDED - STOPPING NOW                   *****" );
            LOG.error( "*****                                                                      *****" );
            LOG.error( "********************************************************************************" );
            LOG.error( "********************************************************************************" );
            LOG.error( "********************************************************************************" );

            mil.contextDestroyed( dummy );
        } catch ( Exception ex ) {
            LOG.error( "Exception", ex );
        }
    }

    private static void wait( int seconds ) {
        try {
            Thread.sleep( 1000L * seconds );
        } catch ( Exception ex ) {
            LOG.warn( "Waiting sleep of {}s interrupted with: {}", seconds, ex.getMessage() );
        }
    }
}