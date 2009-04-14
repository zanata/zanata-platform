<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://richfaces.ajax4jsf.org/rich" prefix="rich" %>

<html>
	<head>
		<title>Shotoku Cache Administration</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<link rel="stylesheet" href="admin.css" type="text/css" />	
	</head>
	<body>
		<f:view>
			<rich:panel styleClass="header_panel">
				<h:outputText styleClass="header" value="Shotoku Cache Administration" />
			</rich:panel>
			
			<h:form>
				<a4j:poll interval="15000" 
					reRender="information,statistics,cacheitem_information,keysInUpdate,keysNotInUpdate,alerts" />
			</h:form>
			
			<h:panelGrid columns="3" styleClass="info_table">
					<rich:simpleTogglePanel switchType="client" label="Information">
						<h:panelGrid columns="2" id="information" columnClasses="left_text,right_text">
							<h:outputText value="Last update date: "/>
							<h:outputText value="#{admin.serviceLastUpdateDate}" />
								
							<h:outputText value="Last update: "/>
							<h:outputText value="#{admin.serviceLastUpdateSecondsAgo} second(s) ago" />
							
							<h:outputText value="Current data packet queue size: "/>
							<h:outputText value="#{admin.currentQueueSize}" />
							
							<h:outputText value="Number of idle threads: "/>
							<h:outputText value="#{admin.idleThreadCount}" />
							
							<h:outputText value="Number of busy threads: "/>
							<h:outputText value="#{admin.busyThreadCount}" />
						</h:panelGrid>
					</rich:simpleTogglePanel>
					
					<rich:simpleTogglePanel switchType="client" label="Statistics">
						<h:panelGrid columns="2" id="statistics" columnClasses="left_text,right_text">
							<h:outputText value="Total number of data packets processed: "/>
							<h:outputText value="#{admin.statistics.numberOfPacketsProcessed}" />
							
							<h:outputText value="Number of data packets, which threw an exception during processing: "/>
							<h:outputText value="#{admin.statistics.numberOfPacketsWithExceptions}" />
							
							<h:outputText value="Average waiting time of a data packet in the queue: "/>
							<h:outputText value="#{admin.statistics.averagePacketTimeInQueue} ms" />
							
							<h:outputText value="Average processing time of a data packet: "/>
							<h:outputText value="#{admin.statistics.averagePacketProcessingTime} ms" />
						</h:panelGrid>
					</rich:simpleTogglePanel>
				
					<h:panelGroup>
					<rich:simpleTogglePanel switchType="client" label="Configuration">
					<h:form>
					<a4j:region id="AdminServiceSubmit">
						<h:panelGrid columns="2" columnClasses="left_text,right_text">
							<h:outputText value="Interval: "/>
							<h:inputText value="#{admin.serviceInterval}" id="Interval" required="true">
								<f:validateLongRange minimum="1000" />
							</h:inputText>
							
							<h:outputText value="Update thread count: "/>
							<h:inputText value="#{admin.updateThreadCount}" id="UpdateThreadCount" required="true">
								<f:validateLongRange maximum="100" />
							</h:inputText>
							
							<h:outputText value="" />
							<a4j:commandButton value="Submit" action="#{admin.updateServiceConfig}" 
								reRender="serviceMessages" />
						</h:panelGrid>
						
						<h:panelGroup id="serviceMessages">
							<a4j:status startText="Sending ..." stopText="" for="AdminServiceSubmit" />
							<h:messages showDetail="true" />
						</h:panelGroup>
					</a4j:region>
					</h:form>
					</rich:simpleTogglePanel>
					
					<rich:simpleTogglePanel switchType="client" label="Monitor configuration" opened="false"
						style="margin-top: 5px;">
					<h:form>
					<a4j:region id="MonitorServiceSubmit">
						<h:panelGrid columns="2" columnClasses="left_text,right_text">
							<h:outputText value="Interval: "/>
							<h:inputText value="#{monitor.interval}" id="MonitorInterval" required="true">
								<f:validateLongRange minimum="1000" />
							</h:inputText>
							
							<h:outputText value="Max. number of alerts: "/>
							<h:inputText value="#{monitor.maximumNumberOfAlerts}" id="MonitorMaxAlerts" required="true">
								<f:validateLongRange maximum="100" minimum="0" />
							</h:inputText>
							
							<h:outputText value="Update alert multiplier: "/>
							<h:inputText value="#{monitor.updateAlertIntervalMultiplier}" id="MonitorUpdateAlertMultiplier" 
								required="true">
								<f:validateLongRange minimum="2" />
							</h:inputText>
							
							<h:outputText value="Update alert email: "/>
							<h:inputText value="#{monitor.alertEmail}" id="MonitorUpdateAlertEmail" />
							
							<h:outputText value="SMTP server: "/>
							<h:inputText value="#{monitor.smtpServer}" id="MonitorSmtpServer" />
							
							<h:outputText value="SMTP user: "/>
							<h:inputText value="#{monitor.smtpUser}" id="MonitorSmtpUser" />
							
							<h:outputText value="SMTP password: "/>
							<h:inputSecret value="#{monitor.smtpPassword}" id="MonitorSmtpPassword" />
							
							<h:outputText value="" />
							<a4j:commandButton value="Submit" action="#{monitor.updateServiceConfig}" 
								reRender="monitorServiceMessages" />
						</h:panelGrid>
						
						<h:panelGroup id="monitorServiceMessages">
							<a4j:status startText="Sending ..." stopText="" for="MonitorServiceSubmit" />
							<h:messages showDetail="true" />
						</h:panelGroup>
					</a4j:region>
					</h:form>
					</rich:simpleTogglePanel>
					</h:panelGroup>
			</h:panelGrid>
			
			<rich:panel styleClass="header_panel" id="cacheitem_messages">
				<h:outputText styleClass="header" value="Cache Items" />
				<h:panelGroup styleClass="header_messages">
					<h:messages showDetail="true" />
				</h:panelGroup>
				<h:panelGroup id="alerts" styleClass="header_messages">
					<h:panelGroup rendered="#{monitor.currentAlertsSize > 0}">
						<h:form>
						<a4j:region id="ClearAlertsSubmit" renderRegionOnly="false">
						<rich:panel styleClass="alert_panel">
							<h:outputText value="Current alerts:" styleClass="alert" />
							<rich:dataList var="alert" value="#{monitor.currentAlerts}">
								<h:outputText value="#{alert}" />
							</rich:dataList>
							<a4j:commandLink value="Clear all alerts" action="#{monitor.clearAlerts}" 
								reRender="alerts,cacheitem_information" ajaxSingle="true" />
						</rich:panel>
						</a4j:region>
						
						<a4j:status for="ClearAlertsSubmit" startText="Wait ..." stopText="" />
						</h:form>
					</h:panelGroup>
				</h:panelGroup>
			</rich:panel>
			
			<rich:simpleTogglePanel switchType="client" opened="true" label="Please note" styleClass="note_panel">
				<f:verbatim>
					<ul>
						<li>only cache items which contain alerts are expanded</li>
						<li>all changes in the settings will be lost on AS restart; remember to modify
							the configuration files</li>
						<li>checking if there are any new cache items requires a page refresh</li>
						<li>a 0 interval in cache item configuration means that the cache item is updated on each
							service update</li>
					</ul>
				</f:verbatim>
			</rich:simpleTogglePanel>
			
			<rich:dataGrid columns="1" var="cacheItem" value="#{admin.cacheItems}" styleClass="cacheitem_table" id="cacheitems">
					<rich:simpleTogglePanel switchType="client" label="#{cacheItem.name}"
						opened="#{cacheItem.alerts != null}">
						<h:panelGrid columns="2">
							<rich:simpleTogglePanel switchType="client" label="Information">
								<h:panelGrid columns="2" columnClasses="left_text,right_text" id="cacheitem_information">
									<h:outputText value="Name: "/>
									<h:outputText value="#{cacheItem.name}" />
									
									<h:outputText value="Info: "/>
									<h:outputText value="#{cacheItem.info}" />
								
									<h:outputText value="FQN: "/>
									<h:outputText value="#{cacheItem.fqn}" />		
								
									<h:outputText value="FQN keys count: "/>
									<h:outputText value="#{cacheItem.fqnKeysCount}" />		
								 
									<h:outputText rendered="#{cacheItem.alerts != null}" styleClass="alert"
										value="Alert(s): "/>
									<rich:dataList rendered="#{cacheItem.alerts != null}" styleClass="alert"
										value="#{cacheItem.alerts}" var="alert">
										<h:outputText value="#{alert.timeFormatted} (#{alert.key}) #{alert.description}" />
										<h:panelGrid columns="1" rendered="#{alert.cause != null}">
											<h:outputText styleClass="plain_text" value="#{alert.cause}" />
										</h:panelGrid>
									</rich:dataList>					
								</h:panelGrid>
							</rich:simpleTogglePanel>
							
							<rich:simpleTogglePanel switchType="client" label="Configuration">
							<h:form>
								<h:inputHidden value="#{cacheItem.id}" />
								
								<h:panelGrid columns="2" columnClasses="left_text,right_text">
									<h:outputText value="Interval: "/>
									<h:inputText value="#{cacheItem.interval}" id="Interval" required="true">
										<f:validateLongRange />
									</h:inputText>
							
									<h:outputText value="" />
									<a4j:commandButton value="Submit" action="#{cacheItem.updateConfig}" 
										reRender="cacheitem_messages" />
								</h:panelGrid>
							</h:form>
							</rich:simpleTogglePanel>
							
							<rich:simpleTogglePanel switchType="client" label="Last updates of keys">
								<h:dataTable var="key" id="keysNotInUpdate" value="#{cacheItem.keysNotDuringUpdate}">
									<h:column>
										<f:facet name="header"><h:outputText value="Key"/></f:facet>
										<h:outputText value="#{key}" />
									</h:column>
									<h:column>
										<f:facet name="header"><h:outputText value="Last update"/></f:facet>
										<h:outputText value="#{cacheItem.lastUpdatesAgo[key]} second(s) ago" />
									</h:column>
								</h:dataTable>
							</rich:simpleTogglePanel>
							
							<rich:simpleTogglePanel switchType="client" label="Keys during update">
								<h:panelGroup id="keysInUpdate">
								<h:form>
								<h:inputHidden value="#{cacheItem.id}" />
								
								<h:dataTable var="key" value="#{cacheItem.keysDuringUpdate}"
									binding="#{admin.keysInUpdateData}">
									<h:column>
										<f:facet name="header"><h:outputText value="Key"/></f:facet>
										<h:outputText value="#{key}" />
									</h:column>
									<h:column>
										<f:facet name="header"><h:outputText value="In update since"/></f:facet>
										<h:outputText value="#{cacheItem.lastUpdatesAgo[key]} second(s)" />
									</h:column>
									<h:column>
										<a4j:commandLink value="Reset" action="#{cacheItem.reset}" 
											reRender="cacheitem_messages,keysInUpdate,keysNotInUpdate" />
									</h:column>
								</h:dataTable>
								</h:form>
								</h:panelGroup>
							</rich:simpleTogglePanel>
						</h:panelGrid>
					</rich:simpleTogglePanel>
			</rich:dataGrid>
		</f:view>
	</body>
</html>
