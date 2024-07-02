package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.entity.User;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.LikeRepository;
import chugpuff.chugpuff.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 주어진 게시글 번호에 대한 좋아요 수 반환
     *
     * @param boardNo 게시글 번호
     * @return 좋아요 수
     */
    public int getLikesCount(int boardNo) {
        return likeRepository.countByBoard_BoardNo(boardNo);
    }

    /**
     * 주어진 게시글 번호와 사용자 ID로 좋아요 추가
     *
     * @param boardNo 게시글 번호
     * @param userId 사용자 ID
     * @return 생성된 좋아요 객체, 또는 게시글을 찾을 수 없는 경우 null
     */
    public Like addLike(int boardNo, String userId) {
        Optional<Board> board = boardRepository.findById(boardNo);
        Optional<User> user = userRepository.findById(userId);
        if (board.isPresent()) {
            Like newLike = new Like();
            newLike.setBoard(board.get());
            newLike.setUser(user.get());
            return likeRepository.save(newLike);
        }
        return null;
    }

    /**
     * 주어진 게시글 번호와 사용자 ID로 좋아요 제거
     *
     * @param boardNo 게시글 번호
     * @param userId 사용자 ID
     */
    public void removeLike(int boardNo, String userId) {
        Optional<Board> board = boardRepository.findById(boardNo);
        Optional<User> user = userRepository.findById(userId);
        if (board.isPresent() && user.isPresent()) {
            Optional<Like> likeOptional = likeRepository.findByBoardAndUserId(board.get(), userId);
            if (likeOptional.isPresent()) {
                likeRepository.delete(likeOptional.get());
            }
        }
    }

    /**
     * 주어진 게시글 번호와 사용자 ID로 좋아요 상태 토글
     * 만약 좋아요가 이미 존재하면 삭제하고, 그렇지 않으면 추가
     *
     * @param boardNo 게시글 번호
     * @param userId 사용자 ID
     */
    public void toggleLike(int boardNo, String userId) {
        Optional<Board> board = boardRepository.findById(boardNo);
        Optional<User> user = userRepository.findById(userId);
        if (board.isPresent() && user.isPresent()) {
            Optional<Like> likeOptional = likeRepository.findByBoardAndUserId(board.get(), userId);
            if (likeOptional.isPresent()) {
                likeRepository.delete(likeOptional.get());
            } else {
                Like newLike = new Like();
                newLike.setBoard(board.get());
                newLike.setUser(user.get());
                likeRepository.save(newLike);
            }
        }
    }
}

