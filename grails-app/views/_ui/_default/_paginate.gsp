<ul class="${p.joinClasses(values:[paginateClass, classes])}">
    <g:if test="${prev}">
        <li><a href="${prev.link}" class="prev">${prev.text}</a></li>
    </g:if>
    <g:if test="${earlier}">
        <li><a href="${earlier.link}" class="earlier">${earlier.text}</a></li>
    </g:if>
    <g:each in="${items}" var="i">
        <li><g:if test="${i.active}">
                ${i.text}
            </g:if>
            <g:else>
                <a href="${i.link}">${i.text}</a>
            </g:else>
        </li>
    </g:each>
    <g:if test="${later}">
        <li><a href="${later.link}" class="later">${later.text}</a></li>
    </g:if>
    <g:if test="${next}">
        <li><a href="${next.link}" class="next">${next.text}</a></li>
    </g:if>
</ul>
