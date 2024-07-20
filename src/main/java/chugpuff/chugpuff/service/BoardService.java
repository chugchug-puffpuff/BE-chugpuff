package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.CategoryRepository;
import chugpuff.chugpuff.repository.LikeRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikeService likeService;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, LikeService likeService, CategoryRepository categoryRepository, MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.likeService = likeService;
        this.categoryRepository = categoryRepository;
        this.memberRepository = memberRepository;
    }
    /**
     * 게시글 저장 메서드
     * @param board 저장할 게시글 엔티티
     * @return 저장된 게시글 엔티티
     */
    @Transactional
    public Board save(Board board) {
        // 카테고리 아이디로 카테고리 이름을 찾아서 설정
        Category category = categoryRepository.findById(board.getCategory().getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + board.getCategory().getCategoryId()));
        board.setCategory(category);


        // Member가 존재하는지 확인
        Member member = memberRepository.findById(board.getMember().getUser_id())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + board.getMember().getUser_id()));
        board.setMember(member);
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
    @Transactional
    public Board update(Board board, Long userId) {
        if (!board.getMember().getUser_id().equals(userId)) {
            throw new IllegalArgumentException("작성자만 게시글을 수정할 수 있습니다.");
        }
        board.setBoardmodifiedDate(LocalDateTime.now());
        Board updatedBoard = boardRepository.save(board);
        return boardRepository.save(board);
    }

    /**
     * 게시글 삭제 메서드
     * @param boardNo 삭제할 게시글 번호
     */
    @Transactional
    public void delete(int boardNo, Long userId) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        if (!board.getMember().getUser_id().equals(userId)) {
            throw new IllegalArgumentException("작성자만 게시글을 삭제할 수 있습니다.");
        }
        // 먼저 좋아요 데이터를 삭제합니다.
        likeService.deleteLikesByBoardNo(boardNo);
        boardRepository.delete(board);
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

    // 제목이나 내용에 키워드가 포함된 게시글 찾기
    public List<Board> searchByKeyword(String keyword) {
        return boardRepository.findByBoardTitleContainingOrBoardContentContaining(keyword, keyword);
    }

    // DTO 변환 메서드
    public BoardDTO convertToDTO(Board board) {
        return new BoardDTO(
                board.getBoardNo(),
                board.getBoardTitle(),
                board.getBoardContent(),
                board.getMember().getName(),
                board.getCategory().getCategoryName()
        );
    }

    // 단일 게시글 DTO 조회
    public BoardDTO findBoardDTOById(int boardNo) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));
        return convertToDTO(board);
    }

    // 모든 게시글 DTO 조회
    public List<BoardDTO> findAllBoardDTOs() {
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
