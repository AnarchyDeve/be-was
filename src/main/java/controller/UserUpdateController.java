package controller;

import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpStatus;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserUpdateController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger(UserUpdateController.class);
    private static final String PROFILE_DIR = "./src/main/resources/static/img/profile/";

    @Override
    public String process(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(HttpStatus.FOUND, "/login/index.html");
            return null;
        }

        // 1. 사용자 입력값 가져오기
        String newName = request.getParameter("name");
        String newPassword = request.getParameter("password");
        String passwordConfirm = request.getParameter("passwordConfirm");
        byte[] fileData = request.getFileData("profileImage");
        String originalFileName = request.getFileName("profileImage");

        // 2. 비밀번호 변경 여부 결정
        String finalPassword = user.getPassword(); // 기본은 기존 비번 유지
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.equals(passwordConfirm)) {
                finalPassword = newPassword;
            } else {
                logger.warn("비밀번호 불일치: userId={}", user.getUserId());
                return "redirect:/mypage?error=password_mismatch";
            }
        }

        // 3. 프로필 이미지 저장 경로 결정
        String currentProfilePath = user.getProfileImage();
        if (fileData != null && fileData.length > 0) {
            String extension = ".png";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String saveFileName = "profile_" + user.getUserId() + extension;

            File uploadDir = new File(PROFILE_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            try (FileOutputStream fos = new FileOutputStream(new File(uploadDir, saveFileName))) {
                fos.write(fileData);
            }
            currentProfilePath = "/img/profile/" + saveFileName;
        }

        // 4. [가장 중요] DB 자체를 업데이트합니다.
        // UserRepository의 SQL UPDATE 문이 실행되는 시점입니다.
        UserRepository.updateUserProfile(user.getUserId(), newName, finalPassword, currentProfilePath);

        // 5. DB에서 바뀐 정보를 다시 조회해서 세션에 넣습니다.
        // 이걸 안 하면 DB는 바뀌어도 서버 메모리(세션) 속 유저는 여전히 옛날 유저입니다.
        User updatedUser = UserRepository.findUserById(user.getUserId());
        session.setAttribute("user", updatedUser);

        logger.info("DB 업데이트 및 세션 갱신 완료: userId={}", user.getUserId());

        // 6. 메인 페이지로 이동 (메인 헤더의 이름/사진이 즉시 바뀜)
        return "redirect:/index.html";
    }
}