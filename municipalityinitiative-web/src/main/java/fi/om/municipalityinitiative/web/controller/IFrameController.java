package fi.om.municipalityinitiative.web.controller;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.service.CachedInitiativeFinder;
import fi.om.municipalityinitiative.web.SearchParameterQueryString;
import fi.om.municipalityinitiative.web.Urls;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static fi.om.municipalityinitiative.web.Urls.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class IFrameController extends BaseController {

    @Resource
    CachedInitiativeFinder cachedInitiativeFinder;

    public IFrameController(boolean optimizeResources, String resourcesVersion) {
        super(optimizeResources, resourcesVersion);
    }


    @RequestMapping(value = {IFRAME_FI, IFRAME_SV}, method = GET)
    public String iframe(@RequestParam(value="municipality", required = false) Long municipality,
                         InitiativeSearch search,
                         Model model,
                         Locale locale,
                         HttpServletRequest request) {


        return iframeOld(municipality, search, model, locale, request);
    }


    @RequestMapping(value={IFRAME_OLD_FI, IFRAME_OLD_SV}, method=GET)
    public String iframeOld(@RequestParam(value="municipality", required = false) Long municipality, InitiativeSearch search,
                         Model model,
                         Locale locale,
                         HttpServletRequest request) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().search());

        convertSingleMunipalityToListIfNeeded(search, municipality);

        Optional<List<Municipality>> municipalities = cachedInitiativeFinder.getMunicipalities(Optional.ofNullable(search.getMunicipalities()));

        SearchParameterQueryString queryString = new SearchParameterQueryString(Urls.get(locale), search, municipalities.orElse(Lists.newArrayList()));

        return ViewGenerator.iframeSearch(
                cachedInitiativeFinder.findIframeInitiatives(search),
                municipalities,
                queryString
        ).view(model, urls.alt().iframe());
    }

    @RequestMapping(value={IFRAME_GENERATOR_FI, IFRAME_GENERATOR_SV}, method=GET)
    public String iframeGenerator(Model model, Locale locale) {
        Urls urls = Urls.get(locale);
        model.addAttribute(ALT_URI_ATTR, urls.alt().search());

        List<Municipality> municipalities = cachedInitiativeFinder.findAllMunicipalities(locale);

        return ViewGenerator.iframeGenerator(municipalities).view(model, urls.alt().iframeGenerator());
    }

    private void convertSingleMunipalityToListIfNeeded(InitiativeSearch search, Long municipality) {
        if (search.getMunicipalities() == null && municipality != null) {
            search.setMunicipalities(municipality);
        }
    }

}
