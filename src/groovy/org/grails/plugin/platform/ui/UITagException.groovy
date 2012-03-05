package org.grails.plugin.platform.ui

class UITagException extends RuntimeException {
    String tagName
    String uiSetName

    UITagException(String tagName, String uiSetName, Throwable cause) {
        super("Could not render UI tag template for [$tagName] from UI Set [$uiSetName] because of ${cause.class.name}: ${cause.message}".toString())
        this.tagName = tagName
        this.uiSetName = uiSetName
    }
}