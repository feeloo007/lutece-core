<%@ page errorPage="../ErrorPage.jsp" %>
<jsp:include page="../PortletAdminHeader.jsp" />

<jsp:useBean id="insertServiceSelector" scope="session" class="fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean" />

<% insertServiceSelector.init( request , "CORE_ADMIN_SITE" ); %>
<%= insertServiceSelector.doInsertIntoElement( request ) %>


<%@ include file="../AdminFooter.jsp" %>