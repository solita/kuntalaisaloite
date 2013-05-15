package fi.om.municipalityinitiative.newdto.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fi.om.municipalityinitiative.json.LocalDateJsonSerializer;
import fi.om.municipalityinitiative.util.Membership;
import org.joda.time.LocalDate;

public class Participant {
    private final String name;
    private final LocalDate participateDate;
    private final Municipality homeMunicipality;
    private final String email;
    private final Membership membership;

    public Participant(LocalDate participateDate, String name, Municipality homeMunicipality, String email, Membership membership) {
        this.name = name;
        this.participateDate = participateDate;
        this.homeMunicipality = homeMunicipality;
        this.email = email;
        this.membership = membership;
    }

    public String getName() {
        return name;
    }

    @JsonSerialize(using=LocalDateJsonSerializer.class)
    public LocalDate getParticipateDate() {
        return participateDate;
    }

    public Municipality getHomeMunicipality() {
        return homeMunicipality;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public Membership getMembership() {
        return membership;
    }
}
