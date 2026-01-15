package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    //  실제 저장될 물리 경로
    private static final String SAVE_PATH = "src/main/resources/static/img/article/";

    public static String saveArticleImage(Long articleId, byte[] fileData, String originalFileName) throws IOException {
        if (fileData == null || fileData.length == 0) return null;

        // 확장자 추출 (예: .jpg, .png)
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) extension = originalFileName.substring(i);

        //  파일명을 게시물 ID 기반으로 생성 (예: article_15.jpg)
        String fileName = "article_" + articleId + extension;

        File directory = new File(SAVE_PATH);
        if (!directory.exists()) directory.mkdirs();

        File file = new File(SAVE_PATH + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileData);
        }

        //  브라우저가 접근할 웹 경로 반환
        return "/img/article/" + fileName;
    }
}