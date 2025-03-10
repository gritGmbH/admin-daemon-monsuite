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

public class WfsSensorConfig extends AbstractSensorConfig {
    /** Spaltennamen */
    private static final String[] ROWIDsWFSC = new String[] { "URL", "FEATURE_CAP", "VERSION_STRING", "FEATURE_GET",
                                                             "HTTP_USER", "HTTP_PASS", "PROXY" };

    /** Auth Type ( i.e. http, deegree-was */
    private String authType = "http";

    /** Auth Url ( service / login-url ) */
    private String authUrl = null;

    /** Features die verf\u00fcgbar sein sollen (Capabilities) */
    private String featureCapabilities = "";

    /** Versionsstring der zur Pr\u00fcfung verwendet werden soll */
    private String version = "";

    /** Layernamen die per GetFeature erreicht werden sollten */
    private String featureGetFeature = "";

    /**
     * Internetadresse des WMS-Services (f\u00fcr getCapabilities) (Alle anderen Adressen lassen sich hiervon ableiten)
     */
    private String url = "";

    /** HTTP Auth Benutzer */
    private String authUser = "";

    /** HTTP Auth Passwort */
    private String authPass = "";

    /** Proxy f\u00fcr Verbindungen Format: (Hostname|IP)[:Port] */
    private String proxyStr;

    public WfsSensorConfig() {
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

        WfsSensorConfig sp = (WfsSensorConfig) o;

        // Vergleich Direktattribute
        if ( Objects.equals( version, sp.getVersion() )
             && Objects.equals( featureCapabilities, sp.getFeatureCapabilities() )
             && Objects.equals( featureGetFeature, sp.getFeatureGetFeature() ) && Objects.equals( url, sp.getUrl() )
             && Objects.equals( authUser, sp.getAuthUser() ) && Objects.equals( authPass, sp.getAuthPass() )
             && Objects.equals( authType, sp.getAuthType() ) && Objects.equals( authUrl, sp.getAuthUrl() )
             && Objects.equals( proxyStr, sp.getProxyStr() ) ) {
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
            this.url = dbres.getString( ROWIDsWFSC[0] );
            this.version = dbres.getString( ROWIDsWFSC[2] );
            this.featureCapabilities = dbres.getString( ROWIDsWFSC[1] );
            this.featureGetFeature = dbres.getString( ROWIDsWFSC[3] );
            this.authUser = dbres.getString( ROWIDsWFSC[4] );
            this.authPass = dbres.getString( ROWIDsWFSC[5] );
            // TODO: implement AuthType, AuthUrl
            this.proxyStr = dbres.getString( ROWIDsWFSC[6] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.version = sen.getVersionString();
        this.featureCapabilities = sen.getFeatureCap();
        this.featureGetFeature = sen.getFeatureGet();
        this.authUser = sen.getHttpUser();
        this.authPass = sen.getHttpPass();
        // TODO: implement AuthType, AuthUrl
        this.proxyStr = sen.getProxy();
    }

    /**
     * Versionsstring setzen
     *
     * @param String
     *            Versionsstring
     */
    public void setVersion( String version ) {
        this.version = version;
    }

    /**
     * Versionsstring zur\u00fcckliefern
     *
     * @return String Versionsstring
     */
    public String getVersion() {
        return version;
    }

    /**
     * Url (getCapabilities) setzen
     *
     * @param url
     *            String Url
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * Url (getCapabilities) zur\u00fcckliefern
     *
     * @return String Url
     */
    public String getUrl() {
        return url;
    }

    /**
     * HTTP Auth Benutzernamen setzen
     *
     * @param httpUser
     *            String HTTP Auth Benutzernamen
     */
    public void setAuthUser( String authUser ) {
        this.authUser = authUser;
    }

    /**
     * HTTP Auth Benutzernamen zur\u00fcckliefern
     *
     * @return String HTTP Auth Benutzernamen
     */
    public String getAuthUser() {
        return authUser;
    }

    /**
     * HTTP Auth Passwort setzen
     *
     * @param httpPass
     *            String HTTP Auth Passwort
     */
    public void setHttpPass( String authPass ) {
        this.authPass = authPass;
    }

    /**
     * HTTP Auth Passwort zur\u00fcckliefern
     *
     * @return String HTTP Auth Passwort
     */
    public String getAuthPass() {
        return authPass;
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( ( authUser == null ) ? 0 : authUser.hashCode() ) );
        sb.append( ( ( authPass == null ) ? 0 : authPass.hashCode() ) );
        sb.append( ( ( authType == null ) ? 0 : authType.hashCode() ) );
        sb.append( ( ( authUrl == null ) ? 0 : authUrl.hashCode() ) );
        sb.append( ( ( url == null ) ? 0 : url.hashCode() ) );
        sb.append( ( ( version == null ) ? 0 : version.hashCode() ) );
        sb.append( ( ( featureCapabilities == null ) ? 0 : featureCapabilities.hashCode() ) );
        sb.append( ( ( featureGetFeature == null ) ? 0 : featureGetFeature.hashCode() ) );
        sb.append( ( proxyStr == null ? 0 : proxyStr.hashCode() ) );
        return sb.toString().hashCode();
    }

    public void setFeatureCapabilities( String featureCapabilities ) {
        this.featureCapabilities = featureCapabilities;
    }

    public String getFeatureCapabilities() {
        return featureCapabilities;
    }

    public void setFeatureGetFeature( String featureGetFeature ) {
        this.featureGetFeature = featureGetFeature;
    }

    public String getFeatureGetFeature() {
        return featureGetFeature;
    }

    public void setAuthType( String authType ) {
        this.authType = authType;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthUrl( String authUrl ) {
        this.authUrl = authUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getProxyStr() {
        return proxyStr;
    }

    public void setProxyStr( String proxyStr ) {
        this.proxyStr = proxyStr;
    }

}