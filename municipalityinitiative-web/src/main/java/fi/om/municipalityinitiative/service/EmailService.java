package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.newdto.ui.InitiativeViewInfo;

public interface EmailService {

    void sendEmail(InitiativeViewInfo initiative);
}