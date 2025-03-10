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

import java.util.Random;

/**
 * DOCUMENT_ME
 */
public class RandomTest {
    /** Nummerngenerator f\u00fcr Zufallszahlen */
    private Random rand = new Random();

    /**
     * Creates a new RandomTest object.
     */
    public RandomTest() {
        for ( int i = 0, j = 50; i < j; i++ ) {
            /*
             * if ( rand.nextBoolean() ) System.err.print("is + "); else System.err.print("is - ");
             * 
             * // 0.0 <= rand.nextFloat < 1.0 System.err.println("Random float="+rand.nextFloat()); //
             */
            float[] res = getRandomPosition( 400, 1000, 100, 100 );
            System.err.println( "res[0]=" + res[0] );
            System.err.println( "res[1]=" + res[1] );
        }
    }

    /**
     * ERmittelt eine Zuf\u00e4llige Posiotion f\u00fcr den Ausschnitt in der Box
     *
     * @param boxy
     *            Breite der zur Verf\u00fcgung stehenden Box
     * @param boxx
     *            H\u00f6he der zur Verf\u00fcgung stehenden Box
     * @param party
     *            Breit des Ausschnitts
     * @param partx
     *            H\u00f6he des Ausschnitts
     *
     * @return Y,X f\u00fcr die linke untere Ecke des Ausschnitts in der Box
     */
    public float[] getRandomPosition( float boxy, float boxx, float party, float partx ) {
        float randy = 0f;
        float randx = 0f;

        float qbby = boxy / 4;
        float qbbx = boxx / 4;

        // Bild gr\u00f6\u00dfer als 1/4 => keine verschiebung
        if ( ( party > qbby ) || ( partx > qbbx ) ) {
            return new float[] { randy, randx };
        }

        // Abweichung Y/X vom Mittelpunkt aus max. 0.99 * 1/4 Y/X
        float dy = rand.nextFloat() * qbby * ( rand.nextBoolean() ? 1 : ( -1 ) );
        float dx = rand.nextFloat() * qbbx * ( rand.nextBoolean() ? 1 : ( -1 ) );

        randy = ( 0 + ( boxy / 2 ) ) - ( party / 2 ) + dy;
        randx = ( 0 + ( boxx / 2 ) ) - ( partx / 2 ) + dx;

        return new float[] { randy, randx };
    }

    /**
     *
     * @param args
     */
    public static void main( String[] args ) {
        new RandomTest();
    }
}