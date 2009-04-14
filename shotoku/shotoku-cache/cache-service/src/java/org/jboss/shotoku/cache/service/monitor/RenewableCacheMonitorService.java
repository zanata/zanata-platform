package org.jboss.shotoku.cache.service.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.jboss.shotoku.cache.CacheItemOperations;
import org.jboss.shotoku.cache.service.RenewableCacheServiceMBean;
import org.jboss.shotoku.tools.CacheTools;

/**
 * 
 * @author <a href="mailto:adam.warski@jboss.org">Adam Warski</a>
 */
public class RenewableCacheMonitorService implements RenewableCacheMonitorServiceMBean {
	private final static Logger log = Logger.getLogger(RenewableCacheMonitorServiceMBean.class);
	
	private long interval;
	private RenewableCacheServiceMBean renewableCacheService;
	private int maximumNumberOfAlerts;
	private int updateAlertIntervalMultiplier;
	
	private String alertEmail;
	private String smtpPassword;
	private String smtpUser;
	private String smtpServer;
	
	/**
	 * A flag that supresses exceptions after the first one, when sending e-mails, until
	 * configuration is changed.
	 */
	private volatile boolean exceptionInEmail;

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public int getMaximumNumberOfAlerts() {
		return maximumNumberOfAlerts;
	}

	public void setMaximumNumberOfAlerts(int maximumNumberOfAlerts) {
		this.maximumNumberOfAlerts = maximumNumberOfAlerts;
	}

	public RenewableCacheServiceMBean getRenewableCacheService() {
		return renewableCacheService;
	}

	public void setRenewableCacheService(
			RenewableCacheServiceMBean renewableCacheService) {
		this.renewableCacheService = renewableCacheService;
	}

	public int getUpdateAlertIntervalMultiplier() {
		return updateAlertIntervalMultiplier;
	}

	public void setUpdateAlertIntervalMultiplier(int updateAlertIntervalMultiplier) {
		this.updateAlertIntervalMultiplier = updateAlertIntervalMultiplier;
	}
	
	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
		exceptionInEmail = false;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
		exceptionInEmail = false;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
		exceptionInEmail = false;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
		exceptionInEmail = false;
	}
	
	//

	private Thread monitorThread;
	
	public void start() {
		monitorThread = new Thread() {
            {
                setDaemon(true);
            }

            public void run() {
                while (true) {
                    try {
                        sleep(getInterval());
                    } catch (InterruptedException e) {
                    	// Quit.
                        log.info("Stopping monitor thread (interrupted).");
                        return;
                    }

                    try {
                        update();
                    } catch (Throwable t) {
                        // Making sure that an exception won't stop the thread.
                        log.error("Monitor update method threw an exception.", t);
                    }
                }
            }
        };
        
        log.info("Starting monitor thread.");
        monitorThread.start();
	}

	public void stop() {
		if (monitorThread != null) {
			log.info("Signalling monitor thread to stop.");
			monitorThread.interrupt();
		}
	}

	//
	
	private Set<CacheItemOperations<?,?>> cacheItemsWithAlerts = new HashSet<CacheItemOperations<?,?>>();
	private Map<CacheItemOperations<?,?>, Set<CacheAlert>> alertsForCacheItems =
		new HashMap<CacheItemOperations<?,?>, Set<CacheAlert>>();
	
	private int currentNumberOfAlerts;
	
	private synchronized int getCurrentNumberOfAlerts() {
		return currentNumberOfAlerts;
	}
	
	public void update() {
		long now = System.currentTimeMillis();
		
		if (getCurrentNumberOfAlerts() == getMaximumNumberOfAlerts()) {
			// We can't add an alert anyway.
			return;
		}
		
		for (CacheItemOperations<?,?> rcid : getRenewableCacheService().getCacheItemsOperations()) {
			// Checking for keys that are in update for too long/ haven't been updated for a long
			// time.
			Set<?> keysDuringUpdate = rcid.getKeysDuringUpdate();
			Map<?, Long> keysUpdates = rcid.getKeysUpdates();
			// Calculating the effective interval of updates of the cache item.
			long interval = rcid.getInterval();
			if (interval == 0) {
				interval = getRenewableCacheService().getInterval();
			}
			
			for (Object key : keysUpdates.keySet()) {
				long keyUpdate = keysUpdates.get(key);
				if (keyUpdate == 0) {
					// The key was initialized, but with a dummy value.
					continue;
				}
				
				if (now - keyUpdate > interval*getUpdateAlertIntervalMultiplier()) {
					// Issuing an alert.
					if (keysDuringUpdate.contains(key)) {
						addAlertForCacheItem(rcid, CacheAlertFactory.createAlertKeyTooLongInUpdate(key));
					} else {
						addAlertForCacheItem(rcid, CacheAlertFactory.createAlertKeyNotUpdated(key));
					}
				}
			}
			
			// Checking for exceptions.
			Map<?, Throwable> keysExceptions = rcid.getKeysExceptions();
			for (Object key : keysExceptions.keySet()) {
				addAlertForCacheItem(rcid, CacheAlertFactory.createAlertKeyException(key, keysExceptions.get(key)));
			}
		}
	}
	
	private synchronized void addAlertForCacheItem(CacheItemOperations<?,?> rcid, CacheAlert alert) {
		Set<CacheAlert> alertsForCacheItem = alertsForCacheItems.get(rcid);
	
		if (alertsForCacheItem == null) {
			alertsForCacheItem = new HashSet<CacheAlert>();
			alertsForCacheItems.put(rcid, alertsForCacheItem);
		}
		
		if (alertsForCacheItem.add(alert)) {
			// The alert that we were supposed to add is a real alert.
			currentNumberOfAlerts++;
			
			if (!CacheTools.isEmpty(getAlertEmail())) {
				sendAlertEmail(rcid, alert);
			}
		}
		
		cacheItemsWithAlerts.add(rcid);
	}

	private static Comparator<CacheAlert> cacheAlertComparator = new Comparator<CacheAlert>() {
		public int compare(CacheAlert ca1, CacheAlert ca2) {
			if (ca1.getTime() == ca2.getTime()) {
				return 0;
			}
			
			if (ca1.getTime() < ca2.getTime()) {
				return 1;
			}
			
			return -1;
		}
	};
	
	public synchronized List<CacheAlert> getAlertsForCacheItem(CacheItemOperations<?,?> rcid) {
		Set<CacheAlert> alertsForCacheItem = alertsForCacheItems.get(rcid);
		if (alertsForCacheItem == null) {
			return null;
		}
		
		// Turning the set into a list and sorting it.
		List<CacheAlert> alertsList = new ArrayList<CacheAlert>(alertsForCacheItem);
		Collections.sort(alertsList, cacheAlertComparator);
		return alertsList;
	}

	public synchronized Set<CacheItemOperations<?,?>> getCacheItemsWithAlerts() {
		return cacheItemsWithAlerts;
	}

	public synchronized  void clearAlerts() {
		currentNumberOfAlerts = 0;
		
		cacheItemsWithAlerts.clear();
		alertsForCacheItems.clear();
		
		// Clearing all exceptions in cache items.
		for (CacheItemOperations<?,?> rcid : getRenewableCacheService().getCacheItemsOperations()) {
			rcid.getKeysExceptions().clear();
		}
	}

	private void sendAlertEmail(CacheItemOperations<?,?> rcid, CacheAlert alert) {
		boolean smtpAuth = !CacheTools.isEmpty(getSmtpUser());
		
		Properties props = new Properties();
	    props.put("mail.smtp.host", getSmtpServer());
	    if (smtpAuth) {
	    	props.put("mail.smtp.auth", "true");
	    } else {
	    	props.put("mail.smtp.auth", "false");
	    }
	    
	    try {
	    	Session session = Session.getDefaultInstance(props, null);
	    	Transport transport = session.getTransport("smtp");
	    	transport.connect(getSmtpServer(), getSmtpUser(), getSmtpPassword());
	    	
	    	Message msg = new MimeMessage(session);
	        msg.setFrom(InternetAddress.parse("Shotoku Cache Admin <do-not-reply@jboss.org>", false)[0]);
	        msg.setHeader("X-Mailer", "Shotoku Cache Admin Mailer");
	        msg.setSentDate(new Date());
	        msg.setRecipients(Message.RecipientType.TO,
	            InternetAddress.parse(getAlertEmail(), false));

	        msg.setSubject("Shotoku Cache Admin alert!");
	        msg.setText("Alert in cache:\n" +
	        		"Cache item name: " + rcid.getName() + "\n" +
	        		"Key: " + alert.getKey() + "\n" +
	        		"Date: " + alert.getTimeFormatted() + "\n" +
	        		"Description: " + alert.getDescription() + "\n" +
	        		"Cause:\n" + alert.getCause());
	        transport.sendMessage(msg, msg.getAllRecipients());
	    } catch (MessagingException e) {
	    	if (!exceptionInEmail) {
	    		log.error(e);
	    		exceptionInEmail = true;
	    	}
	    }
	}
}
