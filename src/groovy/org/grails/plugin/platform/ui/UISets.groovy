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
package org.grails.plugin.platform.ui

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.platform.views.ViewFinder
import org.grails.plugin.platform.views.ViewInfo
import org.springframework.core.io.Resource
import org.slf4j.LoggerFactory

class UISets implements ApplicationContextAware, InitializingBean {
    static UI_TEMPLATES_PREFIX = "/_ui"
    static UI_SET_DEFAULT = '_default'
    
    final log = LoggerFactory.getLogger(UISets)

    ApplicationContext applicationContext
    
    static UI_TAG_NAMES = [
        'block', 
        'message', 

        'tabs', 
        'tab', 

        'image', 
        'avatar', 
        
        'button', 
        
        'navigation',
        'primaryNavigation',
        'secondaryNavigation',
        
        'paginate',
        'form',
        'field',
        'fieldGroup',
        'input',
        'actions',

        'table',
        'tr',
        'th',

        'logo',

        'carousel',
        'slide',
    ] as Set
    
    def pluginManager
    def grailsViewFinder
    def grailsPluginConfiguration
    def pluginConfig
    def grailsThemes
    def grailsUiExtensions
    
    GrailsPlugin platformUiPlugin
    Map<String, UISetDefinition> uiSetsByName = [:]
    List<UISetDefinition> availableUISets = []
    
    void afterPropertiesSet() {
        platformUiPlugin = pluginManager.getGrailsPlugin('platformUi')
        assert platformUiPlugin
        
        pluginConfig = grailsPluginConfiguration.getPluginConfigFor(this)

        loadUISets()
    }

    /**
     * Just for previewing UI sets
     */
    void setPreviewRequestUISet(request, String name) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')['ui.sets.preview'] = [uiSetsByName[name]]
    }
    
    /**
     * Set the UI set for this request
     */
    void setRequestUISet(request, String name) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')['ui.set.name'] = name
    }
    
    /**
     * Get theme's UISetDefinition, as a list in order of resolution so that app can override templates
     */
    List<UISetDefinition> getUISetsToUse(request = null) {
        def reqAttribs = request ? grailsUiExtensions.getPluginRequestAttributes('platformUi') : null
        def cachedResult = reqAttribs?.'ui.sets'
        if (cachedResult) {
            return cachedResult
        }
        
        def testingResult = reqAttribs?.'ui.sets.preview'
        
        List<UISetDefinition> defs = testingResult ?: []
        
        // Don't get the theme ui overrides mixed up in here when previewing UI Sets
        if (!testingResult) {
            if (reqAttribs?.'ui.set.name') {
                defs << reqAttribs.'ui.set.name'
            }
            def theme = grailsThemes.getRequestTheme(request)
            def uiSetName
            if (theme) {
                defs << theme.overridingUISet
                defs << uiSetsByName[theme.uiSet]
            }
            def appUISet = uiSetsByName[pluginConfig.ui.set]
            if (appUISet) {
                defs << appUISet
            }
        }
        
        defs << uiSetsByName[UI_SET_DEFAULT]
        if (log.debugEnabled) {
            log.debug "UI Sets for this request, as determined by theme are: ${defs*.name}"
        }

        defs = defs.findAll { it }
        if (reqAttribs) {
            reqAttribs.'ui.sets' = defs
        }
        return defs
    }

    void loadUISets() {
        if (log.debugEnabled) {
            log.debug "Loading UI Sets..."
        }
        availableUISets.clear()
        
        def uiSetsFound = []
        
        //def appPlugin = PluginUtils.findAppPlugin(grailsApplication.mainContext)
        
        // Discover UI Sets exposed by app 
        def appUISets = grailsViewFinder.listAppViewFoldersAt(UI_TEMPLATES_PREFIX, '_field.gsp')
        uiSetsFound.addAll( appUISets.collect { uiName -> [name:uiName] } )
        if (log.debugEnabled) {
            log.debug "UI sets from app: ${appUISets}"
        }
        
        // Discover themes exposed by all plugins
        pluginManager.allPlugins.each { plugin ->
            def pluginUISets = grailsViewFinder.listPluginViewFoldersAt(plugin, UI_TEMPLATES_PREFIX, '_field.gsp')
            if (log.debugEnabled) {
                log.debug "UI sets from plugin [${plugin.name}]: ${pluginUISets}"
            }
            def uiInfos = pluginUISets.collect { uiName -> [name:uiName, definingPlugin: plugin] } 
            for (ti in uiInfos) {
                if (!(ti.name in appUISets)) {
                    uiSetsFound << ti
                } else {
                    if (log.warnEnabled) {
                        log.warn "Ignoring UI set [${ti.name}] defined in plugin [${ti.definingPlugin.name}] because the application provides one of the same name. Singularity averted!"
                    }
                }
            }
        }

        if (log.debugEnabled) {
            log.debug "Discovered UI sets: ${uiSetsFound*.name}"
        }
        
        for (uiInfo in uiSetsFound) {
            def uiDef = new UISetDefinition(uiInfo)
            if (log.debugEnabled) {
                log.debug "Resolving views for UI set: ${uiInfo.name}"
            }
            uiDef.resolve(grailsViewFinder)

            availableUISets << uiDef
        }
        
        /*
        if (!appPlugin) {
            def defaultUI = new UISetDefinition(name:UI_SET_DEFAULT, definingPlugin: platformUiPlugin) 
            defaultUI.resolve(grailsViewFinder)
            availableUISets << defaultUI   
        }
        */
        
        uiSetsByName.clear()
        for (t in availableUISets) {
            uiSetsByName[t.name] = t
        }
        
    }

    ViewInfo getTemplateView(request, String templateName, boolean strict = true) {
        if (strict && !(templateName in UI_TAG_NAMES)) {
            throw new IllegalArgumentException("Cannot get UI template for tag [${templateName}] because it is not in the list of UI_TAG_NAMES")
        }
        // Ask theme or config which UI Set to use
        List<UISetDefinition> uiSets = getUISetsToUse(request)
        def v
        for (ui in uiSets) {
            if (log.debugEnabled) {
                log.debug "Seeing if UI Set [${ui.name}] has a template for [${templateName}]"
            }
            v = ui.getLayoutForTag(templateName)
            if (v) {
                break
            }
        }
        return v
    }

    String getUICSSClass(request, symbolicName, defaultValue = '') {
        def uiSets = getUISetsToUse(request)
        def v
        for (ui in uiSets) {
            if (log.debugEnabled) {
                log.debug "Seeing if UI Set [${ui?.name}] has a CSS class for [${symbolicName}]"
            }
            def confValue = pluginConfig.ui[ui.name][symbolicName].cssClass
            if (!(confValue instanceof ConfigObject)) {
                v = confValue
                break
            }
        }
        return v == null ? defaultValue : v
    }
    

}