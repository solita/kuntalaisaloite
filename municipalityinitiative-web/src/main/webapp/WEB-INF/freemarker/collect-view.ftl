<#import "/spring.ftl" as spring />
<#import "components/utils.ftl" as u />
<#import "components/forms.ftl" as f />

<#escape x as x?html> 

<#-- TODO: NoSript -->
<#assign participateFormHTML>
<@compress single_line=true>

    <#-- Participate form errors summary -->
    +
    <@u.errorsSummary path="participant.*" prefix="participant."/>
    <@spring.bind "participant.participantName" />
    <#if spring.status.error>
        virhe
    </#if>
    <#list spring.status.errors.allErrors as error>
        * ${springMacroRequestContext.getMessage(error)}
    </#list>
    -

    <form action="${springMacroRequestContext.requestUri}" method="POST" id="form-participate" class="sodirty">
    
     <div class="input-block-content no-top-margin flexible">
        <@f.textField path="participant.participantName" required="required" optional=false cssClass="large" maxLength="512" />
        <@f.formCheckbox path="participant.showName" />
    </div>

    <div class="column col-1of2">
        <div class="input-block-content flexible">
            <#-- TODO: Preselect municipality -->
            <@f.formSingleSelect path="participant.homeMunicipality" options=municipalities required="required" cssClass="municipality-select" />
        </div>
    </div>
    <div class="column col-1of2 last">
        <div id="franchise" class="input-block-content flexible">
            <@f.radiobutton path="participant.franchise" required="required" options={"false":"initiative.franchise.false", "true":"initiative.franchise.true"} attributes="" />
        </div>
    </div>
    
    <div class="input-block-content flexible">
        <button type="submit" name="action-save" class="small-button" ><span class="small-icon save-and-send"><@u.message "action.save" /></span></button>
        <a href="${springMacroRequestContext.requestUri}#participants" class="push close"><@u.message "action.cancel" /></a>
    </div>
    
    </form>

</@compress>
</#assign>

<#assign participantListFranchiseHTML>
<@compress single_line=true>
<#-- Testing modal with large list -->
<div class="css-cols-3"><ul class="no-style"><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li><li>Matti Meikäläinen</li></ul></div>
<#--
    <#if participants??>
        <#list participants.franchise as participant>
            <#if participant_index == 0><ul class="participants no-style"></#if>
                <li>${participant}</li>
            <#if !participant_has_next></ul></#if>
        </#list>
    </#if>
    -->
</@compress>
</#assign>

<#assign participantListNoFranchiseHTML>
<@compress single_line=true>
    <#if participants??>
        <#list participants.noFranchise as participant>
            <#if participant_index == 0><ul class="participants no-style"></#if>
                <li>${participant}</li>
            <#if !participant_has_next></ul></#if>
        </#list>
    </#if>
</@compress>
</#assign>


<#assign topContribution>

    <#--
     * Initiative date and state
    -->    
    <span class="extra-info">
        <#if initiative.createTime??>Aloite luotu <@u.localDate initiative.createTime /></#if>
        <br />Aloitetta ei vielä ole lähetetty kunnalle vaan siihen kerätään ensin tekijöitä
    </span>

</#assign>

<#assign bottomContribution>

    <#-- TODO: Extra details when collecting participants. -->
    <#--
     * Show participants
    -->
    <div id="participants" class="view-block public last">
        <div class="initiative-content-row last">

            <h2>Osallistujat</h2>
            <span class="user-count-total">${participantCount.total!""}</span>
            
            <#-- Disable joining when modal request message is showed. -->
            <#-- TODO: Should be disabled when user has just joined to initiative. What should happen with create success-modal? -->
            <#--<#if !requestMessageModalHTML??>-->
            <#if requestMessages?? && !(requestMessages?size > 0) && !(RequestParameters['participateForm']?? && RequestParameters['participateForm'] == "true")>
                <div class="participate">
                    <a class="small-button js-participate" href="?participateForm=true#participate-form"><span class="small-icon save-and-send">Osallistu aloitteeseen</span></a>
                    <a class="push" href="#">Mitä aloitteeseen osallistuminen tarkoittaa?</a>
                </div>
            </#if>
            <br class="clear">

            <#--<#if RequestParameters['participateForm']?? && RequestParameters['participateForm'] == "true">
                <#noescape><noscript>
                    <div id="participate-form" class="participate-form cf top-margin">${participateFormHTML!""}</div>
                </noscript></#noescape>
            </#if>-->
            <#--<#noescape><div id="participate-form" class="participate-form cf top-margin">${participateFormHTML!""}</div></#noescape>-->

            <div class="top-margin cf">
                <div class="column col-1of2">
                    <p>Äänioikeutettuja jäseniä yhteensä kunnassa ${initiative.municipalityName!""}<br />
                    <span class="user-count">${participantCount.rightOfVoting.total!""}</span><br>
                    <a class="trigger-tooltip js-show-franchise-list" href="#" title="Näytä nimensä julkistaneiden lista">${participantCount.rightOfVoting.publicNames!""} julkista nimeä</a><br>${participantCount.rightOfVoting.privateNames!""} ei julkista nimeä</p>
                </div>
                <div class="column col-1of2 last">
                    <p>Ei äänioikeutettuja jäseniä yhteensä kunnassa ${initiative.municipalityName!""}<br />
                    <span class="user-count">${participantCount.noRightOfVoting.total!""}</span><br>
                    <a class="trigger-tooltip js-show-no-franchise-list" href="#" title="Näytä nimensä julkistaneiden lista">${participantCount.noRightOfVoting.publicNames!""} julkista nimeä</a><br>${participantCount.noRightOfVoting.privateNames!""} ei julkista nimeä</p>
                </div>
            </div>
            
        </div>     
    </div>
    
    
    
    <#--
     * Show management block
    -->
    <#-- TODO: This should come from bottomContribution. When different views for one person and multiple person initiatives are ready. -->
    <#if RequestParameters['mgmnt']?? && RequestParameters['mgmnt'] == "true">
        <div class="system-msg msg-summary">
            <div class="system-msg msg-info">
                <p>Voit nyt lähettää aloitteen kuntaan. <a href="#">Mitä kuntaan lähettäminen tarkoittaa?</a></p>
                <button type="submit" name="action-send" class="small-button"><span class="small-icon save-and-send"><@u.message "action.send" /></span></button>
            </div>
        </div>
    </#if>

</#assign>

<#assign modalData>

    <#-- Modal: Participate initiative -->
    <#if participateFormHTML??>    
        modalData.participateForm = function() {
            return [{
                title:      'Osallistu aloitteeseen',
                content:    '<#noescape>${participateFormHTML?replace("'","&#39;")}</#noescape>'
            }]
        };
    </#if>
    
    modalData.participantListFranchise = function() {
        return [{
            title:      'Äänioikeutetut julkiset osallistujat kunnassa ${initiative.municipalityName!""}',
            content:    '<#noescape><div class="css-cols-3 scrollable">${participantListFranchiseHTML}</div><br/><a href="index.html" class="small-button close"><@u.message "action.close" /></a></#noescape>'
        }]
    };
    
    modalData.participantListNoFranchise = function() {
        return [{
            title:      'Ei äänioikeutetut julkiset osallistujat kunnassa ${initiative.municipalityName!""}',
            content:    '<#noescape><div class="css-cols-3 scrollable">${participantListNoFranchiseHTML}</div><br/><a href="index.html" class="small-button close"><@u.message "action.close" /></a></#noescape>'
        }]
    };
    
</#assign>



<#include "public-view.ftl" />

</#escape> 