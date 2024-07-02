package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.service.BoardService;
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

    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardService.save(board);
    }

    @PutMapping("/{boardNo}")
    public Board updateBoard(@PathVariable int boardNo, @RequestBody Board board) {
        board.setBoardNo(boardNo);
        return boardService.update(board);
    }

    @DeleteMapping("/{boardNo}")
    public void deleteBoard(@PathVariable int boardNo) {
        boardService.delete(boardNo);
    }

    @GetMapping("/{boardNo}")
    public Optional<Board> getBoard(@PathVariable int boardNo) {
        return boardService.findById(boardNo);
    }

    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.findAll();
    }

    @GetMapping("/category/{categoryId}")
    public List<Board> getBoardsByCategory(@PathVariable int categoryId) {
        return boardService.findByCategory(categoryId);
    }

    @GetMapping("/{boardNo}/likes")
    public int getLikesCount(@PathVariable int boardNo) {
        return boardService.getLikesCount(boardNo);
    }

    @GetMapping("/likes")
    public List<Board> getBoardsByLikesDesc() {
        return boardService.findAllByLikesDesc();
    }

    @GetMapping("/recent")
    public List<Board> getBoardsByRecent() {
        return boardService.findAllByBoardDateDesc();
    }

    @GetMapping("/comments")
    public List<Board> getBoardsByCommentsDesc() {
        return boardService.findAllByCommentsDesc();
    }

    @PostMapping("/{boardNo}/like")
    public void toggleLike(@PathVariable int boardNo, @RequestParam String userId) {
        likeService.toggleLike(boardNo, userId);
    }
}

