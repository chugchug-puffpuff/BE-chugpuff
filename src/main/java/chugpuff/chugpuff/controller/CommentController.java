package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    //댓글 생성
    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.save(comment);
    }

    //댓글 수정
    @PutMapping("/{bcNo}")
    public Comment updateComment(@PathVariable int bcNo, @RequestBody Comment comment) {
        comment.setBcNo(bcNo);
        return commentService.update(comment);
    }
    //댓글 삭제
    @DeleteMapping("/{bcNo}")
    public void deleteComment(@PathVariable int bcNo) {
        commentService.delete(bcNo);
    }

    //ID로 댓글 조회
    @GetMapping("/{bcNo}")
    public Comment getComment(@PathVariable int bcNo) {
        return commentService.findById(bcNo);
    }

    //모든 댓글 조회
    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.findAll();
    }
}
