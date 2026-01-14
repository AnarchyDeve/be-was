package controller;

import db.UserRepository;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateControllerTest {
    private UserCreateController controller;

    @BeforeEach
    void setUp() {
        controller = new UserCreateController();
    }

    @Test
    @DisplayName("GET 방식으로 회원가입 요청을 보내면 데이터베이스에 유저가 저장되어야 한다.")
    void signUpTest() throws Exception {
        String rawRequest = "GET /user/create?userId=joy&password=1234&name=Hyeon&email=joy@test.com HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";
        InputStream in = new ByteArrayInputStream(rawRequest.getBytes());

        HttpRequest request = new HttpRequest(in);
        HttpResponse response = new HttpResponse(new ByteArrayOutputStream());

        controller.process(request, response);

        User savedUser = UserRepository.findUserById("joy");

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isEqualTo("joy");
        assertThat(savedUser.getName()).isEqualTo("Hyeon");
//        assertThat(savedUser.getName()).isEqualTo("kkkk"); 실패 케이스를 작성해서 한번 해보기
    }

    @Test
    @DisplayName("여러 명의 유저를 저장하면 DB에 모두 저장되어야 한다")
    void saveMultipleUsersTest() throws Exception {
        // 1. Given: 두 명의 유저 요청 생성
        String request1 = "GET /user/create?userId=user1&password=1234&name=Name1&email=u1@test.com HTTP/1.1\r\n\r\n";
        String request2 = "GET /user/create?userId=user2&password=5678&name=Name2&email=u2@test.com HTTP/1.1\r\n\r\n";

        // 첫 번째 유저 실행
        controller.process(new HttpRequest(new ByteArrayInputStream(request1.getBytes())), new HttpResponse(new ByteArrayOutputStream()));
        // 두 번째 유저 실행
        controller.process(new HttpRequest(new ByteArrayInputStream(request2.getBytes())), new HttpResponse(new ByteArrayOutputStream()));

        // 2. When: DB에서 전체 유저 목록 가져오기 (Database.findAll()이 있다고 가정)
        Collection<User> allUsers = UserRepository.findAll();

        // 3. Then: AssertJ의 강력한 기능 활용

        assertThat(allUsers).hasSize(2); // 전체 개수가 2개인지 확인

        // 특정 아이디를 가진 유저들이 포함되어 있는지 한 번에 확인
        assertThat(allUsers)
                .extracting(User::getUserId) // 유저 객체에서 ID만 뽑아내서
                .containsExactlyInAnyOrder("user1", "user2"); // 순서 상관없이 이 두 ID가 있는지 확인
    }
}
