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
import org.springframework.beans.factory.InitializingBean
import org.grails.plugin.platform.views.ViewFinder
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.platform.views.ViewInfo
import org.grails.plugin.platform.ui.UISets
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import org.codehaus.groovy.grails.web.sitemesh.GSPSitemeshPage
import org.codehaus.groovy.grails.web.util.GrailsPrintWriter
import org.codehaus.groovy.grails.web.util.StreamCharBuffer

/**
 * Script to allow execution of an existing Closure as if it was a Script
 */
class Themes implements InitializingBean {
    def grailsApplication
    def grailsViewFinder
    
    final log = LoggerFactory.getLogger(Themes)

    static ATTRIB_CURRENT_THEME = 'theme.current'
    static ATTRIB_CURRENT_SITEMESH_LAYOUT = 'theme.sitemesh.layout'
    static ATTRIB_CURRENT_THEME_LAYOUT = 'theme.layout'
    static ATTRIB_CURRENT_THEME_LAYOUT_FOUND = 'theme.layout.found'
    static DEFAULT_THEME_NAME = '_default'    
    static DEFAULT_LAYOUT = 'main'

    static CORE_LAYOUTS = [
        'home', 
        'sidebar', 
        'report', 
        'dataentry', 
        'dialog', // single form area, no site chrome
        DEFAULT_LAYOUT
    ]

    def pluginManager
    def grailsPluginConfiguration
    def pluginConfig
    def grailsUiExtensions
    GrailsPlugin platformUiPlugin
    
    List<ThemeDefinition> availableThemes = []
    Map<String, ThemeDefinition> themesByName = [:]
    ThemeDefinition defaultTheme
    
    void afterPropertiesSet() {
        platformUiPlugin = pluginManager.getGrailsPlugin('platformUi')
        assert platformUiPlugin
        
        // @todo Can't remember why we can't be given this via injection...
        pluginConfig = grailsPluginConfiguration.getPluginConfigFor(this)
        
        reload()
    }
    
    void reload() {
        loadThemes()
        loadConfig()
    }
    
    void loadConfig() {
        def defThemeName = pluginConfig.theme.default
        if (!defThemeName) {
            if (availableThemes) {
                defThemeName = availableThemes*.name.sort()[0]
            } else {
                defThemeName = DEFAULT_THEME_NAME
            }
        }
        defaultTheme = themesByName[defThemeName]
        
        if (log.infoEnabled) {
            log.info "Default theme is [${defThemeName}]"
        }
    }
    
    void loadThemes() {
        if (log.infoEnabled) {
            log.info "Loading theme definitions..."
        }

        availableThemes.clear()
        
        def themesFound = []
        
        // Discover themes exposed by app 
        def appThemes = grailsViewFinder.listAppViewFoldersAt('/layouts/themes', 'main.gsp')
        if (log.infoEnabled) {
            log.info "Application theme definitions: ${appThemes}"
        }
        themesFound.addAll( appThemes.collect { themeName -> [name:themeName] } )
        
        // Discover themes exposed by all plugins
        pluginManager.allPlugins.each { plugin ->
            def pluginThemes = grailsViewFinder.listPluginViewFoldersAt(plugin, '/layouts/themes', 'main.gsp')
            def themeInfos = pluginThemes.collect { themeName -> [name:themeName, definingPlugin: plugin] } 
            if (log.infoEnabled) {
                log.info "Plugin theme definitions found in plugin [${plugin.name}]: ${themeInfos.name}"
            }
            for (ti in themeInfos) {
                if (!(ti.name in appThemes)) {
                    themesFound << ti
                } else {
                    if (log.warnEnabled) {
                        log.warn "Ignoring theme [${ti.name}] defined in plugin [${ti.definingPlugin.name}] because the application provides one of the same name. Singularity averted!"
                    }
                }
            }
        }

        if (log.debugEnabled) {
            log.debug "Discovered themes: ${themesFound*.name}"
        }
        
        for (themeInfo in themesFound) {
            def themeDef = new ThemeDefinition(themeInfo)
            themeDef.defaultPlugin = platformUiPlugin
            if (log.debugEnabled) {
                log.debug "Resolving views for theme: ${themeInfo.name}"
            }
            themeDef.resolve(grailsViewFinder)

            availableThemes << themeDef
        }
        
        themesByName.clear()
        for (t in availableThemes) {
            // Get ui set name for this theme from config
            def uiSetName = pluginConfig.themes[t.name].ui.set
            // Fall back to plugin-specific default
            if (!(uiSetName instanceof String) && t.definingPlugin) {
                def pluginUIName = grailsApplication.config.plugin[t.definingPlugin.name].ui.set
                uiSetName = pluginUIName instanceof String ? pluginUIName : null
            }
            if (!(uiSetName instanceof String)) { 
                uiSetName = null
            }
            t.uiSet = uiSetName ?: UISets.UI_SET_DEFAULT
            themesByName[t.name] = t
        }
    }    
    
    ThemeDefinition getRequestTheme(request = null, boolean returnDefault = true) {
        ThemeDefinition theme = grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME]
        if (!theme) {
            theme = grailsUiExtensions.getPluginSession('platformUi')[ATTRIB_CURRENT_THEME]
        }
        if (!theme && returnDefault) {
            theme = defaultTheme
        }
        
        return theme
    }
    
    void setRequestTheme(request, String theme) {
        if (log.debugEnabled) {
            log.debug "Setting current request theme to [${theme}]"
        }
        def themeDef = themesByName[theme]
        if (!themeDef) {
            throw new IllegalArgumentException("Cannot set current theme to [$theme], no theme by that name found. Themes available: ${availableThemes*.name}")
        }
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME] = themeDef
    }

    void setSessionTheme(request, String theme) {
        if (log.debugEnabled) {
            log.debug "Setting current session theme to [${theme}]"
        }
        def themeDef = themesByName[theme]
        if (!themeDef) {
            throw new IllegalArgumentException("Cannot set current theme to [$theme], no theme by that name found. Themes available: ${availableThemes*.name}")
        }
        grailsUiExtensions.getPluginSession('platformUi')[ATTRIB_CURRENT_THEME] = themeDef
    }

    /**
     * Take a requested page layout name and resolve it to a theme sitemesh layout path, and store this in the request. The logic is:
     * 1. Check for config-based theme-specific remapping of layouts
     * 2. Check for config-based glocal remapping of layouts
     * 3. See if the layout we've resolved to exists in the theme
     * 4. Fall back to Config default layout
     * 5. Fall back to "main"
     * @param request The current request
     * @param styleName The name of the page layo1ut requested
     */
    void resolveLayoutForStyle(request, String styleName) {
        def currentTheme = getRequestTheme(request)

        // Plugins can add their styleName -> layout mappings using doWithConfig
        def layoutName = styleName
        def mappedLayoutName = pluginConfig.theme[currentTheme.name].layout.mapping[styleName]
        if (log.debugEnabled) {
            log.debug "Layout name for theme ${currentTheme.name} with page layout ${styleName} from per-theme config mapping is [${mappedLayoutName}]"
        }
        if (mappedLayoutName instanceof ConfigObject) {
            // fall back to cross-theme override
            mappedLayoutName = pluginConfig.theme.layout.mapping[styleName]
            if (log.debugEnabled) {
                log.debug "Layout name for theme ${currentTheme.name} with page layout ${styleName} from config mapping is [${mappedLayoutName}]"
            }
        }
        
        if (!(mappedLayoutName instanceof ConfigObject)) {
            layoutName = mappedLayoutName
        }
        
        if (!currentTheme.hasLayout(layoutName)) {
            // fall back to default layout name
            layoutName = pluginConfig.theme.layout.default
            if (log.debugEnabled) {
                log.debug "Layout name for theme ${currentTheme.name} with page layout ${styleName} from theme's default is [${layoutName}]"
            }
            if (layoutName instanceof ConfigObject) {
                // OK, no config at all, just use this, we know it exists
        
                layoutName = DEFAULT_LAYOUT
                if (log.debugEnabled) {
                    log.debug "Layout name for theme ${currentTheme.name} with page layout ${styleName} falling back to default [${layoutName}]"
                }
            }
        }
        
        // Let's see if it exists
        def layoutPath = currentTheme.getLayoutPathFor(layoutName)
        
        if (!layoutPath) {
            setRequestLayoutFound(request, false)
            layoutPath = defaultTheme.getLayoutPathFor(DEFAULT_LAYOUT)
            if (log.debugEnabled) {
                log.debug "Layout name for theme ${currentTheme.name} with page layout ${styleName} falling back to default [${layoutName}]"
            }
        } else {
            setRequestLayoutFound(request, true)
        }

        if (log.debugEnabled) {
            log.debug "Resolved current request page layout name [${styleName}] to theme layout [${layoutPath}]"
        }
        setRequestSitemeshLayout(request, layoutPath)
    }
    
    void setRequestSitemeshLayout(request, layout) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_SITEMESH_LAYOUT] = layout
    }

    String getRequestSitemeshLayout(request) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_SITEMESH_LAYOUT]
    }
    
    void setRequestLayoutFound(request, boolean found) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME_LAYOUT_FOUND] = found
    }

    boolean getRequestLayoutFound() {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME_LAYOUT_FOUND]
    }

    void setRequestStyle(request, String layoutName) {
        if (log.debugEnabled) {
            log.debug "Setting current request page layout to [${layoutName}]"
        }
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME_LAYOUT] = layoutName
        resolveLayoutForStyle(request, layoutName)
    }

    List<String> getThemeNames() {
        availableThemes*.name
    }
    
    String getRequestStyle(request) {
        grailsUiExtensions.getPluginRequestAttributes('platformUi')[ATTRIB_CURRENT_THEME_LAYOUT] ?: 'main'
    }
    
    // @todo cache these too - get them from the theme
    ViewInfo getRequestThemeTemplateView(request, template) {
        def t = getRequestTheme(request)
        def v
        if (t) {
            v = "/_themes/${t.name}/$template"
        }
        if (!grailsViewFinder.templateExists(v, t.definingPlugin)) {
            if (log.debugEnabled) {
                log.debug "Theme [${t.name}] does not supply a theme template for [${template}], using default"
            }
            v = "/_themes/${DEFAULT_THEME_NAME}/$template"
        }
        return new ViewInfo(
            owner:t.name,
            plugin:t.definingPlugin?.name, 
            path:v)
    }
 
    /**
     * Returns the template path for dummy text for the specific zone, for the current theme
     * Tries for files in the theme's _themes/<themename>/test/_<zonename>.gsp first
     * If not found falls back to _default provided by the platform.
     * If there is no default for the specified zone in the platform, returns null
     */
    ViewInfo getDefaultTemplateForZone(request, zone) {
        def t = getRequestTheme(request)
        def l = getRequestStyle(request)
        def plugin = t?.definingPlugin
        def v
        if (t) {
            v = "/_themes/${t.name}/default/$l/$zone"
        }
        if (!grailsViewFinder.templateExists(v, plugin)) {
            if (log.debugEnabled) {
                log.debug "Theme [$t.name] does not supply a layout-specific default text template for zone [${zone}] in theme layout [${l}], trying theme default"
            }
            v = "/_themes/${t.name}/default/$zone"
            if (!grailsViewFinder.templateExists(v, plugin)) {
                plugin = platformUiPlugin
                if (log.debugEnabled) {
                    log.debug "Theme [$t] does not supply a default text template for zone [${zone}], trying platform layout-specific default"
                }
                v = "/_themes/${DEFAULT_THEME_NAME}/default/$l/$zone"
                if (!grailsViewFinder.templateExists(v, plugin)) {
                    if (log.debugEnabled) {
                        log.debug "Theme [$t] does not supply a default text template for zone [${zone}], using platform default"
                    }
                    v = "/_themes/${DEFAULT_THEME_NAME}/default/$zone"
                    if (!grailsViewFinder.templateExists(v, plugin)) {
                        return null
                    }
                }
            }
        }
        if (log.debugEnabled) {
            log.debug "Dummy text will be from plugin [${plugin}], path [${v}]"
        }
        return new ViewInfo(plugin:plugin?.name, path:v)
    }

    public appendToZone(request, String zone, content) {
        def bufferName = 'theme.zone.'+zone
        appendToContentBuffer(request, bufferName, content)
    }
    
    protected appendToContentBuffer(request, bufferName, body) {
        def htmlPage = getPage(request)
        def contentBuffer = htmlPage.getContentBuffer('page.'+bufferName)
        if(contentBuffer == null) {
            contentBuffer = wrapContentInBuffer(body)
            htmlPage.setContentBuffer(bufferName, contentBuffer)
        } else {
            new GrailsPrintWriter(contentBuffer.writer) << (body instanceof Closure ? body() : body)
        }
    }
/*
    protected appendToHeadBuffer(request, body) {
        def htmlPage = getPage(request)
        def contentBuffer = htmlPage.headBuffer
        if(contentBuffer == null) {
            htmlPage.setHeadBuffer(wrapContentInBuffer(body))
        } else {
            new GrailsPrintWriter(contentBuffer.writer) << body()
        }
    }

*/
    protected getPage(request) {
        return request[org.codehaus.groovy.grails.web.sitemesh.GrailsPageFilter.GSP_SITEMESH_PAGE]
    }

    protected wrapContentInBuffer(content) {
        if (content instanceof Closure) {
            content = content()
        }
        if (!(content instanceof StreamCharBuffer)) {
            // the body closure might be a string constant, so wrap it in a StreamCharBuffer in that case
            def newbuffer = new FastStringWriter()
            newbuffer.print(content)
            content = newbuffer.buffer
        }
        return content
    }
    
    
}