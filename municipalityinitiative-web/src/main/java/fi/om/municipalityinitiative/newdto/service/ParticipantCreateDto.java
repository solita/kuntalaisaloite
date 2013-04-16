package fi.om.municipalityinitiative.newdto.service;

import fi.om.municipalityinitiative.newdto.ui.ParticipantUICreateDto;

public class ParticipantCreateDto {

    private Long municipalityInitiativeId;
    private boolean franchise;
    private boolean showName;
    private String participantName;
    private Long homeMunicipality;
    private String email;

    public static ParticipantCreateDto parse(ParticipantUICreateDto participant, Long initiativeId) {
        ParticipantCreateDto participantCreateDto = new ParticipantCreateDto();

        participantCreateDto.setMunicipalityInitiativeId(initiativeId);
        participantCreateDto.setShowName(participant.getShowName() == null ? false : participant.getShowName());
        participantCreateDto.setParticipantName(participant.getParticipantName());
        participantCreateDto.setHomeMunicipality(participant.getHomeMunicipality());
        participantCreateDto.setEmail(participant.getParticipantEmail());
        return participantCreateDto;
    }

    public Long getMunicipalityInitiativeId() {
        return municipalityInitiativeId;
    }

    public void setMunicipalityInitiativeId(Long municipalityInitiativeId) {
        this.municipalityInitiativeId = municipalityInitiativeId;
    }

    public void setFranchise(boolean franchise) {
        this.franchise = franchise;
    }

    public boolean isFranchise() {
        return franchise;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public boolean isShowName() {
        return showName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setHomeMunicipality(Long homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public Long getHomeMunicipality() {
        return homeMunicipality;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
