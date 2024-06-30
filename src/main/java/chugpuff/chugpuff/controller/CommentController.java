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

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.save(comment);
    }

    @PutMapping("/{bcNo}")
    public Comment updateComment(@PathVariable int bcNo, @RequestBody Comment comment) {
        comment.setBcNo(bcNo);
        return commentService.update(comment);
    }

    @DeleteMapping("/{bcNo}")
    public void deleteComment(@PathVariable int bcNo) {
        commentService.delete(bcNo);
    }

    @GetMapping("/{bcNo}")
    public Comment getComment(@PathVariable int bcNo) {
        return commentService.findById(bcNo);
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.findAll();
    }
}
