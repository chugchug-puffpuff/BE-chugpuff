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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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



    /**
     * 주어진 게시글 번호에 대한 좋아요 수 반환
     *
     * @param boardNo 게시글 번호
     * @return 좋아요 수
     */
    public int getLikesCount(int boardNo) {
        return likeRepository.countByBoard_BoardNo(boardNo);
    }

    @Transactional
    public void toggleLike(int boardNo, Long userId) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(userId)
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

