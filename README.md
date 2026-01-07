# be-was-2025
코드스쿼드 백엔드 교육용 WAS 2025 개정판

## 전체 프로젝트 파일 구조 (Project Structure)

```text

src/main/java/
├── webserver/                  # [엔진: 프론트 컨트롤러]
│   ├── WebServer.java          
│   └── DispatcherServlet.java  # ★ [수정] run() 메서드 시작 시 세션 청소 호출
├── http/                       # [데이터: 부품]
│   ├── HttpRequest.java        
│   ├── HttpResponse.java       
│   ├── HttpSession.java        # [추가] 세션 표준 규격 인터페이스
│   ├── HttpSessionImpl.java    # [추가] 세션 데이터 저장소 구현체
│   ├── HttpSessions.java       # [추가] 세션 Map 관리 및 만료 로직 전담
│   ├── HttpRequestUtils.java   
│   ├── HttpStatus.java         
│   └── MimeType.java           
├── controller/                 # [핸들러: 비즈니스 로직]
│   ├── Controller.java         
│   ├── UserCreateController.java 
│   ├── ResourceController.java   
│   └── HandlerMapping.java     
├── adapter/                    # [연결잭]
│   ├── HandlerAdapter.java     
│   └── ControllerHandlerAdapter.java 
├── view/                       # [응답 처리: 뷰]
│   ├── MyView.java             
│   └── ViewResolver.java       
├── db/                         
│   └── Database.java           
└── model/                      
    └── User.java

