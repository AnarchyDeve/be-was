package model;

public class User {
    private String userId;
    private String password;
    private String name;
    private String email;
    private String profileImage; // 프로필 이미지 웹 경로

    public User(String userId, String password, String name, String email, String profileImage) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;

        //  경로 처리 로직 개선
        if (profileImage == null || profileImage.isEmpty()) {
            // 기본 이미지 설정 (이미 전체 경로임)
            this.profileImage = "/img/profile/basic_profileImage.svg";
        } else if (profileImage.startsWith("/img/")) {
            // 이미 경로가 포함된 경우 그대로 저장
            this.profileImage = profileImage;
        } else {
            // 파일명만 들어온 경우 경로를 붙여서 저장
            this.profileImage = "/img/profile/" + profileImage;
        }
    }

    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    //  이제 필드 자체가 완성된 경로를 들고 있으므로 그대로 반환합니다.
    public String getProfileImage() {
        return profileImage;
    }

    //  중복 방지: 이미 profileImage에 경로가 포함되어 있으므로 추가로 붙이지 않습니다.
    public String getProfileImagePath() {
        return profileImage;
    }

    @Override
    public String toString() {
        return "User [userId=" + userId + ", name=" + name + ", profileImage=" + profileImage + "]";
    }
}