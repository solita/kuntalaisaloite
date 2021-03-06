package fi.om.municipalityinitiative.dto.ui;

import com.google.common.base.Strings;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.util.FixState;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.Optional;

public final class InitiativeViewInfo {

    private final Initiative initiative;

    private InitiativeViewInfo(Initiative initiative) {
        this.initiative = initiative;
    }

    public static InitiativeViewInfo parse(Initiative initiative) {
        return new InitiativeViewInfo(initiative);
    }

    public String getName() {
        return initiative.getName();
    }

    public String getProposal() {
        return initiative.getProposal();
    }

    public int getParticipantCountCitizen() {
        return initiative.getParticipantCountCitizen();
    }

    public LocalDate getCreateTime() {
        return initiative.getCreateTime();
    }

    public Long getId() {
        return initiative.getId();
    }

    public boolean isCollaborative() {
        return initiative.isCollaborative();
    }
    
    public boolean isSingle(){
        return initiative.getType() == InitiativeType.SINGLE;
    }
    
    public boolean isSent() {
        return initiative.isSent();
    }

    public Optional<LocalDate> getSentTime() {
        return initiative.getSentTime();
    }

    public InitiativeState getState() {
        return initiative.getState();
    }

    public Municipality getMunicipality() {
        return initiative.getMunicipality();
    }

    public LocalDate getStateTime() {
        return initiative.getStateTime();
    }

    public String getExtraInfo() {
        return initiative.getExtraInfo();
    }

    public FixState getFixState() {
        return initiative.getFixState();
    }

    public boolean hasNeverBeenSaved() {
        return Strings.isNullOrEmpty(initiative.getName());
    }

    public int getExternalParticipantCount() {
        return initiative.getExternalParticipantCount();
    }

    public boolean isVerifiable() {
        return initiative.getType().isVerifiable();
    }
    
    public InitiativeType getType() {
        return initiative.getType();
    }

    public Optional<Long> getYouthInitiativeId() {
        return initiative.getYouthInitiativeId();
    }

    public Optional<String> getDecisionText() {
        return initiative.getDecision();
    }

    public Optional<DateTime> getDecisionDate() {return initiative.getDecisionDate();}

    public Optional<DateTime> getDecisionModifiedDate() {
        return initiative.getDecisionModifiedDate();
    }
    public ParticipantCount getParticipantCount() {
        ParticipantCount participantCount = new ParticipantCount();
        participantCount.setPrivateNames(initiative.getParticipantCount() - initiative.getParticipantCountPublic());
        participantCount.setPublicNames(initiative.getParticipantCountPublic());
        return participantCount;
    }


    public Optional<String> getVideoUrl() {
        return initiative.getVideoUrl();
    }


}
