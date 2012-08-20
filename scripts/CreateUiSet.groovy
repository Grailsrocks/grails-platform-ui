/* Copyright 2011-2012 the original author or authors:
 *
 *    Marc Palmer (marc@grailsrocks.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
