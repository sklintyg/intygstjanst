<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
  <title>Application Version</title>
</head>
<body>
<div style="padding-left:20px">
  <div>
    <h3>Intygstjänsten</h3>
  </div>
  <div style="width:50%">
    <h4 style="padding-bottom:5px;">Configuration info</h4>

    <div>Application version: <span><spring:message code="project.version"/></span></div>
    <div>Build number: <span><spring:message code="buildNumber"/></span></div>
    <div>Build time: <span><spring:message code="buildTime"/></span></div>
    <div>Spring profiles: <span><%= System.getProperty("spring.profiles.active") %></span></div>
  </div>
</div>
</body>
</html>