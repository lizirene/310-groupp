<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<nav class="appbar" role="navigation">
	<div class="nav-wrapper">
		<div class="logo">USC CS 310 Stock Portfolio Management</div>
		<c:if test="${fn:endsWith(pageContext.request.requestURI, '/portfolio.jsp')}">
			<!-- Logout button -->
      <ul class="right">
				<li><a class="btn waves-effect waves-light red" href="/portfolio/logout">
					Log out<i class="material-icons right hide-on-med-and-down">exit_to_app</i>
				</a></li>
			</ul>
		</c:if>
	</div>
</nav>