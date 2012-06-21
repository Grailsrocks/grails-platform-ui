includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

def coreLayouts = ['home', 'sidebar', 'report', 'dataentry', 'dialog', 'main']

void initLayout(base, n) {
    def year = new Date()[Calendar.YEAR]

    new File(new File(base), "${n}.gsp") << """
<!DOCTYPE html><html>
    <%-- add a body to this head tag to add any meta / common resources --%>
    <theme:head/>
    <theme:body>
        <div class="header">
            <h1><g:layoutTitle default="Untitled"/></h1>
        </div>
        
        <div class="content">
            <%-- add <theme:layoutZone/> tags to the body of the theme:body tag as necessary --%>
            <theme:layoutZone name="body"/>
        </div>
        
        <div class="footer">
            &copy; ${year}
        </div>
    </theme:body>
</html>
"""
}

target("create-theme": "Creates the skeleton of a theme") {
    depends(checkVersion, parseArguments)

    def name = argsMap.params ? argsMap.params[0] : null
    if (!name) {
        ant.input(message:"What is the name of your theme?", defaultValue:'unnamed', addProperty:'themeName')
        name = ant.project.properties.themeName
    }

    def base = "${basedir}/grails-app/views/layouts/themes/${name}"
    ant.mkdir(dir:base)
    
    def templatesBase = "${basedir}/grails-app/views/_themes/${name}"
    ant.mkdir(dir:templatesBase+'/ui')
    ant.mkdir(dir:templatesBase+'/test')

    coreLayouts.each {
        initLayout(base, it)
    }

    // Create resources
    def appDir = new File(new File(basedir), 'grails-app')
    def confDir = new File(appDir, 'conf')
    def resFile = new File(confDir, "${name}ThemeResources.groovy")
    def resSpecimen = new StringBuilder()
    resSpecimen << """
// Put your resources in here
modules = {
    'theme.${name}' {
        // Add your global CSS/JS files here
    }
"""
    coreLayouts.each { l ->
        resSpecimen << """
    'theme.${name}.${l}' {
        // Add your '${l}' specific CSS/JS files here
    }
"""
    }
    resSpecimen << "}"
    resFile << resSpecimen
}

setDefaultTarget("create-theme")
