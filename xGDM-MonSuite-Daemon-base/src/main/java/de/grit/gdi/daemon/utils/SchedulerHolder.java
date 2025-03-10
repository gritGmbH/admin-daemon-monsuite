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
package de.grit.gdi.daemon.utils;

import java.io.Serializable;

import org.quartz.Scheduler;

public class SchedulerHolder implements Serializable {

    private static final long serialVersionUID = -7036907080449665670L;

    public static final String CONTEXT_KEY = SchedulerHolder.class.getName() + ".holder";

    private transient Scheduler scheduler;

    public SchedulerHolder( Scheduler scheduler ) {
        this.scheduler = scheduler;
    }

    public SchedulerHolder() {
        //
    }

    /**
     * The current Scheduler
     * 
     * @return the scheduler or null if not available
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler( Scheduler scheduler ) {
        this.scheduler = scheduler;
    }
}