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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import de.grit.xgdm.monsuite.data.CheckTask;
import de.grit.xgdm.monsuite.data.SensorConfig;

public class HttpSensorConfig extends AbstractSensorConfig {
    private static final String[] ROWIDsHSC = new String[] { "URL", "VERSION_STRING", "HTTP_USER", "HTTP_PASS",
                                                            "PROXY", "REGEXP_REQ", "REGEXP_NOT", "VALID_CODE" };

    /** Versionsstring der zur Pr\u00fcfung verwendet werden soll */
    private String version = "";

    /**
     * Internetadresse des WMS-Services (f\u00fcr getCapabilities) (Alle anderen Adressen lassen sich hiervon ableiten)
     */
    private String url = "";

    /** Auth Benutzer */
    private String authUser = "";

    /** Auth Passwort */
    private String authPass = "";

    /** Proxy f\u00fcr Verbindungen Format: (Hostname|IP)[:Port] */
    private String proxyStr;

    private String regexpNot;

    private String regexpReq;

    private String validCode;

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind
     * 
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     * 
     * @return boolean inhaltlich gleich / ungleich
     */
    @Override
    public boolean equals( Object o ) {
        if ( !equalsBasicSensorConfig( o ) ) {
            return false;
        }

        HttpSensorConfig sp = (HttpSensorConfig) o;

        // Vergleich Direktattribute
        if ( Objects.equals( version, sp.getVersion() ) && Objects.equals( url, sp.getUrl() )
             && Objects.equals( authUser, sp.getAuthUser() ) && Objects.equals( authPass, sp.getAuthPass() )
             && Objects.equals( proxyStr, sp.getProxyStr() ) && Objects.equals( regexpReq, sp.getRegexpReq() )
             && Objects.equals( regexpNot, sp.getRegexpNot() ) && Objects.equals( validCode, sp.getValidCode() ) ) {
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
    @Override
    public boolean loadFromRow( ResultSet dbres ) {
        if ( !loadFromRowBasic( dbres ) ) {
            return false;
        }

        try {
            this.url = dbres.getString( ROWIDsHSC[0] );
            this.version = dbres.getString( ROWIDsHSC[1] );
            this.authUser = dbres.getString( ROWIDsHSC[2] );
            this.authPass = dbres.getString( ROWIDsHSC[3] );
            this.proxyStr = dbres.getString( ROWIDsHSC[4] );
            this.regexpReq = dbres.getString( ROWIDsHSC[5] );
            this.regexpNot = dbres.getString( ROWIDsHSC[6] );
            this.validCode = dbres.getString( ROWIDsHSC[7] );
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
        this.authUser = sen.getHttpUser();
        this.authPass = sen.getHttpPass();
        this.proxyStr = sen.getProxy();
        this.regexpReq = sen.getRegexpReq();
        this.regexpNot = sen.getRegexpNot();
        this.validCode = sen.getValidCode();
    }

    @Override
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( url == null ? 0 : url.hashCode() ) );
        sb.append( ( version == null ? 0 : version.hashCode() ) );
        sb.append( ( authUser == null ? 0 : authUser.hashCode() ) );
        sb.append( ( authPass == null ? 0 : authPass.hashCode() ) );
        sb.append( ( proxyStr == null ? 0 : proxyStr.hashCode() ) );
        sb.append( ( regexpReq == null ? 0 : regexpReq.hashCode() ) );
        sb.append( ( regexpNot == null ? 0 : regexpNot.hashCode() ) );
        sb.append( ( validCode == null ? 0 : validCode.hashCode() ) );
        return sb.toString().hashCode();
    }

    public List<Integer> getValidCodeAsList() {

        List<Integer> obj = new ArrayList<Integer>();

        if ( validCode == null || validCode.trim().length() == 0 )
            return obj;

        StringTokenizer st = new StringTokenizer( validCode, "|", false );

        while ( st.hasMoreTokens() ) {
            String t = st.nextToken();
            if ( t != null && t.length() > 0 ) {
                try {
                    obj.add( new Integer( Integer.parseInt( t ) ) );
                } catch ( Exception ex ) {
                    // ignore
                }
            }
        }

        return obj;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthUser() {
        return authUser;
    }

    public String getAuthPass() {
        return authPass;
    }

    public String getProxyStr() {
        return proxyStr;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public void setAuthUser( String authUser ) {
        this.authUser = authUser;
    }

    public void setAuthPass( String authPass ) {
        this.authPass = authPass;
    }

    public void setProxyStr( String proxyStr ) {
        this.proxyStr = proxyStr;
    }

    public String getRegexpNot() {
        return regexpNot;
    }

    public void setRegexpNot( String regexpNot ) {
        this.regexpNot = regexpNot;
    }

    public String getRegexpReq() {
        return regexpReq;
    }

    public void setRegexpReq( String regexpReq ) {
        this.regexpReq = regexpReq;
    }

    public String getValidCode() {
        return validCode;
    }

    public void setValidCode( String validCode ) {
        this.validCode = validCode;
    }
}