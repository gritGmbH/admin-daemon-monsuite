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

import static de.grit.gdi.daemon.utils.Utilities.getIpAsString;
import static de.grit.vaadin.common.EbeanUtilsCommon.getProp;
import de.grit.gdi.daemon.data.Constants;
import de.grit.gdi.daemon.data.config.CheckTaskConfig;
import de.grit.gdi.daemon.data.message.SimpleMessage;
import de.grit.gdi.daemon.messaging.MessageQueue;

public abstract class AbstractSensor implements IBasicSensor {

    /** Konfiguration des Pr\u00fcfauftrags */
    protected CheckTaskConfig ctConfig;

    public AbstractSensor() {
        super();
    }

    /**
     * Initialisierung
     */
    public void init( CheckTaskConfig ctConfig ) {
        this.ctConfig = ctConfig;
    }

    /**
     * Returns true if current thread is alive and msg is undefined
     * 
     * @param msg
     * @return
     */
    protected boolean canNext( SimpleMessage msg ) {
        if ( Thread.currentThread().isInterrupted() )
            return false;

        return msg.getCode() == Constants.RESULT_CODE_UNDEFIEND;
    }

    protected boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    /**
     * Handle current Message
     * <p>
     * - sets duration if not already done - sets result code to successful on undefined - if remTxtOnUndef = true
     * removes text from msg-codes with undefined code
     * 
     * @return true: Nachricht erfolgreich gesendet, false: Nachricht geloggt
     */
    protected boolean handleMsg( SimpleMessage msg, long start, long stop, boolean remTxtOnUndef ) {
        int duration = (int) ( ( stop - start ) / 1000 );
        if ( msg.getDuration() == 0 ) {
            msg.setDuration( duration );
        }

        if ( msg.getHost() == null ) {
            msg.setHost( getProp( "cfg.bindto", getIpAsString() ) );
        }
        msg.setTerm( stop );
        int num = msg.getCode();
        if ( msg.getCode() == Constants.RESULT_CODE_UNDEFIEND ) {
            msg.setCode( Constants.RESULT_CODE_SUCCESSFUL );
            if ( remTxtOnUndef )
                msg.setText( "" );
        }

        // Nachricht weitergeben
        // return MessageListner.getInstance().handleMessage( (SimpleMessage) msg );
        return MessageQueue.enqueue( msg );
    }
}