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
package de.grit.gdi.daemon.messaging.handler;

import org.snmp4j.smi.OID;

public class SnmpGritConsts {
    /*
     * Groups
     */

    public static final OID grit = new OID( new int[] { 1, 3, 6, 1, 4, 1, 34512 } );

    public static final OID gritmgmt = new OID( grit.toIntArray(), new int[] { 1 } );

    public static final OID xgdmMonSuite = new OID( grit.toIntArray(), new int[] { 10 } );

    public static final OID xgdmMonSuiteTraps = new OID( xgdmMonSuite.toIntArray(), new int[] { 100 } );

    public static final OID xgdmMonSuiteNotify = new OID( xgdmMonSuite.toIntArray(), new int[] { 200 } );

    public static final OID gritmgmt_mtrapargs = new OID( gritmgmt.toIntArray(), new int[] { 3 } );

    /*
     * Datatypes
     */

    public static final OID gritmgmt_mtrapargs_checkTaskNr = new OID( gritmgmt_mtrapargs.toIntArray(), new int[] { 1 } );

    public static final OID gritmgmt_mtrapargs_resultCode = new OID( gritmgmt_mtrapargs.toIntArray(), new int[] { 2 } );

    public static final OID gritmgmt_mtrapargs_resultText = new OID( gritmgmt_mtrapargs.toIntArray(), new int[] { 3 } );

    public static final OID gritmgmt_mtrapargs_checkDuration = new OID( gritmgmt_mtrapargs.toIntArray(),
                                                                        new int[] { 4 } );

    public static final OID gritmgmt_mtrapargs_timeStamp = new OID( gritmgmt_mtrapargs.toIntArray(), new int[] { 5 } );

    /*
     * SNMPv2 Notifications
     */

    public static final OID notifyCheckSuccesResult = new OID( xgdmMonSuiteNotify.toIntArray(), new int[] { 1 } );

    public static final OID notifyCheckFailedResult = new OID( xgdmMonSuiteNotify.toIntArray(), new int[] { 2 } );

    private SnmpGritConsts() {
        // Only Constants
    }
}