package model;

public class Comment {
    private String writerName;    // 작성자 이름
    private String contents;      // 댓글 내용
    // private String userId;    // (선택) ID로 관리하고 싶을 때

    public Comment(String writerName, String contents) {
        this.writerName = writerName;
        this.contents = contents;
    }

    public String getWriterName() { return writerName; }
    public String getContents() { return contents; }
}