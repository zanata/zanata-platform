package org.jboss.shotoku.cache.service;

/**
 * 
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public interface RenewableCacheStatistics {
	public void addPacketWaitingTime(long elapsedTime);
	public void addPacketProcessingTime(long elapsedTime, boolean exception);
	
	public long getAveragePacketTimeInQueue();
	public long getAveragePacketProcessingTime();
	public long getNumberOfPacketsProcessed();
	public long getNumberOfPacketsWithExceptions();
}
