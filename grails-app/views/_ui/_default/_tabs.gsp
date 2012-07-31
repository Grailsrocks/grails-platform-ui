<ul class="tabs${classes}" ${ui.attributes()}>
<g:each in="${tabs}" var="t">   <li ${t == active ? 'class="active"' : ''}><a href="${t.link ?: ('#'+t).encodeAsHTML()}">${t.title.encodeAsHTML()}</a></li>
</g:each></ul>
<div class="tabBodies">
    <g:each in="${tabs}" var="t">
        ${t.body}
    </g:each>
</div>