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
package de.grit.gdi.daemon.utils.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import de.grit.gdi.daemon.data.sensor.WFSCapabilities;
import de.grit.gdi.daemon.monitoring.sensor.WfsSensor;

public class WfsCapabilititesDefSaxHandlerTest {

    private WFSCapabilities parseWfsCapabilities( String name )
                            throws Exception {
        WfsSensor sen = new WfsSensor();
        Method m = WfsSensor.class.getDeclaredMethod( "parseWfsCapabilities", byte[].class );
        m.setAccessible( true );

        byte[] data = getBytesFromInputStream( getClass().getResourceAsStream( "/response/" + name ) );
        WFSCapabilities cap = (WFSCapabilities) m.invoke( sen, data );
        return cap;
    }

    @Test
    public void testCaseParsing()
                            throws Exception {
        WFSCapabilities cap = parseWfsCapabilities( "wfs_getcapabilities_110.xml" );
        List<String> fts = cap.getFeatureTypeNames();
        
        assertEquals( "version matches", "1.1.0", cap.getWfsVersion() );
        assertTrue( "contains upper case",
                    fts.contains( "{http://www.adv-online.de/namespaces/adv/gid/6.0/example}EXAMPLE_FLURSTUECK" ) );
        assertFalse( "does not contain lower case",
                     fts.contains( "{http://www.adv-online.de/namespaces/adv/gid/6.0/example}example_flurstueck" ) );
    }

    public static byte[] getBytesFromInputStream( InputStream is )
                            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for ( int len = is.read( buffer ); len != -1; len = is.read( buffer ) ) {
            os.write( buffer, 0, len );
        }
        return os.toByteArray();
    }

}