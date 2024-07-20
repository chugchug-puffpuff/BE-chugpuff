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

    /**
     * 댓글 저장
     * 작성일을 현재 시간으로 설정하고, 데이터베이스에 저장
     *
     * @param comment 저장할 댓글 엔티티
     * @return 저장된 댓글 엔티티
     */
    public Comment save(Comment comment) {
        comment.setBcDate(LocalDateTime.now()); // 댓글 작성일 설정
        return commentRepository.save(comment);
    }

    /**
     * 댓글 업데이트
     * 수정일을 현재 시간으로 설정하고, 데이터베이스에 저장
     *
     * @param comment 업데이트할 댓글 엔티티
     * @return 업데이트된 댓글 엔티티
     */
    public Comment update(Comment comment) {
        comment.setBcmodifiedDate(LocalDateTime.now()); // 댓글 수정일 설정
        return commentRepository.save(comment);
    }

    /**
     * 댓글 삭제
     *
     * @param bcNo 삭제할 댓글의 ID
     */
    public void delete(int bcNo) {
        commentRepository.deleteById(bcNo);
    }

    /**
     * ID로 댓글 조회
     * 해당 ID의 댓글이 없으면 null 반환
     *
     * @param bcNo 조회할 댓글의 ID
     * @return 조회된 댓글 엔티티 또는 null
     */
    public Comment findById(int bcNo) {
        return commentRepository.findById(bcNo).orElse(null);
    }

    /**
     * 모든 댓글 조회
     *
     * @return 모든 댓글 목록
     */
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }
}