package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.LikeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;

    @Autowired
    public BoardController(BoardService boardService, LikeService likeService) {
        this.boardService = boardService;
        this.likeService = likeService;
    }

    //게시글 작성
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board, Authentication authentication) {
        try {
            Board savedBoard = boardService.save(board, authentication);
            return ResponseEntity.ok(savedBoard);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //게시글 수정
    @PutMapping("/{boardNo}")
    public ResponseEntity<BoardDTO> updateBoard(@PathVariable int boardNo, @RequestBody BoardDTO updateBoardDTO, Authentication authentication) {
        BoardDTO updatedBoard = boardService.update(boardNo, updateBoardDTO, authentication);
        return ResponseEntity.ok(updatedBoard);
    }


    //게시글 삭제
    @DeleteMapping("/{boardNo}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int boardNo, Authentication authentication) {
        boardService.delete(boardNo, authentication);
        return ResponseEntity.ok().build();
    }

    //해당 게시글 조회
    @GetMapping("/{boardNo}")
    public ResponseEntity<BoardDTO> getBoardById(@PathVariable int boardNo) {
        try {
            BoardDTO boardDTO = boardService.findBoardDTOById(boardNo);
            return ResponseEntity.ok(boardDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }


    //모든 게시글 조회
    @GetMapping
    public ResponseEntity<List<BoardDTO>> getAllBoards() {
        List<BoardDTO> boardDTOs = boardService.findAllBoardDTOs();
        return ResponseEntity.ok(boardDTOs);
    }

    //카테고리별 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BoardDTO>> getBoardsByCategory(@PathVariable int categoryId) {
        List<BoardDTO> boardDTOs = boardService.findByCategory(categoryId)
                .stream()
                .map(board -> boardService.convertToDTO(board)) // `this::convertToDTO` 대신 `boardService::convertToDTO` 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    //좋아요 수
    @GetMapping("/{boardNo}/likes")
    public ResponseEntity<Integer> getLikesCount(@PathVariable int boardNo) {
        int likesCount = likeService.getLikesCount(boardNo);
        return ResponseEntity.ok(likesCount);
    }

    //좋아요순 정렬
    @GetMapping("/likes")
    public ResponseEntity<List<BoardDTO>> getBoardsByLikesDesc() {
        List<BoardDTO> boardDTOs = boardService.findAllByOrderByLikesDesc()
                .stream()
                .map(board -> boardService.convertToDTO(board)) // `this::convertToDTO` 대신 `boardService::convertToDTO` 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    //최신순 정렬
    @GetMapping("/recent")
    public ResponseEntity<List<BoardDTO>> getBoardsByRecent() {
        List<BoardDTO> boardDTOs = boardService.findAllByOrderByBoardDateDesc()
                .stream()
                .map(board -> boardService.convertToDTO(board)) // `this::convertToDTO` 대신 `boardService::convertToDTO` 사용
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    //댓글수순 정렬
    @GetMapping("/comments")
    public ResponseEntity<List<BoardDTO>> getBoardsByCommentsDesc() {
        List<BoardDTO> boardDTOs = boardService.findAllByCommentsDesc()
                .stream()
                .map(boardService::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }

    //게시글 좋아요 토글
    @PostMapping("/{boardNo}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable int boardNo) {
        likeService.toggleLike(boardNo);
        return ResponseEntity.ok().build();
    }

    //게시글 검색
    @GetMapping("/search")
    public ResponseEntity<List<BoardDTO>> searchBoards(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {

        List<BoardDTO> boardDTOs = boardService.searchByCategoryAndKeyword(categoryId, keyword)
                .stream()
                .map(board -> boardService.convertToDTO(board))
                .collect(Collectors.toList());
        return ResponseEntity.ok(boardDTOs);
    }


    // 사용자 토큰으로 게시글 조회
    @GetMapping("/user")
    public List<BoardDTO> getUserBoards(Authentication authentication) {
        return boardService.findBoardsByUser(authentication);
    }
}
