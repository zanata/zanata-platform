/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.shotoku.cache;

import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An update thread, which reads data packets from a queue, executes an update
 * on them, and reports on the statistics and possible errors.
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class UpdateThread extends Thread {
    private static final Logger log = Logger.getLogger(UpdateThread.class);
    
    private BlockingQueue<UpdateThreadData<?,?>> queue;
    private RenewableCacheServiceMBean service;

    public UpdateThread(RenewableCacheServiceMBean service, LinkedBlockingQueue<UpdateThreadData<?,?>> queue) {
        this.queue = queue;
        this.service = service;

        setDaemon(true);
    }

    public void run() {
    	service.reportThreadNew();
    	
        while (true) {
            UpdateThreadData<?,?> data;
            try {
                data = queue.take();
                service.reportThreadBusy();
                
                long start = System.currentTimeMillis();
                service.getStatistics().addPacketWaitingTime(start - data.getCreateTime());
                
                if (data instanceof SignalExitUpdateThreadData<?,?>) {
                	service.reportThreadExit();
                	break;
                }
                
                try {
                    data.execute();
                    data.executeOk();
                    service.getStatistics().addPacketProcessingTime(System.currentTimeMillis() - start, false);
                } catch (Throwable t) {
                	data.executeWithException(t);
                    service.getStatistics().addPacketProcessingTime(System.currentTimeMillis() - start, true);
                }
                
                service.reportThreadIdle();
            } catch (InterruptedException e) {
                log.error("Update thread interrupted.", e);
            }
        }
    }
}
