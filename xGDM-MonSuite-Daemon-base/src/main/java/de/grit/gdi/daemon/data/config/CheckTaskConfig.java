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

import java.sql.Date;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.utils.Utilities;

/**
 * Datenklasse f\u00fcr CheckTask's
 */
public class CheckTaskConfig {
    private static final Logger log = LoggerFactory.getLogger( CheckTaskConfig.class );

    private static final String[] ROWIDsCTC = new String[] { "TASK_ID", "START_DATE", "END_DATE", "START_TIME",
                                                            "END_TIME", "TIMEOUT", "PAUSE", "CHECKDAY_0", "CHECKDAY_1",
                                                            "CHECKDAY_2", "CHECKDAY_3", "CHECKDAY_4", "CHECKDAY_5",
                                                            "CHECKDAY_6", };

    /** ID des Datensatzes */
    private int taskId;

    /** Startdatum Pr\u00fcfzeitraum */
    private Date startDate;

    /** Enddatum Pr\u00fcfzeitraum */
    private Date endDate;

    /** Startzeit in Minuten seit 0:00 (Bezug auf den normalen Tag) */
    private int startTime;

    /** Endzeit in Minuten seit 0:00 (Bezug auf den normalen Tag) */
    private int endTime;

    /** Pause zwischen den Pr\u00fcfungen (in s) */
    private int pause;

    /** Timeout f\u00fcr die Pr\u00fcfung / Verbindungsaufbau etc. (in s) */
    private int timeout;

    /** Pr\u00fcftag */
    private boolean checkdaySun;

    /** Pr\u00fcftag */
    private boolean checkdayMon;

    /** Pr\u00fcftag */
    private boolean checkdayTue;

    /** Pr\u00fcftag */
    private boolean checkdayWed;

    /** Pr\u00fcftag */
    private boolean checkdayThu;

    /** Pr\u00fcftag */
    private boolean checkdayFri;

    /** Pr\u00fcftag */
    private boolean checkdaySat;

    /** Sensorkonfiguration */
    private AbstractSensorConfig sensorConfig;

    /**
     * Creates a new CheckTaskConfig object.
     */
    public CheckTaskConfig() {
        // Nothing
    }

    /**
     * Pr\u00fcfen, ob beide Instanzen inhaltlich gleich sind (sensor wird mit gepr\u00fcft)
     *
     * @param o
     *            Objekt mit dem das aktuelle verglichen werden soll
     *
     * @return boolean inhaltlich gleich / ungleich
     */
    public boolean equals( Object o ) {
        if ( o == this ) {
            return true;
        }

        if ( o == null ) {
            return false;
        }

        if ( this.getClass() != o.getClass() ) {
            return false;
        }

        CheckTaskConfig sp = (CheckTaskConfig) o;

        // Vergleich Direktattribute
        if ( ( this.taskId == sp.getTaskId() ) && Objects.equals( startDate, sp.getStartDate() )
             && Objects.equals( endDate, sp.getEndDate() ) && ( this.startTime == sp.getStartTime() )
             && ( this.endTime == sp.getEndTime() ) && ( this.timeout == sp.getTimeout() )
             && ( this.pause == sp.getPause() ) && Objects.equals( sensorConfig, sp.getSensor() )
             && ( this.checkdaySun == sp.isCheckdaySun() ) && ( this.checkdayMon == sp.isCheckdayMon() )
             && ( this.checkdayTue == sp.isCheckdayTue() ) && ( this.checkdayWed == sp.isCheckdayWed() )
             && ( this.checkdayThu == sp.isCheckdayThu() ) && ( this.checkdayFri == sp.isCheckdayFri() )
             && ( this.checkdaySat == sp.isCheckdaySat() ) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Laden eines SQL-Datensatzes (Basisteile) in das Config Objekt Achtung: der Sensor wird nicht mitgeladen)
     *
     * @param dbres
     *            ResultSet aktueller Datensatz
     *
     * @return boolean erfolgreich / fehlgeschlagen
     */
    public boolean loadFromRow( ResultSet dbres ) {
        try {
            this.taskId = dbres.getInt( ROWIDsCTC[0] );
            this.startDate = dbres.getDate( ROWIDsCTC[1] );
            this.endDate = dbres.getDate( ROWIDsCTC[2] );
            this.startTime = Utilities.strTimeToMin( dbres.getString( ROWIDsCTC[3] ) );
            this.endTime = Utilities.strTimeToMin( dbres.getString( ROWIDsCTC[4] ) );
            this.timeout = dbres.getInt( ROWIDsCTC[5] );
            this.pause = dbres.getInt( ROWIDsCTC[6] );
            this.checkdaySun = dbres.getInt( ROWIDsCTC[7] ) == 1;
            this.checkdayMon = dbres.getInt( ROWIDsCTC[8] ) == 1;
            this.checkdayTue = dbres.getInt( ROWIDsCTC[9] ) == 1;
            this.checkdayWed = dbres.getInt( ROWIDsCTC[10] ) == 1;
            this.checkdayThu = dbres.getInt( ROWIDsCTC[11] ) == 1;
            this.checkdayFri = dbres.getInt( ROWIDsCTC[12] ) == 1;
            this.checkdaySat = dbres.getInt( ROWIDsCTC[13] ) == 1;
        } catch ( Exception ex1 ) {
            log.error( "error reading checktask from sql result", ex1 );

            return false;
        }

        return true;
    }

    /**
     * Simpler Hashcode
     *
     * @return int Hashcode
     */
    public int hashCode() {
        StringBuffer sb = new StringBuffer();
        sb.append( taskId );
        sb.append( ( ( startDate == null ) ? 0 : startDate.hashCode() ) );
        sb.append( ( ( endDate == null ) ? 0 : endDate.hashCode() ) );
        sb.append( startTime );
        sb.append( endTime );
        sb.append( timeout );
        sb.append( pause );
        sb.append( checkdaySun );
        sb.append( checkdayMon );
        sb.append( checkdayTue );
        sb.append( checkdayWed );
        sb.append( checkdayThu );
        sb.append( checkdayFri );
        sb.append( checkdaySat );
        sb.append( ( ( sensorConfig == null ) ? 0 : sensorConfig.hashCode() ) );

        return sb.toString().hashCode();
    }

    /**
     * Pr\u00fcft ob der Checktaskt beim angegebenen Zeitpunkt im Zeitfenster ist
     *
     * @param Zeitpunkt
     * @return boolean innerhalb / Au\u00dferhalb des Zeitbereichs
     * @see de.grit.gdi.gui.model.dataaccess.SensorStatusSVRowImpl:getTimeframe()
     */
    public boolean taskInTimeFrame( long now ) {
        // ***/DateFormat fmt = new SimpleDateFormat( "dd.MM.yy HH:mm:ss" );
        // ***/log.debug("taskInTimeFrame now=" + now + " sys=" +System.currentTimeMillis());

        // ***/log.debug(this.toString());
        // ***/log.debug("now="+fmt.format( new Date(now) )+" ut="+now);
        // Nach 01.01.1970
        if ( now < 0 ) {
            throw new IllegalArgumentException();
        }

        Calendar tmp_cal = Calendar.getInstance();
        tmp_cal.setTimeInMillis( now );
        tmp_cal.set( Calendar.HOUR_OF_DAY, 0 );
        tmp_cal.set( Calendar.MINUTE, 0 );
        tmp_cal.set( Calendar.SECOND, 0 );
        tmp_cal.set( Calendar.MILLISECOND, 0 );

        Date tnow = new Date( tmp_cal.getTimeInMillis() );

        // ***/log.debug("tnow="+fmt.format( tnow )+" ut="+tnow.getTime());
        // ***/log.debug("startDate="+fmt.format( startDate )+" ut="+startDate.getTime());

        if ( ( startDate != null ) && ( tnow.getTime() < startDate.getTime() ) ) {
            // Vor dem Startdatum
            log.debug( "taskInTimeFrame - before startday" );

            return false;
        }

        // Auf den vorherigen Tag setzen, da das Enddatum inkl. ist
        // falsch/ tnow = new Date(tmp_cal.getTimeInMillis() - (1000 * 60 * 60 * 24));

        // ***/log.debug("tnow="+fmt.format( tnow )+" ut="+tnow.getTime());
        // ***/log.debug("endDate="+fmt.format( endDate )+" ut="+endDate.getTime());
        // TRICKEY a.after( b ) ==> a.getTime() > b.getTime()
        // Auf > umgestellt da logig so besser erkennbar
        if ( ( endDate != null ) && ( tnow.getTime() > endDate.getTime() ) ) {
            // Nach dem Enddatum (Enddatum eingeschlossen)
            log.debug( "taskInTimeFrame - after endday" );

            return false;
        }

        tmp_cal.setTimeInMillis( now );

        int now_min = ( tmp_cal.get( Calendar.HOUR_OF_DAY ) * 60 ) + tmp_cal.get( Calendar.MINUTE );

        if ( now_min < startTime ) {
            // Vor Startzeit
            log.debug( "taskInTimeFrame - before starttime" );

            return false;
        }

        if ( ( now_min > endTime ) && ( endTime != 0 ) ) {
            // Nach Endzeit, wenn Zeit angegeben
            log.debug( "taskInTimeFrame - after endtime, if set" );

            return false;
        }

        // Wochentagspr\u00fcfung
        tmp_cal.setTimeInMillis( now );

        boolean weekday_ok = false;

        switch ( tmp_cal.get( Calendar.DAY_OF_WEEK ) ) {
        case Calendar.SUNDAY:
            weekday_ok = checkdaySun;

            break;

        case Calendar.MONDAY:
            weekday_ok = checkdayMon;

            break;

        case Calendar.TUESDAY:
            weekday_ok = checkdayTue;

            break;

        case Calendar.WEDNESDAY:
            weekday_ok = checkdayWed;

            break;

        case Calendar.THURSDAY:
            weekday_ok = checkdayThu;

            break;

        case Calendar.FRIDAY:
            weekday_ok = checkdayFri;

            break;

        case Calendar.SATURDAY:
            weekday_ok = checkdaySat;

            break;
        }

        if ( !weekday_ok ) {
            log.debug( "taskInTimeFrame - current weekday not set" );

            return false;
        }

        log.debug( "taskInTimeFrame - in timeframe" );

        return true;
    }

    /**
     * ID des Datensatzes setzen
     *
     * @param taskId
     *            int ID des Datensatzes
     */
    public void setTaskId( int taskId ) {
        this.taskId = taskId;
    }

    /**
     * ID des Datensatzes zur\u00fcckgeben
     *
     * @return int ID des Datensatzes
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Startdatum setzen
     *
     * @param startDate
     *            java.util.Date Startdatum
     */
    public void setStartDate( Date startDate ) {
        this.startDate = startDate;
    }

    /**
     * Startdatum zur\u00fcckliefern
     *
     * @return java.util.Date Startdatum
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Enddatum setzen
     *
     * @param endDate
     *            java.util.Date Enddatum
     */
    public void setEndDate( Date endDate ) {
        this.endDate = endDate;
    }

    /**
     * Enddatum zur\u00fcckgeben
     *
     * @return java.util.Date Enddatum
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Startzeit setzen F\u00fcr den ganzen Tag muss als Start- und Endzeit jeweils 0 angegeben werden
     *
     * @param startTime
     *            int Minuten seit 0:00
     */
    public void setStartTime( int startTime ) {
        this.startTime = startTime;
    }

    /**
     * Startzeit zur\u00fcckliefern F\u00fcr den ganzen Tag muss als Start- und Endzeit jeweils 0 angegeben werden
     *
     * @return int Minuten seit 0:00
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * Endzeit setzen F\u00fcr den ganzen Tag muss als Start- und Endzeit jeweils 0 angegeben werden
     *
     * @param endTime
     *            int Minuten seit 0:00
     */
    public void setEndTime( int endTime ) {
        this.endTime = endTime;
    }

    /**
     * Endzeit setzen F\u00fcr den ganzen Tag muss als Start- und Endzeit jeweils 0 angegeben werden
     *
     * @return int Minuten seit 0:00
     */
    public int getEndTime() {
        return endTime;
    }

    /**
     * Pause setzen
     *
     * @param pause
     *            int Pause in Sekunden
     */
    public void setPause( int pause ) {
        this.pause = pause;
    }

    /**
     * Pause zur\u00fcckliefern
     *
     * @return int Pause in Sekunden
     */
    public int getPause() {
        return pause;
    }

    /**
     * Timeout setzen
     *
     * @param timeout
     *            int Timeout in Sekunden
     */
    public void setTimeout( int timeout ) {
        this.timeout = timeout;
    }

    /**
     * Timeout zur\u00fcckliefern
     *
     * @return int Timeout in Sekunden
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Timeout multiplicated with ratio
     * 
     * @param ratio
     *            multiplication ratio ( 1 <= r <= 100
     * @return
     */
    public int getExtendedTimeout( double ratio ) {
        if ( ratio <= 0d )
            ratio = 1d;
        else if ( ratio < 1d )
            ratio += 1.0d;
        else if ( ratio > 100d )
            ratio = 100d;

        double tmp = ( ( (double) timeout ) * ratio ) + 0.5d;
        return (int) tmp;
    }

    /**
     * Sensorkonfiguration setzen
     *
     * @param sensor
     *            BasicSensorConfig Sensorkonfiguration
     */
    public void setSensor( AbstractSensorConfig sensor ) {
        this.sensorConfig = sensor;
    }

    /**
     * Sensorkonfiguration zur\u00fcckliefern
     *
     * @return BasicSensorConfig Sensorkonfiguration
     */
    public AbstractSensorConfig getSensor() {
        return sensorConfig;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "(taskId=" + taskId );
        sb.append( "|startDate=" + ( ( startDate == null ) ? "null" : startDate.toString() ) );
        sb.append( "|endDate=" + ( ( endDate == null ) ? "null" : endDate.toString() ) );
        sb.append( "|startTime=" + startTime );
        sb.append( "|endTime=" + endTime );
        sb.append( "|pause=" + pause );
        sb.append( "|timeout=" + timeout );
        sb.append( "|checkdaySun=" + checkdaySun );
        sb.append( "|checkdayMon=" + checkdayMon );
        sb.append( "|checkdayTue=" + checkdayTue );
        sb.append( "|checkdayWed=" + checkdayWed );
        sb.append( "|checkdayThu=" + checkdayThu );
        sb.append( "|checkdayFri=" + checkdayFri );
        sb.append( "|checkdaySat=" + checkdaySat );
        sb.append( "@CheckTaskConfig)" );
        sb.append( "|sensorConfig=" + ( ( sensorConfig == null ) ? "null" : sensorConfig.toString() ) );

        return sb.toString();
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdaySun
     *            DOCUMENT_ME
     */
    public void setCheckdaySun( boolean checkdaySun ) {
        this.checkdaySun = checkdaySun;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdaySun() {
        return checkdaySun;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdayMon
     *            DOCUMENT_ME
     */
    public void setCheckdayMon( boolean checkdayMon ) {
        this.checkdayMon = checkdayMon;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdayMon() {
        return checkdayMon;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdayTue
     *            DOCUMENT_ME
     */
    public void setCheckdayTue( boolean checkdayTue ) {
        this.checkdayTue = checkdayTue;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdayTue() {
        return checkdayTue;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdayWed
     *            DOCUMENT_ME
     */
    public void setCheckdayWed( boolean checkdayWed ) {
        this.checkdayWed = checkdayWed;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdayWed() {
        return checkdayWed;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdayThu
     *            DOCUMENT_ME
     */
    public void setCheckdayThu( boolean checkdayThu ) {
        this.checkdayThu = checkdayThu;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdayThu() {
        return checkdayThu;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdayFri
     *            DOCUMENT_ME
     */
    public void setCheckdayFri( boolean checkdayFri ) {
        this.checkdayFri = checkdayFri;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdayFri() {
        return checkdayFri;
    }

    /**
     * DOCUMENT_ME
     *
     * @param checkdaySat
     *            DOCUMENT_ME
     */
    public void setCheckdaySat( boolean checkdaySat ) {
        this.checkdaySat = checkdaySat;
    }

    /**
     * DOCUMENT_ME
     *
     * @return DOCUMENT_ME
     */
    public boolean isCheckdaySat() {
        return checkdaySat;
    }
}