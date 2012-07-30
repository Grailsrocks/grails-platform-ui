<g:if test="${beanObject}">
    <g:set var="value" value="${beanObject[name]}"/>
</g:if>
<label for="${name.encodeAsHTML()}">${label.encodeAsHTML()}</label>
<g:if test="${!(type in ['select', 'textarea', 'datepicker'])}">
    <input name="${name.encodeAsHTML()}" type="${type}" value="${value?.encodeAsHTML()}"/><br/>
</g:if>
<%-- @todo use callTag here and pass in extra attribs --%>
<g:elseif test="${type == 'select'}">
    <g:select name="${name}" value="${value}" noSelection="[null:'No value']" from="${[ [value:value] ]}"/>
</g:elseif>
<g:elseif test="${type == 'textarea'}">
    <textarea name="${name}">${value?.encodeAsHTML()}</textarea>
</g:elseif>
<g:elseif test="${type == 'datepicker'}">
    <g:datePicker name="${name}" value="${value}"/>
</g:elseif>
<g:if test="${hint}">
    <span class="${ui.cssClass(name:'fieldHint')}">${hint.encodeAsHTML()}</span>
</g:if>
