<div id="${id}" class="${p.joinClasses(values:[carouselClass, classes])}" ${ui.attributes()}>
    <g:each in="${slides}" var="s">
        ${s.bodyContent}
    </g:each>
</div>