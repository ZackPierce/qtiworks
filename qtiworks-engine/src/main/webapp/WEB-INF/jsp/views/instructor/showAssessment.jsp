<%--

Copyright (c) 2012-2013, The University of Edinburgh.
All Rights Reserved

Shows information about a particular Assessment

--%>
<%@ include file="/WEB-INF/jsp/includes/pageheader.jspf" %>
<page:page title="Assessment details">

  <header class="actionHeader">
    <nav class="breadcrumbs">
      <a href="${utils:escapeLink(primaryRouting['dashboard'])}">QTIWorks Dashboard</a> &#xbb;
      <a href="${utils:escapeLink(primaryRouting['listAssessments'])}">Assessment Manager</a> &#xbb;
    </nav>
    <h2>
      <span class="assessmentLabel">Assessment&#xa0;${utils:formatAssessmentType(assessment)}</span>
      ${fn:escapeXml(assessmentPackage.fileName)}
      <span class="assessmentTitle"> [${fn:escapeXml(assessmentPackage.title)}]</span>
    </h2>
    <div class="hints">
      <p>
        This page lets you manage this Assessment and drill down into the management of the Deliveries of
        this Assessment. You can view its validation status and try the Assessment
        out (unless it has a lot of errors). You may also configure how LTI outcomes should be returned for
        this Assessment.
      </p>
    <div>
  </header>

  <table class="dashboard">
    <tbody>
      <%-- Launchability status --%>
      <c:set var="status" value="${assessmentPackage.launchable ? 'statusGood' : 'statusError'}"/>
      <tr class="${status}">
        <td class="indicator"></td>
        <td class="category">
          <div class="name">Launchability:</div>
          <div class="value">
            <c:choose>
              <c:when test="${assessmentPackage.launchable}">
              This Assessment can be launched
              <c:if test="${!assessmentPackage.valid}"> but has validation issues so may not work correctly</c:if>
              </c:when>
              <c:otherwise>This assessment has too many errors and cannot be launched</c:otherwise>
            </c:choose>
          </div>
        </td>
        <td class="actions">
          <page:postLink path="${assessmentRouting['try']}" title="Try Out"/>
        </td>
      </tr>
      <%-- Validation status --%>
      <c:set var="status">
        <c:choose>
          <c:when test="${assessmentPackage.valid}">statusGood</c:when>
          <c:when test="${assessmentPackage.errorCount > 0}">statusError</c:when>
          <c:when test="${assessmentPackage.warningCount > 0}">statusWarning</c:when>
        </c:choose>
      </c:set>
      <tr class="${status}">
        <td class="indicator"></td>
        <td class="category">
          <div class="name">Validation status:</div>
          <div class="value">
            <c:choose>
              <c:when test="${assessmentPackage.valid}">
                All validation tests successful
              </c:when>
              <c:when test="${assessmentPackage.errorCount > 0}">
                ${assessmentPackage.errorCount}&#xa0;validation&#xa0;
                ${assessmentPackage.errorCount > 1 ? 'errors' : 'error'}
              </c:when>
              <c:when test="${assessmentPackage.warningCount > 0}">
                ${assessmentPackage.warningCount}&#xa0;validation&#xa0;
                ${assessmentPackage.warningCount > 1 ? 'warnings' : 'warning'}
              </c:when>
            </c:choose>
          </div>
        </td>
        <td class="actions">
          <a href="${utils:escapeLink(assessmentRouting['validate'])}">Show&#xa0;validation&#xa0;status</a>
        </td>
      </tr>
      <%-- Package info --%>
      <tr class="statusOk">
        <td class="indicator"></td>
        <td class="category">
          <div class="name">Assessment Package Details:</div>
          <div class="value">
            Version #${assessment.packageImportVersion}
            uploaded from
            <c:choose>
              <c:when test="${assessmentPackage.importType=='CONTENT_PACKAGE'}">IMS Content Package</c:when>
              <c:when test="${assessmentPackage.importType=='STANDALONE_ITEM_XML'}">Standalone Item XML</c:when>
              <c:otherwise>(System sample)</c:otherwise>
            </c:choose>
            on ${utils:formatDayDateAndTime(assessmentPackage.creationTime)}
          </div>
        </td>
        <td class="actions">
          <a href="${utils:escapeLink(assessmentRouting['replace'])}">Replace&#xa0;Package&#xa0;Content</a>
        </td>
      </tr>
      <%-- LTI outcomes --%>
      <c:set var="status" value="${empty assessment.ltiResultOutcomeIdentifier ? 'statusError' : 'statusGood'}"/>
      <tr class="${status}">
        <td class="indicator"></td>
        <td class="category">
          <div class="name">LTI Outcomes Reporting Setup:</div>
          <div class="value">
            <c:choose>
              <c:when test="${!empty assessment.ltiResultOutcomeIdentifier}">
                Reporting outcome <code>${assessment.ltiResultOutcomeIdentifier}</code>
                with range [${assessment.ltiResultMinimum}..${assessment.ltiResultMaximum}]
              </c:when>
              <c:otherwise>
                Not set up. Outcomes cannot be returned for this Assessment until set up
              </c:otherwise>
            </c:choose>
          </div>
        </td>
        <td class="actions">
          <a href="${utils:escapeLink(assessmentRouting['outcomesSettings'])}">Set&#xa0;up&#xa0;LTI&#xa0;outcomes</a>
        </td>
      </tr>
    </tbody>
  </table>

  <c:if test="${assessmentPackage.launchable && !empty deliverySettingsList}">
    <div>Try out using Delivery Settings:</div>
    <ul class="menu">
      <c:forEach var="deliverySettings" items="${deliverySettingsList}">
        <li>
          <page:postLink path="${assessmentRouting['try']}/${deliverySettings.id}" title="${fn:escapeXml(deliverySettings.title)}"/>
        </li>
      </c:forEach>
    </ul>
  </c:if>

  <h3><span class="deliveryLabel">Delivery</span> Assessment Deliveries</h3>
  <div class="hints">
    <p>
      Each Delivery you have created for this Assessment is listed below. You can create new Deliveries to make this Assessment
      available to candidates via a simple LTI launch link.
    </p>
  </div>
  <table class="listTable">
    <thead>
      <th></th>
      <th>Title</th>
      <th>Available to candidates?</th>
      <th>Selected Delivery Settings</th>
    </thead>
    <tbody>
      <c:forEach var="delivery" items="${deliveryList}" varStatus="loopStatus">
        <tr>
          <td align="center">
            <div class="bigStatus">${loopStatus.index + 1}</div>
          </td>
          <td align="center">
            <a href="${utils:escapeLink(deliveryListRouting[delivery.id]['show'])}"><c:out value="${delivery.title}"/></a>
          </td>
          <td align="center">
            ${delivery.open ? 'Yes' : 'No' }
          </td>
          <td align="center">
            <c:choose>
              <c:when test="${empty deliverySettings}">
                (Using QTIWorks default Delivery Settings)
              </c:when>
              <c:otherwise>
                Using '${fn:escapeXml(deliverySettings.title)}'
              </c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
      <tr>
        <td class="plus"></td>
        <td colspan="3" class="actions">
          <a href="${utils:escapeLink(assessmentRouting['createDelivery'])}">Create new Delivery</a>
        </td>
      </tr>
    </tbody>
  </table>

  <div class="floatRight actions scary">
    <c:set var="nonTerminatedSessionCount" value="${assessmentStatusReport.nonTerminatedSessionCount}" scope="request"/>
    <page:postLink path="${assessmentRouting['delete']}"
      confirm="Are you sure? This will permanently delete the Assessment and all data gathered about it. There are currently ${nonTerminatedSessionCount} candidate sessions(s) running on this Assessment."
      title="Delete this Assessment"/>
  </div>

</page:page>
