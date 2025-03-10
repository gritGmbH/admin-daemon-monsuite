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
package test.gdi.daemon.sensor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketTester {
    private static final Logger LOG = LoggerFactory.getLogger( SocketTester.class.getName() );

    public SocketTester() {
        LOG.info( SocketTester.class.getName() + " Started" );
    }

    public long test( String destAddr, int destPort ) {
        long res = -1;

        Socket so = new Socket();

        try {
            SocketAddress addr = new InetSocketAddress( destAddr, destPort );
            long start = System.currentTimeMillis();
            so.connect( addr, 100 * 10 ); // 10 ms timeout
            long end = System.currentTimeMillis();
            res = end - start;
        } catch ( SocketTimeoutException e1 ) {
            LOG.warn( "SocketTimeout", e1 );
            res = -2;
        } catch ( UnknownHostException e2 ) {
            LOG.warn( "UnknownHost", e2 );
            res = -3;
        } catch ( IOException e3 ) {
            LOG.warn( "IO", e3 );
            res = -4;
        } finally {
            try {
                so.close();
            } catch ( Exception e4 ) {
                LOG.warn( "Unable to close", e4 );
            }
        }

        // so.connect()
        return res;
    }

    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        SocketTester sT = new SocketTester();
        LOG.info( "duration=" + sT.test( "127.0.0.1", 2013 ) );
    }
}