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
package test.gdi.daemon;

import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.messaging.MessageQueue;

/**
 * DOCUMENT ME
 */
public class MessageManagerPoolTest {
    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        // TODO: remove Debugging mains

        System.out.println( "starteed" );

        for ( int i = 0, j = 2; i < j; i++ ) {
            CheckTaskMessage sm = new CheckTaskMessage();
            sm.setCheckTaksId( 3 );
            sm.setCode( i + 1 );
            sm.setTerm( System.currentTimeMillis() );
            sm.setDuration( 15 * 1 );
            sm.setHost( "Tester123" );
            sm.setText( "" );

            System.out.println( MessageQueue.enqueue( sm ) );
            try {
                Thread.sleep( 1000 * 2 );
            } catch ( InterruptedException ex ) {
            }

        }

        /*
         * try { Thread.sleep(1000 * 15); } catch (InterruptedException ex) { } System.out.println("last one");
         * System.out.println(mmp.enqueue(new StandardMesage()));
         */

        try {
            Thread.sleep( 1000 * 30 );
            Thread.sleep( 1000 * 2 );
        } catch ( InterruptedException ex ) {
        }

        System.out.println( "complete" );
    }
}