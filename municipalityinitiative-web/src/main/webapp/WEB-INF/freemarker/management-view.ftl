<#import "/spring.ftl" as spring />
<#import "components/layout.ftl" as l />
<#import "components/utils.ftl" as u />
<#import "components/forms.ftl" as f />
<#import "components/elements.ftl" as e />
<#import "components/progress.ftl" as prog />
<#import "components/mobile-components.ftl" as mobile />

<#escape x as x?html> 

<#assign managementURL = urls.getManagement(initiative.id) />

<#--
 * Layout parameters for HTML-title and navigation.
 * 
 * page = "page.management"
 * pageTitle = initiative.name if exists, otherwise empty string
-->

<@l.main page="page.management" pageTitle=initiative.name!"">
    
    <div class="msg-block">
        <div class="system-msg msg-info">
            <h2><@u.message "management.title" /></h2>
            <p><@u.messageHTML "management.description" /></p>
            <p><@u.messageHTML "management.instruction" /></p>
            
            <a class="small-button " href="${urls.edit(initiative.id)}"><span class="small-icon edit"><@u.messageHTML "action.editInitiative" /></span></a>
            <a class="small-button push" href="${urls.view(initiative.id)}"><span class="small-icon document"><@u.messageHTML "action.previewInitiative" /></span></a>
        </div>
    </div>

    <@e.initiativeTitle initiative />
    
    <@prog.progress initiative=initiative public=false />
    
    <div class="view-block first">
        <@e.initiativeViewManage initiative />
    </div>

    <div id = "authors" class="view-block public">
        <h2><@u.message key="initiative.authors.title" args=[authors?size] /></h2>

        <div class="initiative-content-row last"}">
            <@e.initiativeContactInfo authorList=authors showTitle=false/>
        </div>
    </div>

    <#if !initiative.single && initiative.state == InitiativeState.PUBLISHED>
        <@e.participantInfo admin=true/>
        <@mobile.participantsBlock participantCount=participantCount canFollow=false showFollowForm=false admin=true/>
    </#if>

    <#if !initiative.single>
        <#if initiative.state == InitiativeState.REVIEW>
            <div class="msg-block">
                <div class="system-msg msg-info">
                    <h2><@u.message "initiative.stateInfo.REVIEW" /></h2>
                    <p><@u.message "initiative.stateInfo.REVIEW.description" /></p>
                    <p><@u.message "initiative.stateInfo.REVIEW.emailToAuthors" /></p>
                </div>
            </div>
        </#if>
        <#if initiative.fixState == FixState.REVIEW>
            <div class="msg-block">
                <div class="system-msg msg-info">
                    <h2><@u.message "initiative.stateInfo.REVIEW" /></h2>
                    <p><@u.message "initiative.fixStateInfo.REVIEW.description" /></p>
                    <p><@u.message "initiative.fixStateInfo.REVIEW.emailToAuthors" /></p>
                </div>
            </div>
        </#if>
    </#if>

    <#if managementSettings.allowSendToReview>
        <#assign sendToReviewConfirm = false />
        <#assign sendToReviewAndCollectConfirm = false />
        
        <#if RequestParameters['send-to-review']?? && RequestParameters['send-to-review'] == "confirm">
            <#assign sendToReviewConfirm = true />
        </#if>        
        
        <#if RequestParameters['send-to-review']?? && RequestParameters['send-to-review'] == "confirm-collect">
            <#assign sendToReviewAndCollectConfirm = true />
        </#if>
    
        <#if !sendToReviewConfirm && !sendToReviewAndCollectConfirm>
            <#if initiative.isVerifiable()>
            
                <div class="msg-block">
                    <div class="system-msg msg-info">
                        <h2><@u.message "management.sendToReview.verifiable.title" /></h2>
                        
                        <p><@u.message "management.sendToReview.verifiable.description" /></p>
            
                        <a href="${managementURL}?send-to-review=confirm-collect#send-to-review" id="js-send-to-review-collect" class="small-button js-send-to-review-collect"><span class="small-icon save-and-send"><@u.messageHTML "action.sendToReview" /></span></a>
                    </div>
                </div>
            
            <#else>
            
                <div class="view-block">
                    <h2><@u.message "management.sendToReview.title" /></h2>
                    
                    <#assign href="${urls.help(HelpPage.TERMS_OF_USE.getUri(locale))}" />
                    <@u.systemMessage path="management.sendToReview.description" type="info" args=[href] />
            
                    <br/>
                    <div class="faux-cols">
                        <div class="column col-60">
                            <h3><@u.message "management.sendToReview.collect.title" /></h3>
    
                            <div class="choose-workflow"> </div>
                            
                            <p><@u.message "management.sendToReview.collect" /></p>
                        </div>
                        <div class="column col-40 last">
                            <h3><@u.message "management.sendToReview.doNotCollect.title" /><br /><br /></h3>
                            
                            <div class="choose-workflow send"> </div>
                            
                            <p><@u.message "management.sendToReview.doNotCollect" /></p>
                        </div>
                        
                        <br class="clear" />
                        <div class="column col-60">
                            <a href="${managementURL}?send-to-review=confirm-collect#send-to-review" id="js-send-to-review-collect" class="large-button js-send-to-review-collect"><span class="large-icon next"><@u.messageHTML "action.sendToReview.bigBtn" /></span></a>
                        </div>
                        <div class="column col-40 last">
                            <a href="${managementURL}?send-to-review=confirm#send-to-review" id="js-send-to-review" class="large-button js-send-to-review"><span class="large-icon next-secondary"><@u.messageHTML "action.sendToReview.doNotCollect" /></span></a>
                        </div>
                        <br class="clear" />
                        
                        <div class="border pos-60"> </div>
                    </div>
                </div>
            
            </#if>
            
        </#if>
        
            
        <#assign sendToReviewDoNotCollect>
            <@compress single_line=true>

            <@u.errorsSummary path="comment.*" prefix="comment."/>

                <p><@u.message "sendToReview.doNotCollect.confirm.description" /></p>
                
                <form action="${springMacroRequestContext.requestUri}?formError=sendToReviewDoNotCollect" method="POST" >
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>

                    <div class="input-block-content">
                        <@f.textarea path="comment.comment" required="" optional=false cssClass="medium" maxLength=InitiativeConstants.INITIATIVE_COMMENT_MAX key="sendToMunicipality.sentComment" />
                    </div>
                    
                    <div class="input-block-content">
                        <button type="submit" name="${UrlConstants.ACTION_SEND_TO_REVIEW}" id="modal-${UrlConstants.ACTION_SEND_TO_REVIEW}" value="${UrlConstants.ACTION_SEND_TO_REVIEW}" class="large-button"><span class="large-icon mail"><@u.messageHTML "action.sendToReview.doNotCollect" /></button>
                        <a href="${managementURL}" class="push close"><@u.message "action.cancel" /></a>
                    </div>
                </form>
            </@compress>
        </#assign>
    
        <#-- Confirm send to REVIEW for NOSCRIPT-users -->
        <#if sendToReviewConfirm>
        <noscript>
            <div id="send-to-review" class="msg-block cf">
                <h2><@u.message "sendToReview.doNotCollect.confirm.title.nojs" /></h2>
                <#noescape>${sendToReviewDoNotCollect}</#noescape>
            </div>
        </noscript>
        </#if>
        
        <#assign sendToReviewCollect>
            <@compress single_line=true>
            
                <p><@u.message "sendToReview.collect.confirm.description" /></p>
                <p><@u.message "sendToReview.collect.confirm.instruction" /></p>
                
                <form action="${springMacroRequestContext.requestUri}" method="POST" >
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>
                    <button type="submit" name="${UrlConstants.ACTION_SEND_TO_REVIEW_COLLECT}" id="modal-${UrlConstants.ACTION_SEND_TO_REVIEW_COLLECT}" value="${UrlConstants.ACTION_SEND_TO_REVIEW_COLLECT}" class="small-button"><span class="small-icon save-and-send"><@u.message "action.sendToReview" /></button>
                    <a href="${managementURL}" class="push close"><@u.message "action.cancel" /></a>
                </form>
            </@compress>
        </#assign>
    
        <#-- Confirm send to REVIEW for NOSCRIPT-users -->
        <#if sendToReviewAndCollectConfirm>
        <noscript>
            <div id="send-to-review" class="msg-block cf">
                <h2><@u.message "sendToReview.collect.confirm.title" /></h2>
                <#noescape>${sendToReviewCollect}</#noescape>
            </div>
        </noscript>
        </#if>
    </#if> <#-- /managementSettings.allowSendToReview -->
    
    <#if managementSettings.allowSendToMunicipality || managementSettings.allowInviteAuthors || managementSettings.allowPublish>
        <#assign startCollectingConfirm = false />
        <#assign sendToMunicipalityConfirm = false />
        
        <#if RequestParameters['start-collecting']?? && RequestParameters['start-collecting'] == "confirm">
            <#assign startCollectingConfirm = true />
        </#if>        
        
        <#if RequestParameters['send-to-municipality']?? && RequestParameters['send-to-municipality'] == "confirm">
            <#assign sendToMunicipalityConfirm = true />
        </#if>
    
        <#if !sendToMunicipalityConfirm && !startCollectingConfirm>
            <div class="msg-block">

                <#if managementSettings.allowPublish>
                    <div class="system-msg msg-info">
                        <h2 id="start-collecting"><@u.message "startCollecting.title" /></h2>
                        <p><@u.message "startCollecting.description" /></p>
        
                        <a href="${managementURL}?start-collecting=confirm#start-collecting" id="js-start-collecting" class="small-button js-start-collecting"><span class="small-icon save-and-send"><@u.message "action.startCollecting" /></span></a>
                    </div>
                <#else>
                    <div class="system-msg msg-info">
                        <@u.message "collecting.info" />
                    </div>
                </#if>

                <#if managementSettings.allowInviteAuthors>
                    <div class="system-msg msg-info">
                        <h2><@u.message "addAuthors.title" /></h2>
                        <p><@u.message "addAuthors.description" /></p> <#--<@u.link href=urls.manageAuthors(initiative.id) labelKey="addAuthors.link" />-->

                        <a href="${urls.manageAuthors(initiative.id)}" class="small-button"><span class="small-icon user"><@u.message "addAuthors.link" /></span></a>
                    </div>
                </#if>

                <#if managementSettings.allowSendToMunicipality>
                    <div class="system-msg msg-info">
                        <h2 id="send-to-municipality"><@u.message "sendToMunicipality.title" /></h2>
                        <p><@u.message "sendToMunicipality.description" /></p>

                        <a href="${managementURL}?send-to-municipality=confirm#send-to-municipality" id="js-send-to-municipality" class="small-button js-send-to-municipality"><span class="small-icon mail"><@u.message "action.sendToMunicipality" /></span></a>
                    </div>
                </#if>
            </div>
        </#if>
        
        <#assign startCollecting>
            <@compress single_line=true>
            
                <p><@u.message "startCollecting.confirm.description" /></p>
                <p><@u.message "startCollecting.confirm.description.2" /></p>
                
                <form action="${springMacroRequestContext.requestUri}" method="POST" >
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>
                    <button type="submit" name="${UrlConstants.ACTION_START_COLLECTING}" id="modal-${UrlConstants.ACTION_START_COLLECTING}" value="${UrlConstants.ACTION_START_COLLECTING}" class="small-button"><span class="small-icon save-and-send"><@u.message "action.startCollecting.confirm" /></button>
                    <a href="${managementURL}#start-collecting" class="push close"><@u.message "action.cancel" /></a>
                </form>
            </@compress>
        </#assign>
    
        <#-- Confirm start collecting for NOSCRIPT-users -->
        <#if startCollectingConfirm>
        <noscript>
            <div id="start-collecting" class="msg-block cf">
                <#noescape>
                    <h2><@u.message "startCollecting.confirm.title" /></h2>
                    ${startCollecting}
                </#noescape>
            </div>
        </noscript>
        </#if>

        <#assign sendToMunicipality>
            <@compress single_line=true>

                <@u.errorsSummary path="comment.*" prefix="comment."/>

                <form action="${springMacroRequestContext.requestUri}?formError=sendToMunicipality" method="POST" >
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>

                    <div class="input-block-content">
                        <@f.textarea path="comment.comment" required="" optional=false cssClass="medium" maxLength=InitiativeConstants.INITIATIVE_COMMENT_MAX key="sendToMunicipality.sentComment" />
                    </div>
                    
                    <div class="input-block-content">
                        <button type="submit" name="${UrlConstants.ACTION_SEND_TO_MUNICIPALITY}" id="modal-${UrlConstants.ACTION_SEND_TO_MUNICIPALITY}" value="${UrlConstants.ACTION_SEND_TO_MUNICIPALITY}" class="small-button"><span class="small-icon mail"><@u.message "action.sendToMunicipality.confirm" /></button>
                        <a href="${managementURL}#send-to-municipality" class="push close"><@u.message "action.cancel" /></a>
                    </div>
                </form>
            </@compress>
        </#assign>
    
        <#-- Confirm send to municipality for NOSCRIPT-users -->
        <#if sendToMunicipalityConfirm>
        <noscript>
            <div id="send-to-municipality" class="msg-block cf">
                <#noescape>
                    <h2><@u.message "sendToMunicipality.confirm.title.nojs" /></h2>
                    ${sendToMunicipality}
                </#noescape>
            </div>
        </noscript>
        </#if>
        
    
    </#if> <#-- /managementSettings.allowSendToMunicipality -->
        
    <#if managementSettings.allowSendFixToReview>
        <#if !RequestParameters['send-fix-to-review']??>
            <div class="msg-block">
                <div class="system-msg msg-info">
                    <h2 id="send-fix-to-review"><@u.message "sendFixToReview.title" /></h2>
                    
                    <p><@u.message "sendFixToReview.description" /></p>
                    <p><@u.message "sendFixToReview.instruction" /></p>
        
                    <a href="${managementURL}?send-fix-to-review=confirm#send-fix-to-review" id="js-send-fix-to-review" class="small-button js-send-fix-to-review"><span class="small-icon save-and-send"><@u.message "action.sendToReview" /></span></a>
                </div>
            </div>
        </#if>
        
        <#assign sendFixToReview>
            <@compress single_line=true>
                <form action="${springMacroRequestContext.requestUri}" method="POST" >
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>
                    
                    <p><@u.message "sendToReview.collect.confirm.description" /></p>
                    <p><@u.message "sendToReview.collect.confirm.instruction" /></p>
                    
                    <div class="input-block-content">
                        <button type="submit" name="${UrlConstants.ACTION_SEND_FIX_TO_REVIEW}" id="modal-${UrlConstants.ACTION_SEND_FIX_TO_REVIEW}" value="${UrlConstants.ACTION_SEND_FIX_TO_REVIEW}" class="small-button"><span class="small-icon save-and-send"><@u.message "action.sendToReview" /></button>
                        <a href="${managementURL}#send-fix-to-review" class="push close"><@u.message "action.cancel" /></a>
                    </div>
                </form>
            </@compress>
        </#assign>
        
        <#-- Confirm send fix to review for NOSCRIPT-users -->
        <#if RequestParameters['send-fix-to-review']?? && RequestParameters['send-fix-to-review'] == "confirm">
        <noscript>
            <div id="send-fix-to-review" class="msg-block cf">
                <#noescape>
                    <h2><@u.message "sendFixToReview.confirm.title" /></h2>
                    ${sendFixToReview}
                </#noescape>
            </div>
        </noscript>
        </#if>
    </#if> <#-- /managementSettings.allowSendFixToReview -->

    <#--
     * Management VIEW modals
     * 
     * Uses jsRender for templating.
     * Same content is generated for NOSCRIPT and for modals.
     *
     * Modals:
     *  Request message (defined in macro u.requestMessage)
     *  Confirm send for publish and to municiaplity
     *  Confirm send for publish
     *
     * jsMessage:
     *  Warning if cookies are disabled
    -->
    <@u.modalTemplate />
    <@u.jsMessageTemplate />
    
    <script type="text/javascript">
        var modalData = {};
        
        <#-- Modal: Request messages. Check for components/utils.ftl -->
        <#if requestMessageModalHTML??>    
            modalData.requestMessage = function() {
                return [{
                    title:      '<@u.message requestMessageModalTitle+".title" />',
                    content:    '<#noescape>${requestMessageModalHTML?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        
        <#-- Modal: Confirm send for publish and to municiaplity. -->
        <#if sendToReviewDoNotCollect??>    
            modalData.sendToReviewDoNotCollect = function() {
                return [{
                    title:      '<@u.message "sendToReview.doNotCollect.confirm.title" />',
                    content:    '<#noescape>${sendToReviewDoNotCollect?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        <#-- Autoload modal if it has errors -->
        <#if RequestParameters['formError']?? && RequestParameters['formError'] == "sendToReviewDoNotCollect">
        modalData.sendToReviewDoNotCollectAutoLoad = function() {
            return [{
                title:      '<@u.message "sendToReview.doNotCollect.confirm.title" />',
                content:    '<#noescape>${sendToReviewDoNotCollect?replace("'","&#39;")}</#noescape>'
            }]
        };
        </#if>
        
        <#-- Modal: Confirm send for publish. -->
        <#if sendFixToReview??>    
            modalData.sendFixToReview = function() {
                return [{
                    title:      '<@u.message "sendFixToReview.confirm.title" />',
                    content:    '<#noescape>${sendFixToReview?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        
        <#-- Modal: Send fix to review. -->
        <#if sendToReviewCollect??>    
            modalData.sendToReviewCollect = function() {
                return [{
                    title:      '<@u.message "sendToReview.collect.confirm.title" />',
                    content:    '<#noescape>${sendToReviewCollect?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        
        <#-- Modal: Confirm start collecting. -->
        <#if startCollecting??>    
            modalData.startCollecting = function() {
                return [{
                    title:      '<@u.message "startCollecting.confirm.title" />',
                    content:    '<#noescape>${startCollecting?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        
        <#-- Modal: Confirm send to municipality. -->
        <#if sendToMunicipality??>    
            modalData.sendToMunicipality = function() {
                return [{
                    title:      '<@u.message "sendToMunicipality.confirm.title" />',
                    content:    '<#noescape>${sendToMunicipality?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
        <#-- Autoload modal if it has errors -->
        <#if RequestParameters['formError']?? && RequestParameters['formError'] == "sendToMunicipality">
            modalData.sendToMunicipalityAutoLoad = function() {
                return [{
                    title:      '<@u.message "sendToMunicipality.confirm.title" />',
                    content:    '<#noescape>${sendToMunicipality?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>
    
        var messageData = {};

        <#-- jsMessage: Warning if cookies are not enabled -->
        messageData.warningCookiesDisabled = function() {
            return [{
                type:      'warning',
                content:    '<h3><@u.message "warning.cookieError.title" /></h3><div><@u.messageHTML key="warning.cookieError.description" args=[managementURL] /></div>'
            }]
        };


    </script>

</@l.main>

</#escape> 