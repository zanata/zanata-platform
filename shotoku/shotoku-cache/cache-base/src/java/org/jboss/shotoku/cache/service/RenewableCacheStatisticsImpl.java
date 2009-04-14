package org.jboss.shotoku.cache.service;

public class RenewableCacheStatisticsImpl implements RenewableCacheStatistics {
	private long allStartedPackets;
	private long allProcessedPackets;
	private long packetsWithErrors;
	private long totalProcessingTime;
	private long totalWaitingTime;
	
	public void addPacketProcessingTime(long elapsedTime, boolean exception) {
		totalProcessingTime += elapsedTime;
		
		allProcessedPackets++;
		
		if (exception) {
			packetsWithErrors++;
		}
	}

	public void addPacketWaitingTime(long elapsedTime) {
		totalWaitingTime += elapsedTime;
		
		allStartedPackets++;
	}

	public long getAveragePacketTimeInQueue() {
		if (allStartedPackets == 0) {
			return 0;
		}
		
		return totalWaitingTime/allStartedPackets;
	}

	public long getAveragePacketProcessingTime() {
		if (allProcessedPackets == 0) {
			return 0;
		}
		
		return totalProcessingTime/allProcessedPackets;
	}

	public long getNumberOfPacketsProcessed() {
		return allProcessedPackets;
	}

	public long getNumberOfPacketsWithExceptions() {
		return packetsWithErrors;
	}
}
