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

import de.grit.gdi.daemon.data.config.ArcGisSensorConfig;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.monitoring.sensor.ArcGisSensor;

public class ArcGisSensorTester {

    public ArcGisSensorTester() {

        ArcGisSensorConfig cfg = new ArcGisSensorConfig();

        cfg.setUrl( "http://***REMOVED***/***REMOVED***/rest/services" );
        cfg.setServiceName( "WO_Standard" );
        // cfg.setLayerName("Querschnitte");
        cfg.setLayerName( "Abc" );
        cfg.setBbox( "1,2,3,4" );
        cfg.setImgSize( "400" );

        // Dummy
        cfg.setId( 99 );
        cfg.setName( "Tester" );
        cfg.setType( "ARCGIS" );

        CheckTaskConfig ccfg = new CheckTaskConfig();
        ccfg.setSensor( cfg );
        ccfg.setTimeout( 60 );

        // Dummy
        ccfg.setTaskId( 111 );

        ArcGisSensor sen = new ArcGisSensor();
        sen.init( ccfg );
        sen.run();
    }

    /**
     * 
     * @param args
     */
    public static void main( String[] args ) {
        new ArcGisSensorTester();
    }

}