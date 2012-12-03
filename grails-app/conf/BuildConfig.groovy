
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsRepo 'http://grails.org/plugins' 

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        build('org.grails:grails-gdoc-engine:1.0.1') {
            export = false
        }

   }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.0.2") {
            export = false
        }

        compile(":hibernate:$grailsVersion"){
            export = false
        }

        compile(':platform-core:1.0.RC2')
        runtime(':resources:1.2.RC3')

        // For run-app testing of this plugin, we cam pull in a theme
        // runtime(':bootstrap-theme:1.0.RC2')

/*        
        compile(':spock:0.6-SNAPSHOT'){
            export = false
        }
*/
    }
}
