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

public class SqlPostgresSensorConfig extends AbstractSensorConfig {

    public static int DEFAULT_PORT = 5432;

    public static String DEFAULT_INSTANCE = "postgres";

    public static String DEFAULT_SERVER = "postgresql-server";

    public static String DEFAULT_SQL_CMD = "SELECT 'PONG'";

    private String url = "";

    private String appUser = "";

    private String appPass = "";

    private String sqlCmd = "";

    private static final String[] ROWIDsSOSC = new String[] { "URL", "APP_USER", "APP_PASS", "SQL_CMD" };

    @Override
    public boolean equals( Object o ) {
        if ( !equalsBasicSensorConfig( o ) ) {
            return false;
        }

        SqlPostgresSensorConfig sp = (SqlPostgresSensorConfig) o;

        // Vergleich Direktattribute
        if ( Objects.equals( url, sp.getUrl() ) && Objects.equals( appUser, sp.getAppUser() )
             && Objects.equals( appPass, sp.getAppPass() ) && Objects.equals( getSqlCmd(), sp.getSqlCmd() ) ) {
            return true;
        } else {
            return false;
        }
    }

    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.hashCode() );
        sb.append( ( appUser == null ? 0 : appUser.hashCode() ) );
        sb.append( ( appPass == null ? 0 : appPass.hashCode() ) );
        sb.append( ( sqlCmd == null ? 0 : sqlCmd.hashCode() ) );
        return sb.toString().hashCode();
    }

    @Override
    public boolean loadFromRow( ResultSet dbres ) {
        if ( !loadFromRowBasic( dbres ) ) {
            return false;
        }

        try {
            this.url = dbres.getString( ROWIDsSOSC[0] );
            this.appUser = dbres.getString( ROWIDsSOSC[1] );
            this.appPass = dbres.getString( ROWIDsSOSC[2] );
            this.sqlCmd = dbres.getString( ROWIDsSOSC[3] );

        } catch ( Exception ex1 ) {
            return false;
        }

        return true;
    }

    @Override
    protected void load( CheckTask ct, SensorConfig sen )
                            throws Exception {
        this.url = sen.getUrl();
        this.appUser = sen.getAppUser();
        this.appPass = sen.getAppPass();
        this.sqlCmd = sen.getSqlCmd();
    }

    public String getUrl() {
        return url;
    }

    public String getAppUser() {
        return appUser;
    }

    public String getAppPass() {
        return appPass;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public void setAppUser( String appUser ) {
        this.appUser = appUser;
    }

    public void setAppPass( String appPass ) {
        this.appPass = appPass;
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

    public int getUrlPort() {
        try {
            int idxPortSep = url.indexOf( ':' );
            int idxPathSep = url.indexOf( '/' );

            String tmp = null;

            if ( idxPortSep != -1 && idxPathSep == -1 )
                tmp = url.substring( idxPortSep );
            else if ( idxPortSep != -1 && idxPortSep < idxPathSep )
                tmp = url.substring( idxPortSep + 1, idxPathSep );

            return Integer.parseInt( tmp );
        } catch ( Exception ex ) {
            // default port
            return DEFAULT_PORT;
        }
    }

    public String getUrlServer() {
        if ( url == null || url.length() == 0 )
            return DEFAULT_SERVER;

        int idxPortSep = url.indexOf( ':' );
        int idxPathSep = url.indexOf( '/' );

        if ( idxPortSep != -1 && idxPortSep > 0 )
            return url.substring( 0, idxPortSep );
        else if ( idxPortSep == -1 && idxPathSep != -1 && idxPathSep > 0 )
            return url.substring( 0, idxPathSep );
        else if ( idxPortSep == -1 && idxPathSep == -1 )
            return url;
        else
            return DEFAULT_SERVER;
    }

    public String getUrlInstance() {
        if ( url == null || url.length() == 0 )
            return DEFAULT_INSTANCE;

        int idxPathSep = url.indexOf( '/' );

        if ( idxPathSep == -1 || idxPathSep == ( url.length() - 1 ) )
            return DEFAULT_INSTANCE;
        else
            return url.substring( idxPathSep + 1 );
    }
}