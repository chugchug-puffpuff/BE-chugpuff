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

    public int getLikesCount(int boardNo) {
        return likeRepository.countByBoard_BoardNo(boardNo); // 여기서 countByBoard_BoardNo를 호출합니다.
    }

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

