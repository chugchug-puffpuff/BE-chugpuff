package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository; // Board 엔티티의 데이터 액세스를 위한 Repository

    @Autowired
    private LikeRepository likeRepository; // Like 엔티티의 데이터 액세스를 위한 Repository

    @Autowired
    private CategoryService categoryService; // 카테고리 관련 비즈니스 로직을 처리하는 서비스 클래스

    @Autowired
    private LikeService likeService; // 좋아요 관련 비즈니스 로직을 처리하는 서비스 클래스

    /**
     * 게시글 저장 메서드
     * @param board 저장할 게시글 엔티티
     * @return 저장된 게시글 엔티티
     */
    public Board save(Board board) {
        // 게시글에 설정된 카테고리 이름으로 실제 카테고리 엔티티를 조회하여 설정
        Category category = categoryService.findCategoryByName(board.getCategoryName());
        board.setCategory(category);

        // 게시글 작성일 설정
        board.setBoardDate(LocalDateTime.now());

        // 게시글 저장 후 반환
        return boardRepository.save(board);
    }

    /**
     * 게시글 수정 메서드
     * @param board 수정할 게시글 엔티티
     * @return 수정된 게시글 엔티티
     */
    public Board update(Board board) {
        // 게시글 수정일 설정
        board.setBoardmodifiedDate(LocalDateTime.now());

        // 수정된 게시글 저장 후 반환
        return boardRepository.save(board);
    }

    /**
     * 게시글 삭제 메서드
     * @param boardNo 삭제할 게시글 번호
     */
    public void delete(int boardNo) {
        boardRepository.deleteById(boardNo);
    }

    /**
     * 게시글 번호로 게시글 조회 메서드
     * @param boardNo 조회할 게시글 번호
     * @return Optional 형태의 게시글 엔티티
     */
    public Optional<Board> findById(int boardNo) {
        return boardRepository.findById(boardNo);
    }

    /**
     * 모든 게시글 조회 메서드
     * @return 모든 게시글 엔티티 리스트
     */
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    /**
     * 카테고리별 게시글 조회 메서드
     * @param categoryId 조회할 카테고리 ID
     * @return 해당 카테고리에 속하는 모든 게시글 엔티티 리스트
     */
    public List<Board> findByCategory(int categoryId) {
        return boardRepository.findByCategory_CategoryId(categoryId);
    }

    /**
     * 좋아요 수로 내림차순 정렬된 게시글 조회 메서드
     * @return 좋아요 수로 정렬된 모든 게시글 엔티티 리스트
     */
    public List<Board> findAllByOrderByLikesDesc() {
        return boardRepository.findAllByOrderByLikesDesc();
    }

    /**
     * 게시글 작성일로 내림차순 정렬된 게시글 조회 메서드
     * @return 게시글 작성일로 정렬된 모든 게시글 엔티티 리스트
     */
    public List<Board> findAllByOrderByBoardDateDesc() {
        return boardRepository.findAllByOrderByBoardDateDesc();
    }

    /**
     * 댓글 수로 내림차순 정렬된 게시글 조회 메서드
     * @return 댓글 수로 정렬된 모든 게시글 엔티티 리스트
     */
    public List<Board> findAllByCommentsDesc() {
        return boardRepository.findAllByCommentsCountDesc();
    }

    /**
     * 게시글 좋아요 수 조회 메서드
     * @param boardNo 조회할 게시글 번호
     * @return 해당 게시글의 좋아요 수
     */
    @GetMapping("/{boardNo}/likes")
    public int getLikesCount(@PathVariable int boardNo) {
        return likeService.getLikesCount(boardNo);
    }

    /**
     * 게시글 좋아요 토글 메서드
     * @param boardNo 좋아요를 토글할 게시글 번호
     * @param userId 좋아요를 토글할 유저 ID
     */
    @PostMapping("/{boardNo}/like")
    public void toggleLike(@PathVariable int boardNo, @RequestParam String userId) {
        likeService.toggleLike(boardNo, userId);
    }
}
