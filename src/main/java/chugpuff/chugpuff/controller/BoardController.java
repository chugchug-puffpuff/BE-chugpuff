package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.entity.Board;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;
    private final CategoryService categoryService;
    private final MemberService memberService;

    @Autowired
    public BoardController(BoardService boardService, LikeService likeService, CategoryService categoryService, MemberService memberService) {
        this.boardService = boardService;
        this.likeService = likeService;
        this.categoryService = categoryService;
        this.memberService = memberService;
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board, @RequestParam("userId") Long userId, @RequestParam("categoryId") int categoryId) {
        try {
            Board savedBoard = boardService.save(board, userId, categoryId);
            BoardDTO boardDTO = boardService.convertToDTO(savedBoard);
            return ResponseEntity.ok(savedBoard);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 게시글 수정
    @PutMapping("/{boardNo}")
    public ResponseEntity<Board> updateBoard(@PathVariable int boardNo, @RequestParam Long userId, @RequestBody Board board) {
        board.setBoardNo(boardNo);
        Board updatedBoard = boardService.update(board, userId);
        BoardDTO boardDTO = boardService.convertToDTO(updatedBoard);
        return ResponseEntity.ok(updatedBoard);
    }

    // 게시글 삭제
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int boardNo, @RequestParam Long userId) {
        boardService.delete(boardNo, userId);
        return ResponseEntity.ok().build();
    }

    // 해당 게시글 조회
    @GetMapping("/{boardNo}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable int boardNo) {
        try {
            BoardDTO boardDTO = boardService.findBoardDTOById(boardNo);
            return ResponseEntity.ok(boardDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<BoardDTO>> getAllBoards() {
        List<BoardDTO> boardDTOs = boardService.findAllBoardDTOs();
        return ResponseEntity.ok(boardDTOs);
    }

    // 카테고리 별 게시글 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BoardDTO>> getBoardsByCategory(@PathVariable int categoryId) {
        List<BoardDTO> boardDTOs = boardService.findByCategory(categoryId)
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    // 해당 게시글의 좋아요 수 조회
    @GetMapping("/{boardNo}/likes")
    public ResponseEntity<Integer> getLikesCount(@PathVariable int boardNo) {
        int likesCount = likeService.getLikesCount(boardNo);
        return ResponseEntity.ok(likesCount);
    }

    // 좋아요 수 기준 -> 게시글 내림차순 조회
    @GetMapping("/likes")
    public ResponseEntity<List<BoardDTO>> getBoardsByLikesDesc() {
        List<BoardDTO> boardDTOs = boardService.findAllByOrderByLikesDesc()
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    // 최근 게시글 조회 (최신순)
    @GetMapping("/recent")
    public ResponseEntity<List<BoardDTO>> getBoardsByRecent() {
        List<BoardDTO> boardDTOs = boardService.findAllByOrderByBoardDateDesc()
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    // 댓글 수 기준 -> 게시글 내림차순 조회
    @GetMapping("/comments")
    public ResponseEntity<List<BoardDTO>> getBoardsByCommentsDesc() {
        List<BoardDTO> boardDTOs = boardService.findAllByCommentsDesc()
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    // 해당 게시글 <- 사용자 좋아요 토글
    @PostMapping("/{boardNo}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable int boardNo, @RequestParam Long userId) {
        likeService.toggleLike(boardNo, userId);
        return ResponseEntity.ok().build();
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<List<BoardDTO>> searchBoards(@RequestParam("keyword") String keyword) {
        List<BoardDTO> boardDTOs = boardService.searchByKeyword(keyword)
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }
}
