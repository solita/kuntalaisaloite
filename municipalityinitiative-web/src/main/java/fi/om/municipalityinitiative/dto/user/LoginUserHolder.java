package fi.om.municipalityinitiative.dto.user;

import fi.om.municipalityinitiative.service.AccessDeniedException;

public class LoginUserHolder<E extends User> {

    E user;

    public LoginUserHolder(E user) {
        if (user == null)
            throw new RuntimeException("User was null");
        this.user = user;
    }

    public E getUser() {
        return user;
    }

    public void assertManagementRightsForInitiative(Long initiativeId) {
        if (hasNoManagementRightsForInitiative(initiativeId)) {
            throw new AccessDeniedException("No access for initiative with id: " + initiativeId);
        }
    }

    private boolean hasNoManagementRightsForInitiative(Long initiativeId) {
        return !hasManagementRightsForInitiative(initiativeId);
    }

    public boolean hasManagementRightsForInitiative(Long initiativeId) {
        return user.hasRightToInitiative(initiativeId);
    }

    public Long getAuthorId() {
        return user.getAuthorId();
    }

    public void assertOmUser() {
        if (user.isNotOmUser()){
            throw new AccessDeniedException("No privileges");
        }
    }
}