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

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.WmsSensorConfig;
import de.grit.gdi.daemon.data.message.SimpleMessage;
import de.grit.gdi.daemon.messaging.MessageQueue;
import de.grit.gdi.daemon.monitoring.sensor.WmsSensor2;

public class WmsTestImage {

    private static final Logger LOG = LoggerFactory.getLogger( WmsTestImage.class );

    private void runLoop( WmsSensorConfig config ) {
        CheckTaskConfig ccfg = new CheckTaskConfig();
        ccfg.setSensor( config );
        ccfg.setTimeout( 6 );
        ccfg.setTaskId( 111 );

        WmsSensor2 sen = new WmsSensor2();
        sen.init( ccfg );

        boolean stop = false;
        int counter = 0;

        while ( counter < 100 && !stop ) {

            sen.run();

            SimpleMessage msg = null;
            while ( ( msg = MessageQueue.poll() ) != null ) {
                if ( msg.isCodeSuccessfull() ) {
                    LOG.info( "Message success, duration: {}, text: {}", msg.getDuration(), msg.getText() );
                } else {
                    LOG.warn( "Message: [{}] {}", msg.getCode(), msg.getText() );
                    stop = true;
                }
            }

            counter++;

        }
    }

    @Test
    @Ignore
    public void testBlattschnitteImage() {
        ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( "utilities.xml" ) ).setLevel( Level.INFO );
        LOG.info( "Stating on Java {}", System.getProperty( "java.version" ) );

        WmsSensorConfig wcfg = new WmsSensorConfig();
        wcfg.setUrl( "http://127.0.0.1/capabilities.xml" );
        wcfg.setVersion( "1.1.1" );
        // wcfg.setLayerImgcheck( "blattschnitt_tk10" );
        wcfg.setLayerAvail( "dop2c_tiled|blattschnitt_tk10" );
        wcfg.setLayerImgcheck( "dop2c_tiled" );
        wcfg.setImgcheckStyles( "default99" );
        // wcfg.setImgcheckFormat( "image/png" );
        // wcfg.setImgcheckFormat( "image/jpeg" );
        // wcfg.setImgcheckFormat( "image/jpg" );

        wcfg.setId( 99 );
        wcfg.setName( getClass().getName() );
        wcfg.setType( "WMSBASIC" );

        runLoop( wcfg );
    }

    @Test
    @Ignore
    public void testGritDop20swImage() {
        ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( "utilities.xml" ) ).setLevel( Level.INFO );
        LOG.info( "Stating on Java {}", System.getProperty( "java.version" ) );

        WmsSensorConfig wcfg = new WmsSensorConfig();
        wcfg.setUrl( "http://***REMOVED***/services?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities" );
        wcfg.setVersion( "1.1.1" );
        wcfg.setLayerAvail( "dop20sw" );
        wcfg.setLayerImgcheck( "dop20sw" );
        wcfg.setImgcheckStyles( "unknownstyle" );
        wcfg.setImgcheckFormat( "image/jpeg" );
        wcfg.setImgcheckSrs( "EPSG:31468" );
        wcfg.setImgcheckBbox( "1.3,2.2,3.5,4.1" );

        wcfg.setId( 99 );
        wcfg.setName( getClass().getName() );
        wcfg.setType( "WMSBASIC" );

        runLoop( wcfg );
    }

    @Test
    @Ignore
    public void testGemuekProdImage() {
        ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( "utilities.xml" ) ).setLevel( Level.INFO );
        LOG.info( "Stating on Java {}", System.getProperty( "java.version" ) );

        WmsSensorConfig wcfg = new WmsSensorConfig();
        wcfg.setUrl( "http://127.0.0.1/capabilities_prod.xml" );
        wcfg.setVersion( "1.1.1" );
        wcfg.setLayerAvail( "gmkueb_flur" );
        wcfg.setLayerImgcheck( "gmkueb_flur" );
        wcfg.setImgcheckFormat( "image/png" );
        wcfg.setImgcheckSrs( "EPSG:31468" );
        wcfg.setImgcheckBbox( "1,2,3,4" );

        wcfg.setId( 99 );
        wcfg.setName( getClass().getName() );
        wcfg.setType( "WMSBASIC" );

        runLoop( wcfg );
    }
}