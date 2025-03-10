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

import de.grit.gdi.daemon.data.config.AbstractSensorConfig;
import de.grit.gdi.daemon.data.config.ArcGisSensorConfig;
import de.grit.gdi.daemon.data.config.DbpSensorConfig;
import de.grit.gdi.daemon.data.config.HttpSensorConfig;
import de.grit.gdi.daemon.data.config.ImsSensorConfig;
import de.grit.gdi.daemon.data.config.SqlOracleSensorConfig;
import de.grit.gdi.daemon.data.config.SqlPostgresSensorConfig;
import de.grit.gdi.daemon.data.config.WfsSensorConfig;
import de.grit.gdi.daemon.data.config.WmsSensorConfig;
import de.grit.gdi.daemon.data.config.OafSensorConfig;
import de.grit.gdi.daemon.monitoring.sensor.*;

public class DefaultSensorProvider implements SensorProvider {
    @Override
    public AbstractSensorConfig createConfig( String type ) {
        switch ( type.toLowerCase() ) {
        case "wmsbasic":
            return new WmsSensorConfig();
        case "wfsbasic":
            return new WfsSensorConfig();
        case "dbpbasic":
            return new DbpSensorConfig();
        case "arcgis":
            return new ArcGisSensorConfig();
        case "sqloracle":
            return new SqlOracleSensorConfig();
        case "sqlpg":
            return new SqlPostgresSensorConfig();
        case "httpchk":
            return new HttpSensorConfig();
        case "imsbasic": // OLD Sensor
            return new ImsSensorConfig();
        case "oafbasic": 
            return new OafSensorConfig();/*Gibt OafSensorconfig zurueck*/
        default:
            return null;
        }
    }

    @Override
    public AbstractSensor createSensor( String type ) {
        switch ( type.toLowerCase() ) {
        case "wmsbasic":
            return new WmsSensor2();
        case "wfsbasic":
            return new WfsSensor();
        case "dbpbasic":
            return new DbpSensor();
        case "arcgis":
            return new ArcGisSensor();
        case "sqloracle":
            return new SqlOracleSensor();
        case "sqlpg":
            return new SqlPostgresSensor();
        case "httpchk":
            return new HttpSensor();
        case "imsbasic": // OLD Sensor
            return new ImsSensor();
        case "oafbasic": 
            return new OafSensor(); /*Gibt OafSensor zurueck*/
        default:
            return null;
        }
    }
}