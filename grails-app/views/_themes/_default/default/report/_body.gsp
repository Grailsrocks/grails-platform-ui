<ui:table>
    <thead>
        <tr>
            <ui:th>id</ui:th>
            <ui:th>Artist</ui:th>
            <ui:th>Title</ui:th>
            <ui:th>Genre</ui:th>
        </tr>
    </tr>
    </thead>
    <tbody>
    <g:each in="${1..10}" var="i">
    <ui:tr>
        <td>${i.encodeAsHTML()}</td>
        <td>Farmers Market</td>
        <td>Slav To Rhythm</td>
        <td>Jazz</td>
    </ui:tr>
    </g:each>
    </tbody>
</ui:table>
