<#import "/spring.ftl" as spring />
<#import "components/layout.ftl" as l />
<#import "components/utils.ftl" as u />
<#import "components/elements.ftl" as e />
<#import "components/forms.ftl" as f />

<#escape x as x?html> 

<#--
 * Layout parameters for HTML-title and navigation.
 * 
 * page = "page.initiative.public" or "page.initiative.unnamed"
 * pageTitle = initiative.name if exists, otherwise empty string
-->
<@l.main "page.initiative.manageAuthors" initiative.name!"">

    <@u.errorsSummary path="newInvitation.*" prefix="newInvitation."/>

    <div class="msg-block">
        <h2><@u.message "authors.title" /></h2>
        <p><@u.message "authors.description" /></p>
        <p><@u.message "authors.instruction" /></p>
    </div>

    <h1 class="name">${initiative.name!""}</h1>
    
    <div class="municipality">${initiative.municipality.getName(locale)}</div>
    
    <@e.stateInfo initiative />
    
    <@returnPrevious />

    <div class="view-block ">
        <div class="initiative-content-row last">
            <h2><@u.message "authors.title" /></h2>
        
            <#list authors as a>
                <div class="author cf ${a_has_next?string("","last")}">
                    <div class="details">
                        <h4 class="header">${a.contactInfo.name}</h4>
                        <div class="email">${a.contactInfo.email}</div>
                    </div>
    
                    <div class="invitation">
                        <span class="status"><span class="icon-small confirmed"></span> <span class="trigger-tooltip" title="<@u.message "invitation.accepted" />"><@u.localDate a.createTime/></span></span>
                        <#if a.id != user.authorId>
                        <span class="action"><span class="icon-small cancel"></span> <a href="?deleteAuthor=${a.id!""}" class="js-delete-author"
                            data-id="${a.id!""}"
                            data-name="${a.contactInfo.name!""}"
                            data-email="${a.contactInfo.email!""}"><@u.message "deleteParticipant.delete" /></a></span>
                        </#if>
                    </div>
                </div>
            </#list>
        </div>
    </div>

    <div class="view-block last">
        <div class="initiative-content-row">
            <h2><@u.message "invitations.title" /> (vanhenevat tunnissa)</h2>

            <#list invitations as i>
                <div class="author cf ${i.rejected?string("rejected","")} ${i_has_next?string("","last")}">
                    <div class="details">
                         <h4 class="header">${i.name}</h4>
                         <div class="email">${i.email}</div>
                    </div>

                    <div class="invitation">
                        <#if i.rejected>
                            <span class="status"><span class="icon-small rejected"></span> <@u.message "invitation.rejected" /> <@u.localDate i.rejectTime.value /></span>
                        <#elseif i.expired>
                            <span class="status"><span class="icon-small expired"></span> <@u.message "invitation.expired" /></span>
                            <span class="action push"><@u.message "invitation.sent" /> <@u.localDate i.invitationTime /></span>
                            <span class="action">
                                <form action="${springMacroRequestContext.requestUri}" method="POST" id="resend_${i.confirmationCode}">
                                    <@f.securityFilters/>
                                    <input type="hidden" name="${UrlConstants.PARAM_INVITATION_CODE}" value="${i.confirmationCode}"/>
                                    <button type="submit" value="<@u.message "invitation.resend"/>" class="btn-link"><span class="icon-small resend"></span> <@u.message "invitation.resend"/></button>
                                </form>
                            </span>
                        <#else>
                            <span class="status"><span class="icon-small unconfirmed"></span> <@u.message "invitation.unconfirmed" /> <#-- TODO <span class="bull">&bull;</span> <a href="#"><span class="icon-small cancel"></span> peru kutsu</a></span>-->
                            <span class="action push"><@u.message "invitation.sent" /> <@u.localDate i.invitationTime /></span>
                        </#if>
                    </div>
                </div>
            </#list>
            
        </div>
        
        <#-- Bind form for detecting validation errors -->
        <@spring.bind "newInvitation.*" />
        <#assign validationError = spring.status.error />
        
        <div class="initiative-content-row last">
            <div class="js-open-block hidden ${validationError?string("js-hide","")}">
                <a class="small-button gray js-btn-open-block" data-open-block="js-block-container" href="#"><span class="small-icon add"><@u.message "action.addAuthor" /></span></a>
            </div>
    
            <div class="cf js-block-container ${validationError?string("","js-hide")}">
                <h2><@u.message "invitations.addAuthor.title" /></h2>
                
                <div class="input-block-content no-top-margin">
                    <@u.systemMessage path="invitation.description" type="info" showClose=false />
                </div>
    
                <form action="${springMacroRequestContext.requestUri}" method="POST" id="form-send" class="sodirty">
                    <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>
                    
                    <div class="input-block-content no-top-margin">
                        <@f.textField path="newInvitation.authorName" required="required" optional=false cssClass="large" maxLength=InitiativeConstants.CONTACT_NAME_MAX />
                        <@f.textField path="newInvitation.authorEmail" required="required" optional=false cssClass="large" maxLength=InitiativeConstants.CONTACT_EMAIL_MAX />
                    </div>

                    <div class="input-block-content no-top-margin">
                        <button type="submit" name="${UrlConstants.ACTION_ACCEPT_INITIATIVE}" class="small-button"><span class="small-icon save-and-send"><@u.message "action.sendInvitation" /></span></button>
                        <a href="${springMacroRequestContext.requestUri}" class="push ${validationError?string("","js-btn-close-block hidden")}"><@u.message "action.cancel" /></a>
                    </div>
                    <br/><br/>
                </form>
            </div>
    
        </div>
    </div>
    
    <@returnPrevious />
    
    <#-- HTML for confirm Modal -->
    <#assign deleteAuthor>
        <@compress single_line=true>
            <form action="${springMacroRequestContext.requestUri}" method="POST" id="delete-author-form">
                <input type="hidden" name="CSRFToken" value="${CSRFToken}"/>
                
                <input type="hidden" name="${UrlConstants.PARAM_AUTHOR_ID}" id="${UrlConstants.PARAM_AUTHOR_ID}" value=""/>
                
                <h3><@u.message "deleteAuthor.confirm.description" /></h3>
                <div id="selected-author" class="details"></div>
                
                <br/>
                
                <div class="input-block-content">
                    <button type="submit" name="${UrlConstants.ACTION_DELETE_AUTHOR}" id="modal-${UrlConstants.ACTION_DELETE_AUTHOR}" value="${UrlConstants.ACTION_DELETE_AUTHOR}" class="small-button"><span class="small-icon save-and-send"><@u.message "action.deleteAuthor.confirm" /></button>
                    <a href="#" class="push close"><@u.message "action.cancel" /></a>
                </div>
            </form>
        </@compress>
    </#assign>
    
    <#--
     * Manage author modals
     * 
     * Uses jsRender for templating.
     * Same content is generated for NOSCRIPT and for modals.
     *
     * Modals:
     *  Delete author
     *
     * jsMessage:
     *  Warning if cookies are disabled
    -->
    <@u.modalTemplate />
    <@u.jsMessageTemplate />
    
    <script type="text/javascript">
        var modalData = {};
        
        <#-- Modal: Request messages. Check for components/utils.ftl -->
        <#if deleteAuthor??>    
            modalData.deleteAuthor = function() {
                return [{
                    title:      '<@u.message "deleteAuthor.confirm.title" />',
                    content:    '<#noescape>${deleteAuthor?replace("'","&#39;")}</#noescape>'
                }]
            };
        </#if>

        var messageData = {};

        <#-- jsMessage: Warning if cookies are not enabled -->
        messageData.warningCookiesDisabled = function() {
            return [{
                type:      'warning',
                content:    '<h3><@u.message "warning.cookieError.title" /></h3><div><@u.messageHTML key="warning.cookieError.description" args=[urls.participantList(initiative.id)] /></div>'
            }]
        };
    </script>
    
</@l.main>

<#-- 
 * returnPrevious
 *
 *  If request header referer equals management
 *      previousPageURI is the management URI
 *  Otherwise
 *      previousPageURI is the public view URI
-->
<#macro returnPrevious>
    <p><a href="${urls.management(initiative.id)}">&laquo; <@u.message "participantList.return.management" /></a></p>
</#macro>



</#escape> 

