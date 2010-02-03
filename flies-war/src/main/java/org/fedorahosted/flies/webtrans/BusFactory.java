package org.fedorahosted.flies.webtrans;

import org.jboss.errai.bus.client.MessageBus;

public class BusFactory {
	private static MessageBus messageBus;
	
	public static void initMessageBus(MessageBus bus) {
		messageBus = bus;
	}
	public static MessageBus getMessageBus() {
		return messageBus;
	}
}
