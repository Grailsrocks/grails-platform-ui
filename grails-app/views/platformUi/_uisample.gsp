<ui:block title="Typography">
    <ui:h1>Theme-relative Heading 1</ui:h1>
    <ui:h2>Theme-relative Heading 2</ui:h2>  
    <ui:h3>Theme-relative Heading 3</ui:h3>
    <ui:h4>Theme-relative Heading 4</ui:h4>
    <p>Paragraph text: <p:dummyText size="1"/></p>

    <em>Unordered lists</em>
    <ul>
        <li>Item 1</li>
        <li>Item 2</li>
        <li>Item 3</li>
    </ul>
    <em>Ordered lists</em>
    <ol>
        <li>Item 1</li>
        <li>Item 2</li>
        <li>Item 3</li>
    </ol>
    <em>Definition lists</em>
    <dl>
        <li>Item 1</li>
        <li>Item 2</li>
        <li>Item 3</li>
    </dl>

    <em>Definition lists</em>
    <dl>
        <li>Item 1</li>
        <li>Item 2</li>
        <li>Item 3</li>
    </dl>

    <em>Block quote</em>
    <blockquote>
        <p>OMG this theming system is so awesome!</p>
        <cite>A happy Grails user</cite>
    </blockquote>
</ui:block>

<ui:block title="Navigation">
    <ui:h1>Primary Navigation</ui:h1>
    <ui:primaryNavigation/>
    
    <ui:h1>Secondary Navigation</ui:h1>
    <ui:secondaryNavigation/>
    
    <ui:h1>General Navigation</ui:h1>
    <ui:navigation scope="dev"/>
</ui:block>

<ui:block title="Logo">
    <p>At default size</p>
    <ui:logo/>
    <p>At specific size</p>
    <ui:logo width="450" height="150"/>
</ui:block>


<ui:block title="blocks.with.titles.from.message.bundle">
    <p>Blocks like this use <code>ui:block</code> and take a <code>title</code> that 
        has i18n applied. The theme controls how a block is rendered, typically adding an outer div and a class, and a heading.</p>
    <p:dummyText size="2"/>
</ui:block>

<ui:block title="Links">
    <p>Perhaps there is custom styling for <a href="#">inline links</a>.
    </p>
</ui:block>

<ui:block title="Images and Avatars">
    <p>The <code>ui:image</code> tag is used to style images such that the theme can decorate them suitably with thumbnail borders or automatically implement light boxes etc.</p>
    <ui:image uri="http://grails.org/static/cXmUZIAv28XIiNgkRiz4RRl21TsGZ5HoGpZw1UITNyV.png" width="200" title="Grails Logo" alt="This is the Grails Logo"/>
    <p>The <code>ui:avatar</code> tag is used to style avatar images in the same way, but the 
        Theme is responsible for which avatar types it supports. By default Gravatar is supported
        without the need for any extra plugins.</p>
    <ui:avatar user="marc@anyware.co.uk" size="50" rating="G" title="An avatar" alt="This is an Avatar"/>
    <p>As with all UI tags, all the markup as well as CSS classes can be changed per theme.</p>
</ui:block>

<ui:block title="Buttons">
    <p>To allow themes to customize the rendering of buttons, we provide the <code>ui:button</code> tag. Themes can use this to 
        change the CSS classes applied to the HTML <code>button</code>, <code>a</code>, <code>input type="submit"</code> tags or
        to use completely different markup entirely if they so wish, provided they are functionally equivalent.</p>
    <p>Guaranteed available modes are <code>primary</code>, <code>secondary</code>, <code>danger</code> and <code>cancel</code>.</p>
    
    <ui:button>Save</ui:button>
    <ui:button mode="primary">OK</ui:button>
    <ui:button mode="secondary">Back</ui:button>
    <ui:button mode="danger">Yes, format my C: drive</ui:button>
    <ui:button mode="cancel">Cancel</ui:button>
    <ui:button disabled="true">This is not enabled</ui:button>
    <ui:button mode="primary" text="button.ok"/>
    <ui:button kind="anchor" href="#" mode="cancel" text="button.cancel"/>
    <ui:button kind="submit" mode="primary" text="Send"/>
</ui:block>

<ui:block title="Tabs">
    <p>Tabs are a common UI element with no standard implementation so Themes can customize the implementation of the output of the
        implementation-neutral <code>ui:tabs</code> and <code>ui:tab</code> tags. Themes can add JS behaviours as appropriate, apply CSS classes
        and alter the markup if they so wish.</p> 
    <ui:h3>Tabs with hardcoded titles</ui:h3>
    <ui:tabs>
        <ui:tab title="One" active="true"/>
        <ui:tab title="Two"/>
        <ui:tab title="Three"/>
    </ui:tabs>
    <ui:h3>Tabs with i18n titles and links only</ui:h3>
    <ui:tabs prefix="my.tab.title">
        <ui:tab title="a" controller="books"/>
        <ui:tab title="b" controller="books" action="search" active="true"/>
        <ui:tab title="c" action="latest"/>
    </ui:tabs>
    <ui:h3>Tabs with i18n titles and bodies defined</ui:h3>
    <ui:tabs prefix="other.tabtitle">
        <ui:tab title="d"><ui:h4>Tab D Content</ui:h4><p:dummyText size="2"/></ui:tab>
        <ui:tab title="e"><ui:h4>Tab E Content</ui:h4><p:dummyText size="2"/></ui:tab>
        <ui:tab title="f" active="true"><ui:h4>Tab F Content</ui:h4><p:dummyText size="2"/></ui:tab>
     </ui:tabs>
</ui:block>

<ui:block title="Carousel">
    <p>Carousels provide a way to show multiple sections of content, transitioning from one to another.</p> 
    <ui:carousel>
        <ui:slide active="true">
            <p>This is slide one</p>
        </ui:slide>
        <ui:slide>
            <p>This is our second marketing slide</p>
        </ui:slide>
        <ui:slide>
            <p>And here is our third marketing slide</p>
        </ui:slide>
    </ui:carousel>
</ui:block>

<ui:block title="Messages">
    <p>These simple UI feedback messages are rendered using <code>ui:message</code>, with Themes typically customizing the CSS class applied.</p>
    <ui:message type="debug"><p>This is some debug output, only for development mode</p></ui:message>
    <ui:message type="info" text="alert.message.something.happened"/>
    <ui:message type="error" text="alert.message.something.failed"/>
    <ui:message type="warning" text="alert.message.something.dodgy"/>
    This message came from the controller:
    <ui:displayMessage/>
</ui:block>

<ui:block title="Forms">
    <p>The other HTML input elements work as normal with Grails GSP tags. Themes are responsible for styling them with CSS.</p>
    <ui:form>
        <legend>Form using explicity body of ad hoc fields and explicit actions</legend>
        <ui:field name="textField" type="text" hint="This is a little hint" label="Text field"/>
        <ui:field name="checkboxField" type="checkbox" hint="This is an error hint" label="Checkbox field with an error" error="${true}"/>
        <ui:field name="radioField" type="radio" label="Radio field"/>
        <ui:field name="passwordField" type="password" label="Password field"/>
        <ui:field name="textareaField" type="textarea" label="Textarea field">Hello world</ui:field>
        <ui:actions>
            <ui:button kind="submit">Search</ui:button>
            <ui:button>Go</ui:button>
        </ui:actions>
    </ui:form>
    <ui:form>
        <legend>Form using fields of beans</legend>
        <g:set var="form" value="${new org.grails.plugin.platform.test.TestBean()}"/>
        <ui:field bean="${form}" name="name"/>
        <ui:field bean="${form}" name="enabled"/>
        <ui:field bean="${form}" name="dateOfBirth"/>
        <ui:actions>
            <ui:button kind="submit">Search</ui:button>
            <ui:button>Go</ui:button>
        </ui:actions>
    </ui:form>
    <ui:form>
        <legend>Form using custom fields layout to create aggregate fields</legend>
        <g:set var="form" value="${new org.grails.plugin.platform.test.TestBean()}"/>
        <ui:field custom="true" name="personalInfo">
            <ui:fieldInput>
                <ui:input bean="${form}" name="name"/>
                and
                <ui:input bean="${form}" name="dateOfBirth"/>
            </ui:fieldInput>
            <ui:fieldErrors>
                <g:each in="${ui.errors(bean:'form', name:'name') + ui.errors(bean:'form',  name:'dateOfBirth')}" var="err">
                    <span class="error">${err.encodeAsHTML()}</span>
                </g:each>
            </ui:fieldErrors>
        </ui:field>
    </ui:form>
</ui:block>

<ui:block title="Tables and Pagination">
    <ui:table>
        <thead>
        <tr>
            <ui:th text="table.id"/>
            <ui:th text="table.artist"/>
            <ui:th text="table.title"/>
            <ui:th text="table.genre"/>
        </tr>
        </thead>
        <tbody>
        <g:each in="${1..10}" var="i">
        <ui:tr>
            <td>${i.encodeAsHTML()}</td>
            <td>Farmers Market</td>
            <td>Slav To Rhythm</td>
            <td>Jazz</td>
        </ui:tr>
        </g:each>
        </tbody>
    </ui:table>
    <ui:paginate controller="dummy" total="${100}"/>
</ui:block>