package fi.om.municipalityinitiative.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.om.municipalityinitiative.dto.user.OmLoginUserHolder;
import fi.om.municipalityinitiative.newdao.InfoTextDao;
import fi.om.municipalityinitiative.dto.InfoPageText;
import fi.om.municipalityinitiative.dto.InfoTextFooterLink;
import fi.om.municipalityinitiative.dto.InfoTextSubject;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.util.InfoTextCategory;
import fi.om.municipalityinitiative.util.LanguageCode;
import fi.om.municipalityinitiative.util.Locales;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InfoTextService implements FooterLinkProvider {

    private InfoTextDao infoTextDao;

    public InfoTextService(InfoTextDao infoTextDao) {
        this.infoTextDao = infoTextDao;
    }

    public InfoPageText getPublished(String uri) {
        InfoPageText published = infoTextDao.getPublished(uri);
        return published;
    }

    @Override
    public List<InfoTextFooterLink> getFooterLinks(Locale locale) {
        return infoTextDao.getFooterLinks(languageCode(locale));
    }

    public InfoPageText getDraft(String uri, OmLoginUserHolder requiredOmLoginUserHolder) {
        requiredOmLoginUserHolder.assertOmUser();
        return infoTextDao.getDraft(uri);
    }

    public Map<String, List<InfoTextSubject>> getPublicSubjectList(Locale locale) {
        return mapByCategory(infoTextDao.getNotEmptySubjects(languageCode(locale)));
    }

    public Map<String, List<InfoTextSubject>> getOmSubjectList(Locale locale, OmLoginUserHolder requiredOmLoginUserHolder) {
        requiredOmLoginUserHolder.assertOmUser();
        return mapByCategory(infoTextDao.getAllSubjects(languageCode(locale)));
    }

    public void updateDraft(OmLoginUserHolder requiredOmLoginUserHolder, String localizedPageName, String content, String subject) {
        requiredOmLoginUserHolder.assertOmUser();

        InfoPageText infoPageText = InfoPageText.builder(localizedPageName)
                .withText(subject, content)
                .withModifier(requiredOmLoginUserHolder.getUser().getName(), DateTime.now())
                .build();
        infoTextDao.saveDraft(infoPageText);
    }

    public void publishDraft(String uri, OmLoginUserHolder requiredOmLoginUserHolder) {
        requiredOmLoginUserHolder.assertOmUser();
        infoTextDao.publishFromDraft(uri, requiredOmLoginUserHolder.getUser().getName());
    }

    public void restoreDraftFromPublished(String uri, OmLoginUserHolder requiredOmLoginUserHolder) {
        requiredOmLoginUserHolder.assertOmUser();
        infoTextDao.draftFromPublished(uri, requiredOmLoginUserHolder.getUser().getName());
    }

    private static Map<String, List<InfoTextSubject>> mapByCategory(List<InfoTextSubject> subjectList) {
        Map<String, List<InfoTextSubject>> map = Maps.newHashMap();
        for (InfoTextCategory infoTextCategory : InfoTextCategory.values()) {
            map.put(infoTextCategory.name(), Lists.<InfoTextSubject>newArrayList());
        }

        for (InfoTextSubject infoTextSubject : subjectList) {
            map.get(infoTextSubject.getInfoTextCategory().name()).add(infoTextSubject);
        }

        return map;
    }


    private static LanguageCode languageCode(Locale locale) {
        return locale.equals(Locales.LOCALE_FI) ? LanguageCode.FI : LanguageCode.SV;
    }
}
