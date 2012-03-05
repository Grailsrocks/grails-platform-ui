<g:if test="${!link}">
    <div id="tab_${id}" class="tab${active ? ' active' : ''}${classes}"${ui.attributes()}>
        ${bodyContent}
    </div>
</g:if>