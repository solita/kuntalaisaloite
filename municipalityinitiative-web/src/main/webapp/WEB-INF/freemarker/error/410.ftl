<#import "../components/layout.ftl" as l />
<#import "../components/utils.ftl" as u />

<#escape x as x?html>
<@l.error "error.410.title">

    <!-- Error: 410 - Gone -->

    <h1><@u.message errorMessage+".title"/></h1>

    <@u.messageHTML key=errorMessage/>

</@l.error>
</#escape>