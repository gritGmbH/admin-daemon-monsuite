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
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.xgdm.monsuite.data.CheckTask;
import de.grit.xgdm.monsuite.data.SensorConfig;

/**
 * DOCUMENT ME!
 */
public abstract class AbstractSensorConfig {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractSensorConfig.class );

    private static final String[] ROWIDsASC = new String[] { "SENSOR_ID", "NAME", "SENSOR_TYP_ID" };

    /** Typ der Konfiguration (LowerCase) */
    protected String type = "";

    /** Id des Datensatzes */
    protected int id = -1;

    /** Name der Konfiguration */
    protected String name = "";

    /**
     * Creates a new BasicSensorConfig object.
     */
    AbstractSensorConfig() {
        // Nothing
    }

    /*
     * Momentan nur vorgesehen
     * 
     * \u00dcberpr\u00fcfen der Konfiguration auf Vollst\u00e4ndigkeit
     * 
     * protected abstract void validate();
     */

    /**
     * Laden eines SQL-Datensatzes in das Config Objekt
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     */
    public abstract boolean loadFromRow( ResultSet dbres );

    /**
     * Laden eines SQL-Datensatzes (Basisteile) in das Config Objekt
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     *
     * @return boolean erfolgreich / fehlgeschlagen
     */
    protected boolean loadFromRowBasic( ResultSet dbres ) {
        try {
            this.id = dbres.getInt( ROWIDsASC[0] );
            this.name = dbres.getString( ROWIDsASC[1] );
            this.type = dbres.getString( ROWIDsASC[2] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    protected abstract void load( CheckTask ct, SensorConfig sen )
                            throws Exception;

    final public boolean load( CheckTask ct ) {
        try {
            id = ct.getId().intValue();
            name = ct.getName();
            type = ct.getSensor().getType();

            load( ct, ct.getSensor().getConfig() );
        } catch ( Exception ex1 ) {
            LOG.warn( "Failed configuration: {}", ex1.getMessage() );
            return false;
        }
        return true;
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind
     *
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    abstract public boolean equals( Object o );

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich f\u00fcr die Attribute aus Basic gleich sind (Nur intern verwenden)
     *
     * @param o
     *            Object Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    protected boolean equalsBasicSensorConfig( Object o ) {
        if ( o == this ) {
            return true;
        }

        if ( o == null ) {
            return false;
        }

        if ( this.getClass() != o.getClass() ) {
            return false;
        }

        AbstractSensorConfig sp = (AbstractSensorConfig) o;

        // Vergleich Direktattribute
        if ( ( this.id == sp.getId() ) && Objects.equals( name, sp.getName() ) && Objects.equals( type, sp.getType() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Id des Datensatzes zur\u00fcckliefern
     *
     * @return int Id des Datensatzes
     */
    public int getId() {
        return this.id;
    }

    /**
     * Id des Datensatzes setzen
     *
     * @param id
     *            Id des Datensatzes
     */
    public void setId( int id ) {
        this.id = id;
    }

    /**
     * Name der Konfiguration zur\u00fcckliefern
     *
     * @return String Name der Konfiguration
     */
    public String getName() {
        return this.name;
    }

    /**
     * Name der Konfiguration setzen
     *
     * @param name
     *            String Name der Konfiguration
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Typ der Konfiguration zur\u00fcckgeben
     *
     * @return int Typ der Konfiguration
     */
    public String getType() {
        return this.type;
    }

    /**
     * Typ der Konfiguration setzen
     *
     * @param type
     *            String Typ der Konfiguration
     */
    public void setType( String type ) {
        this.type = type.toLowerCase();
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        return Objects.hash( id, name, type );
    }

    /**
     * Textrepresentation des Objekts zur\u00fcckgeben
     * 
     * @return String Textrepresentation des Objekts
     */
    public String toString() {
        return "(id=" + this.id + "|name=" + this.name + "|type=" + this.type + "@AbstractSensorConfig)";
    }
}