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
 * Konfiguration f\u00fcr einen WMS-Sensor
 */
public class WmsSensorConfig extends AbstractSensorConfig {

    private static final String[] ROWIDsWMSC = new String[] { "URL", "LAYER_AVAIL", "VERSION_STRING", "LAYER_IMGCHECK",
                                                              "HTTP_USER", "HTTP_PASS", "PROXY", "SRS", "BBOX",
                                                              "STYLES_IMGCHECK", "FORMAT_IMGCHECK", "ROTATION" };

    /** Layernamen die als Image gepr\u00fcft werden sollen */
    private String layerImgcheck = "";

    /** Versionsstring der zur Pr\u00fcfung verwendet werden soll */
    private String version = "";

    /** Layernamen die verf\u00fcgbar sein sollen (Capabilities) */
    private String layerAvail = "";

    /**
     * Internetadresse des WMS-Services (f\u00fcr getCapabilities) (Alle anderen Adressen lassen sich hiervon ableiten)
     */
    private String url = "";

    /** Auth Benutzer */
    private String authUser = "";

    /** Auth Passwort */
    private String authPass = "";

    /** Auth Type ( i.e. http, deegree-was */
    private String authType = "http";

    /** Auth Url (Service / login-url) */
    private String authUrl = null;

    /** Proxy f\u00fcr Verbindungen Format: (Hostname|IP)[:Port] */
    private String proxyStr;

    /** SRS fuer Image-Anfrage */
    private String imgcheckSrs;

    /** BoundingBox fuer Image-Anfrage */
    private String imgcheckBbox;

    /** Styles fuer Image-Anfrage */
    private String imgcheckStyles;

    /** Format fuer Image-Anfrage */
    private String imgcheckFormat;

    /** Achsenorientierung */
    private int rotation;

    /**
     * Creates a new WmsSensorConfig object.
     */
    public WmsSensorConfig() {
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

        WmsSensorConfig sp = (WmsSensorConfig) o;

        // Vergleich Direktattribute
        if ( Objects.equals( layerAvail, sp.getLayerAvail() ) && Objects.equals( version, sp.getVersion() )
             && Objects.equals( layerImgcheck, sp.getLayerImgcheck() ) && Objects.equals( url, sp.getUrl() )
             && Objects.equals( authUser, sp.getAuthUser() ) && Objects.equals( authPass, sp.getAuthPass() )
             && Objects.equals( authType, sp.getAuthType() ) && Objects.equals( authUrl, sp.getAuthUrl() )
             && Objects.equals( proxyStr, sp.getProxyStr() ) && Objects.equals( imgcheckSrs, sp.getImgcheckSrs() )
             && Objects.equals( imgcheckBbox, sp.getImgcheckBbox() )
             && Objects.equals( imgcheckStyles, sp.getImgcheckStyles() )
             && Objects.equals( imgcheckFormat, sp.getImgcheckFormat() ) ) {
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
            this.url = dbres.getString( ROWIDsWMSC[0] );
            this.layerAvail = dbres.getString( ROWIDsWMSC[1] );
            this.version = dbres.getString( ROWIDsWMSC[2] );
            this.layerImgcheck = dbres.getString( ROWIDsWMSC[3] );
            this.authUser = dbres.getString( ROWIDsWMSC[4] );
            this.authPass = dbres.getString( ROWIDsWMSC[5] );
            // TODO: Implement AuthUser, AuthType
            this.proxyStr = dbres.getString( ROWIDsWMSC[6] );
            this.imgcheckSrs = dbres.getString( ROWIDsWMSC[7] );
            this.imgcheckBbox = dbres.getString( ROWIDsWMSC[8] );
            this.imgcheckStyles = dbres.getString( ROWIDsWMSC[9] );
            this.imgcheckFormat = dbres.getString( ROWIDsWMSC[10] );
            this.rotation = dbres.getInt( ROWIDsWMSC[11] );
        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.layerAvail = sen.getLayerAvail();
        this.version = sen.getVersionString();
        this.layerImgcheck = sen.getLayerImgCheck();
        this.authUser = sen.getHttpUser();
        this.authPass = sen.getHttpPass();
        // TODO: Implement AuthUser, AuthType
        this.proxyStr = sen.getProxy();
        this.imgcheckSrs = sen.getSrs();
        this.imgcheckBbox = sen.getBbox();
        this.imgcheckStyles = sen.getStylesImgCheck();
        this.imgcheckFormat = sen.getFormatImgCheck();
        this.rotation = sen.getRotation();
    }

    /**
     * Layernamen setzen
     *
     * @param layerImgcheck
     *            String Layernamen
     */
    public void setLayerImgcheck( String layerImgcheck ) {
        this.layerImgcheck = layerImgcheck;
    }

    /**
     * Layernamen zur\u00fcckliefern
     *
     * @return String Layernamen
     */
    public String getLayerImgcheck() {
        return layerImgcheck;
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
     * Layernamen setzen
     *
     * @param layerImgcheck
     *            String Layernamen
     */
    public void setLayerAvail( String layerAvail ) {
        this.layerAvail = layerAvail;
    }

    /**
     * Layernamen zur\u00fcckliefern
     *
     * @return String Layernamen
     */
    public String getLayerAvail() {
        return layerAvail;
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
    public void setAuthPass( String authPass ) {
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
        sb.append( ( authUser == null ? 0 : authUser.hashCode() ) );
        sb.append( ( authPass == null ? 0 : authUser.hashCode() ) );
        sb.append( ( authType == null ? 0 : authType.hashCode() ) );
        sb.append( ( authUrl == null ? 0 : authUrl.hashCode() ) );
        sb.append( ( layerAvail == null ? 0 : layerAvail.hashCode() ) );
        sb.append( ( version == null ? 0 : version.hashCode() ) );
        sb.append( ( layerImgcheck == null ? 0 : layerImgcheck.hashCode() ) );
        sb.append( ( url == null ? 0 : url.hashCode() ) );
        sb.append( ( proxyStr == null ? 0 : proxyStr.hashCode() ) );
        sb.append( ( imgcheckSrs == null ? 0 : imgcheckSrs.hashCode() ) );
        sb.append( ( imgcheckBbox == null ? 0 : imgcheckBbox.hashCode() ) );
        sb.append( ( imgcheckStyles == null ? 0 : imgcheckStyles.hashCode() ) );
        sb.append( ( imgcheckFormat == null ? 0 : imgcheckFormat.hashCode() ) );
        return sb.toString().hashCode();
    }

    public String getProxyStr() {
        return proxyStr;
    }

    public void setProxyStr( String proxyStr ) {
        this.proxyStr = proxyStr;
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

    public String getImgcheckSrs() {
        return imgcheckSrs;
    }

    public void setImgcheckSrs( String imgcheckSrs ) {
        this.imgcheckSrs = imgcheckSrs;
    }

    public String getImgcheckBbox() {
        return imgcheckBbox;
    }

    public void setImgcheckBbox( String imgcheckBbox ) {
        this.imgcheckBbox = imgcheckBbox;
    }

    public String getImgcheckStyles() {
        return imgcheckStyles;
    }

    public void setImgcheckStyles( String imgcheckStyles ) {
        this.imgcheckStyles = imgcheckStyles;
    }

    public String getImgcheckFormat() {
        return imgcheckFormat;
    }

    public void setImgcheckFormat( String imgcheckFormat ) {
        this.imgcheckFormat = imgcheckFormat;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation( int rotation ) {
        this.rotation = rotation;
    }
}