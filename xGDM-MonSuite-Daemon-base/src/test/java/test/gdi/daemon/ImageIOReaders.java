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

import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class ImageIOReaders {
    /**
     * Creates a new ImageIOReaders object.
     */
    public ImageIOReaders() {
        String[] formatNames = ImageIO.getReaderFormatNames();
        formatNames = unique( formatNames );

        // e.g. png jpeg gif jpg
        for ( int i = 0, j = formatNames.length; i < j; i++ ) {
            System.out.println( i + " - " + formatNames[i] );
        }

    }

    // Converts all strings in 'strings' to lowercase
    // and returns an array containing the unique values.
    // All returned values are lowercase.
    public static String[] unique( String[] strings ) {
        Set<String> set = new HashSet<String>();

        for ( int i = 0; i < strings.length; i++ ) {
            String name = strings[i].toLowerCase();
            set.add( name );
        }

        return (String[]) set.toArray( new String[0] );
    }

    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        new ImageIOReaders();
    }
}