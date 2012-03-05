<ui:form controller="testing">
    <fieldset>
        <legend>Part 1 of the form</legend>
        <g:each in="${(1..5)}" var="n">
            <ui:field name="field${n}"/>
        </g:each>
    </fieldset>
    <fieldset>
        <legend>Part 2 of the form</legend>
        <g:each in="${(1..4)}" var="n">
            <ui:field name="field${n}"/>
        </g:each>
    </fieldset>
    <ui:actions>
        <ui:button mode="primary" type="submit">Save</ui:button>
        <ui:button kind="anchor" mode="cancel">Cancel</ui:button>
    </ui:actions>
</ui:form>
