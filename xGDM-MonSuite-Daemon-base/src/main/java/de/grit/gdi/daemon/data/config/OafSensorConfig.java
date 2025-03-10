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

public class OafSensorConfig extends AbstractSensorConfig {

    private static final String[] ROWIDsOafSC = new String[] { "URL", "FEATURE_CAP", "LAYER_AVAIL", "FEATURE_GET",
                                                               "PROXY", "HTTP_PASS", "HTTP_USER" };

    /**
     * Basis Internetadresse des OAF-Services
     */
    private String url = "";

    /**
     * Collection die erreicht werden sollten
     */
    private String collectionidAvailable = "";

    private String collectionidContent = "";

    /**
     * Feature die erreicht werden sollten
     */
    private String featureid = "";

    /**
     * Auth Type
     */
    private String authType = "http";

    /**
     * Proxy verbindungen Format: (Hostname|IP)[:Port]
     */
    private String proxyStr;

    /**
     * Auth Url
     */
    private String authUrl = null;

    /**
     * HTTP Auth Passwort
     */
    private String authPass = "";

    /**
     * HTTP Auth Benutzer
     */
    private String authUser = "";

    /**
     * HTTP Auth Benutzernamen zur\u00e4ckliefen
     * 
     * @return httpUser String => HTTP Auth Benutzernamen
     */
    public String getAuthUser() {
        return authUser;
    }

    /**
     * HTTP Auth Benutzernamen setzen
     * 
     * @param httpUser
     *            String => HTTP Auth Benutzernamen
     */
    public void setAuthUser( String authUser ) {
        this.authUser = authUser;
    }

    /**
     * HTTP Auth Passwort zur\u00e4ckliefen
     * 
     * @return authPass String => HTTP Auth Passwort
     */
    public String getAuthPass() {
        return authPass;
    }

    /**
     * HTTP Auth Passwort setzen
     * 
     * @param authPass
     *            String => HTTP Auth Password
     */
    public void setHttpPass( String authPass ) {
        this.authPass = authPass;
    }

    /**
     * url zur\u00e4ckliefen
     * 
     * @return authUrl String
     */
    public String getAuthUrl() {
        return authUrl;
    }

    /**
     * url setzen
     * 
     * @param authUrl
     *            String => url
     */
    public void setAuthUrl( String authUrl ) {
        this.authUrl = authUrl;
    }

    /**
     * proxyStr zur\u00e4ckliefen
     * 
     * @return proxyStr String
     */
    public String getProxyStr() {
        return proxyStr;
    }

    /**
     * proxyStr setzen
     * 
     * @param proxyStr
     *            String
     */
    public void setProxyStr( String proxyStr ) {
        this.proxyStr = proxyStr;
    }

    /**
     * Auth Type zur\u00e4ckliefen
     * 
     * @return Auth Type String
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Auth Type setzen
     * 
     * @param authType
     *            String
     */
    public void setAuthType( String authType ) {
        this.authType = authType;
    }

    public OafSensorConfig() {
        super();
    }

    /**
     * url zur\u00e4ckliefen
     * 
     * @return url String
     */
    public String getUrl() {
        return url;
    }

    /**
     * url setzen
     * 
     * @param url
     *            String
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * Collection zur\u00e4ckliefen
     * 
     * @return Collection String
     */
    public String getCollectionidAvailable() {
        return collectionidAvailable;
    }

    public String getCollectionidContent() {
        return collectionidContent;
    }

    /**
     * Collection setzen
     * 
     * @param Collection
     *            String aus <-> Datenbank <-> Oberfl\u00e4che
     */
    public void setCollectionidAvailable( String collectionid ) {
        this.collectionidAvailable = collectionid;
    }

    public void setCollectionidContent( String collectionid1 ) {
        this.collectionidContent = collectionid1;
    }

    /**
     * Feature zur\u00e4ckliefen
     * 
     * @return url String
     */
    public String getFeatureid() {
        return featureid;
    }

    /**
     * Feature setzen
     * 
     * @param feauture
     *            String aus <-> Datenbank <-> Oberfl\u00fcche
     */
    public void setFeatureid( String featureid ) {
        this.featureid = featureid;
    }

    /**
     * Prfcft, ob zwei Instanzen inhaltlich gleich sind
     * 
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     * 
     * @return boolean [Inhaltlich gleich / ungleich]
     */
    @Override
    public boolean equals( Object o ) {

        if ( !equalsBasicSensorConfig( o ) ) {
            return false;
        }

        OafSensorConfig sp = (OafSensorConfig) o;

        if ( Objects.equals( url, sp.getUrl() )
             && Objects.equals( collectionidAvailable, sp.getCollectionidAvailable() )
             && Objects.equals( featureid, sp.getFeatureid() ) && Objects.equals( authType, sp.getAuthType() )
             && Objects.equals( proxyStr, sp.getProxyStr() ) && Objects.equals( authUrl, sp.getAuthUrl() )
             && Objects.equals( authPass, sp.getAuthPass() ) && Objects.equals( authUser, sp.getAuthUser() )
             && Objects.equals( collectionidContent, sp.getCollectionidContent() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Laden eines SQL-Datensatzes (Erweiterte Attribute) in das Config Objekt
     * 
     * @param dbres:
     *            ResultSet aktueller Datensatz
     * 
     * @return boolean: erfolgreich / fehlgeschlagen
     */
    @Override
    public boolean loadFromRow( ResultSet dbres ) {
        if ( !loadFromRowBasic( dbres ) ) {
            return false;
        }

        try {
            this.url = dbres.getString( ROWIDsOafSC[0] );
            this.collectionidAvailable = dbres.getString( ROWIDsOafSC[1] );
            this.collectionidContent = dbres.getString( ROWIDsOafSC[2] );
            this.featureid = dbres.getString( ROWIDsOafSC[3] );
            this.proxyStr = dbres.getString( ROWIDsOafSC[4] );
            this.authPass = dbres.getString( ROWIDsOafSC[5] );
            this.authUser = dbres.getString( ROWIDsOafSC[6] );

        } catch ( Exception ex1 ) {
            return false;
        }
        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {

        this.url = sen.getUrl();

        this.collectionidAvailable = sen.getFeatureCap();

        this.collectionidContent = sen.getLayerAvail();

        this.featureid = sen.getFeatureGet();

        this.proxyStr = sen.getProxy();

        this.authPass = sen.getHttpPass();

        this.authUser = sen.getHttpUser();

    }

    /**
     * Simpler Hashcode
     * 
     * @return int Hashcode
     */
    public int hashcode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( ( url == null ) ? 0 : url.hashCode() ) );
        sb.append( ( ( collectionidAvailable == null ) ? 0 : collectionidAvailable.hashCode() ) );
        sb.append( ( ( collectionidContent == null ) ? 0 : collectionidContent.hashCode() ) );
        sb.append( ( ( featureid == null ) ? 0 : featureid.hashCode() ) );
        sb.append( ( ( authType == null ) ? 0 : authType.hashCode() ) );
        sb.append( ( proxyStr == null ? 0 : proxyStr.hashCode() ) );
        sb.append( ( ( authUrl == null ) ? 0 : authUrl.hashCode() ) );
        sb.append( ( ( authPass == null ) ? 0 : authPass.hashCode() ) );
        sb.append( ( ( authUser == null ) ? 0 : authUser.hashCode() ) );
        return sb.toString().hashCode();

    }

}