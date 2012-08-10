<g:if test="${beanObject}">
    <g:set var="value" value="${beanObject[name]}"/>
</g:if>
<g:if test="${!(type in ['select', 'textarea', 'datepicker'])}">
    <input id="${id.encodeAsHTML()}" name="${name.encodeAsHTML()}" class="${classes.encodeAsHTML()}" type="${type}" value="${value?.encodeAsHTML()}"/><br/>
</g:if>
<%-- @todo use callTag here and pass in extra attribs --%>
<g:elseif test="${type == 'select'}">
    <g:select id="${id.encodeAsHTML()}" name="${name}" value="${value}" noSelection="[null:'No value']" from="${[ [value:value] ]}"/>
</g:elseif>
<g:elseif test="${type == 'textarea'}">
    <textarea id="${id.encodeAsHTML()}" name="${name}">${value?.encodeAsHTML()}</textarea>
</g:elseif>
<g:elseif test="${type == 'datepicker'}">
    <g:datePicker id="${id.encodeAsHTML()}" name="${name}" value="${value}"/>
</g:elseif>
