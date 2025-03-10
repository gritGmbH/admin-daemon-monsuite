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
package de.grit.gdi.daemon.data.config;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * Configuration f\u00fcr einen Monitor
 */
public class MonitorConfig {

    private static final String[] ROWIDsMC = new String[] { "DAEMON_ID", "NAME", "HOST" };

    /** Name des Monitors */
    private String name;

    /** IP-Adresse als String */
    private String host;

    /** Id des Datensatzes */
    private int id;

    /**
     * Liste der Sensoren
     *
     * @associates <{de.grit.gdi.daemon.data.config.CheckTaskConfig}>
     * @supplierCardinality 0..*
     * @clientCardinality
     * @label Config_Monitor_to_CheckTaskConfig
     */
    private List<CheckTaskConfig> checkTask = new Vector<CheckTaskConfig>();

    /**
     * Creates a new MonitorConfig object.
     */
    public MonitorConfig() {
        // Nothing jet
    }

    /**
     * P\u00fcrft ob der Monitor lokal (true) ist
     *
     * @param currentHost
     *            String IP-Adresse des Controllers
     *
     * @return boolean true (lokal) / false (remote)
     */
    public boolean isLocal( String currentHost ) {
        return host.equalsIgnoreCase( currentHost );
    }

    /**
     * Laden eines SQL-Datensatzes (Basisteile) in das Config Objekt Achtung: die CheckTasks werden nicht mitgeladen)
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     *
     * @return boolean erfolgreich / fehlgeschlagen
     */
    public boolean loadFromRow( ResultSet dbres ) {
        try {
            this.id = dbres.getInt( ROWIDsMC[0] );
            this.name = dbres.getString( ROWIDsMC[1] );
            this.host = dbres.getString( ROWIDsMC[2] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind (Nur Basisattribute)
     *
     * @param o
     *            Object Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    public boolean equalsBase( Object o ) {
        if ( o == this ) {
            return true;
        }

        if ( o == null ) {
            return false;
        }

        if ( this.getClass() != o.getClass() ) {
            return false;
        }

        MonitorConfig sp = (MonitorConfig) o;

        // Vergleich Direktattribute
        if ( ( this.id == sp.getId() ) && Objects.equals( name, sp.getName() ) && Objects.equals( host, sp.getHost() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind
     *
     * @param o
     *            Object Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    public boolean equals( Object o ) {
        if ( !equalsBase( o ) ) {
            return false;
        }

        MonitorConfig sp = (MonitorConfig) o;

        // Vergleich der Anzahl
        if ( checkTask.size() != sp.getCheckTaskCount() )
            return false;

        // Inhaltsvergleich CheckTasks
        boolean found;

        for ( int i = 0, ilen = checkTask.size(); i < ilen; i++ ) {
            found = false;

            for ( int j = 0, jlen = sp.getCheckTaskCount(); j < jlen; j++ ) {
                if ( this.getCheckTask( i ).equals( sp.getCheckTask( j ) ) ) {
                    found = true;

                    break;
                }
            }

            if ( !found ) {
                return false;
            }
        }

        return true;
    }

    /**
     * Liste der CheckTasks setzen
     *
     * @param checkTask
     *            List Liste der CheckTasks
     */
    public void setCheckTask( List<CheckTaskConfig> checkTask ) {
        this.checkTask = checkTask;
    }

    /**
     * CheckTask zur\u00fcckliefern
     *
     * @param nr
     *            int Listenposition des CheckTasks
     *
     * @return BasicSensorConfig CheckTask
     */
    public CheckTaskConfig getCheckTask( int nr ) {
        if ( ( nr >= 0 ) && ( nr < this.checkTask.size() ) ) {
            return (CheckTaskConfig) this.checkTask.get( nr );
        } else {
            return null;
        }
    }

    /**
     * CheckTask hinzuf\u00fcgen
     *
     * @param sensor
     *            BasicSensorConfig CheckTask
     */
    public void addCheckTask( CheckTaskConfig checkTask ) {
        this.checkTask.add( checkTask );
    }

    /**
     * CheckTask entfernen
     *
     * @param nr
     *            int Listenposition des CheckTasks
     */
    void remveCheckTask( int nr ) {
        if ( ( nr >= 0 ) && ( nr < this.checkTask.size() ) ) {
            this.checkTask.remove( nr );
        }
    }

    /**
     * Anzhal der Sensoren zur\u00fcckliefern
     *
     * @return int Anzahl Sensoren
     */
    public int getCheckTaskCount() {
        return this.checkTask.size();
    }

    /**
     * Namen setzen
     *
     * @param name
     *            String Namen
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Namen zur\u00fcckliefern
     *
     * @return String Namen
     */
    public String getName() {
        return name;
    }

    /**
     * Host (Ip-Adresse) setzen
     *
     * @param host
     *            String Host (Ip-Adresse)
     */
    public void setHost( String host ) {
        this.host = host;
    }

    /**
     * Host (Ip-Adresse) zur\u00fcckliefern
     *
     * @return String Host (Ip-Adresse)
     */
    public String getHost() {
        return host;
    }

    /**
     * ID des Datensatzes setzen
     *
     * @param id
     *            int ID des Datensatzes
     */
    public void setId( int id ) {
        this.id = id;
    }

    /**
     * ID des Datensatzes zur\u00fcckliefern
     *
     * @return int ID des Datensatzes
     */
    public int getId() {
        return id;
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( id );
        sb.append( ( ( host == null ) ? 0 : host.hashCode() ) );
        sb.append( ( ( host == null ) ? 0 : name.hashCode() ) );

        for ( Iterator<CheckTaskConfig> i = checkTask.iterator(); i.hasNext(); ) {
            sb.append( i.next().hashCode() );
        }

        return sb.toString().hashCode();
    }

    /**
     * DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    public String toString() {
        return "" + this.id + "-" + this.name + "@" + this.host;
    }

    /**
     * Alle CheckTasks als Liste zur\u00fcckliefern
     *
     * @return List Liste von CheckTaskConfig Elementen
     */
    public List<CheckTaskConfig> getCheckTasks() {
        return checkTask;
    }
}