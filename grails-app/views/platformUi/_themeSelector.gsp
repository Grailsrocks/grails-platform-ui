    
        <p:uiOverlay>
            <div id="themeselecta">
                <form method="GET">
                    <label for="theme">Theme</label><g:select name="_debugThemeName" from="${theme.listThemes().name}" value="${theme.name()}"/>
                    <%-- @todo include all layouts for all themes and show correct ones --%><br/>
                    <label for="layout">Layout</label><g:select name="_debugThemeLayout" from="${theme.current().layouts}" value="${params._debugThemeLayout ?: 'main'}"/><br/>
                    <g:checkBox name="_debugThemeUISample" checked="${params._debugThemeUISample != null}"/> UI Test<br/>
                    <div class="actions"><button type="submit">Show</button></div>
                </form>
            </div>
            
            <g:if test="${params._debugThemeUISample}">
                <theme:zone name="body">
                    <g:render plugin="platformUi" template="uisample"/>
                </theme:zone>
            </g:if>
        </p:uiOverlay>
