<g:if test="${params.id}">
<% grailsApplication.mainContext.grailsUISets.setPreviewRequestUISet(request, params.id) %>
</g:if>

<html>
    <head>
        <meta name="layout" content="platform/overlay"/>
        <title>UI Sets</title>
        <r:require module="plugin.platformUi.tools"/>
    </head>
    <body>
        <div style="width:800px; margin:0 auto 0 auto">
            <h1>UI Sets</h1>
        
            <g:render plugin="platformUi" template="uisample"/>
        </div>

        <p:uiOverlay>
            <div id="themeselecta">
                <form method="GET">
                    <label for="uiset">UI Set</label><g:select name="id" from="${ui.listSets().name}" value="${params.id ?: '_default'}"/>
                    <div class="actions"><button type="submit">Show</button></div>
                </form>
            </div>
        </p:uiOverlay>
    </body>
</html>