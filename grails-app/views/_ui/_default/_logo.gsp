<%--
  Tag: ui.logo
  Usage: Render application logo image at default or fixed size
  
  Available variables:
  
  classes - String of classes supplied
  logoClass - Class to apply to the logo element
  uiArgs
--%>
<g:if test="${logoUri}">
    <a href="${ui.siteURL().encodeAsHTML()}"><r:img uri="${logoUri.encodeAsHTML()}" class="${ui.joinClasses(values:[classes, logoClass])}" alt="${applicationName.encodeAsHTML()}" width="${w}" height="${h}"/></a>
</g:if>
<g:else>
    <a href="${ui.siteURL().encodeAsHTML()}" class="${classes}"><h1>${applicationName.encodeAsHTML()}</h1></a>
</g:else>
