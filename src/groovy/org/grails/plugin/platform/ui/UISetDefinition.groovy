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

import org.slf4j.LoggerFactory

import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.grails.plugin.platform.views.ViewFinder
import org.grails.plugin.platform.views.ViewInfo
import org.springframework.core.io.Resource

class UISetDefinition {
    final log = LoggerFactory.getLogger(UISetDefinition)

    String name
    
    GrailsPlugin definingPlugin

    private Map<String, ViewInfo> layoutPaths = [:]
    private List layoutNames = []

    ViewInfo makeViewInfo(String path, String viewFolder, GrailsPlugin plugin) {
        def fn = path[1..-1] - '.gsp'
        new ViewInfo(
            owner:name,
            name:fn-"_",
            plugin:plugin?.name, 
            path:viewFolder + '/' + fn)
    }
    
    private resolveViews(ViewFinder finder, viewFolder) {
        if (log.debugEnabled) {
            log.debug "Resolving UI Set [$name] views in folder [$viewFolder]"
        }
        // App trumps plugin always & can add views missing from ui plugin
        def appViews = finder.listAppViewsAt(viewFolder)

        List<Resource> pluginViews = definingPlugin ? 
          finder.listPluginViewsAt(definingPlugin, viewFolder) :
          Collections.EMPTY_LIST

        def mergedViews = appViews.collect { path ->
          makeViewInfo(path, viewFolder, null)
        }

        def pluginViewInfos = pluginViews.collect { path ->
          makeViewInfo(path, viewFolder, definingPlugin)
        }

        for (p in pluginViewInfos) {
          if (!mergedViews.find { v -> v == p}) {
              mergedViews << p
          }
        }

        return mergedViews
    }
    
    void resolve(grailsViewFinder) {
        if (log.debugEnabled) {
            log.debug "Loading UI Set view definitions for set [$name]"
        }
        layoutPaths.clear()

        def viewFolder = UISets.UI_TEMPLATES_PREFIX+'/'+name
        for (p in resolveViews(grailsViewFinder, viewFolder)) {
            if (log.debugEnabled) {
                log.debug "UI view [$p.path] supplied by plugin [${p.plugin}]"
            }
            layoutPaths[p.name] = p
        }

        if (log.debugEnabled) {
            log.debug "UI templates: ${layoutPaths}"
        }
    }    
    
    ViewInfo getLayoutForTag(String tagName) {
        def v = layoutPaths[tagName]
        if (log.debugEnabled) {
            log.debug "Returning view [${v?.dump()}] for UI layout [${tagName}] - layouts we know about: ${layoutPaths.dump()}"
        }
        return v
    }
}