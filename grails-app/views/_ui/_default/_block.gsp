<div class="block${classes}" ${ui.attributes()}>
	<g:if test="${title}">
	     <ui:h2>${title.encodeAsHTML()}</ui:h2>
 	</g:if>
    ${bodyContent}
</div>
