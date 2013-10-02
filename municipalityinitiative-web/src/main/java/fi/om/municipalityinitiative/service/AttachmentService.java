package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dao.AttachmentDao;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.util.ImageModifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AttachmentService {

    public static final Integer MAX_WIDTH = 1000;
    public static final Integer MAX_HEIGHT = 500;
    public static final Integer THUMBNAIL_MAX_WIDTH = 100;
    public static final Integer THUMBNAIL_MAX_HEIGHT = 100;

    public static final String[] FILE_TYPES = { "png", "jpg" };
    public static final String[] CONTENT_TYPES = { "image/png", "image/jpg", "image/jpeg" };

    private long id = 0;

    private String attachmentDir;

    @Resource
    AttachmentDao attachmentDao;

    public AttachmentService(String attachmentDir) {
        this.attachmentDir = attachmentDir;
    }

    @Transactional(readOnly = false)
    public void addAttachment(Long initiativeId, LoginUserHolder<User> loginUserHolder, MultipartFile file) throws IOException {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);

        file.getSize(); // TODO: Don't allow too large files

        String fileType = parseFileType(file.getOriginalFilename());
        assertFileType(fileType);
        assertContentType(file.getContentType());

        Long attachmentId = attachmentDao.addAttachment(initiativeId, file.getOriginalFilename());

        File realFile = new File(attachmentDir + "/" + attachmentId + "." + fileType);
        try (FileOutputStream fileOutputStream = new FileOutputStream(realFile, false)) {
            ImageModifier.modify(file.getInputStream(), fileOutputStream, fileType, MAX_WIDTH, MAX_HEIGHT);
            fileOutputStream.write(file.getBytes());
        }
        File thumbnailFile = new File(attachmentDir + "/" + attachmentId + "_thumbnail." + fileType);
        try (FileOutputStream fileOutputStream = new FileOutputStream(thumbnailFile, false)) {
            ImageModifier.modify(file.getInputStream(), fileOutputStream, fileType, THUMBNAIL_MAX_WIDTH, THUMBNAIL_MAX_HEIGHT);
            fileOutputStream.write(file.getBytes());
        }
    }

    public static void assertFileType(String givenFileType) {
        for (String fileType : FILE_TYPES) {
            if (fileType.equals(givenFileType))
                return;
        }
        throw new RuntimeException("Invalid fileName");
    }

    private static String parseFileType(String fileName) {
        String[] split = fileName.split("\\.");
        if (split.length == 1) {
            throw new RuntimeException("Invalid filename");
        }

        return split[split.length-1];
    }

    private static void assertContentType(String contentType) {
        for (String type : CONTENT_TYPES) {
            if (type.equals(contentType))
                return;
        }
        throw new RuntimeException("Invalid content-type:" + contentType);
    }
}
