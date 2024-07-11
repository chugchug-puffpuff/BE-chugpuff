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
    private BoardService boardService; // Avoid direct circular reference if possible

    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }
    @Autowired
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;


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
    public void toggleLike(int boardNo, Member member) {
        Board board = boardService.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("Board not found with id: " + boardNo));

        Optional<Like> likeOptional = likeRepository.findByBoardAndMember(board, member);
        if (likeOptional.isPresent()) {
            likeRepository.delete(likeOptional.get());
            board.setLikes(board.getLikes() - 1); // 좋아요 수 감소
        } else {
            Like like = new Like();
            like.setBoard(board);
            like.setMember(member);
            likeRepository.save(like);
            board.setLikes(board.getLikes() + 1); // 좋아요 수 증가
        }
        boardService.update(board); // Board 엔티티 업데이트
    }
}

