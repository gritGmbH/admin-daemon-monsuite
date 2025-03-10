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

import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.OafSensorConfig;
import de.grit.gdi.daemon.monitoring.sensor.OafSensor;

/** Pr\u00fcft die OAFsensor: nur eine Pr\u00fcfung kann durchgef\u00fchrt werden */
public class OafSensorTester {

    public OafSensorTester() {
        OafSensorConfig wcfg = new OafSensorConfig();

        wcfg.setUrl( "https://ogc-api.nrw.de/lika/v1" );

        /** Erste Pr\u00fcfung */
        // wcfg.setCollectionidAvailable("flurstueck");

        /** Zweite Pr\u00fcfung */
        // wcfg.setCollectionidContent( "flurstueck_punkt|gebaeude_bauwerk" );

        /** Dritte Pr\u00fcfung */
        wcfg.setFeatureid( "{gebaeude_bauwerk}***REMOVED***|{nutzung}***REMOVED***" );

        wcfg.setId( 99 );
        wcfg.setName( "Tester" );
        wcfg.setType( "OAFBASIC" );

        CheckTaskConfig ccfg = new CheckTaskConfig();
        ccfg.setSensor( wcfg );
        ccfg.setTimeout( 60 );

        ccfg.setTaskId( 111 );

        OafSensor sen = new OafSensor();
        sen.init( ccfg );
        sen.run();

    }

    public static void main( String[] args ) {
        new OafSensorTester();

    }

}