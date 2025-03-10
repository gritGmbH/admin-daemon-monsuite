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
 * 
 * TT OO DD OO
 * 
 */
public class DbpSensorConfig extends AbstractSensorConfig {

    public static String DEFAULT_SQL_CMD = "SELECT 'PONG' FROM DUAL";

    private static final String[] ROWIDsSOSC = new String[] { "URL", "HTTP_USER", "HTTP_PASS", "DBP_DATABASE",
                                                             "APP_USER", "APP_PASS", "DBP_ACTION", "SQL_CMD" };

    private String appUser = "";

    private String appPass = "";

    /** Datenbankname */
    private String database = "";

    private int action = 0;

    private String sqlCmd = "";

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
    public DbpSensorConfig() {
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

        DbpSensorConfig sp = (DbpSensorConfig) o;

        // Vergleich Direktattribute
        if ( ( action == sp.getAction() ) && Objects.equals( appUser, sp.getAppUser() )
             && Objects.equals( appPass, sp.getAppPass() ) && Objects.equals( database, sp.getDatabase() )
             && Objects.equals( url, sp.getUrl() ) && Objects.equals( httpUser, sp.getHttpUser() )
             && Objects.equals( httpPass, sp.getHttpPass() ) && Objects.equals( sqlCmd, sp.getSqlCmd() ) ) {
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
            this.url = dbres.getString( ROWIDsSOSC[0] );
            this.httpUser = dbres.getString( ROWIDsSOSC[1] );
            this.httpPass = dbres.getString( ROWIDsSOSC[2] );
            this.database = dbres.getString( ROWIDsSOSC[3] );
            this.appUser = dbres.getString( ROWIDsSOSC[4] );
            this.appPass = dbres.getString( ROWIDsSOSC[5] );
            this.action = dbres.getInt( ROWIDsSOSC[6] );
            this.sqlCmd = dbres.getString( ROWIDsSOSC[7] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.httpUser = sen.getHttpUser();
        this.httpPass = sen.getHttpPass();
        this.database = sen.getDbpDatabase();
        this.appUser = sen.getAppUser();
        this.appPass = sen.getAppPass();
        this.action = sen.getDbpAction();
        this.sqlCmd = sen.getSqlCmd();
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
        sb.append( ( ( appUser == null ) ? 0 : appUser.hashCode() ) );
        sb.append( ( ( appPass == null ) ? 0 : appPass.hashCode() ) );
        sb.append( ( ( database == null ) ? 0 : database.hashCode() ) );
        sb.append( ( ( action > 0 ) ? ( "" + action ).hashCode() : 0 ) );
        sb.append( ( ( url == null ) ? 0 : url.hashCode() ) );
        sb.append( ( ( sqlCmd == null ) ? 0 : sqlCmd.hashCode() ) );

        return sb.toString().hashCode();
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

    public void setAppUser( String appUser ) {
        this.appUser = appUser;
    }

    public String getAppUser() {
        return appUser;
    }

    public void setAppPass( String appPass ) {
        this.appPass = appPass;
    }

    public String getAppPass() {
        return appPass;
    }

    public void setDatabase( String database ) {
        this.database = database;
    }

    public String getDatabase() {
        return database;
    }

    public void setSqlCmd( String sqlCmd ) {
        this.sqlCmd = sqlCmd;
    }

    public String getSqlCmd() {
        if ( sqlCmd == null || sqlCmd.trim().length() == 0 )
            return DEFAULT_SQL_CMD;
        else
            return sqlCmd;
    }
}