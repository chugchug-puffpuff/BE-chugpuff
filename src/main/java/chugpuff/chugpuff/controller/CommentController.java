package chugpuff.chugpuff.controller;


import chugpuff.chugpuff.dto.CommentDTO;
import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    //댓글 생성
    @PostMapping
    public ResponseEntity<CommentDTO> saveComment(@RequestBody CommentDTO commentDTO, @RequestParam int boardNo, Authentication authentication) {
        CommentDTO savedComment = commentService.save(commentDTO, boardNo, authentication);
        return ResponseEntity.ok(savedComment);
    }

    //댓글 수정
    @PutMapping("/{bcNo}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable int bcNo, @RequestBody Comment comment, Authentication authentication) {
        CommentDTO updatedComment = commentService.update(bcNo, comment, authentication);
        return ResponseEntity.ok(updatedComment);
    }

    //댓글 삭제
    @DeleteMapping("/{bcNo}")
    public ResponseEntity<Void> deleteComment(@PathVariable int bcNo, Authentication authentication) {
        commentService.delete(bcNo, authentication);
        return ResponseEntity.noContent().build();
    }


    // 사용자 토큰으로 댓글 조회
    @GetMapping("/user")
    public List<CommentDTO> getUserComments(Authentication authentication) {
        return commentService.findCommentsByUser(authentication);
    }

    // 특정 게시글에 달린 댓글 조회
    @GetMapping("/board/{boardNo}")
    public ResponseEntity<List<CommentDTO>> getCommentsByBoard(@PathVariable int boardNo) {
        List<CommentDTO> comments = commentService.findCommentsByBoard(boardNo);
        return ResponseEntity.ok(comments);
    }
}
