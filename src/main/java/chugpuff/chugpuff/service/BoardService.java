package chugpuff.chugpuff.service;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private CategoryService categoryService;

    public Board save(Board board) {
        Category category = categoryService.findCategoryByName(board.getCategoryName());
        board.setCategory(category);
        board.setBoardDate(LocalDateTime.now());
        return boardRepository.save(board);
    }

    public Board update(Board board) {
        board.setBoardmodifiedDate(LocalDateTime.now()); // 게시글 수정일 설정
        return boardRepository.save(board);
    }
    public void delete(int boardNo) {
        boardRepository.deleteById(boardNo);
    }

    public Optional<Board> findById(int boardNo) {
        return boardRepository.findById(boardNo);
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public List<Board> findByCategory(int categoryId) {
        return boardRepository.findByCategory_CategoryId(categoryId);
    }

    public List<Board> findAllByLikesDesc() {
        return boardRepository.findAllByLikesDesc();
    }

    public List<Board> findAllByBoardDateDesc() {
        return boardRepository.findAllByBoardDateDesc();
    }

    public List<Board> findAllByCommentsDesc() {
        return boardRepository.findAllByCommentsCountDesc();
    }

    public int getLikesCount(int boardNo) {
        return likeRepository.countByBoardNo(boardNo);
    }

}
