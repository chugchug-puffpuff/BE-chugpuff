package chugpuff.chugpuff.service;


import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.CommentDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.CommentRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BoardRepository boardRepository;


    //댓글 저장
    @Transactional
    public CommentDTO save(CommentDTO commentDTO, int boardNo, Authentication authentication) {
        Comment comment = new Comment();
        comment.setBcContent(commentDTO.getBcContent());
        comment.setBcDate(LocalDateTime.now());

        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        comment.setMember(member);

        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        comment.setBoard(board);

        commentRepository.save(comment);

        return convertToDTO(comment);
    }

    //댓글 수정
    @Transactional
    public CommentDTO update(int commentId, Comment updatedComment, Authentication authentication) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 사용자 인증 확인
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 댓글 작성자와 인증된 사용자가 일치하는지 확인
        if (!existingComment.getMember().equals(member)) {
            throw new SecurityException("댓글 작성자만 수정할 수 있습니다.");
        }

        existingComment.setBcContent(updatedComment.getBcContent());
        existingComment.setBcModifiedDate(LocalDateTime.now());

        commentRepository.save(existingComment);

        return convertToDTO(existingComment);
    }

    //댓글삭제
    @Transactional
    public void delete(int commentId, Authentication authentication) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        // 사용자 인증 확인
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 댓글 작성자와 인증된 사용자가 일치하는지 확인
        if (!comment.getMember().equals(member)) {
            throw new SecurityException("댓글 작성자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }


    // 사용자별 댓글 조회
    public List<CommentDTO> findCommentsByUser(Authentication authentication) {
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        return commentRepository.findByMember_Id(member.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 특정 게시글에 달린 댓글 조회
    public List<CommentDTO> findCommentsByBoard(int boardNo) {
        List<Comment> comments = commentRepository.findByBoard_BoardNo(boardNo);
        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // DTO 변환
    public CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setBcNo(comment.getBcNo());
        dto.setBcContent(comment.getBcContent());
        dto.setBcDate(comment.getBcDate());
        dto.setBcModifiedDate(comment.getBcModifiedDate());
        dto.setMemberName(comment.getMember().getName());
        return dto;
    }

}