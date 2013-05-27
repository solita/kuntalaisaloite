package fi.om.municipalityinitiative.dto.service;

import fi.om.municipalityinitiative.util.Maybe;
import org.joda.time.DateTime;

public class AuthorInvitation {

    private Long initiativeId;
    private String confirmationCode;
    private String name;
    private String email;
    private DateTime invitationTime;
    private Maybe<DateTime> rejectTime;

    public Long getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(Long initiativeId) {
        this.initiativeId = initiativeId;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInvitationTime(DateTime invitationTime) {
        this.invitationTime = invitationTime;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public DateTime getInvitationTime() {
        return invitationTime;
    }

    public boolean isExpired() {
        return invitationTime.isBefore(new DateTime().minusMinutes(60));
    }

    public void setRejectTime(Maybe<DateTime> rejectTime) {
        this.rejectTime = rejectTime;
    }

    public boolean isRejected() {
        return rejectTime.isPresent();
    }

    public Maybe<DateTime> getRejectTime() {
        return rejectTime;
    }
}