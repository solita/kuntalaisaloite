<#import "../components/email-utils.ftl" as u />
<#import "../components/email-blocks.ftl" as b />

<#assign type="text" />

<@u.message "email.initiative" /> - ${initiative.municipality.getLocalizedName(locale)!""}

<#if (initiative.extraInfo)?has_content>
    <@b.comment type initiative.extraInfo "email.commentToMunicipality" />
    ----
    
</#if>

<@b.initiativeDetails type />

----

<@b.contactInfo type />

----

<@b.publicViewLink type />

----

<@b.emailFooter type />
