<ul class="tabs${classes}" ${ui.attributes()}>
<g:each in="${tabs}" var="t">   <li ${t == active ? 'class="active"' : ''}><a href="${t.link ?: ('#'+t).encodeAsHTML()}"><g:message code="${prefix ? prefix+'.'+t.title : t.title}" encodeAs="HTML"/></a></li>
</g:each></ul>
<div class="tabBodies">
    <g:each in="${tabs}" var="t">
        ${t.body}
    </g:each>
</div>