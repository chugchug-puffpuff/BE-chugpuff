package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.MemberRepository;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.CategoryService;
import chugpuff.chugpuff.service.LikeService;
import chugpuff.chugpuff.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final MemberRepository memberRepository;

    @Autowired
    public BoardController(BoardService boardService, MemberRepository memberRepository) {
        this.boardService = boardService;
        this.memberRepository = memberRepository;
    }

    @Autowired
    private LikeService likeService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MemberService memberService;

    //board 객체 받아와 저장
    //게시글 작성
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board, @RequestParam("userId") Long userId) {
        try {
            Optional<Member> optionalMember = memberRepository.findById(userId);
            if (optionalMember.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Member member = optionalMember.get();
            board.setMember(member); // 게시글에 작성자 설정
            Board savedBoard = boardService.save(board); // 게시글 저장
            return ResponseEntity.ok(savedBoard);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    //게시글 수정
    @PutMapping("/{boardNo}")
    public Board updateBoard(@PathVariable("boardNo") int boardNo, @RequestBody Board board) {
        board.setBoardNo(boardNo);
        return boardService.update(board);
    }

    //게시글 삭제
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("boardNo") int boardNo) {
        boardService.delete(boardNo);
        return ResponseEntity.noContent().build();
    }
    //해당 게시글 조회
    @GetMapping("/{boardNo}")
    public Optional<Board> getBoard(@PathVariable("boardNo") int boardNo) {
        return boardService.findById(boardNo);
    }

    //모든 게시글 조회
    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.findAll();
    }

    //카테고리 별 게시글 조회
    @GetMapping("/category/{categoryId}")
    public List<Board> getBoardsByCategory(@PathVariable int categoryId) {
        return boardService.findByCategory(categoryId);
    }

    //해당 게시글의 좋아요 수 조회
    @GetMapping("/{boardNo}/likes")
    public int getLikesCount(@PathVariable int boardNo) {
        return boardService.getLikesCount(boardNo);
    }

    //좋아요 수 기준 -> 게시글 내림차순 조회
    @GetMapping("/likes")
    public List<Board> getBoardsByLikesDesc() {
        return boardService.findAllByOrderByLikesDesc();
    }

    //최근 게시글 조회 (최신순)
    @GetMapping("/recent")
    public List<Board> getBoardsByRecent() {
        return boardService.findAllByOrderByBoardDateDesc();
    }

    //댓글 수 기준 -> 게시글 내림차순 조회
    @GetMapping("/comments")
    public List<Board> getBoardsByCommentsDesc() {
        return boardService.findAllByCommentsDesc();
    }

    //해당 게시글 <- 사용자 좋아요 토글
    @PostMapping("/{boardNo}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable int boardNo, @RequestParam Long userId) {
        try {
            Member member = new Member();
            member.setUser_id(userId);
            likeService.toggleLike(boardNo, member);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //게시글 검색
    @GetMapping("/search")
    public List<Board> searchBoards(@RequestParam("keyword") String keyword) {
        return boardService.searchByKeyword(keyword);
    }
}

