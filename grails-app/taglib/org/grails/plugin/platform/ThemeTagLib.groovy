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
package org.grails.plugin.platform

import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

import com.opensymphony.module.sitemesh.RequestConstants

import org.grails.plugin.platform.util.TagLibUtils
import org.grails.plugin.platform.themes.Themes
import org.codehaus.groovy.grails.web.sitemesh.GSPSitemeshPage

class ThemeTagLib {
    static namespace = "theme"

    static REQ_ATTR_ZONE_LIST = 'zone.list'
    static REQ_ATTR_TITLE = 'title'
    
    static returnObjectForTags = ['name', 'current', 'listThemes']

    def grailsThemes
    def grailsViewFinder
    def servletContext
    
    def layout = { attrs ->
        if (log.debugEnabled) {
            log.debug "Setting page's theme layout: ${attrs}"
        }
        if (!attrs.name) {
            attrs.name = Themes.DEFAULT_LAYOUT
        }
        grailsThemes.setRequestStyle(request, attrs.name)
        def layoutName = grailsThemes.getRequestSitemeshLayout(request)
        out << sitemesh.captureMeta(gsp_sm_xmlClosingForEmptyTag:"/", name:"layout",content:layoutName)
    }
    
    protected getPage() {
        return request[org.codehaus.groovy.grails.web.sitemesh.GrailsPageFilter.GSP_SITEMESH_PAGE]
    }
    
    private boolean isZoneDefined(id, boolean includeImplicitBody = false) {
        def zones = pluginRequestAttributes[REQ_ATTR_ZONE_LIST]
        if (zones?.contains(id)) {
            return true
        } else if (includeImplicitBody && (id == 'body')) {
            def htmlPage = getPage()
            return htmlPage.getContentBuffer('page.body') 
        } else {
            return false
        }
    }

    private void doDefineZone(id) {
        def zones = pluginRequestAttributes[REQ_ATTR_ZONE_LIST]
        if (!zones) {
            def s = new HashSet()
            s.add(id)
            pluginRequestAttributes[REQ_ATTR_ZONE_LIST] = s
            return
        } else {
            if (!zones.contains(id)) {
                pluginRequestAttributes[REQ_ATTR_ZONE_LIST] << id
            }
        }
    }

    /**
     * Set the theme to use for the current request
     * @attr name The theme name to use
     */
    def set = { attrs ->
        def name = attrs.name
        if (!name) {
            throwTagError("Tag [theme:set] requires [name] attribute containing a valid theme name")
        }
        grailsThemes.setRequestTheme(request, name)
    }
    
    def zone = { attrs, tagBody ->
        def id = attrs.name ?: 'body'
        doDefineZone(id)
    
        def htmlPage = getPage()
        if(!(htmlPage instanceof GSPSitemeshPage)) {
            throwTagError("Tag [theme:zone] requires 'grails.views.gsp.sitemesh.preprocess = true' in Config")
        }
        def b = tagBody()
        grailsThemes.appendToZone(request, id, b)
        return null
    }

    private boolean isDebugMode() {
        pluginRequestAttributes['theme.debug.mode']
    }
    
    def ifZoneContent = { attrs, tagBody ->
        if (isZoneDefined(attrs.name, true)) {
            out << tagBody()
        }
    }
    
    def ifNoZoneContent = { attrs, tagBody ->
        if (!isZoneDefined(attrs.name, true)) {
            out << tagBody()
        }
    }

    /**
     * Used by themes to render application-supplied content from zones defined in GSP pages or GSP templates.
     * Typically for "footer" or other common elements that would normally be in the application SiteMesh layout.
     * Themes may have varying support for these.
     * These also allow the page to override the content. The content is located so:
     * 1. A check is made for a "zone" with the name "template.<name>", if found this is rendered
     * 2. A check is made for a GSP template in the application at the path /_theme_templates/_<name>.gsp
     * 3. Failing (1) and (2), the body is rendered, assumed to contain theme's default sample content
     */
    def layoutZone = { attrs -> 
        mustBeInALayout('layoutZone')

        def id = attrs.name
        if (!id) {
            throwTagError "Tag [theme:layoutZone] requires a [name] attribute containing the name of the zone to render"
        }

        if (debugMode) {
            out << "<!-- BEGIN Content from theme zone ${id} -->\n"
        }

        if (!isZoneDefined(id)) {
            // If this is body zone but none defined, just dump out body
            if (id == 'body') {
                out << g.layoutBody()
                return
            }
            
            // Location of app's standard content for theme zones
            def templatePath = "/_themes/zones/${id}"
            
            // First see if the application provides default content for this zone (e.g. a footer or social panel)
            if (grailsViewFinder.templateExists(templatePath)) {
                if (debugMode) {
                    out << "<!-- Content defined by Theme's default template (${templatePath}) for this zone ${id} -->\n"
                }
                out << g.render(template:templatePath) 
            } else {
                if (debugMode) {
                    out << "<!-- Content defined by defaultContent tag for this zone ${id} -->\n"
                }
                out << theme.defaultContent([zone:id])
                if (!debugMode) {
                    if (log.warnEnabled) {
                        log.warn "Could not layout zone [$id], there is no content for it from the GSP page, and no application template at ${templatePath}"
                    }
                }
            }
        } else {
            if (debugMode) {
                out << "<!-- Content defined by GSP for this zone ${id} -->\n"
            }
            def bufferedZone = g.pageProperty(name:"page.theme.zone."+id)
            out << bufferedZone
        }
        if (debugMode) {
            out << "<!-- END Content from theme zone ${id} -->\n"
        }
    }
    
    def name = { attrs ->
        out << grailsThemes.getRequestTheme(request).name
    }
    
    def listThemes = { attrs ->
        grailsThemes.availableThemes
    }
    
    void mustBeInALayout(tagName) {
        if (!getPage()) {
            throwTagError "Tag [theme:$tagName] can only be used inside a Sitemesh layout" 
        }
    }
    
    def ifLayoutIs = { attrs, tagBody ->
        if (!attrs.name) {
            throwTagError "Attribute [name] is required on the ifLayoutIs tag"
        }
        def style = grailsThemes.getRequestStyle(request)
        if (style == attrs.name) {
            out << tagBody()
        }
    }
    
    def ifLayoutIsNot = { attrs, tagBody ->
        if (!attrs.name) {
            throwTagError "Attribute [name] is required on the ifLayoutIsNot tag"
        }
        def style = grailsThemes.getRequestStyle(request)
        if (style != attrs.name) {
            out << tagBody()
        }
    }
    
    def ifThemeIs = { attrs, tagBody ->
        if (!attrs.name) {
            throwTagError "Attribute [name] is required on the ifThemeIs tag"
        }
        def theme = grailsThemes.getRequestTheme(request)
        if (theme.name == attrs.name) {
            out << tagBody()
        }
    }
    
    def ifThemeIsNot = { attrs, tagBody ->
        if (!attrs.name) {
            throwTagError "Attribute [name] is required on the ifThemeIsNot tag"
        }
        def theme = grailsThemes.getRequestTheme(request)
        if (theme.name != attrs.name) {
            out << tagBody()
        }
    }
    
    def resources = { attrs ->
        if (log.debugEnabled) {
            log.debug "theme:resources Writing out resources"
        }
        def themeModules = []
        def theme = grailsThemes.getRequestTheme(request)
        out << r.require(modules:"theme.${theme.name}", strict:false) // @todo fix this
        def style = grailsThemes.getRequestStyle(request)
        out << r.require(modules:["theme.${theme.name}.${style}", "app.theme.${theme.name}", "app.theme.${theme.name}.${style}"], strict:false)
        // Write out the resources for the UI Set we are using, otherwise it will be too late if the lazy init does it in body
        out << ui.resources()
    }
    
    def current = { attrs ->
        return grailsThemes.getRequestTheme(request)
    }
    
    // @todo move this to TagLibUtils and use messageSource
    protected getMessageOrBody(Map attrs, Closure tagBody) {
        def textCode = attrs.remove('text')
        def textCodeArgs = attrs.remove('textArgs')
        def textFromCode = textCode ? p.text(code:textCode, args:textCodeArgs) : null
        if (textFromCode) {
            textFromCode = textFromCode.encodeAsHTML()
        }
        def v = textFromCode ?: tagBody()
        return v
    }
    
    // @todo Move to platform-core?
    /**
     * Set the title of the page with i18n support, can be called in a GSP Page or a Layout. 
     */
    def title = { attrs, tagBody ->
        // @todo store just the args + body text so that if it is i18n we can resolve SEO title string by convention
        def text = getMessageOrBody(attrs, tagBody)
        pluginRequestAttributes[ThemeTagLib.REQ_ATTR_TITLE] = text
    }
    
    /**
     * Render the page title in the content 
     */
    def layoutTitle = { attrs ->
        out << ui.h1(Collections.EMPTY_MAP, 
                pluginRequestAttributes[ThemeTagLib.REQ_ATTR_TITLE] ?: g.layoutTitle(default:'Untitled') )
    }
    
    /**
     * Render the page title in the head section
     */
    def layoutHTMLTitle = { attrs ->
        out << '<title>'
        def themeTitle = pluginRequestAttributes[REQ_ATTR_TITLE]
        def title = g.layoutTitle(default:themeTitle ?: 'Untitled')
        out << title
        out << '</title>\n'
    }
    
    def head = { attrs, tagBody ->
        mustBeInALayout('head')

        out << "<head>\n"
        out << theme.layoutHTMLTitle()
        out << resources()
        out << tagBody()
        out << g.layoutHead()

        def resourceLinks = r.layoutResources()
        out << resourceLinks
        
        out << """</head>"""
    }

    /**
     * Defines the body part of a Theme layout, body should contain tags like theme:layoutZone
     */
    def body = { attrs, tagBody ->
        mustBeInALayout('body')

        def bodyAttrs = attrs.bodyAttrs
        def bodyAttrsStr = ''
        if (bodyAttrs instanceof Map) {
            bodyAttrsStr = HTMLTagLib.attrsToString(bodyAttrs)
        } else if (bodyAttrs instanceof List) {
            def bodyAttrsMap = [:]
            bodyAttrs.each { p -> bodyAttrsMap[p] = g.pageProperty(name:'body.'+p) }
            bodyAttrsStr = HTMLTagLib.attrsToString(bodyAttrsMap)
        }
        out << "<body${bodyAttrsStr}>"
        if (debugMode) {
            // We need the body of the debug GSP as it has the panel in it
            // @todo we can probably ditch this layoutBody if theme previewer concats to "body" zone
            out << g.layoutBody()
        }
        out << tagBody()
        out << r.layoutResources() 
        out << """</body>"""
    }

    def layoutTemplate = { attrs ->
        def templateView = grailsThemes.getRequestThemeTemplateView(request, attrs.name)
        if (log.debugEnabled) {
            log.debug "Resolved current request's theme template for [${attrs.name}] to [${templateView}]"
        }
        out << g.render(template:templateView.path, plugin:templateView.plugin)
    }

    def defaultContent = { attrs -> 
        if (!attrs.zone) {
            throwTagError "The attribute 'zone' is required to denote the zone for which default content is required"
        }
        def view = grailsThemes.getDefaultTemplateForZone(request, attrs.zone)
        if (view) {
            if (log.debugEnabled) {
                log.debug "Rendering default content for zone [$attrs.zone] using view [${view.path} (plugin: ${view.plugin})]"
            }
            out << g.render(template:view.path, plugin:view.plugin) 
        } else {
            if (log.debugEnabled) {
                log.debug "Rendering default inline content for zone [$attrs.zone]"
            }
            out << ui.h2([:], "Default content for zone [${attrs.zone}]")
            out << p.dummyText(size:1)
        }
    }
    
    def debugMode = { attrs ->
        pluginRequestAttributes['theme.debug.mode'] = true
    }
    

}