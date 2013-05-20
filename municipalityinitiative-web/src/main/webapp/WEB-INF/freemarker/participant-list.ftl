<#import "components/layout.ftl" as l />
<#import "components/utils.ftl" as u />
<#import "components/elements.ftl" as e />

<#escape x as x?html> 

<#--
 * Layout parameters for HTML-title and navigation.
 * 
 * page = "page.initiative.public" or "page.initiative.unnamed"
 * pageTitle = initiative.name if exists, otherwise empty string
-->
<@l.main "page.initiative.participants" initiative.name!"">

    <h1 class="name">${initiative.name!""}</h1>
    
    <div class="municipality">${initiative.municipality.getName(locale)}</div>
    
    <@e.stateInfo initiative />
    
    <@returnPrevious />

    <#-- VIEW BLOCKS -->
    <div class="view-block single public cf">
        <h2><@u.message key="participantList.title" args=[participantCount.publicNames!""] /></h2>
        <#if hasManagementRightForInitiative>
            <a style="float:right;" href="${urls.participantListManage(initiative.id)}">Osallistujahallinta</a>
        </#if>
        
        <@participantList participants />
    </div>
    
    <@returnPrevious />
</@l.main>

<#-- 
 * participantList
 *
 * Prints public participant name list with municipality and participate date.
 *
 * @param participants is participants object list
-->
<#macro participantList participants>
    <#list participants as participant>
        <#if participant_index == 0><ul class="participant-list no-style"></#if>
            <li><span class="date"><@u.localDate participant.participateDate!"" /></span> <span class="name-container"><span class="name">${participant.name!""}</span> <span class="home-municipality"><span class="bull">&bull;</span> ${participant.homeMunicipality.getName(locale)!""}</span></span></li>
        <#if !participant_has_next></ul></#if>
    </#list>
</#macro>

<#-- 
 * participantListColumns
 *
 * Same as participantList but it has an option to create columns
 *
 * @param participants is participants object list
-->
<#macro participantListColumns participants>
    <#assign participantCount = participants?size />
    <#assign cue = (participantCount/2)?ceiling />

    <#assign columns=1 />    
    <#if ( participantCount > 19)>
        <#assign columns=2 />
    </#if>

    <div class="column col-1of2">

        <#list participants as participant>
            <#if participant_index == 0><ul class="participant-list no-style"></#if>
                <li><span class="date"><@u.localDate participant.participateDate!"" /></span> <span class="name-container"><span class="name">${participant.name!""}</span> <span class="home-municipality">- ${participant.homeMunicipality.getLocalizedName(locale)!""}</span></span></li>
                
                <#if columns == 2 && participant_index == cue>
                    </ul></div>
                    <div class="column col-1of2 last"><ul class="participant-list no-style">
                </#if>
                
            <#if !participant_has_next></ul></#if>
        </#list>
            
    </div>
    <br class="clear" />

</#macro>

<#-- 
 * returnPrevious
 *
 *  If request header referer equals management
 *      previousPageURI is the management URI
 *  Otherwise
 *      previousPageURI is the public view URI
-->
<#macro returnPrevious>
    <#if previousPageURI == urls.getManagement(initiative.id)>
        <p><a href="${previousPageURI}">&laquo; <@u.message "participantList.return.management" /></a></p>
    <#else>
        <p><a href="${previousPageURI}">&laquo; <@u.message "participantList.return.view" /></a></p>
    </#if>
</#macro>


</#escape> 

