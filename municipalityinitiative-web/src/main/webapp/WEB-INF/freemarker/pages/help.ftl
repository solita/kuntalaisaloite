<#import "../components/layout.ftl" as l />
<#import "../components/utils.ftl" as u />

<#if omUser>
    <#import "../components/wysiwyg-editor.ftl" as editor />
    <#global editorStyles><@editor.styles /></#global>
</#if>

<#escape x as x?html>

<#if content??>
    <#assign pageTitle>${content.subject}</#assign>
<#else>
    <#assign pageTitle>jee</#assign>
</#if>

<#--
 * Navigation for subpages (public view)
 *
 * @param map is the hashMap for navigation items
 * @param titleKey is for navigation block title
 * @param cssClass is used for box-style links
-->
<#macro navigation map titleKey="" cssClass="">
    <#if cssClass == "box">
        <#list map as link>
            <a href="${urls.help(link.uri)}" class="${(link.uri == helpPage)?string("active","")} ${cssClass} ${link_has_next?string("","last")}">
                <#-- NOTE: we could also use urls (fi/sv) to determine the class -->
                <#if link_index == 0>
                    <#assign iconClass="author" />
                <#elseif link_index == 1>
                    <#assign iconClass="participants" />
                <#else>
                    <#assign iconClass="info" />
                </#if>
            
                <span class="help-nav-icon icon-${iconClass}">${link.subject}</span>
            </a>
        </#list>
    <#else>
        <#if titleKey?has_content><h3 class="navi-title"><@u.message titleKey /></h3></#if>
        <ul class="navi block-style">
            <#list map as link>
                <li><a href="${urls.help(link.uri)}" <#if link.uri == helpPage>class="active"</#if>>${link.subject}</a></li>
            </#list>
        </ul>
    </#if>
</#macro>

<#--
 * Layout parameters for HTML-title
 *
 * @param page is for example "page.help.general.title"
 * @param pageTitle used in HTML title.
-->
<@l.main "page.help" pageTitle!"">

    <div class="columns cf">

        <div class="column col-1of4 extra-margin navigation">
            <@navigation categoryLinksMap['MAIN'] "" "box" />
            <@navigation categoryLinksMap['KUNTALAISALOITE_FI'] "help.service.title" />
            <@navigation categoryLinksMap['KUNTALAISALOITE'] "help.general.title" />
        </div>

        <#if omUser>
            <div class="editor-buttons bootstrap-icons hidden">
                <a href="${urls.helpEdit(helpPage)}" class="btn" href="#"><@u.message "editor.switchToEdit" />&nbsp;&nbsp;<i class="icon-chevron-right"></i></a>
            </div>
        </#if>

        <div class="column col-3of4 last">
            <#if content??>
                <h1>${content.subject!""}</h1>
                <#noescape>${content.content!""}</#noescape>
            <#else>
                <h1>Otsikkoo jee</h1>
                Moikkelis
            </#if>
        </div>
    </div>

</@l.main>
</#escape>

