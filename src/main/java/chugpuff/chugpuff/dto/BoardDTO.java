package chugpuff.chugpuff.dto;

public class BoardDTO {
    private int boardNo;
    private String boardTitle;
    private String boardContent;
    private String memberName;
    private String categoryName;

    // Constructors, Getters, and Setters

    public BoardDTO() {}

    public BoardDTO(int boardNo, String boardTitle, String boardContent, String memberName, String categoryName) {
        this.boardNo = boardNo;
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.memberName = memberName;
        this.categoryName = categoryName;
    }

    // Getters and Setters

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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
