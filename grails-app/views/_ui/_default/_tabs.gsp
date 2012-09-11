<ul class="${p.joinClasses(values:[tabsClass, classes])}" ${ui.attributes()}>
<g:each in="${tabs}" var="t">
    <li ${t == active ? 'class="active"' : ''}><a href="${t.link ?: ('#'+t.id).encodeAsHTML()}">${t.title.encodeAsHTML()}</a></li>
</g:each>
</ul>
<div class="tabBodies">
    <g:each in="${tabs}" var="t">
        ${t.bodyContent}
    </g:each>
</div>
