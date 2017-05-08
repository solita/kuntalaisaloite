package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.dto.service.NormalParticipant;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.dto.service.VerifiedParticipant;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.util.Membership;

import java.util.Collection;
import java.util.List;

public interface ParticipantDao {

    Long prepareConfirmedParticipant(Long initiativeId, Long homeMunicipality, String email, Membership membership, boolean showName);

    Long create(ParticipantCreateDto createDto, String confirmationCode);

    void confirmParticipation(Long participantId, String confirmationCode);

    void verifiedUserParticipatesNormalInitiative(Long participantId, VerifiedUserId userId, boolean verified);

    Collection<Long> getNormalInitiativesVerifiedUserHasParticipated(VerifiedUserId userId);

    List<NormalParticipant> findNormalPublicParticipants(Long initiativeId);

    List<NormalParticipant> findNormalPublicParticipants(Long initiativeId, int offset, int limit);

    List<NormalParticipant> findNormalAllParticipants(Long initiativeId, int offset, int limit);

    List<VerifiedParticipant> findVerifiedPublicParticipants(Long initiativeId, int offset, int limit);

    List<VerifiedParticipant> findVerifiedAllParticipants(Long initiativeId, int offset, int limit);

    Maybe<Long> getInitiativeIdByParticipant(Long participantId);

    void deleteParticipant(Long initiativeId, Long participantId);

    void deleteVerifiedParticipant(Long initiativeId, Long participantId);

    void updateVerifiedParticipantShowName(Long initiativeId, String hash, boolean showName);

    void addVerifiedParticipant(Long initiativeId, VerifiedUserId verifiedUserId, boolean showName, boolean present, Long aLong, Membership municipalMembership);

    List<Participant> findAllParticipants(Long initiativeId, boolean requireShowName);
}
