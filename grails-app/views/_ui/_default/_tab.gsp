<g:if test="${!link}">
    <div id="tab_${id}" class="${p.joinClasses(values:[active ? ' active' : '', tabClass, classes])}"${ui.attributes()}>
        ${bodyContent}
    </div>
</g:if>
