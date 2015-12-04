package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.exceptions.InvalidVideoUrlException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class VideoServiceTest extends ServiceIntegrationTestBase{

    public static final String HTTPS_PROTOCOL = "https://";
    public static final String HTTP_PROTOCOL = "http://";
    public static final String VALID_VIDEO_URL = "https://www.youtube.com/watch?v=lq-54gox6-k";
    public static final String VIDEO_ID = "lq-54gox6-k";
    public static final String VALID_EMBED_VIDEO_URL = "https://www.youtube.com/embed/lq-54gox6-k";
    public static final String VALID_VIMEO_URL = "https://vimeo.com/93350761";
    public static final String INVALID_YOUTUBE_URL = "https://www.youtube.com/aBQzKKM6480?t=97";
    public static final String WRONG_HOST = "https://youtu.be/aBQzKKM6480?t=97";
    private VideoService videoService;


    @Override
    protected void childSetup() {
        videoService =  new VideoService();
    }

    @Test
    public void get_youtubevideoid_from_watch_url() throws InvalidVideoUrlException {
        assertThat(videoService.getYouTubeVideoId(VALID_VIDEO_URL), is(VIDEO_ID));
    }

    @Test
    public void get_youtubevideoid_from_embed_url() throws InvalidVideoUrlException {
        assertThat(videoService.getYouTubeVideoId(VALID_EMBED_VIDEO_URL), is(VIDEO_ID));
    }

    @Test
    public void validate_youtube_video_url_success() {
        assertThat(VALID_VIDEO_URL.matches(videoService.VIDEO_REGEX), is(true));
    }

    @Test
    public void validate_embedded_youtube_video_url_success() {
        assertThat(VALID_EMBED_VIDEO_URL.matches(videoService.VIDEO_REGEX), is(true));
    }

    @Test
    public void validate_vimeo_url_success(){
        assertThat(VALID_VIMEO_URL.matches(videoService.VIDEO_REGEX), is(true));
    }

    @Test
    public void get_parameters_invalid(){
        assertThat(INVALID_YOUTUBE_URL.matches(videoService.VIDEO_REGEX), is(false));
    }

    @Test
    public void wrong_host_invalid() {
        assertThat(WRONG_HOST.matches(videoService.VIDEO_REGEX), is(false));
    }
}