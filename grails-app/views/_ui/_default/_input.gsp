<g:if test="${beanObject}">
    <g:set var="value" value="${beanObject[name]}"/>
</g:if>
<g:if test="${!(type in ['select', 'textarea', 'datepicker'])}">
    <input name="${name.encodeAsHTML()}" id="${id.encodeAsHTML()}" class="${classes.encodeAsHTML()}" type="${type}" value="${value?.encodeAsHTML()}"/><br/>
</g:if>
<%-- @todo use callTag here and pass in extra attribs --%>
<g:elseif test="${type == 'select'}">
    <g:select name="${name}" id="${id.encodeAsHTML()}" value="${value}" noSelection="[null:'No value']" from="${[ [value:value] ]}"/>
</g:elseif>
<g:elseif test="${type == 'textarea'}">
    <textarea name="${name}" id="${id.encodeAsHTML()}">${value?.encodeAsHTML()}</textarea>
</g:elseif>
<g:elseif test="${type == 'datepicker'}">
    <g:datePicker name="${name}" id="${id.encodeAsHTML()}" value="${value}"/>
</g:elseif>
