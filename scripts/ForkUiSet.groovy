includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')
includeTargets << grailsScript("_GrailsPlugins")

void discoveUISetsInProject(File projectDir, String pluginName, List results) {
    // Won't work for binary plugins - tough luck people, you reap what you sow 
    def projectViewsDir = new File(projectDir, 'grails-app'+File.separator+'views')

    def projectUISetsDir = new File(projectViewsDir, '_ui')

    if (projectUISetsDir.exists()) {
        projectUISetsDir.eachDir { d ->
            results << [name:d.name, plugin:pluginName, uiSetDir:d]
        }
    }
}

def discoverUISets() {
    def results = []
    def pluginInfos = pluginSettings.getPluginInfos()
    for (plugin in pluginInfos) {
        discoveUISetsInProject(plugin.pluginDir.file, plugin.name, results)
    }
    
    // Now check project's own themes
    discoveUISetsInProject(new File(basedir), null, results)
    
    return results
}

target("fork-ui-set": "Clones an installed UI set into your project for customization") {
    depends(checkVersion, parseArguments)

    def name = argsMap.params ? argsMap.params[0] : null
    def newName 
    
    def uiSets = discoverUISets()

    println """

NOTE: Forking a UI Set is a quick way to customize a UI Set in your project.

However it may be difficult to incorporate future changes to the original UI Set.
You should consider forking the original UI Sets's plugin if you can and install
your forked plugin into your app. This way it will be easier for you
to update the UI Set when the upstream plugin is updated in future.

If you uninstall the original plugin providing the UI Set you forked, you will need
to manually clone the resources provided by that theme and perhaps other Config settings.

"""
    // Get the theme name and new theme name
    if (!name || !(uiSets.find { it.name == name}) ) {
        name = null
        
        println "Available UI sets are:"
        uiSets.each { t ->
            println "${t.name.padRight(30)} (provided by ${t.plugin ? t.plugin : 'your application'})"
        }
        
        while (name == null) {
            ant.input(message:"What is the name of the UI Set you would like to fork into your project?", defaultValue:'', addProperty:'uiSetName')
            name = ant.project.properties.uiSetName
            if (!(uiSets.find { it.name == name})) {
                println "Sorry, there is no UI Set called '${name}' please try again"
                name = null
            }
        }
    }
        
    ant.input(message:"Choose a name for your new forked UI set", defaultValue:name+'-fork', addProperty:'newName')
    newName = ant.project.properties.newName

    def srcUISet = uiSets.find { it.name == name }

    // Copy the layouts and templates
    def newUISetTemplatesDir = "${basedir}/grails-app/views/_ui/${newName}"
    ant.mkdir(dir:newUISetTemplatesDir)
    ant.copy(toDir:newUISetTemplatesDir) {
        fileset(dir:srcUISet.uiSetDir)
    }
    
    // Now do the Resources part
    // Create resources
    def appDir = new File(new File(basedir), 'grails-app')
    def confDir = new File(appDir, 'conf')
    def newNameCaps = newName[0].toUpperCase() + newName[1..-1]
    def resFile = new File(confDir, "${newNameCaps}UISetResources.groovy")
    def resSpecimen = new StringBuilder()
    resSpecimen << """
// Put your UI Set resources in here
modules = {
    'ui.${newName}' {
        dependsOn 'ui.${name}'
        
        // Add your global CSS/JS files here
    }
}
"""
    resFile << resSpecimen
    
    println """
Your UI set "${newName}" has been created.

Edit the files in ${newUISetTemplatesDir} to customize it.

You may need to update Config.groovy to specify this as your default UI set or ti apply it to existing themes. See docs for details.
"""
}

setDefaultTarget("fork-ui-set")
