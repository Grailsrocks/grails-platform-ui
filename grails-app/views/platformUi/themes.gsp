<g:if test="${params.theme}">
<% grailsApplication.mainContext.grailsThemes.setSessionTheme(request, params.theme) %>
</g:if>
<theme:debugMode/>

<html>
    <head>
        <theme:layout name="${params.layout ?: 'main'}"/>
        <title>Theme Previewer - Layout '${params.layout ?: 'main'}' in theme '<theme:name/>'</title>
        <r:require module="plugin.platformUi.tools"/>
    </head>
    <body>
        <theme:zone id="body">
            <p:uiOverlay>
                <div id="themeselecta">
                    <form method="GET">
                        <label for="theme">Theme</label><g:select name="theme" from="${theme.listThemes().name}" value="${theme.name()}"/>
                        <%-- @todo include all layouts for all themes and show correct ones --%><br/>
                        <label for="layout">Layout</label><g:select name="layout" from="${theme.current().layouts}" value="${params.layout ?: 'main'}"/><br/>
                        <g:checkBox name="uitest" checked="${params.uitest != null}"/> UI Test<br/>
                        <div class="actions"><button type="submit">Show</button></div>
                    </form>
                </div>
            </p:uiOverlay>

            <theme:defaultContent zone="body"/>
            <g:if test="${params.uitest}">
                <g:render plugin="platformUi" template="uisample"/>
            </g:if>
        </theme:zone>
    </body>
</html>