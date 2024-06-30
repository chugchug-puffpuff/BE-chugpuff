package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Like;
import chugpuff.chugpuff.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LikeService {
    @Autowired
    private LikeRepository likeRepository;

    public List<Like> getAllLikes() {
        return likeRepository.findAll();
    }

    public Like saveLike(Like like) {
        return likeRepository.save(like);
    }

    public void deleteLike(int likeId) {
        likeRepository.deleteById(likeId);
    }

    public void toggleLike(int boardNo, String userId) {
        Optional<Like> existingLike = likeRepository.findByBoardNoAndUserId(boardNo, userId);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            Like newLike = new Like();
            newLike.setBoardNo(boardNo);
            newLike.setUserId(userId);
            likeRepository.save(newLike);
        }
    }
}
