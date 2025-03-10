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
package de.grit.gdi.daemon.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class ExceptionAnalyserTest {

    @Test
    public void testValidXml()
                            throws Exception {
        String data = "<?xml version='1.0' encoding='UTF-8'?><ServiceExceptionReport>\r\n"
                      + "  <ServiceException code=\"StyleNotDefined\" locator=\"styles\">Message321.</ServiceException>\r\n"
                      + "</ServiceExceptionReport>";

        ExceptionAnalyser ea = new ExceptionAnalyser( data.getBytes() );
        assertTrue( ea.isException );
        assertEquals( "StyleNotDefined", ea.getCode() );
        assertEquals( "styles", ea.getLocator() );
        assertEquals( "Message321.", ea.getMessage() );
    }

    @Test
    public void testValidNoProcessing()
                            throws Exception {
        String data = "<ServiceExceptionReport>\r\n"
                      + "  <ServiceException code=\"StyleNotDefined\" locator=\"styles\">Message321.</ServiceException>\r\n"
                      + "</ServiceExceptionReport>";

        ExceptionAnalyser ea = new ExceptionAnalyser( data.getBytes() );
        assertTrue( ea.isException );
        assertEquals( "StyleNotDefined", ea.getCode() );
        assertEquals( "styles", ea.getLocator() );
        assertEquals( "Message321.", ea.getMessage() );
    }

    @Test
    public void testNoExceptionXML()
                            throws Exception {
        String data = "<?xml version='1.0' encoding='UTF-8'?><root><report>test</report></root>";

        ExceptionAnalyser ea = new ExceptionAnalyser( data.getBytes() );
        assertFalse( ea.isException );
        assertNull( ea.getCode() );
        assertNull( ea.getLocator() );
    }

    @Test
    public void testEmpty()
                            throws Exception {
        byte[] data = new byte[0];
        ExceptionAnalyser ea = new ExceptionAnalyser( data );
        assertFalse( ea.isException );
        assertNull( ea.getCode() );
        assertNull( ea.getLocator() );
    }

    @Test
    public void testPicture()
                            throws Exception {
        byte[] data = readClassResource( "/1px_white.png" );
        ExceptionAnalyser ea = new ExceptionAnalyser( data );
        assertFalse( ea.isException );
        assertNull( ea.getCode() );
        assertNull( ea.getLocator() );
    }

    private byte[] readClassResource( String name )
                            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream is = getClass().getResourceAsStream( name );
        byte[] buffer = new byte[4096]; // 4k
        int len = 0;

        while ( ( len = is.read( buffer ) ) > 0 ) {
            out.write( buffer, 0, len );
        }

        return out.toByteArray();
    }
}