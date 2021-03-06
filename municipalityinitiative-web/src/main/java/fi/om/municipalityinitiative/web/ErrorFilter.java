package fi.om.municipalityinitiative.web;

import fi.om.municipalityinitiative.dao.InvitationNotValidException;
import fi.om.municipalityinitiative.exceptions.*;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.NestedServletException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class ErrorFilter implements Filter {
    
    public static final String ATTR_ERROR_CASE_ID = "errorCaseId";

    private final Logger log = LoggerFactory.getLogger(ErrorFilter.class); 
    
    private final String feedbackEmail;
    
    public ErrorFilter(String feedbackEmail) {
        this.feedbackEmail = feedbackEmail;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        try {
            chain.doFilter(servletRequest, servletResponse);
        } catch (final Throwable e) {
            // Nested exception is needed to find out the cause. 
            // Original exception is needed for logging.

            Throwable nested;
            if (e instanceof NestedServletException) {
                nested = e.getCause();
            } else {
                nested = e;
            }

            if (nested instanceof InvitationNotValidException) {
                handeInvitationNotValid(request, response, e);
            } else if (nested instanceof InvalidParticipationConfirmationException) {
                handleParticipationNotValid(request, response, e);
            } else if (nested instanceof OperationNotAllowedException) {
                handleOperationNotAllowed(request, response, e);
            }
            else if (nested instanceof NotFoundException) {
                handleNotFound(request, response, e);
            } else if (nested instanceof AccessDeniedException
                    || nested instanceof AuthenticationRequiredException
                    || nested instanceof InvalidLoginException) {
                handleAccessDenied(request, response, e);
            }
            else {
                handleUnexpectedError(request, response, e);
            }
        }
    }

    private void handleOperationNotAllowed(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        log.info(getErrorMessage("Operation not allowed:" + e.getMessage(), null, request));
        //log.info("Invitation not valid", e);
        request.setAttribute("errorMessage", "error.409.operation.not.allowed");
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }

    private void handeInvitationNotValid(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        log.info(getErrorMessage("Invitation not valid: " + e.getMessage(), null, request));
        //log.info("Invitation not valid", e);
        request.setAttribute("errorMessage", "error.410.invitation.not.valid.error.message");
        response.sendError(HttpServletResponse.SC_GONE);
    }

    private void handleParticipationNotValid(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        log.info(getErrorMessage("Participation confirmation not valid: " + e.getMessage(), null, request));
        //log.info("Participation confirmation not valid", e);
        request.setAttribute("errorMessage", "error.410.participation.confirmation.not.valid.error.message");
        response.sendError(HttpServletResponse.SC_GONE);
    }

    private void handleUnexpectedError(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        String caseId = UUID.randomUUID().toString();
        caseId = LocalDate.now().toString("yyyyMMdd") + "-" + caseId; //date prefix makes easier to find right log file
        log.error(getErrorMessage("UnexpectedError", caseId, request), e);

        request.setAttribute("errorMessage", e.getMessage());
        request.setAttribute(ATTR_ERROR_CASE_ID, caseId);
        request.setAttribute("requestURI", request.getRequestURI());
        request.setAttribute("feedbackEmail", feedbackEmail);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }

    private void handleNotFound(HttpServletRequest request, HttpServletResponse response, Throwable e)
            throws IOException {
        log.info(getErrorMessage("NotFound - " + e.getMessage(), null, request));
        log.info("Not found", e);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        log.info(getErrorMessage("AccessDenied - " + e.getMessage(), null, request));
        // User doesn't need to know difference between 404 and 403
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    
    private String getErrorMessage(String label, String caseId, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(128);
        
        sb.append(label).append(": ").append(getFullURI(request)).append("\n");
        if (caseId != null) {
            sb.append("  CASE: ").append(caseId).append("\n");
        }
        // NOTE: Referee is NOT logged - it might expose personal data (like SSN (hetu) from Vetuma vetumaLogin response)
        sb.append("  UA: ").append(request.getHeader("User-Agent")).append("\n");
        sb.append("  IP: ").append(request.getRemoteAddr());
        
        return sb.toString();
    }
    
    private String getFullURI(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(64);
        sb.append(request.getMethod()).append(" ");
        sb.append(request.getRequestURI());
        String qs = request.getQueryString();
        if (qs != null) {
            sb.append("?").append(qs);
        }
        return sb.toString();
    }
    
    @Override
    public void destroy() {
    }

}
