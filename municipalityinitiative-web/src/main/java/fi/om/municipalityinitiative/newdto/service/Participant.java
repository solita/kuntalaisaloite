package fi.om.municipalityinitiative.newdto.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.om.municipalityinitiative.json.LocalDateJsonSerializer;
import org.joda.time.LocalDate;

public class Participant {
    private final String name;
    private final boolean franchise;
    private final LocalDate participateDate;
    private final Municipality homeMunicipality;

    public Participant(LocalDate participateDate, String name, boolean franchise, Municipality homeMunicipality) {
        this.name = name;
        this.franchise = franchise;
        this.participateDate = participateDate;
        this.homeMunicipality = homeMunicipality;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public boolean isFranchise() {
        return franchise;
    }

    @JsonSerialize(using=LocalDateJsonSerializer.class)
    public LocalDate getParticipateDate() {
        return participateDate;
    }

    public Municipality getHomeMunicipality() {
        return homeMunicipality;
    }
}
