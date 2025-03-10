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
package de.grit.gdi.daemon.monitoring.sensor;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.config.OafSensorConfig;
import de.grit.gdi.daemon.data.message.CheckTaskMessage;
import de.grit.gdi.daemon.utils.OafHelp;
import de.grit.vaadin.common.Pair;

public class OafSensor extends AbstractOGCSensor {

    /** Sensorkonfiguration */
    private OafSensorConfig config;

    /** Log4J Logger */
    private final Logger LOG = LoggerFactory.getLogger( "monitor.sensor.oafsensor" );

    /** Nachrichtenstatus des Sensors */
    private boolean msgStatus;

    /**
     * Initialisierung des Sensors
     * 
     * @param config
     *            objekt von CheckTaskConfig
     */
    public void init( CheckTaskConfig ctConfig ) {

        super.init( ctConfig );

        if ( !( ctConfig.getSensor() instanceof OafSensorConfig ) ) {
            throw new IllegalArgumentException();
        }

        this.config = (OafSensorConfig) ctConfig.getSensor();

        this.msgStatus = true;

    }

    /**
     * Pruefauftrag Konfiguration zur\u00e4ckgeben
     * 
     * @return Konfiguration des Pr\u00e4fauftrag
     */
    public CheckTaskConfig getCheckTaskConfig() {
        return ctConfig;
    }

    /**
     * Nachrichtenstatus zur\u00e4ckgeben
     * 
     * @return Nachrichten erfolgreich zugestellt
     */
    public boolean getMessageStatus() {
        return this.msgStatus;
    }

    /**
     * ThreadNamen an den Worker zur\u00e4ckgeben
     * 
     * @return String Threadnamen
     */
    public String getThreadName() {
        return config.getId() + "-" + config.getName();
    }

    /**
     * Logger an den Worker zur\u00e4ckgeben
     * 
     * @return Logger Log4J Logger
     */
    public Logger getLogger() {
        return LOG;
    }

    /**
     * Funktion um die Collection und die Features zu pr\u00e4fen
     */
    public void run() {

        long start = System.currentTimeMillis();
        CheckTaskMessage msg = new CheckTaskMessage();
        msg.setCheckTaksId( ctConfig.getTaskId() );
        String noFoundCollection = "";
        int code = 0;
        OafHelp help = new OafHelp();
        if ( canNext( msg ) ) {

            LOG.debug( "vailable: {}", config.getCollectionidAvailable() );
            LOG.debug( "content: {}", config.getCollectionidContent() );
            LOG.debug( "featureID: {}", config.getFeatureid() );
            // Verfuegbarkeit Pruefung
            if ( config.getCollectionidAvailable() != null && !config.getCollectionidAvailable().isEmpty() ) {
                String[] collectionArray = config.getCollectionidAvailable().split( "\\|" );

                JSONArray collection = help.collectionsArray( config.getUrl(), httpwrp );

                if ( collection == null ) {
                    code = Constants.RESULT_CODE_CONNECTION_TIMEOUT;
                    nachricht( noFoundCollection, msg, code );
                    msg.setCode( code );
                } else {
                    for ( String coolection : collectionArray ) {

                        boolean collections = help.toStream( collection ).anyMatch( col -> ( col.get( "id" ).equals( coolection )
                                                                                             && col.get( "itemType" ).equals( "feature" ) ) );
                        if ( !collections ) {
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            code = msg.getCode();
                            noFoundCollection = noFoundCollection + "  " + coolection;
                        }
                    }
                    nachricht( noFoundCollection, msg, code );
                }

            } // Inhaltpruefung
            else if ( config.getCollectionidContent() != null && !config.getCollectionidContent().isEmpty() ) {

                String[] collectionArray = config.getCollectionidContent().split( "\\|" );

                JSONArray collection = help.collectionsArray( config.getUrl(), httpwrp );

                if ( collection == null ) {
                    code = Constants.RESULT_CODE_CONNECTION_TIMEOUT;
                    nachricht( noFoundCollection, msg, code );
                    msg.setCode( code );
                } else {
                    JSONObject feature = null;
                    for ( String coolection : collectionArray ) {
                        List<JSONObject> featureCollection = help.toStream( collection ).filter( col -> col.get( "id" ).equals( coolection ) ).collect( Collectors.toList() );
                        if ( featureCollection.isEmpty() ) {
                            noFoundCollection = noFoundCollection + "  " + coolection;
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            code = msg.getCode();
                        } else {
                            feature = help.jsonCollection( featureCollection.get( 0 ), httpwrp );
                            if ( feature.getJSONArray( "features" ).isEmpty() ) {
                                msg.setCode( Constants.RESULT_EMPTY_ARRAY );
                                code = msg.getCode();
                                noFoundCollection = noFoundCollection + "  " + coolection;
                            }
                        }

                    }
                    nachricht( noFoundCollection, msg, code );
                }

            } // Feature Pruefung
            else if ( config.getFeatureid() != null && !config.getFeatureid().isEmpty() ) {

                String[] featureArray = config.getFeatureid().split( "\\|" );

                JSONArray collection = help.collectionsArray( config.getUrl(), httpwrp );

                if ( collection == null ) {
                    code = Constants.RESULT_CODE_CONNECTION_TIMEOUT;
                    nachricht( noFoundCollection, msg, code );
                    msg.setCode( code );
                } else {
                    JSONObject feature = null;
                    for ( String fture : featureArray ) {
                        Pair<String, String> splitedFeature = splite( fture );
                        List<JSONObject> featureCollection = help.toStream( collection ).filter( col -> col.get( "id" ).equals( splitedFeature.first ) ).collect( Collectors.toList() );

                        if ( featureCollection.isEmpty() ) {
                            noFoundCollection = noFoundCollection + "  " + splitedFeature.first;
                            msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                            code = msg.getCode();
                        } else {
                            feature = help.jsonFeature( featureCollection.get( 0 ), httpwrp, splitedFeature.second );
                            if ( feature == null ) {
                                msg.setCode( Constants.RESULT_CODE_FAILED_WRONG_DATA );
                                code = msg.getCode();
                                noFoundCollection = noFoundCollection + "  " + splitedFeature.second;
                            }
                        }
                    }
                    nachricht( noFoundCollection, msg, code );
                }

            }

            httpwrp.cleanup();
        }

        if ( !isInterrupted() ) {
            long stop = System.currentTimeMillis();
            this.msgStatus = handleMsg( msg, start, stop, true );
        }
    }

    /**
     * Setzt die Nachricht, wenn eine collection oder ein Feature nicht gefunden wurde
     */
    private void nachricht( String noFoundservice, CheckTaskMessage msg, int code ) {
        if ( code != 0 ) {
            msg.setCode( code );
            if ( config.getCollectionidAvailable() != null && !config.getCollectionidAvailable().isEmpty() ) {

                if ( code == Constants.RESULT_CODE_CONNECTION_TIMEOUT ) {
                    msg.setText( "URL zur Landing Page nicht erreichbar" );
                    msg.setCode( code );
                } else {
                    String message = noFoundservice.split( "\\s+" ).length == 2 ? "Collection: " + noFoundservice
                                                                                  + " wurde nicht gefunden"
                                                                                : "Collections: " + noFoundservice
                                                                                  + " wurden nicht gefunden";
                    msg.setText( message );
                }
            } else if ( config.getCollectionidContent() != null && !config.getCollectionidContent().isEmpty() ) {
                if ( code == Constants.RESULT_CODE_CONNECTION_TIMEOUT ) {
                    msg.setText( "URL zur Landing Page nicht erreichbar" );
                    msg.setCode( code );
                } else if ( code != Constants.RESULT_EMPTY_ARRAY ) {
                    String message = noFoundservice.split( "\\s+" ).length == 2 ? "Collection: " + noFoundservice
                                                                                  + " wurde nicht gefunden"
                                                                                : "Collections: " + noFoundservice
                                                                                  + " wurden nicht gefunden";
                    msg.setText( message );
                } else if ( code == Constants.RESULT_EMPTY_ARRAY ) {
                    String message = noFoundservice.split( "\\s+" ).length == 2 ? "Collection: " + noFoundservice
                                                                                  + " hat keine Feature"
                                                                                : "Collections: " + noFoundservice
                                                                                  + " haben keine Feature";
                    msg.setText( message );
                }

            } else if ( config.getFeatureid() != null && !config.getFeatureid().isEmpty() ) {
                if ( code == Constants.RESULT_CODE_CONNECTION_TIMEOUT ) {
                    msg.setText( "URL zur Landing Page nicht erreichbar" );
                    msg.setCode( code );
                } else if ( code == Constants.RESULT_CODE_FAILED_WRONG_DATA ) {
                    String message = noFoundservice.split( "\\s+" ).length == 2 ? "Feature: " + noFoundservice
                                                                                  + " wurde nicht gefunden"
                                                                                : "Features: " + noFoundservice
                                                                                  + " wurden nicht gefunden";
                    msg.setText( message );
                }
            }

        }
    }

    private Pair<String, String> splite( String s ) {
        String feature = s.split( "\\}" )[1];
        String collection = s.split( "\\}" )[0].substring( 1 );
        return new Pair<>( collection, feature );
    }

}