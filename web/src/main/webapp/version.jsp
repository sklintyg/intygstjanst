<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>
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
    <h3>Intygstjänsten</h3>
  </div>
  <div>
    <h4 style="padding-bottom:5px;">Configuration info</h4>

    <div>Application version: <span><spring:message code="project.version"/></span></div>
    <div>Build number: <span><spring:message code="buildNumber"/></span></div>
    <div>Build time: <span><spring:message code="buildTime"/></span></div>
    <div>
      Spring profiles:
      <div>
        <span style="margin-left: 20px;">From SYSTEM (Primary):</span> <span class="label label-info"><%= System.getProperty("spring.profiles.active") %></span><br/>
        <span style="margin-left: 20px;">From ENV (Secondary):</span> <span class="label label-info"><%= System.getenv("SPRING_PROFILES_ACTIVE") %></span>
      </div>
    </div>
  </div>
</div>
</body>
</html>
