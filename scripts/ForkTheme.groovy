includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')
includeTargets << grailsScript("_GrailsPlugins")

void discoverThemesInProject(File projectDir, String pluginName, List results) {
    // Won't work for binary plugins - tough luck people, you reap what you sow 
    def projectViewsDir = new File(projectDir, 'grails-app'+File.separator+'views')

    def projectLayoutsDir = new File(projectViewsDir, 'layouts'+File.separator+'themes')
//        def pluginTemplatesDir = new File(pluginViewsDir, '_themes')

    if (projectLayoutsDir.exists()) {
        def themeNames = projectLayoutsDir.eachDir { d ->
            results << [name:d.name, plugin:pluginName, layoutsDir:d, viewsDir:projectViewsDir]
        }
    }
}

def discoverThemes() {
    def results = []
    def pluginInfos = pluginSettings.getPluginInfos()
    for (plugin in pluginInfos) {
        discoverThemesInProject(plugin.pluginDir.file, plugin.name, results)
    }
    
    // Now check project's own themes
    discoverThemesInProject(new File(basedir), null, results)
    
    return results
}

target("fork-theme": "Clones an installed theme into your application for customization") {
    depends(checkVersion, parseArguments)

    def name = argsMap.params ? argsMap.params[0] : null
    def newName 
    
    def themes = discoverThemes()

    println """

NOTE: Forking a theme is a quick way to customize a theme in your project.

However it may be difficult to incorporate future changes to the original theme.
You should consider forking the original theme's plugin if you can and install
your forked plugin into your app. This way it will be easier for you
to update the theme when the upstream plugin is updated in future.

If you uninstall the original plugin providing the theme you forked, you will need
to manually clone the resources provided by that theme and perhaps other Config settings.

"""
    // Get the theme name and new theme name
    if (!name || !themes.find { it.name == name} ) {
        println "Available themes are:"
        themes.each { t ->
            println "${t.name.padRight(30)} (provided by ${t.plugin ? t.plugin : 'your application'})"
        }
        
        while (!themes.find { it.name == name}) {
            ant.input(message:"What is the name of the theme you would like to fork into your project?", defaultValue:'', addProperty:'themeName')
            name = ant.project.properties.themeName
        }
    }
        
    ant.input(message:"What the new name for your forked theme?", defaultValue:name+'-fork', addProperty:'newName')
    newName = ant.project.properties.newName

    def srcTheme = themes.find { it.name == name }

    def newThemeLayoutsDir = "${basedir}/grails-app/views/layouts/themes/${newName}"
    ant.mkdir(dir:newThemeLayoutsDir)
    def newThemeTemplatesDir = "${basedir}/grails-app/views/_themes/${newName}"
    ant.mkdir(dir:newThemeTemplatesDir)

    def srcThemeTemplatesDir = srcTheme.viewsDir.toString()+'/_themes/'+srcTheme.name
    ant.copy(toDir:newThemeLayoutsDir) {
        fileset(dir:srcTheme.layoutsDir)
    }
    ant.copy(toDir:newThemeTemplatesDir) {
        fileset(dir:new File(srcTheme.viewsDir, '_themes'+File.separator+srcTheme.name))
    }
}

setDefaultTarget("fork-theme")
