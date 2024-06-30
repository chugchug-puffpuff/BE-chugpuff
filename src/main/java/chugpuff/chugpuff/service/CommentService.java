package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public Comment save(Comment comment) {
        comment.setBcDate(LocalDateTime.now()); // 댓글 작성일 설정
        return commentRepository.save(comment);
    }

    public Comment update(Comment comment) {
        comment.setBcmodifiedDate(LocalDateTime.now()); // 댓글 수정일 설정
        return commentRepository.save(comment);
    }
    public void delete(int bcNo) {
        commentRepository.deleteById(bcNo);
    }

    public Comment findById(int bcNo) {
        return commentRepository.findById(bcNo).orElse(null);
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }
}
