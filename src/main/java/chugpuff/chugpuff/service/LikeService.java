package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.LikeRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private BoardRepository boardRepository;

    private MemberRepository memberRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, BoardRepository boardRepository, MemberRepository memberRepository) {
        this.likeRepository = likeRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    public int getLikesCount(int boardNo) {
        return likeRepository.countByBoard_BoardNo(boardNo);
    }

    @Transactional
    public void toggleLike(int boardNo) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 토큰에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Optional<Like> likeOptional = likeRepository.findByBoardAndMember(board, member);
        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            board.setLikes(board.getLikes() - 1);
        } else {
            Like like = new Like(board, member);
            likeRepository.save(like);
            board.setLikes(board.getLikes() + 1);
        }
        boardRepository.save(board);
    }

    @Transactional
    public void deleteLikesByBoardNo(int boardNo) {
        likeRepository.deleteByBoard_BoardNo(boardNo);
    }
}

