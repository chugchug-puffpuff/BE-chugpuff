package chugpuff.chugpuff.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class BoardDTO {
    private int boardNo;
    private String boardTitle;
    private String boardContent;
    private String memberName;
    private LocalDateTime boardDate;
    private LocalDateTime boardModifiedDate;
    private int likes;
    private int commentCount;
    private List<String> commentContents;
    private CategoryDTO category;

    // 기본 생성자
    public BoardDTO() {
        this.boardDate = LocalDateTime.now();
        this.boardModifiedDate = LocalDateTime.now();
    }

    // 모든 필드를 포함하는 생성자
    public BoardDTO(int boardNo, String boardTitle, String boardContent, String memberName, LocalDateTime boardDate, LocalDateTime boardModifiedDate, int likes, int commentCount, List<String> commentContents, CategoryDTO category) {
        this.boardNo = boardNo;
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.memberName = memberName;
        this.boardDate = boardDate;
        this.boardModifiedDate = boardModifiedDate;
        this.likes = likes;
        this.commentCount = commentCount;
        this.commentContents = commentContents;
        this.category = category;
    }


    // 각 필드에 대한 getter와 setter
    public int getBoardNo() {
        return boardNo;
    }

    public void setBoardNo(int boardNo) {
        this.boardNo = boardNo;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public void setBoardTitle(String boardTitle) {
        this.boardTitle = boardTitle;
    }

    public String getBoardContent() {
        return boardContent;
    }

    public void setBoardContent(String boardContent) {
        this.boardContent = boardContent;
    }

    public LocalDateTime getBoardDate() {
        return boardDate;
    }

    public void setBoardDate(LocalDateTime boardDate) {
        this.boardDate = boardDate;
    }

    public LocalDateTime getBoardModifiedDate() {
        return boardModifiedDate;
    }

    public void setBoardModifiedDate(LocalDateTime boardModifiedDate) {
        this.boardModifiedDate = boardModifiedDate;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<String> getCommentContents() {
        return commentContents;
    }

    public void setCommentContents(List<String> commentContents) {
        this.commentContents = commentContents;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }
}
