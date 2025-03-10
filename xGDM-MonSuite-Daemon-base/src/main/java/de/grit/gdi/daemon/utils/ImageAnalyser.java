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

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.stream.ImageInputStream;

public class ImageAnalyser {
    private static Set<String> warnings = new HashSet<String>();

    private ImageAnalyser() {
    }

    public static synchronized ImageAnalyserResult analyse( InputStream is ) {
        IIOReadWarningListener iiorwarnlstnr = new IIOReadWarningListener() {
            public void warningOccurred( ImageReader imageReader, String str ) {
                // Meldung hinzuf\u00fcgen, sofern noch nicht vorhanden
                ImageAnalyser.warnings.add( str );
            }
        };

        ImageAnalyserResult res = new ImageAnalyserResult();
        res.setResult( true );
        try {
            // Create an image input stream on the image
            ImageInputStream iis = ImageIO.createImageInputStream( is );

            // Find all image readers that recognize the image format
            Iterator<ImageReader> iter = ImageIO.getImageReaders( iis );
            ImageReader reader = iter.next();

            // Listner f\u00fcr Warnungen registrieren
            reader.addIIOReadWarningListener( iiorwarnlstnr );

            // Bild mit Defaultparameter einlesen
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput( iis, true, true );

            BufferedImage img = reader.read( 0, param );
            res.setHeight( img.getHeight() );
            res.setWidth( img.getWidth() );
            img = null;

            // jpeg ==> jpg
            String imgformat = reader.getFormatName().toLowerCase();
            if ( imgformat.equalsIgnoreCase( "jpeg" ) )
                imgformat = "jpg";
            res.setImgtype( imgformat );

            reader.dispose();
            iis.close();
        } catch ( Exception ex ) {
            res.setMessage( ex.getMessage() );
            res.setResult( false );
            return res;
        }

        if ( ImageAnalyser.warnings.size() != 0 ) {
            Iterator<String> itr = ImageAnalyser.warnings.iterator();
            StringBuffer sb = new StringBuffer();
            while ( itr.hasNext() ) {
                if ( sb.length() > 0 )
                    sb.append( ", " );

                sb.append( itr.next() );
            }
            res.setMessage( sb.toString() );
            res.setResult( false );
        }

        return res;
    }
}