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
package de.grit.gdi.daemon.data;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.config.AbstractSensorConfig;
import de.grit.gdi.daemon.monitoring.sensor.AbstractSensor;

public class SensorFactory {

    private static List<SensorProvider> _provider;

    private static synchronized List<SensorProvider> getProvider() {
        if ( _provider == null ) {
            Logger log = LoggerFactory.getLogger( SensorFactory.class );
            log.info( "Loading Sensor Provider" );
            _provider = new LinkedList<SensorProvider>();
            for ( SensorProvider sp : ServiceLoader.load( SensorProvider.class ) ) {
                if ( sp == null )
                    continue;

                log.info( " -> Loaded provider: {}", sp.getClass() );
                _provider.add( sp );
            }

        }
        return _provider;
    }

    public static AbstractSensorConfig createConfig( String type ) throws UnknownSensorTypeException {
        AbstractSensorConfig cfg = null;
        for ( SensorProvider sp : getProvider() ) {
            cfg = sp.createConfig( type );
            if ( cfg != null )
                break;
        }
        if ( cfg == null )
            throw new UnknownSensorTypeException( "Unknown sensor type (config): " + type );

        return cfg;
    }

    public static AbstractSensor createSensor( String type ) throws UnknownSensorTypeException {
        AbstractSensor sen = null;
        for ( SensorProvider sp : getProvider() ) {
            sen = sp.createSensor( type );
            if ( sen != null )
                break;
        }
        if ( sen == null )
            throw new UnknownSensorTypeException( "Unknown sensor type (sensor): " + type );

        return sen;
    }
}