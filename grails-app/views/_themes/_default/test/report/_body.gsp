<ui:table>
    <thead>
        <tr>
            <ui:th>id</ui:th>
            <ui:th>title</ui:th>
            <ui:th>description</ui:th>
            <ui:th>date</ui:th>
        </tr>
    </tr>
    </thead>
    <tbody>
    <g:each in="${1..10}" var="i">
    <ui:tr>
        <td>${i.encodeAsHTML()}</td>
        <td>The Inbetweeners</td>
        <td>A comedy about teenagers and clunge</td>
        <td>26 September 2011</td>
    </ui:tr>
    </g:each>
    </tbody>
</ui:table>
