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
package de.grit.gdi.daemon.monitoring.sensor;

import org.slf4j.Logger;

import de.grit.gdi.daemon.data.config.CheckTaskConfig;

/**
 * BasicSensor Interface
 *
 * @version 1.0
 */
public interface IBasicSensor extends Runnable {
    /**
     * Initialisierung des Sensors
     *
     * @param config
     *            AbstractSensorConfig Konfiguration des Sensors
     * @param timeout
     *            int Timeout bei Pr\u00fcfungen
     * @param pause
     *            int Pause zwischen den Pr\u00fcfungen
     */
    public void init( CheckTaskConfig ctConfig );

    /**
     * Konfiguration des Pr\u00fcfauftrags zur\u00fcckliefern ( enth\u00e4ht Sensorkonfiguration )
     * 
     * @retun Konfiguration des Pr\u00fcfauftrages
     */
    public CheckTaskConfig getCheckTaskConfig();

    /**
     * Logger, der vom PoolWorker verwendet werden soll
     *
     * @return Logger Log4J Logger f\u00fcr den PoolWorker
     */
    public Logger getLogger();

    /**
     * Basisnamen des Threads zur\u00fcckliefern
     *
     * @return String Namesprefix f\u00fcr den Thread
     */
    public String getThreadName();

    /**
     * Nachrichtenstatus ermitteln
     * 
     * @return True wenn nachrichten Fehlerfrei gesendet werden k\u00f6nnen
     */
    public boolean getMessageStatus();

    /**
     * Run Methode
     * 
     * @see java.lang.Runnable#run()
     */
    public void run();
}