package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.dto.CategoryDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.CategoryRepository;
import chugpuff.chugpuff.repository.CommentRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    private final CommentRepository commentRepository;

    @Autowired
    public BoardService(BoardRepository boardRepository, LikeService likeService, CategoryRepository categoryRepository, MemberRepository memberRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.likeService = likeService;
        this.categoryRepository = categoryRepository;
        this.memberRepository = memberRepository;
        this.commentRepository = commentRepository;
    }

    //게시글 작성
    @Transactional
    public Board save(Board board, Authentication authentication) {
        // 토큰에서 사용자 정보 추출
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        board.setMember(member);

        // 카테고리 아이디로 카테고리 이름을 찾아서 설정
        Category category = categoryRepository.findById(board.getCategory().getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + board.getCategory().getCategoryId()));
        board.setCategory(category);

        // 게시글 작성일 설정
        board.setBoardDate(LocalDateTime.now());

        // 게시글 저장 후 반환
        return boardRepository.save(board);
    }

    //게시글 수정
    @Transactional
    public BoardDTO update(int boardId, BoardDTO updatedBoardDTO, Authentication authentication) {
        Board existingBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // 사용자 인증 확인
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 게시글 작성자와 인증된 사용자가 일치하는지 확인
        if (existingBoard.getMember() == null || !existingBoard.getMember().equals(member)) {
            throw new SecurityException("게시글 작성자만 수정할 수 있습니다.");
        }

        existingBoard.setBoardTitle(updatedBoardDTO.getBoardTitle());
        existingBoard.setBoardContent(updatedBoardDTO.getBoardContent());
        existingBoard.setBoardModifiedDate(LocalDateTime.now()); // 수정 날짜 반영

        boardRepository.save(existingBoard);

        return convertToDTO(existingBoard);
    }

    //게시글 삭제
    @Transactional
    public void delete(int boardNo, Authentication authentication) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        //작성자 확인
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        if (!board.getMember().equals(member)) {
            throw new SecurityException("게시글 작성자만 삭제할 수 있습니다.");
        }

        // 좋아요 데이터를 삭제
        likeService.deleteLikesByBoardNo(boardNo);
        // 게시글에 달린 댓글 삭제
        commentRepository.deleteByBoard_BoardNo(boardNo);
        //게시글 삭제
        boardRepository.delete(board);
    }

    //해당 게시글 조회
    public Optional<BoardDTO> findById(int boardNo) {
        return boardRepository.findById(boardNo).map(this::convertToDTO);
    }
    //모든 게시글 조회
    public List<BoardDTO> findAll() {
        return boardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    //카테고리별 조회
    public List<Board> findByCategory(int categoryId) {
        return boardRepository.findByCategory_CategoryId(categoryId);
    }

    //좋아요순 정렬
    public List<Board> findAllByOrderByLikesDesc() {
        return boardRepository.findAllByOrderByLikesDesc();
    }
    //최신순 정렬
    public List<Board> findAllByOrderByBoardDateDesc() {
        return boardRepository.findAllByOrderByBoardDateDesc();
    }

    //댓글수순 정렬
    public List<Board> findAllByCommentsDesc() {
        return boardRepository.findAllByCommentsCountDesc();
    }
    //좋아요 수 계산
    public int getLikesCount(int boardNo) {
        return likeService.getLikesCount(boardNo);
    }

    //게시글 검색
    public List<Board> searchByCategoryAndKeyword(Integer categoryId, String keyword) {
        if (categoryId != null && keyword != null) {
            // 카테고리와 키워드가 모두 있을 때: 해당 카테고리에서 제목 또는 내용에 키워드 포함된 게시글 검색
            return boardRepository.findByCategory_CategoryIdAndBoardTitleContainingOrCategory_CategoryIdAndBoardContentContaining(categoryId, keyword, categoryId, keyword);
        } else if (categoryId != null) {
            // 카테고리만 있을 때: 해당 카테고리의 모든 게시글 반환
            return boardRepository.findByCategory_CategoryId(categoryId);
        } else if (keyword != null) {
            // 키워드만 있을 때: 모든 카테고리에서 제목 또는 내용에 키워드 포함된 게시글 검색
            return boardRepository.findByBoardTitleContainingOrBoardContentContaining(keyword, keyword);
        } else {
            // 카테고리와 키워드 둘 다 없으면 모든 게시글 반환
            return boardRepository.findAll();
        }
    }




    // 사용자별 게시글 조회
    public List<BoardDTO> findBoardsByUser(Authentication authentication) {
        String username = authentication.getName();
        Member member = memberRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        return boardRepository.findByMember_Id(member.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 게시글 DTO로 변환
    public BoardDTO convertToDTO(Board board) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardNo(board.getBoardNo());
        boardDTO.setBoardTitle(board.getBoardTitle());
        boardDTO.setBoardContent(board.getBoardContent());
        boardDTO.setMemberName(board.getMember().getName());
        boardDTO.setBoardDate(board.getBoardDate());
        boardDTO.setBoardModifiedDate(board.getBoardModifiedDate());
        boardDTO.setLikes(board.getLikes());
        boardDTO.setCommentCount(board.getComments().size());
        boardDTO.setCommentContents(board.getComments().stream().map(Comment::getBcContent).collect(Collectors.toList()));

        if (board.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategoryId(board.getCategory().getCategoryId());
            categoryDTO.setCategoryName(board.getCategory().getCategoryName());
            boardDTO.setCategory(categoryDTO);
        }

        return boardDTO;
    }


    // 특정 게시글 DTO 조회
    public BoardDTO findBoardDTOById(int boardNo) {
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return convertToDTO(board);
    }

    // 모든 게시글 DTO 조회
    public List<BoardDTO> findAllBoardDTOs() {
        return boardRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
