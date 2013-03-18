<#import "utils.ftl" as u />
<#import "forms.ftl" as f />

<#escape x as x?html> 

<#--
 * blockHeader
 *
 * Block header for management-view.
 * 
 * @param key is for example "initiative.basicDetails.title"
 * @param step is the number of current block
 -->
<#macro blockHeader key step=0>
    <div id="step-header-${step}" class="content-block-header edit ${(step == 1)?string('open','')}">
        <h2>${step}. <@u.message key!"" /></h2><span class="arrow hidden"> </span>
    </div>
</#macro>

<#--
 * buttons
 *
 * Save and cancel buttons used in block-edit-mode.
 * 
 * @param type is the type of the button: next, save-and-send, save
 * @param nextStep is the number of the following block
 -->
<#macro buttons type="" nextStep="0">
    <#if type == "next">
        <a href="#step-header-${nextStep}" id="button-next-${nextStep}" class="small-button disable-dbl-click-check ignoredirty js-proceed-to-next" data-next-step="${nextStep}"><span class="small-icon next"><@u.message "action.continue" /></span></a>
    <#elseif type == "save-and-send">
        <button type="submit" name="action-save-and-send" class="small-button"><span class="small-icon mail"><@u.message "action.saveAndSend" /></span></button>
    <#elseif type == "save">
        <button type="submit" id="" name="action-save" class="small-button" value="true" ><span class="small-icon save-and-send">Tee kuntalaisaloite</span></button>
        <#--<button type="submit" id="action-save" name="action-save" class="small-button" value="true" ><span class="small-icon save-and-send"><@u.message "action.saveAndCollect" /></span></button>-->
    </#if>
</#macro>

<#--
 * municipalityBlock
 *
 * Choose municipality
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro municipalityBlock step municipality="">      

    <div id="step-${step}" class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.municipality" />
                <@f.helpText "help.homeMunicipality" />
            </div>
        </div>

        <div class="input-block-content">
            <#assign href="#" />
            <@u.systemMessage path="initiative.municipality.description" type="info" showClose=false args=[href] />
        </div>
        
        <div class="input-block-content">       
            <@f.municipalitySelect path="initiative.municipality" options=municipalities required="required" cssClass="municipality-select" preSelected=municipality />
        </div>
        <div class="input-block-content">
            <@f.municipalitySelect path="initiative.homeMunicipality" options=municipalities required="required" cssClass="municipality-select" preSelected=municipality />
        </div>
        <br class="clear" />
        
        <div id="municipalMembership" class="municipality-not-equal js-hide hidden">
            <div class="input-block-content">
                <#assign href="#" />
                <@u.systemMessage path="initiative.municipality.notEqual" type="info" showClose=false args=[href] />
            </div>
            <div class="input-block-content">
                <@f.radiobutton path="initiative.municipalMembership" required="" options={"true":"initiative.municipalMembership.true", "false":"initiative.municipalMembership.false"} attributes="" header=false />
            </div>
            
            <div class="input-block-content is-not-member js-hide">
                <@u.systemMessage path="warning.initiative.notMember" type="warning" showClose=false />
            </div>
        </div>

        <#-- Different treat for NOSCRIPT-users. Dummy checkbox and hidden field for better UX. -->
        <noscript>
        <div class="input-block-content">
            <label>
                <input type="hidden" name="municipalMembership" value="true" />
                <#assign href="#" />
                <input type="checkbox" name="placeholder" id="placeholder" checked="checked" disabled="disabled" /><span class="label"><@u.messageHTML key="initiative.checkMembership" args=[href] /></span>
            </label>
        </div>
        </noscript>

        <#--<div class="input-block-content hidden">
            <@buttons type="next" nextStep=step+1 />
        </div>-->
    </div>
</#macro>


<#macro chooseInitiativeType>

    <div class="column col-1of3">
        <label>
            <span>Normaali aloite</span>
            <input type="radio" id="initiativeType[0]" name="initiativeType" value="normal" />
        </label>
    </div>
    <div class="column col-1of3">
        <label>
            <span>2%:n aloite</span>
            <input type="radio" id="initiativeType[1]" name="initiativeType" value="two-percent" disabled="disabled" />
        </label>
    </div>
    <div class="column col-1of3 last">
        <label>
            <span>5%:n aloite</span>
            <input type="radio" id="initiativeType[2]" name="initiativeType" value="five-percent" disabled="disabled" />
        </label>
    </div>

</#macro>


<#--
 * initiativeBlock
 *
 * Add initiative title and content
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro initiativeBlock step>      
    <div id="step-${step}" class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.name" />
                <@f.helpText "help.proposal" />
            </div>
        </div>

        <div class="input-block-content">
            <#assign href="#" />
            <@u.systemMessage path="initiative.proposal.description" type="info" showClose=false args=[href] />  
        </div>
        
        <div class="input-block-content">      
            <div class="input-header hidden">
                <@u.message "selectedMunicipality.title" />
            </div>
            <p id="selected-municipality" class="hidden"><i><@u.message "selectedMunicipality.notSelected" /></i></p>
            
            <@f.textField path="initiative.name" required="required" optional=true cssClass="large" maxLength=InitiativeConstants.INITIATIVE_NAME_MAX />
        </div>

        <div class="input-block-content no-top-margin">
            <@f.textarea path="initiative.proposal" required="required" optional=false cssClass="textarea-tall" />
        </div>
        
        <#--
        <div class="input-block-content no-top-margin">
            <@f.textarea path="initiative.extraInfo" required="required" optional=false cssClass="textarea" />
        </div>
        -->
        <div class="input-block-content">
            <label for="extraInfo" class="input-header">
                Lisätiedot
            </label>
            <textarea class="textarea" name="extraInfo" id="extraInfo"></textarea>
        </div>
        
        <div class="input-block-content hidden">
            <@buttons type="next" nextStep=step+1 />
        </div>
    </div>
</#macro>


<#--
 * currentAuthorBlock
 *
 * Add author details
 *  - Name, Home municipality, suffrage
 *  - Email address, phone, street address
 *
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro currentAuthorBlock step>
    <div id="step-${step}" class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.contactInfo.name" />
                <@f.helpText "help.contactInfo.contactDetails" />
            </div>
        </div>

         <div class="input-block-content">
            <@u.systemMessage path="initiative.ownDetails.description" type="info" showClose=false />  
        </div>
        
        <div class="input-block-content">
            <@f.textField path="initiative.contactInfo.name" required="required" optional=false cssClass="large" maxLength=InitiativeConstants.CONTACT_NAME_MAX />
            
            <@f.formCheckbox path="initiative.showName" checked=true />
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="initiative.contactInfo.description" type="info" showClose=false />
        </div>

        <div class="input-block-content">
            <@f.contactInfo path="initiative.contactInfo" realPath=initiative.contactInfo mode="full" />
        </div>
        
        <div class="input-block-content hidden">
            <@buttons type="next" nextStep=step+1 />
        </div>
    </div>
</#macro>
      
<#--
 * saveBlock
 *
 * Initiative type and save or save and send
 *
 * Prints help-texts and validation errors in this block
 *
 * @param step is the number of current block
 -->
<#macro saveBlock step>      
    <div id="step-${step}" class="input-block cf">
        <div class="input-block-extra">
            <div class="input-block-extra-content">
                <@f.helpText "help.send" />
                <@f.helpText "help.collect" />
            </div>
        </div>

        <div class="input-block-content">
            <@u.systemMessage path="initiative.save.description" type="info" showClose=false />
        </div>
        
        <#-- TODO: Testing the layout. Finalize the test layout.-->
        <div class="input-block-content">
        
            <div class="column-separator cf">
                <div class="column col-1of2">
                    <div class="highlight-box">
                        <div class="title"><@u.message "initiative.send.title" /></div>
                    </div>
                    
                    <p><@u.message "initiative.send.description" /></p>
                </div>
                    
                <div class="column col-1of2 last cf">
                    <div class="highlight-box">
                        <div class="title"><@u.message "initiative.collect.title" /></div>
                    </div>
                
                    <p><@u.message "initiative.collect.description" /></p>
    
                    <div id="franchise" class="">
                        <@f.radiobutton path="initiative.franchise" required="required" options={"true":"initiative.franchise.true", "false":"initiative.franchise.false"} attributes="" />
                    </div>
    
                    <br/>
                </div>
               
                <div class="column col-1of2">
                    <@buttons type="save-and-send" />
                </div>
                    
                <div class="column col-1of2 last cf">
                    <@buttons type="save" />
                </div>
            </div>
            
        </div>
        
    </div>
</#macro>

</#escape> 
