<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<body>

<h2>Shotoku service administration</h2>

<a href="admin">Refresh</a>

<hr />

<c:if test="${not empty status}">
	<b>Status message: </b> ${status}
	<hr />
</c:if>

<c:forEach var="asg" items="${asgs}">
	<c:set var="service" value="${asg.service}" />
	<table border="0">
	<tr>
		<td><b>Service name:</b></td>
		<td>${service.serviceName}</td>
	</tr>
	<tr>
		<td><b>Service description:</b></td>
		<td>${service.serviceDescription}</td>
	</tr>
	<tr>
		<td><b>Service runnable:</b></td>
		<td>
			<span style="<c:if test="${service.serviceRunnable == false}">color: red; font-weight: bold</c:if>">
			${service.serviceRunnable}</span>
		</td>
	</tr>
	<tr>
		<td><b>Last update:</b></td>
		<td>	
			<c:set var="lastUpdate" value="${(now - service.lastUpdate)/1000}" />
			<fmt:formatDate value="${service.lastUpdateDate}" type="both" dateStyle="full" timeStyle="full"/>, that is:
			<span style="<c:if test="${lastUpdate > 120}">color: red</c:if>; font-weight: bold">
				<fmt:formatNumber value="${lastUpdate}" maxFractionDigits="0" />
			</span> seconds ago.
		</td>
	</tr>
	<tr>
		<td><b>Actions:</b></td>
		<td>
			<c:choose>
				<c:when test="${service.serviceRunnable == true}">
					<a href="admin?action=stop&serviceId=${service.serviceId}">Stop</a>
        			</c:when>
			        <c:otherwise>
					<a href="admin?action=start&serviceId=${service.serviceId}">Start</a>
			        </c:otherwise>
			</c:choose>
		</td>
	</tr>
	</table>

	<hr />
</c:forEach>

</body>
</html>
