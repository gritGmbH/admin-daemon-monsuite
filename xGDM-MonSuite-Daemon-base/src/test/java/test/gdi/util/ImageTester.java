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
package test.gdi.util;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import de.grit.gdi.daemon.utils.HttpWrapper;
import de.grit.gdi.daemon.utils.ImageAnalyser;
import de.grit.gdi.daemon.utils.ImageAnalyserResult;

public class ImageTester {
    public ImageTester( String url, String user, String pass, int timeout ) {
        HttpWrapper httpwrp = new HttpWrapper( timeout, null );

        // Optionale Authentifizierung setzen
        if ( ( user != null ) && ( user.length() > 0 ) && ( pass != null ) && ( pass.length() > 0 ) ) {
            httpwrp.setAuth( user, pass );
        }

        int biresult = httpwrp.doGet( url );
        System.out.println( "GET Result=" + biresult );

        byte[] idata = httpwrp.getLastData();

        InputStream is = new ByteArrayInputStream( idata );
        ImageAnalyserResult ires = ImageAnalyser.analyse( is );

        System.out.println( "ImageAnalyserResult" );
        System.out.println( "Result=" + ires.isResult() );
        System.out.println( "Gr\u00f6\u00dfe=" + ires.getWidth() + "x" + ires.getHeight() );
        System.out.println( "Message=" + ires.getMessage() );
        System.out.println( "Type=" + ires.getImgtype() );

        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance( "MD5" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        byte[] hash = md.digest( idata );

        String passwordHash = null;

        try {
            BigInteger bihash = new BigInteger( 1, hash );
            passwordHash = bihash.toString( 16 );
            System.out.println( "MD5Sum=" + passwordHash );
        } catch ( Exception e ) {
            System.out.println( "MD5Sum Fehler: " + e.getMessage() );
        }

        // Write to File
        try {
            String filename;

            synchronized ( this ) {
                filename = "c:\\image_tester_" + System.currentTimeMillis() + "." + ires.getImgtype();
            }

            is.reset();

            OutputStream out = new FileOutputStream( filename );

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ( ( len = is.read( buf ) ) > 0 ) {
                out.write( buf, 0, len );
            }

            is.close();
            out.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length != 4 ) {
            System.out.println( "Parameters: URL HTTP-User HTTP-Passwort Timeout" );
            System.exit( 5 );
        }

        int timeout = 30;

        try {
            timeout = Integer.parseInt( args[3] );
        } catch ( Exception e ) {
            System.out.println( "Timeout not numeric (" + e.getMessage() + ")" );
            System.exit( 5 );
        }

        // Process each parameter
        for ( int i = 0; i < args.length; i++ ) {
            System.out.println( i + " -> " + args[i] );
        }

        // ImageAnalyser.addWarning("Test");
    }
}