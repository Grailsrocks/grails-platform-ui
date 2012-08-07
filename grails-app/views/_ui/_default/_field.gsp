<div class="field">
	<g:if test="${customLabel}">
		${customLabel}
	</g:if>
	<g:else>
		<label for="${id.encodeAsHTML()}">${label.encodeAsHTML()}</label>
	</g:else>

	<g:if test="${customInput}">
		${customInput}
	</g:if>
	<g:else>
		${input}
	</g:else>

	<g:if test="${customHint}">
		${customHint}
	</g:if>
	<g:elseif test="${hint}">
		<span class="${hintClass}">${hint.encodeAsHTML()}</span>
	</g:elseif>

	<g:if test="${customErrors}">
		${customErrors}
	</g:if>
	<g:elseif test="${errors}">
		<g:each in="${errors}" var="err">
			<span class="${errorClass}">${err.encodeAsHTML()}</span>
		</g:each>
	</g:elseif>
</div>
