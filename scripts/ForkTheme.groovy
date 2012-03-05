includeTargets << grailsScript('_GrailsInit')
includeTargets << grailsScript('_GrailsArgParsing')

target("fork-theme": "Clones an installed theme into your application for customization") {
    depends(checkVersion, parseArguments)

    println "Sorry this is not implemented yet!"
}

setDefaultTarget("fork-theme")
