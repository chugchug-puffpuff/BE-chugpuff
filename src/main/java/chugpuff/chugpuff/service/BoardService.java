package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.CategoryRepository;
import chugpuff.chugpuff.service.LikeService;
import chugpuff.chugpuff.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final LikeService likeService;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, LikeService likeService, CategoryRepository categoryRepository, MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.likeService = likeService;
        this.categoryRepository = categoryRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Board save(Board board, Long userId, int categoryId) {
        // 카테고리 아이디로 카테고리 이름을 찾아서 설정
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        board.setCategory(category);

        // Member가 존재하는지 확인
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + userId));
        board.setMember(member);

        // 게시글 작성일 설정
        board.setBoardDate(LocalDateTime.now());

        // 게시글 저장 후 반환
        return boardRepository.save(board);
    }

    @Transactional
    public Board update(Board board, Long userId) {
        Board existingBoard = boardRepository.findById(board.getBoardNo())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!existingBoard.getMember().getUser_id().equals(userId)) {
            throw new IllegalArgumentException("작성자만 게시글을 수정할 수 있습니다.");
        }

        existingBoard.setBoardTitle(board.getBoardTitle());
        existingBoard.setBoardContent(board.getBoardContent());
        existingBoard.setBoardmodifiedDate(LocalDateTime.now());

        return boardRepository.save(existingBoard);
    }

    @Transactional
    public void delete(int boardNo, Long userId) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!board.getMember().getUser_id().equals(userId)) {
            throw new IllegalArgumentException("작성자만 게시글을 삭제할 수 있습니다.");
        }

        // 먼저 좋아요 데이터를 삭제합니다.
        likeService.deleteLikesByBoardNo(boardNo);

        boardRepository.delete(board);
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

    public List<Board> findAllByOrderByLikesDesc() {
        return boardRepository.findAllByOrderByLikesDesc();
    }

    public List<Board> findAllByOrderByBoardDateDesc() {
        return boardRepository.findAllByOrderByBoardDateDesc();
    }

    public List<Board> findAllByCommentsDesc() {
        return boardRepository.findAllByCommentsCountDesc();
    }

    public int getLikesCount(int boardNo) {
        return likeService.getLikesCount(boardNo);
    }

    public List<Board> searchByKeyword(String keyword) {
        return boardRepository.findByBoardTitleContainingOrBoardContentContaining(keyword, keyword);
    }

    public BoardDTO convertToDTO(Board board) {
        return new BoardDTO(
                board.getBoardNo(),
                board.getBoardTitle(),
                board.getBoardContent(),
                board.getMember().getName(),
                board.getCategory().getCategoryName()
        );
    }

    public BoardDTO findBoardDTOById(int boardNo) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("Board not found"));
        return convertToDTO(board);
    }

    public List<BoardDTO> findAllBoardDTOs() {
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}

