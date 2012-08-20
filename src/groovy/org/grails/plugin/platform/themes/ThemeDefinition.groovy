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
package org.grails.plugin.platform.themes

import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.platform.views.ViewFinder
import org.grails.plugin.platform.ui.UISetDefinition

class ThemeDefinition {
    final log = LoggerFactory.getLogger(ThemeDefinition)

    static THEME_LAYOUTS_FOLDER = 'themes'
    static THEME_LAYOUTS_PREFIX = '/layouts/'+THEME_LAYOUTS_FOLDER+'/'
    static THEME_TEMPLATES_PREFIX = '/_themes/'
    static UI_TEMPLATES_PREFIX = '/ui'
    static TEST_TEMPLATES_PREFIX = '/test'

    String name
    GrailsPlugin definingPlugin
    GrailsPlugin defaultPlugin
    String uiSet
    UISetDefinition overridingUISet

    private Map layoutPaths = [:]
    private List layoutNames = []

    /*
     * Find all the views that the plugin supplies, and fill the gaps with those
     * from the platform default theme
     * @todo I'm not sure this is working how we want - I think we need to store the plugin
     * for each discovered/merged view at this point
     */
    private resolveViews(finder, viewFolder) {
        // App trumps plugin always & can add views missing from theme plugin
        def appViews = finder.listAppViewsAt(viewFolder)

        def pluginViews = definingPlugin ? 
            finder.listPluginViewsAt(definingPlugin, viewFolder) :
            Collections.EMPTY_LIST

        def mergedViews = []
        mergedViews.addAll(appViews)

        for (p in pluginViews) {
            if (!mergedViews.find { v -> v == p}) {
                mergedViews << p
            }
        }

        def defaultPlatformViews = definingPlugin != defaultPlugin ?
            finder.listPluginViewsAt(defaultPlugin, viewFolder) :
            Collections.EMPTY_LIST

        for (p in defaultPlatformViews) {
            if (!mergedViews.find { v -> v == p }) {
                mergedViews << p
            }
        }

        return mergedViews
    }

    void resolve(ViewFinder finder) {
        def viewFolder = THEME_LAYOUTS_PREFIX+name
        for (p in resolveViews(finder, viewFolder)) {
            def fn = p - '.gsp'
            layoutPaths[fn] = THEME_LAYOUTS_FOLDER + '/' + name + '/' + fn
        }

        layoutNames = Collections.unmodifiableList(layoutPaths.keySet().sort())
        
        if (log.debugEnabled) {
            log.debug "Theme [${name}] layouts: ${layoutPaths}"
        }
        
        def missingLayouts = Themes.CORE_LAYOUTS - layoutNames
        if (missingLayouts) {
            log.warn "Theme [${name}] is missing core layouts: ${missingLayouts} - things will look bad if your app or plugins use them"
        }
        
        overridingUISet = new UISetDefinition(name:'theme.ui.set.'+name, definingPlugin:definingPlugin)
        overridingUISet.resolve(finder)
    }

    String getLayoutPathFor(String themeLayoutName) {
        layoutPaths[themeLayoutName]
    }
    
    boolean hasLayout(String layoutName) {
        layoutPaths.containsKey(layoutName)
    }
    
    List getLayouts() {
        layoutNames
    }
}