<#import "utils.ftl" as u />

<#escape x as x?html> 

<#-- 
 * initiativeView
 * 
 * Generates initiative's public view block
 *
 * @param initiative is initiative
-->
<#macro initiativeView initiative>
    <h2><@u.message "initiative.proposal.title" /></h2>

    <@u.text initiative.proposal!"" />

    <#if (initiative.extraInfo)?has_content>
        <h2><@u.message "initiative.extraInfo.title" /></h2>
        <@u.text initiative.extraInfo!"" />
    </#if>
</#macro>

<#-- 
 * initiativeAuthor
 * 
 * Generates initiative's public author name
 *
 * @param initiative is initiative
-->
<#macro initiativeAuthor publicAuthors>
    <h3><@u.message key="initiative.authors.title" args=[publicAuthors.publicNames+publicAuthors.privateNames] /></h3>

    <#if (publicAuthors.publicNames > 0)>
        <#list publicAuthors.publicAuthors as publicAuthor>
            <div class="column ${((publicAuthor_index + 1) % 3 == 0)?string("last","")}">
                <h4 class="header">${publicAuthor.name}</h4>
                <p>${publicAuthor.municipality.getName(locale)}</p>
            </div>
            <#if ((publicAuthor_index + 1) % 3 == 0) || !publicAuthor_has_next><br class="clear" /></#if>
        </#list>
    
    </#if>
    <#if (publicAuthors.publicNames == 0) && (publicAuthors.privateNames == 1)>
        <p><@u.message key="authors.onlyOnePrivate" /></p>
    <#elseif (publicAuthors.privateNames > 0)>
        <p><@u.messageHTML key="authors.privateAuthors" args=[publicAuthors.publicNames, publicAuthors.privateNames] /></p>
    </#if>
</#macro>

<#-- 
 * initiativeAuthor
 * 
 * Generates initiative's contact info for private views
 *
 * @param contactInfo is author.contactInfo
-->
<#macro initiativeContactInfo authorList>
    <h2 class="inline-style"><@u.message "initiative.contactinfo.title" /></h2><span class="push"><@u.message "initiative.contactinfo.notPublic" /></span>
    <br class="clear" />
    <#list authorList as a>
        <div class="column ${((a_index + 1) % 3 == 0)?string("last","")}">
            <p><strong>${a.contactInfo.name!""}</strong>, ${a.municipality.getName(locale)}<br />
            ${a.contactInfo.email!""}<br />
            <#if a.contactInfo.address?? && a.contactInfo.address != ""><#noescape>${a.contactInfo.address?replace('\n','<br/>')!""}</#noescape><br /></#if>
            ${a.contactInfo.phone!""}</p>
        </div>
        <#if ((a_index + 1) % 3 == 0) || !a_has_next><br class="clear" /></#if>
    </#list>
</#macro>



<#-- 
 * stateInfo
 * 
 * Generates initiative's state dates
 *
 * @param initiative is initiative
-->
<#macro stateInfo initiative>
    
    <span class="extra-info">
        <#if initiative.sentTime.present>
            <#assign sentTime><@u.localDate initiative.sentTime.value /></#assign>
            <@u.message key="initiative.date.sent" args=[sentTime] />
        <#else>
            <#assign createTime><@u.localDate initiative.createTime /></#assign>
            <@u.message key="initiative.date.create" args=[createTime] />
            <#assign stateTime><@u.localDate initiative.stateTime/></#assign>
            <#if initiative.state??>
                <span class="bull">&bull;</span> <@u.message key="initiative.stateInfo."+initiative.state args=[stateTime]/>
                <#if initiative.state == InitiativeState.PUBLISHED && initiative.collectable><@u.message key="initiative.stateInfo.collecting" /></#if>
            </#if>
        </#if>
    </span>

</#macro>

<#-- 
 * participantCounts
 * 
 * Generates participant count infos
 *
 * NOTE: Do we need this block when VETUMA-initiatives are possible
-->

<#macro participantCounts>
[EMPTY MACRO]
<#--
    <div class="top-margin cf">
        <div class="column col-1of2">
            <p><@u.message "participantCount.total"/><br />
            <span class="user-count">${participantCount.total!""}</span><br />
            <#if (participantCount.total > 0)>
                <#if (participantCount.publicNames > 0)><a class="trigger-tooltip" href="${urls.participantList(initiative.id)}" title="<@u.message key="participantCount.publicNames.show"/>"><@u.message key="participantCount.publicNames" args=[participantCount.publicNames!""] /></a><br /></#if>
                <#if (participantCount.privateNames > 0)><@u.message key="participantCount.privateNames" args=[participantCount.privateNames!""] /></p></#if>
            </#if>
        </div>
        <div class="column col-1of2 last">
            <p><@u.message "participantCount.total" /><br />
            <span class="user-count">${participantCount.total!""}</span><br>
            <#if (participantCount.total > 0)>
                <#if (participantCount.publicNames > 0)><a class="trigger-tooltip js-show-no-franchise-list" href="${urls.participantList(initiative.id)}?show=others" title="<@u.message key="participantCount.publicNames.show"/>"><@u.message key="participantCount.publicNames" args=[participantCount.publicNames!""] /></a><br></#if>
                <#if (participantCount.privateNames > 0)><@u.message key="participantCount.privateNames" args=[participantCount.privateNames!""] /></p></#if>
            </#if>
        </div>
    </div>
-->
</#macro>

</#escape> 