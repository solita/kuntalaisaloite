package fi.om.municipalityinitiative.service;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.ui.ParticipantListInfo;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class ParticipantServiceTest {

    // There is no currently really functionality at ParticipantService....

    @Test
    public void toListInfo_sets_author_flag_to_true_if_author() {
        List<Participant> participants = Lists.newArrayList();
        participants.add(participant(3L, "Authori"));
        participants.add(participant(5L, "Ei Authori"));

        List<ParticipantListInfo> participantListInfos = ParticipantService.toListInfo(participants, Collections.singleton(3L));

        assertThat(participantListInfos, hasSize(2));
        assertThat(participantListInfos.get(0).getName(), is("Authori"));
        assertThat(participantListInfos.get(0).isAuthor(), is(true));

        assertThat(participantListInfos.get(1).getName(), is("Ei Authori"));
        assertThat(participantListInfos.get(1).isAuthor(), is(false));
    }

    private static Participant participant(long id, String name) {
        Participant participant = new Participant();
        participant.setId(id);
        participant.setName(name);
        return participant;
    }

}
