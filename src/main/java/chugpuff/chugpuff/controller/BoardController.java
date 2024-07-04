package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Board;
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

    @PostMapping("/create")
    public Board createBoard(@RequestBody Board board) {
        return boardService.save(board);
    }

    @PutMapping("/update/{boardNo}")
    public Board updateBoard(@PathVariable int boardNo, @RequestBody Board board) {
        board.setBoardNo(boardNo);
        return boardService.update(board);
    }

    @DeleteMapping("/delete/{boardNo}")
    public void deleteBoard(@PathVariable int boardNo) {
        boardService.delete(boardNo);
    }

    @GetMapping("/get/{boardNo}")
    public Optional<Board> getBoard(@PathVariable int boardNo) {
        return boardService.findById(boardNo);
    }

    @GetMapping("/getAll")
    public List<Board> getAllBoards() {
        return boardService.findAll();
    }

    @GetMapping("/category/{categoryId}")
    public List<Board> getBoardsByCategory(@PathVariable int categoryId) {
        return boardService.findByCategory(categoryId);
    }

    @GetMapping("/{boardNo}/getLikesCount")
    public int getLikesCount(@PathVariable int boardNo) {
        return boardService.getLikesCount(boardNo);
    }

    @GetMapping("/getBoardsByLikesDesc")
    public List<Board> getBoardsByLikesDesc() {
        return boardService.findAllByOrderByLikesDesc();
    }

    @GetMapping("/getBoardsByRecent")
    public List<Board> getBoardsByRecent() {
        return boardService.findAllByOrderByBoardDateDesc();
    }

    @GetMapping("/getBoardsByCommentsDesc")
    public List<Board> getBoardsByCommentsDesc() {
        return boardService.findAllByCommentsDesc();
    }

    @PostMapping("/{boardNo}/like")
    public void toggleLike(@PathVariable int boardNo, @RequestParam String userId) {
        likeService.toggleLike(boardNo, userId);
    }

    @GetMapping("/search")
    public List<Board> searchBoards(@RequestParam String keyword) {
        return boardService.searchByKeyword(keyword);
    }
}

