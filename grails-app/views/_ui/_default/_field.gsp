<g:if test="${beanObject}">
    <label for="${name.encodeAsHTML()}">${label.encodeAsHTML()}</label>
    <input name="${name.encodeAsHTML()}" value="${beanObject[name]?.encodeAsHTML()}"/><br/>
</g:if>
<g:else>
    <label for="${name.encodeAsHTML()}">${name.encodeAsHTML()}</label>
    <input name="${name.encodeAsHTML()}" value="${params[name]?.encodeAsHTML()}"/><br/>
</g:else> 