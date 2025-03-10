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
import de.grit.gdi.daemon.data.config.WfsSensorConfig;
import de.grit.gdi.daemon.monitoring.sensor.WfsSensor;

public class WfsSensorTester {
    public WfsSensorTester() {
        WfsSensorConfig wcfg = new WfsSensorConfig();

        // HTTP-User/Password

        // wcfg.setHttpUser( "auskunft" );
        // wcfg.setHttpPass( "***REMOVED***" );

        wcfg.setUrl( "http://***REMOVED***/***REMOVED***/services/wfs-t_test?SERVICE=WFS&REQUEST=GetCapabilities&VERSION=1.1." );
        // wcfg.setFeatureCapabilities("{http://www.***REMOVED***.demo.grit.de/***REMOVED***}alb");
        // wcfg.setFeatureGetFeature("{http://www.***REMOVED***.demo.grit.de/***REMOVED***}alb");

        // wcfg.setFeatureCapabilities( "{http://***REMOVED***/***REMOVED***}blattschnitt_tk25" );
        // wcfg.setFeatureGetFeature( "{http://***REMOVED***/***REMOVED***}blattschnitt_tk25" );

        // Dummy
        wcfg.setId( 99 );
        wcfg.setName( "Tester" );
        wcfg.setType( "WFSBASIC" );

        CheckTaskConfig ccfg = new CheckTaskConfig();
        ccfg.setSensor( wcfg );
        ccfg.setTimeout( 60 );

        // Dummy
        ccfg.setTaskId( 111 );

        WfsSensor sen = new WfsSensor();
        sen.init( ccfg );
        sen.run();
    }

    public static void main( String[] args ) {
        new WfsSensorTester();
    }

}