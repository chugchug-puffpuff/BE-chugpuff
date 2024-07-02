package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.CategoryService;
import chugpuff.chugpuff.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/board")
public class BoardController {
    @Autowired
    private BoardService boardService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private CategoryService categoryService;

    //board 객체 받아와 저장
    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardService.save(board);
    }

    //해당 게시글 업데이트
    @PutMapping("/{boardNo}")
    public Board updateBoard(@PathVariable int boardNo, @RequestBody Board board) {
        board.setBoardNo(boardNo);
        return boardService.update(board);
    }

    //해당 게시글 삭제
    @DeleteMapping("/{boardNo}")
    public void deleteBoard(@PathVariable int boardNo) {
        boardService.delete(boardNo);
    }

    //해당 게시글 조회
    @GetMapping("/{boardNo}")
    public Optional<Board> getBoard(@PathVariable int boardNo) {
        return boardService.findById(boardNo);
    }

    //모든 게시글 조회
    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.findAll();
    }

    ///카테고리 별 게시글 조회
    @GetMapping("/category/{categoryId}")
    public List<Board> getBoardsByCategory(@PathVariable int categoryId) {
        return boardService.findByCategory(categoryId);
    }

    //해당하는 게시글의 좋아요 수 조회
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
    public void toggleLike(@PathVariable int boardNo, @RequestParam String userId) {
        likeService.toggleLike(boardNo, userId);
    }
}

