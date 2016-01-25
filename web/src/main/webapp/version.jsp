<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%--
  ~ Copyright (C) 2016 Inera AB (http://www.inera.se)
  ~
  ~ This file is part of sklintyg (https://github.com/sklintyg).
  ~
  ~ sklintyg is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ sklintyg is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<html>
<head>
  <title>Application Version</title>
</head>
<body>
<div style="padding-left:20px">
  <div>
    <h3>Intygstj�nsten</h3>
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
