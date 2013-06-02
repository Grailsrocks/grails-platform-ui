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

import grails.util.Environment
import grails.converters.JSON
import java.util.concurrent.ConcurrentHashMap
import org.grails.plugin.platform.ui.UITagException

import org.springframework.beans.factory.InitializingBean
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugin.platform.util.TagLibUtils
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import grails.util.GrailsNameUtils

class UITagLib implements InitializingBean {
    static namespace = "ui"
 
    static MESSAGE_DEBUG = 'debug'
    static MESSAGE_INFO = 'info'
    static MESSAGE_ERROR = 'error'
    static MESSAGE_WARNING = 'warning'
    
    static BUTTON_TYPES = ['button', 'anchor', 'submit']
    static BUTTON_MODES = ['danger', 'primary', 'secondary', 'cancel']

    static BASE_HEADING_ATTRIBUTE = 'base.heading'
    
    static LOGO_RESOURCE_URI_PREFIX = "/images/ui-logo"
    
    static returnObjectForTags = ['listSets', 'activeSets', 'errors']

    Map logosBySize = new ConcurrentHashMap()
    
    def grailsThemes
    def grailsUISets
    def grailsUiExtensions

    GrailsPlugin platformUiPlugin
    def grailsApplication
    
    void afterPropertiesSet() {
        platformUiPlugin = grailsApplication.mainContext.pluginManager.getGrailsPlugin('platformUi')
    }
    
    private renderResources() {
        if (!pluginRequestAttributes['uiset.loaded']) {
            def uiSets = grailsUISets.getUISetsToUse(request)
            def uiModules = []
            for (ui in uiSets) {
                uiModules << "ui.${ui.name}"
            }
            out << r.require(modules:uiModules, strict:false) 
            pluginRequestAttributes['uiset.candidates'] = uiSets
            pluginRequestAttributes['uiset.loaded'] = true
        }
    }
    
    def ifSetActive = { attrs, body ->
        if (pluginRequestAttributes['uiset.candidates']?.contains(attrs.name)) {
            out << body()
        }
    }
    
    def ifSetNotActive = { attrs, body ->
        if (!pluginRequestAttributes['uiset.candidates']?.contains(attrs.name)) {
            out << body()
        }
    }
    
    def activeSets = { attrs ->
        renderResources()
        return pluginRequestAttributes['uiset.candidates']
    }
    
    /**
     * Include the resources needed for the UI Set
     * Attributes:
     * @attr ignore Optional boolean value. Set to true to prevent any resources being required,
     * to prevent Resources plugin complaining that you have not called layoutResources in ajax response fragments.
     */
    def resources = { attrs ->
        if (attrs.ignore?.toBoolean()) {
            pluginRequestAttributes['uiset.loaded'] = true
        } else {
            renderResources()
        }
    }

    def listSets = { attrs ->
        grailsUISets.availableUISets
    }
    
    /**
     * Write out a the attributes passed in or available in the pageScope.attrs as passed to the templates
     * used to render UI elements. 
     * @attr in Optional list of attribute names
     * @attr from Optional map of attribute key/values
     */
    def attributes = { attrs ->
        def attribs
        def includes = TagLibUtils.attrSetOfItems('include', attrs.include, Collections.EMPTY_SET)
        def excludes = TagLibUtils.attrSetOfItems('exclude', attrs.excludes, Collections.EMPTY_SET)
        
        def attributeMap = attrs.from != null ? attrs.from : pageScope.variables.attrs

        attribs = attributeMap?.findAll { k, v -> 
            boolean keep = !includes || (k in includes)
            if (excludes) {
                keep &= !k in excludes
            }
            return keep
        }

        if (attribs) {
            out << TagLibUtils.attrsToString(attribs)
        }
    }
    
    def button = { attrs, buttonBody ->
        def type = attrs.remove('kind') ?: 'button'
        if (!(type in BUTTON_TYPES)) {
            throwTagError "Unsupported button type [${type}]"
        }

        def mode = attrs.remove('mode')
        def disabled = attrs.remove('disabled')
        if (disabled) {
            disabled = disabled.toString().toBoolean()
        }
        def classes = attrs.remove('class') 
        if (classes == null) {
            classes = ''
        }
        def text = getMessageOrBody(attrs, buttonBody)

        def buttonClass = grailsUISets.getUICSSClass(request, 'button', 'button')
        
        out << renderUITemplate('button', [
            attrs:attrs, 
            buttonClass:buttonClass, 
            classes:classes, 
            kind:type,
            text:text,
            mode:mode,
            disabled:disabled])
    }

    public renderUITemplate(String templateName, Map model) {
        renderResources()
        if (log.debugEnabled) {
            log.debug "Rendering UI template [$templateName]"
        }
        def t = grailsUISets.getTemplateView(request, templateName)
        try {
            def output = g.render(plugin:t.plugin, template:t.path, model:model)        
            return output
        } catch (Throwable e) {
            log.error "Could not render UI tag template $templateName from UI Set $t?.owner", e
            throw new UITagException(templateName, t.owner, e) 
        }
    }

    /** 
     * For non-standard UI templates (extra tags) that resolve through UI Sets
     * ...but how do they call this?
     */
    public renderExtendedUITemplate(String templateName, Map model) {
        renderResources()
        def t = grailsUISets.getTemplateView(request, templateName, false)
        try {
            return g.render(plugin:t.plugin, template:t.path, model:model)        
        } catch (Throwable e) {
            throw new UITagException(templateName, t.owner, e) 
        }
    }
    
    def tabs = { attrs, body ->
        def prefix = attrs.remove('prefix')

        def classes = attrs.remove('class')
        def tabsClass = grailsUISets.getUICSSClass(request, 'tabs', 'tabs')

        def tabBodiesBuffer = []
        // collect up the bodies and tab titles
        def bodyContent = body(_ui_tabBodies:tabBodiesBuffer)
        // Output the list of tabs
        out << renderUITemplate('tabs', [
            id:attrs.id ?: TagLibUtils.newUniqueId(request),
            attrs:attrs,
            classes:classes, 
            tabsClass:tabsClass,
            tabs:tabBodiesBuffer
        ])
    }

    private extractCreateLinkArgs(attrs) {
        def args = [:]
        for (s in ['controller', 'action', 'id', 'params', 'url', 'uri']) {
            if (attrs.containsKey(s)) {
                args[s] = attrs.remove(s)
            }
        }
        return args
    }

    def tab = { attrs, body ->
        def buffer = pageScope.variables._ui_tabBodies
        if (buffer == null) {
            throwTagError("The [ui:tab] tag can only be invoked inside a [tabs] tag body")
        }
        
        def title = attrs.remove('title')
        if (title) {
            title = p.text(code:title.toString(), args:attrs.titleArgs, default:title).toString()
        }
        
        def classes = attrs.remove('class')
        def active = attrs.remove('active')?.toBoolean()

        def tabClass = grailsUISets.getUICSSClass(request, 'tab', 'tab')

        def linkArgs = extractCreateLinkArgs(attrs)
        def link = linkArgs ? g.createLink(linkArgs) : null

        def tabId = attrs.tabId != null ? attrs.remove('tabId') : TagLibUtils.newUniqueId(request)
        
        // State saved for the tabs tag to use when rendering this tab's content
        def bodyPanelArgs = [
            id:tabId,
            title:title,
            link:link,
            active:active,
            attrs:attrs
        ]

        bodyPanelArgs.bodyContent = renderUITemplate('tab', [
            id:tabId,
            classes:classes,
            attrs:attrs,
            title:title,
            active:active,
            tabClass:tabClass,
            link:link,
            bodyContent: link ? null : body()
        ])
        
        buffer << bodyPanelArgs
        return null
    }

    def block = { attrs, body ->
        def classes = attrs.remove('class')
        def title = attrs.remove('title') 
        if (title) {
            title = p.text(code:title.toString(), args:attrs.titleArgs, default:title).toString()
        }
        def blockClass = grailsUISets.getUICSSClass(request, 'block', 'block')
        out << renderUITemplate('block', [
            title:title,
            bodyContent:body(), 
            classes:classes,
            blockClass:blockClass,
            attrs:attrs])
    }

    def image = { attrs ->
        def classes = attrs.remove('class')
        def title = attrs.remove('title') 
        if (title) {
            title = p.text(code:title.toString(), args:attrs.titleArgs, default:title).toString()
        }
        def imageClass = grailsUISets.getUICSSClass(request, 'image', 'image')
        out << renderUITemplate('image', [classes:classes, attrs:attrs, imageClass:imageClass, title:title])
    }
    
    def avatar = { attrs ->
        def user = attrs.remove('user')
        def defaultSrc = attrs.remove('default')
        def rating = attrs.remove('rating')
        def classes = attrs.remove('class')
        def size = attrs.remove('size') 
        if (size) {
            attrs.width = size
            attrs.height = size
        }
        def imageClass = grailsUISets.getUICSSClass(request, 'avatar', 'avatar')
        out << renderUITemplate('avatar', [
            user:user, 
            defaultSrc:defaultSrc, 
            rating:rating, 
            size:size, 
            avatarClass:imageClass, 
            classes:classes, 
            attrs:attrs])
    }
    
    /**
     * Render navigation items. The Theme controls how they are rendered.
     * The input items, if specified, must have the attributes: text, id, link and enabled. The text will be resolved against i18n.
     * The link must be the app-relative or absolute link to the target. "id" must be a unique id, which is
     * used as the value of "active" to denote which item is currently active
     */
    def primaryNavigation = { attrs, body ->
        def classes = attrs.remove('class')
        def navClass = grailsUISets.getUICSSClass(request, 'primaryNavigation', 'nav primary')
        out << renderUITemplate('primaryNavigation', [classes:classes, attrs:attrs, primaryNavigationClass:navClass])
    }

    def secondaryNavigation = { attrs, body ->
        def classes = attrs.remove('class')

        def navClass = grailsUISets.getUICSSClass(request, 'secondaryNavigation', 'nav secondary')
        out << renderUITemplate('secondaryNavigation', [classes:classes, attrs:attrs, secondaryNavigationClass:navClass])
    }
    
    def navigation = { attrs, body ->
        def classes = attrs.remove('class')
        def scope = attrs.remove('scope') 

        def navClass = grailsUISets.getUICSSClass(request, 'navigation', 'nav')
        out << renderUITemplate('navigation', [classes:classes, attrs:attrs, scope:scope, navigationClass:navClass])
    }

    def displayMessage = { attrs, body ->
        def searchScopes = [
            grailsUiExtensions.getPluginRequestAttributes('platformCore'), 
            grailsUiExtensions.getPluginFlash('platformCore')
        ]
        for (scope in searchScopes) {
            def msgParams = grailsUiExtensions.getDisplayMessage(scope)
            if (msgParams) {
                def msgAttribs = [
                    text:msgParams.text,
                    textArgs:msgParams.args,
                    type:msgParams.type
                ]
                def msg = ui.message(msgAttribs)
                out << msg
            }
        }
    }
    
    def message = { attrs, body ->
        def type = attrs.remove('type') ?: MESSAGE_INFO
        
        def messageClass = grailsUISets.getUICSSClass(request, 'navigation', 'nav')

        if (MESSAGE_DEBUG != type || Environment.current == Environment.DEVELOPMENT) {
            def classes = attrs.remove('class')
            def text = getMessageOrBody(attrs, body)
            def output = renderUITemplate('message', [
                bodyContent:text, 
                type:type, 
                classes:classes, 
                attrs:attrs,
                messageClass:messageClass])
            out << output
        }
    }
    
    def baseHeading = { attrs ->
        if (!attrs.level) {
            throwTagError "You must specify the [level] attribute which must be a number from 1 to 3, denoting the first heading level the GSP content should use"
        }
        pluginRequestAttributes[BASE_HEADING_ATTRIBUTE] = attrs.int('level')
    }

    private doHeading(int offset, attrs, body) {
        int level = pluginRequestAttributes[BASE_HEADING_ATTRIBUTE] ?: (int)0
        level += offset

        // Capture message first, as it modifies attrs
        def msg = getMessageOrBody(attrs, body)
        out << "<h$level"
        out << TagLibUtils.attrsToString(attrs)
        out << '>'
        out << msg
        out << "</h$level>"
    }
    
    def h1 = { attrs, body ->
        doHeading((int)1, attrs, body)
    }
    
    def h2 = { attrs, body ->
        doHeading((int)2, attrs, body)
    }
    
    def h3 = { attrs, body ->
        doHeading((int)3, attrs, body)
    }
    
    def h4 = { attrs, body ->
        doHeading((int)4, attrs, body)
    }

    def paginate = { attrs ->
        def writer = out
        if (attrs.total == null) {
            throwTagError("Tag [ui:paginate] is missing required attribute [total]")
        }
        def messageSource = grailsAttributes.messageSource
        def locale = RCU.getLocale(request)

        def total = attrs.int('total') ?: 0
        def action = (attrs.action ? attrs.action : (params.action ? params.action : "list"))
        def offset = params.int('offset') ?: 0
        def max = params.int('max')
        def maxsteps = (attrs.int('maxsteps') ?: 10)

        if (!offset) offset = (attrs.int('offset') ?: 0)
        if (!max) max = (attrs.int('max') ?: 10)

        def linkParams = [:]
        if (attrs.params) linkParams.putAll(attrs.params)
        linkParams.offset = offset - max
        linkParams.max = max
        if (params.sort) linkParams.sort = params.sort
        if (params.order) linkParams.order = params.order

        def linkTagAttrs = [action:action]
        if (attrs.controller) {
            linkTagAttrs.controller = attrs.controller
        }
        if (attrs.id != null) {
            linkTagAttrs.id = attrs.id
        }
        if (attrs.fragment != null) {
            linkTagAttrs.fragment = attrs.fragment
        }
        linkTagAttrs.params = linkParams

        // determine paging variables
        def steps = maxsteps > 0
        int currentstep = (offset / max) + 1
        int firststep = 1
        int laststep = Math.round(Math.ceil(total / max))

        def items = []
        def prevItem = [
            enabled:false, 
            text:(attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
        ]
        def nextItem = [
            enabled:false, 
            text:(attrs.next ?: messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
        ]
        
        // display previous link when not on firststep
        if (currentstep > firststep) {
            linkParams.offset = offset - max
            prevItem.enabled = true
            prevItem.link = createLink(linkTagAttrs.clone())
        }
        
        def earlierItem
        def laterItem
        
        // display steps when steps are enabled and laststep is not firststep
        if (steps && laststep > firststep) {
            // determine begin and endstep paging variables
            int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
            int endstep = currentstep + Math.round(maxsteps / 2) - 1

            if (beginstep < firststep) {
                beginstep = firststep
                endstep = maxsteps
            }
            if (endstep > laststep) {
                beginstep = laststep - maxsteps + 1
                if (beginstep < firststep) {
                    beginstep = firststep
                }
                endstep = laststep
            }

            // display firststep link when beginstep is not firststep
            if (beginstep > firststep) {
                linkParams.offset = 0
                earlierItem = [earlier:true, link:createLink(linkTagAttrs.clone()), text: firststep]
            }

            // display paginate steps
            (beginstep..endstep).each { i ->
                linkParams.offset = (i - 1) * max
                def link = createLink(linkTagAttrs.clone())
                if (currentstep == i) {
                    items << [active:true, link:link, text: i]
                }
                else {
                    items << [link:link, text: i]
                }
            }

            // display laststep link when endstep is not laststep
            if (endstep < laststep) {
                linkParams.offset = (laststep -1) * max
                laterItem = [later:true, link:createLink(linkTagAttrs.clone()), text: laststep]
                items << [link:createLink(linkTagAttrs.clone()), text: laststep]
            }
        }

        // display next link when not on laststep
        if (currentstep < laststep) {
            linkParams.offset = offset + max
            nextItem.enabled = true
            nextItem.link = createLink(linkTagAttrs.clone())
        }

        def paginateClass = grailsUISets.getUICSSClass(request, 'paginate', 'paginate')

        out << renderUITemplate('paginate', [
            paginateClass:paginateClass, 
            classes:attrs.'class', 
            items:items, 
            next:nextItem.enabled ? nextItem : null, 
            prev:prevItem.enabled ? prevItem : null,
            earlier:earlierItem,
            later:laterItem
        ])
    }
    
    def form = { attrs, body ->
        def classes = attrs.remove('class')
        def formClass = grailsUISets.getUICSSClass(request, 'form', 'form')

        def formBodiesBuffer = [:]
        def bodyContent = body(_ui_formBodies:formBodiesBuffer)

        out << renderUITemplate('form', [
            attrs:attrs, 
            bodyContent:bodyContent,
            actionsContent:formBodiesBuffer.actions, 
            formClass:formClass, 
            classes:classes])
    }   

    def actions = { attrs, body ->
        def classes = attrs.remove('class')
        def actionsClass = grailsUISets.getUICSSClass(request, 'actions', 'actions')
        def bodies = pageScope.variables._ui_formBodies
        if (bodies == null) {
            throwTagError "[ui:actions] can only be invoked inside a [ui:form] tag"
        }

        bodies.actions = renderUITemplate('actions', [
            attrs:attrs, 
            bodyContent:body(), 
            actionsClass: actionsClass, 
            classes:classes])
    }
    
    def input = { attrs, body ->
        def bean = attrs.remove('bean')
        def name = attrs.remove('name')

        if (!attrs.id) {
            attrs.id = name
        }

        def invalid = attrs.remove('invalid')?.toBoolean()
        def required = attrs.remove('required')?.toBoolean()

        def type = attrs.remove('type')
        def value = attrs.remove('value')

        def invalidClass = grailsUISets.getUICSSClass(request, 'invalid', 'invalid')
        def inputClass = grailsUISets.getUICSSClass(request, 'input', 'input')
        
        def classes = p.joinClasses(values:[
                attrs.remove('class'), 
                inputClass,
                invalid ? invalidClass : null])


        def args = [
            attrs:attrs, 
            classes:classes, 
            inputClass:inputClass,
            invalidClass:invalidClass,

            id:attrs.id, 
            invalid:invalid, 
            required:required, 
            name:name, 
            beanObject:bean, // have to use safe name - "bean" clashes with BeanFields see http://jira.grails.org/browse/GRAILS-8870
            value:value,
            type:type
        ]
            
        out << renderUITemplate('input', args)
    }
    
    def fieldLabel = { attrs, body -> 
        def args = pluginRequestAttributes['field_custom_args']
        if (args == null) {
            throwTagError "[ui:fieldLabel] can only be called inside a [ui:field] with custome=\"true\""
        }
        args.label = body()
    }
    
    def fieldInput = { attrs, body -> 
        def args = pluginRequestAttributes['field_custom_args']
        if (args == null) {
            throwTagError "[ui:fieldInput] can only be called inside a [ui:field] with custome=\"true\""
        }
        args.input = body()
    }

    def fieldErrors = { attrs, body -> 
        def args = pluginRequestAttributes['field_custom_args']
        if (args == null) {
            throwTagError "[ui:fieldErrors] can only be called inside a [ui:field] with custome=\"true\""
        }
        args.errors = body()
    }

    def fieldHint = { attrs, body -> 
        def args = pluginRequestAttributes['field_custom_args']
        if (args == null) {
            throwTagError "[ui:fieldHint] can only be called inside a [ui:field] with custome=\"true\""
        }
        args.hint = body()
    }

    String resolvePropertyName(String path) {
        def period = path.lastIndexOf('.')
        return (period != -1) && (period < path.size()-1) ? path[period+1..-1] : path
    }

    private resolveErrorsForField(rootBean, String path) {
        if (rootBean.metaClass.hasProperty(rootBean, 'errors')) {
            rootBean.errors.getFieldErrors(path)
        } else {
            []
        }
    }
    def errors = { attrs ->
        resolveErrorsForField(attrs.bean, attrs.name)
    }

    def field = { attrs, body ->
        def custom

        def customInput
        def customLabel
        def customErrors
        def customHint

        def label
        def input
        def errors
        def hint

        def name = attrs.name
        def type = attrs.type 
        def value = attrs.value
        def beanObject = attrs.bean
        def classes = attrs.remove('classes')
        def i18name = name
        if (!attrs.id) {
            attrs.id = name
        }

        def customArgs = [:]
        pluginRequestAttributes['field_custom_args'] = customArgs
        body()

        if (customArgs) {
            customInput = customArgs.input
            customLabel = customArgs.label
            customErrors = customArgs.errors
            customHint = customArgs.hint
        }
        pluginRequestAttributes['field_custom_args'] = null

        if (!customLabel) {
            def labelCode = attrs.remove('label')
            if (!labelCode && name) {
                def propName = resolvePropertyName(name)
                label = GrailsNameUtils.getNaturalName(propName)
            } 
            if ((labelCode == null) && !label) {
                throwTagError "A value must be provided for [label] or [name] if no custom label is provided"
            }
            if (labelCode != null) {
                def labelArgs = attrs.remove('labelArgs')
                label = p.text(code:labelCode ?: "field.label.${i18nname}", default:labelCode, args:labelArgs).toString()
            }
        }
        
        // Hints are not required. If there is no hint we try to resolve a default code but still do nothing
        // if that is undefined, so template knows not to try to render any hint
        if (!customHint) {
            def hintText = attrs.remove('hint')
            def hintArgs = attrs.remove('hintArgs')
            hint = hintText != null ? p.text(code:hintText ?: "field.hint.${i18name}", default:hintText, args:hintArgs).toString() : null
        }

        if (!customErrors) {
            errors = attrs.remove('errors')
            if (errors == null && beanObject) {
                def fieldErrors = resolveErrorsForField(beanObject, name)
                errors = fieldErrors.collect { err ->
                    p.text(codes:err.codes, args: err.arguments as List)
                }
            }
        }

        def invalid = attrs.invalid == null ? errors : attrs.invalid?.toBoolean()
        def required = attrs.required

        def invalidClass = grailsUISets.getUICSSClass(request, 'invalid', 'invalid')
        def fieldClass = grailsUISets.getUICSSClass(request, 'field', 'field')

        // Do this after extracting other attrs
        if (!customInput) {
            input = ui.input(attrs)
        }
                
        def args = [
            attrs:attrs, 
            invalidClass:invalidClass, 
            fieldClass:fieldClass, 
            classes:classes, 
            id:attrs.id,

            customLabel:customLabel,
            customHint:customHint,
            customInput:customInput,
            customErrors:customErrors,
            input:input,
            errors:errors, 
            label:label, 
            hint:hint, 
            invalid:invalid, 
            required:required, 
            name:name, 
            beanObject:beanObject, // have to use safe name - "bean" clashes with BeanFields see http://jira.grails.org/browse/GRAILS-8870
            value:value,
            type:type
        ]
            
        out << renderUITemplate('field', args)
    }
    
    def fieldGroup = { attrs, body ->
        def classes = attrs.remove('class')
        def fieldGroupClass = grailsUISets.getUICSSClass(request, 'fieldGroup', 'fieldGroup')
        out << renderUITemplate('fieldGroup', [attrs:attrs, bodyContent:body(), fieldGroupClass: fieldGroupClass, classes:classes])
    }

    def cssClass = { attrs ->
        def name = attrs.name
        def defaultValue = attrs.default ?: ''
        out << grailsUISets.getUICSSClass(request, name, defaultValue)
    }
    
    def table = { attrs, body ->
        def classes = attrs.remove('class')
        def tableClass = grailsUISets.getUICSSClass(request, 'table', 'table')
        out << renderUITemplate('table', [
            attrs:attrs, 
            bodyContent:body(tableBodyRow:(int)0), 
            tableClass: tableClass,
            classes:classes])
    }
    
    def tr = { attrs, body ->
        Integer idx = pageScope.tableBodyRow
        pageScope.tableBodyRow = idx + 1
        def classes = attrs.remove('class')
        def oddClass = grailsUISets.getUICSSClass(request, 'trOdd', 'odd')
        def evenClass = grailsUISets.getUICSSClass(request, 'trEven', 'even')
        boolean odd = idx % (int)2
        out << renderUITemplate('tr', [
            attrs:attrs, 
            bodyContent:body(), 
            oddEvenClass:odd ? oddClass : evenClass, 
            row:idx,
            classes:classes, 
            odd:odd
        ])
    }
    
    def th = { attrs, body ->
        def text = getMessageOrBody(attrs, body)
        def classes = attrs.remove('class')
        def otherAttrs = TagLibUtils.attrsToString(attrs)
        def thClass = grailsUISets.getUICSSClass(request, 'th', 'th')
        out << renderUITemplate('th', [
            thClass:thClass,    
            attrs:otherAttrs, 
            bodyContent:text, 
            classes:classes
        ])
    }

    def logo = { attrs ->
        def args = [applicationName:grailsApplication.metadata['app.name']]
        args.classes = attrs.remove('class')
        args.logoClass = grailsUISets.getUICSSClass(request, 'logo', 'logo')
        def w = attrs.width
        def h = attrs.height
        args.width = w
        args.height = h
        
        // This may look for "x" or "300x" or "x500" or "300x500"
        args.logoUri = resolveLogo(w, h)

        out << renderUITemplate('logo', args)
    }
    
    def carousel = { attrs, body ->
        def classes = attrs.remove('class')
        def carouselClass = grailsUISets.getUICSSClass(request, 'carousel', 'carousel')

        def carouselBodiesBuffer = []
        // collect up the bodies
        def bodyContent = body(_ui_carouselBodies:carouselBodiesBuffer)
        // Output the list of tabs
        out << renderUITemplate('carousel', [
            id:attrs.id ?: TagLibUtils.newUniqueId(request),
            attrs:attrs,
            classes:classes, 
            carouselClass:carouselClass,
            slides:carouselBodiesBuffer
        ])
    }

    def slide = { attrs, body ->
        def buffer = pageScope.variables._ui_carouselBodies
        if (buffer == null) {
            throwTagError("The [ui:slide] tag can only be invoked inside a [carousel] tag body")
        }
        
        def classes = attrs.remove('class')
        def active = attrs.remove('active')?.toBoolean()

        def slideClass = grailsUISets.getUICSSClass(request, 'slide', 'slide')

        def slideId = attrs.slideId != null ? attrs.slideId : TagLibUtils.newUniqueId(request)
        
        def bodyPanelArgs = [
            id:slideId,
            active:active
        ]

        bodyPanelArgs.bodyContent = renderUITemplate('slide', [
            id:slideId,
            classes:classes,
            attrs:attrs,
            active:active,
            slideClass:slideClass,
            bodyContent: body()
        ])
        
        buffer << bodyPanelArgs
        return null
    }

    // @todo move this to TagLibUtils and use messageSource
    protected getMessageOrBody(Map attrs, Closure body) {
        def textCode = attrs.remove('text')
        def textCodeArgs = attrs.remove('textArgs')
        def textFromCode
        if (textCode) {
            if (textCode.startsWith('plugin.')) {
                textFromCode = g.message(code:textCode, args:textCodeArgs)
            } else {
                textFromCode = p.text(code:textCode, args:textCodeArgs).toString()
            }
        }
        if (textFromCode) {
            textFromCode = textFromCode.encodeAsHTML()
        }
        def v = textFromCode ?: body()
        return v
    }
    
    private resolveLogo(w, h) {
        if (log.debugEnabled) {
            log.debug "Resolving logo for size: $w x $h"
        }
        def key = "${w ?: ''}x${h ?: ''}"
        def logoUri = logosBySize[key]
        if (logoUri) {
            return logoUri 
        } else {
            logoUri = logosBySize["x"]
            if (logoUri) {
                return logoUri 
            }
        }
        
        def stretchyUri = "${LOGO_RESOURCE_URI_PREFIX}.png"
        def sizeSuffix = w || h ? "-${key}" : ''
        // Even if width is specified, height is optional
        def uri = sizeSuffix ? "${LOGO_RESOURCE_URI_PREFIX}${sizeSuffix}.png" : stretchyUri
        
        // See if app supplies logo
        if (servletContext.getResource(uri)) {
            if (log.debugEnabled) {
                log.debug "Resolving logo for size: $w x $h to $uri"
            }
            logoUri = uri
        } else if (servletContext.getResource(stretchyUri)) {
            if (log.debugEnabled) {
                log.debug "Resolving logo for size: $w x $h to $stretchyUri"
            }
            logoUri = stretchyUri 
        } else {
            // See if the theme (if there is one) has a default logo
            def theme = grailsThemes.getRequestTheme(request)
            if (theme) {
                if (theme.definingPlugin) {
                    def themeUri = "/plugins/${theme.definingPlugin.fileSystemName}${uri}"
                    if (servletContext.getResource(themeUri)) {
                        if (log.debugEnabled) {
                            log.debug "Resolving logo for size: $w x $h to theme logo $themeUri"
                        }
                        logoUri = themeUri
                    }
                }
            } 
        }
        
        if (!logoUri) {
            // OK fall back to our default stretchy logo
            logoUri = "/plugins/${platformUiPlugin.fileSystemName}${stretchyUri}"
        }
        if (log.debugEnabled) {
            log.debug "Resolve logo for size: $w x $h to $logoUri"
        }
        if (logoUri) {
            logosBySize[key] = logoUri
        }
        return logoUri
    }
    
/* Some experimental stuff that probably doesn't belong here

    def jsModel = { attrs, body ->
        def model = [i18n:[:], model:[:], params:[:]]
        def varName = attrs.var ?: 'model'
        
        def i18nKeys = TagLibUtils.attrListOfItems('i18n', attrs.i18n)
        if (i18nKeys) {
            for (key in i18nKeys) {
                model.i18n[key] = g.message(code:key)
            }
        }

        def modelKeys = TagLibUtils.attrListOfItems('model', attrs.model)
        if (modelKeys) {
            def modelVars = pageScope.variables
            for (key in modelKeys) {
                model.model[key] = modelVars.containsKey(key) ? modelVars[key] : null
            }
        }

        def paramsKeys = TagLibUtils.attrListOfItems('params', attrs.params)
        if (paramsKeys) {
            if ((paramsKeys.size() == 1) && (paramsKeys[0] == '*')) {
                paramsKeys = params.keySet()
            }
            for (key in paramsKeys) {
                model.params[key] = params[key]
            }
        }

        out << """\
<script type="text/javascript">
var ${varName} = ${model as JSON}
</script>
""" 
    }
    */
}
