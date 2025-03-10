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

import de.grit.xgdm.monsuite.data.CheckTask;
import de.grit.xgdm.monsuite.data.SensorConfig;

/**
 * Konfiguration f\u00fcr einen IMS Sensor
 */
public class ImsSensorConfig extends AbstractSensorConfig {
    /** Spaltenname von url zum Backend */
    private static final String ROWID_URL = "-";

    /** Spaltenname von layerDetail */
    private static final String ROWID_PIPENAME = "-";

    /** Spaltenname von layerAvail */
    private static final String ROWID_PIPEHOST = "-";

    /** Spaltenname von layerAvail */
    private static final String ROWID_ACTION = "-";

    /** Spaltenname von httpUser */
    private static final String ROWID_HTTPUSER = "-";

    /** Spaltenname von httpPass */
    private static final String ROWID_HTTPPASS = "-";

    /** Namen der Pipe */
    private String pipeName = "";

    /** Layernamen die als Detail gepr\u00fcft werden sollen */
    private String pipeHost = "";

    /** Layernamen die verf\u00fcgbar sein sollen (Capabilities) */
    private int action = 0;

    /**
     * Internetadresse des WMS-Services (f\u00fcr getCapabilities) (Alle anderen Adressen lassen sich hiervon ableiten)
     */
    private String url = "";

    /** HTTP Auth Benutzer */
    private String httpUser = "";

    /** HTTP Auth Passwort */
    private String httpPass = "";

    /**
     * Creates a new ImsSensorConfig object.
     */
    public ImsSensorConfig() {
        super();
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind
     *
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    public boolean equals( Object o ) {
        if ( !equalsBasicSensorConfig( o ) ) {
            return false;
        }

        ImsSensorConfig sp = (ImsSensorConfig) o;

        // Vergleich Direktattribute
        if ( ( action == sp.getAction() ) && Objects.equals( pipeHost, sp.getPipeHost() )
             && Objects.equals( pipeName, sp.getPipeName() ) && Objects.equals( url, sp.getUrl() )
             && Objects.equals( httpUser, sp.getHttpUser() ) && Objects.equals( httpPass, sp.getHttpPass() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Laden eines SQL-Datensatzes (Erweiterte Attribute) in das Config Objekt
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     *
     * @return boolean erfolgreich / fehlgeschlagen
     */
    public boolean loadFromRow( ResultSet dbres ) {
        if ( !loadFromRowBasic( dbres ) ) {
            return false;
        }

        try {
            this.url = dbres.getString( ROWID_URL );
            this.pipeHost = dbres.getString( ROWID_PIPEHOST );
            this.pipeName = dbres.getString( ROWID_PIPENAME );
            this.action = dbres.getInt( ROWID_ACTION );
            this.httpUser = dbres.getString( ROWID_HTTPUSER );
            this.httpPass = dbres.getString( ROWID_HTTPPASS );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.pipeHost = sen.getImsHost();
        this.pipeName = sen.getImsPipe();
        this.action = sen.getImsAction();
        this.httpUser = sen.getHttpUser();
        this.httpPass = sen.getHttpPass();
    }

    /**
     * DOCUMENT_ME
     *
     * @param pipeName
     *            DOCUMENT_ME
     */
    public void setPipeName( String pipeName ) {
        this.pipeName = pipeName;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getPipeName() {
        return pipeName;
    }

    /**
     * DOCUMENT_ME
     *
     * @param pipeHost
     *            DOCUMENT_ME
     */
    public void setPipeHost( String pipeHost ) {
        this.pipeHost = pipeHost;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getPipeHost() {
        return pipeHost;
    }

    /**
     * DOCUMENT_ME
     *
     * @param action
     *            DOCUMENT_ME
     */
    public void setAction( int action ) {
        this.action = action;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public int getAction() {
        return action;
    }

    /**
     * DOCUMENT_ME
     *
     * @param url
     *            DOCUMENT_ME
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String getUrl() {
        return url;
    }

    /**
     * HTTP Auth Benutzernamen zur\u00fcckliefern
     *
     * @return String HTTP Auth Benutzernamen
     */
    public String getHttpUser() {
        return httpUser;
    }

    /**
     * HTTP Auth Passwort setzen
     *
     * @param httpPass
     *            String HTTP Auth Passwort
     */
    public void setHttpPass( String httpPass ) {
        this.httpPass = httpPass;
    }

    /**
     * HTTP Auth Passwort zur\u00fcckliefern
     *
     * @return String HTTP Auth Passwort
     */
    public String getHttpPass() {
        return httpPass;
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( ( httpUser == null ) ? 0 : httpUser.hashCode() ) );
        sb.append( ( ( httpPass == null ) ? 0 : httpPass.hashCode() ) );
        sb.append( ( ( pipeHost == null ) ? 0 : pipeHost.hashCode() ) );
        sb.append( ( ( pipeName == null ) ? 0 : pipeName.hashCode() ) );
        sb.append( ( ( action > 0 ) ? ( "" + action ).hashCode() : 0 ) );
        sb.append( ( ( url == null ) ? 0 : url.hashCode() ) );

        return sb.toString().hashCode();
    }
}