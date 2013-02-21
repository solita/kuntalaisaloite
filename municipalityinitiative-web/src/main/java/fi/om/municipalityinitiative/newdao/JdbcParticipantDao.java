package fi.om.municipalityinitiative.newdao;

import com.mysema.query.Tuple;
import com.mysema.query.sql.postgres.PostgresQueryFactory;
import com.mysema.query.support.Expressions;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.MappingProjection;
import com.mysema.query.types.expr.CaseBuilder;
import com.mysema.query.types.expr.SimpleExpression;
import fi.om.municipalityinitiative.dao.SQLExceptionTranslated;
import fi.om.municipalityinitiative.newdto.service.Municipality;
import fi.om.municipalityinitiative.newdto.service.Participant;
import fi.om.municipalityinitiative.newdto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.newdto.ui.ParticipantCount;
import fi.om.municipalityinitiative.sql.QMunicipality;
import fi.om.municipalityinitiative.util.MaybeHoldingHashMap;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static fi.om.municipalityinitiative.sql.QParticipant.participant;

@Transactional(readOnly = true)
@SQLExceptionTranslated
public class JdbcParticipantDao implements ParticipantDao {

    @Resource
    PostgresQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = false)
    public Long create(ParticipantCreateDto createDto) {
        return queryFactory.insert(participant)
                .set(participant.franchise, createDto.isFranchise())
                .set(participant.municipalityId, createDto.getHomeMunicipality())
                .set(participant.municipalityInitiativeId, createDto.getMunicipalityInitiativeId())
                .set(participant.name, createDto.getParticipantName())
                .set(participant.showName, createDto.isShowName())
                .executeWithKey(participant.id);
    }

    // TODO: Fix magic strings to enum constants or something.
    @Override
    public ParticipantCount getParticipantCount(Long initiativeId) {

        Expression<String> caseBuilder = new CaseBuilder()
                .when(participant.franchise.isTrue().and(participant.showName.isTrue())).then(new ConstantImpl<String>("11"))
                .when(participant.franchise.isTrue().and(participant.showName.isFalse())).then(new ConstantImpl<String>("10"))
                .when(participant.franchise.isFalse().and(participant.showName.isTrue())).then(new ConstantImpl<String>("01"))
                .when(participant.franchise.isFalse().and(participant.showName.isFalse())).then(new ConstantImpl<String>("00"))
                .otherwise(new ConstantImpl<String>("XX"));


        SimpleExpression<String> simpleExpression = Expressions.as(caseBuilder, "testi");
        MaybeHoldingHashMap<String, Long> map = new MaybeHoldingHashMap<String, Long>(queryFactory
                .from(participant)
                .where(participant.municipalityInitiativeId.eq(initiativeId))
                .groupBy(simpleExpression)
                .map(simpleExpression, participant.count()));

        ParticipantCount participantCount = new ParticipantCount();
        participantCount.getFranchise().setPublicNames(map.get("11").or(0L));
        participantCount.getFranchise().setPrivateNames(map.get("10").or(0L));
        participantCount.getNoFranchise().setPublicNames(map.get("01").or(0L));
        participantCount.getNoFranchise().setPrivateNames(map.get("00").or(0L));
        return participantCount;

    }

    @Override
    public List<Participant> findPublicParticipants(Long initiativeId) {
        return queryFactory.query()
                .from(participant)
                .where(participant.municipalityInitiativeId.eq(initiativeId))
                .leftJoin(participant.participantMunicipalityFk, QMunicipality.municipality)
                .where(participant.showName.eq(true))
                .orderBy(participant.id.desc())
                .list(participantMapping);
    }

    @Override
    public List<Participant> findAllParticipants(Long initiativeId) {
        return queryFactory.query()
                .from(participant)
                .where(participant.municipalityInitiativeId.eq(initiativeId))
                .leftJoin(participant.participantMunicipalityFk, QMunicipality.municipality)
                .orderBy(participant.id.desc())
                .list(participantMapping);
    }

    Expression<Participant> participantMapping =
            new MappingProjection<Participant>(Participant.class,
                    participant.all(), QMunicipality.municipality.all()) {
                @Override
                protected Participant map(Tuple row) {
                    return new Participant(
                            row.get(participant.participateTime),
                            row.get(participant.name),
                            row.get(participant.franchise),
                            new Municipality(
                                    row.get(QMunicipality.municipality.name),
                                    row.get(QMunicipality.municipality.id)
                            )
                    );
                }
            };

}
