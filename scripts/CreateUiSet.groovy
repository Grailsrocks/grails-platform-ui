import grails.util.GrailsNameUtils

includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

target("create-ui-set": "Creates the skeleton of a ui-set") {
    depends(checkVersion, parseArguments)

    def name = argsMap.params ? argsMap.params[0] : null
    if (!name) {
        ant.input(message:"What is the name of your UI set?", defaultValue:'unnamed', addProperty:'uiSetName')
        name = ant.project.properties.uiSetName
    }

    def className = GrailsNameUtils.getClassNameRepresentation(name)
    
    def gsppath = "grails-app/views/_ui/${name}"
    def gspbase = "${basedir}/${gsppath}"
    ant.mkdir(dir:gspbase)

    // GSPs
    ant.copy(toDir:gspbase) {
        fileset(dir: new File(platformUiPluginDir, 'grails-app/views/_ui/_default'))
    }

    // Resource template
    def resPath = "grails-app/conf/${className}UIResources.groovy"
    def resFile = "${basedir}/${resPath}"
    ant.copy(file: new File(platformUiPluginDir, 'src/templates/ui/UIResources.groovy'), toFile:resFile) 
        
    ant.replace(file: resFile, token: "@artifact.name@", value: name)
    
    output "Created your UI Set and resources template"
    output "The GSP views are in ${gsppath}"
    output "The resources are in ${resPath}"
}

void output(text) {
    println text
}
setDefaultTarget("create-ui-set")
