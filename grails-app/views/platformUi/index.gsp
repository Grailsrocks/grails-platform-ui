<!doctype html>
<html>
	<head>
		<title>Grails Platform UI</title>
        <meta name="layout" content="platform/dev"/>
        <r:script>
            $(".collapse").collapse();
        </r:script>
	</head>
	<body>
        <div class="hero-unit">
            <h1>Grails Platform UI</h1>
            <p>Welcome the the Platform UI tools</p>
        </div>

        <section>        
        <h2>Tools</h2>
        <ul>
            <li><g:link mapping="platformUiNormal" controller="platformUi" action="themes">Open Theme previewer</g:link></li>
            <li><g:link mapping="platformUiNormal" controller="platformUi" action="uisets">Open UI Set previewer</g:link></li>
        </ul>
        </section>

        <section>        
            <h2>You have the following Themes installed:</h2>
            <div class="accordion" id="themeList">
                <g:each in="${theme.listThemes()}" var="t" status="i">
                    <div class="accordion-group">
                        <div class="accordion-heading">
                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#themeList" href="#theme${i}">
                                ${t.name.encodeAsHTML()}
                            </a>
                        </div>
                        <div id="theme${i}" class="accordion-body collapse in">
                            <div class="accordion-inner">
                                Uses UI Set [${t.uiSet.encodeAsHTML()}], defined by plugin [${t.definingPlugin?.name.encodeAsHTML()}], Theme layouts:
                                <ul>
                                    <g:each in="${t.layoutPaths}" var="l">
                                        <li>
                                            <strong>${l.key.encodeAsHTML()}</strong> &raquo; ${l.value.encodeAsHTML()}
                                        </li>
                                    </g:each>
                                </ul>                        
                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
            
            <h2>You have the following UI Sets installed:</h2>
            <div class="accordion" id="uiSetList">
                <g:each in="${ui.listSets()}" var="s" status="i">
                    <div class="accordion-group">
                        <div class="accordion-heading">
                            <a class="accordion-toggle" data-toggle="collapse" data-parent="#uiSetList" href="#uiSet${i}">
                                ${s.name.encodeAsHTML()}
                            </a>
                        </div>
                        <div id="uiSet${i}" class="accordion-body collapse in">
                            <div class="accordion-inner">
                                 Defined by plugin [${s.definingPlugin?.name.encodeAsHTML()}], UI templates:
                                <ul>
                                    <g:each in="${s.layoutPaths}" var="l">
                                        <li><strong>${l.key.encodeAsHTML()}</strong> &raquo; ${l.value.encodeAsHTML()}</li>
                                    </g:each>
                                </ul>
                                Anything not listed will resolve to the UI template in the _default theme.                      
                            </div>
                        </div>
                    </div>
                </g:each>
            </div>
        </section>
	</body>
</html>