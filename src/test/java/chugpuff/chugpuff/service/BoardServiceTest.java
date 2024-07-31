package chugpuff.chugpuff.service;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.entity.Comment;
import chugpuff.chugpuff.repository.BoardRepository;
import chugpuff.chugpuff.repository.CategoryRepository;
import chugpuff.chugpuff.repository.MemberRepository;
import chugpuff.chugpuff.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private BoardService boardService;

    @Mock
    private LikeService likeService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConvertToDTO() {
        // Create a sample Board object
        Board board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
        LocalDateTime now = LocalDateTime.now();
        board.setBoardDate(now);
        board.setBoardModifiedDate(now);
        board.setLikes(10);

        Member member = new Member();
        member.setName("Test Member");
        board.setMember(member);

        Comment comment1 = new Comment();
        comment1.setBcContent("Test Comment 1");

        Comment comment2 = new Comment();
        comment2.setBcContent("Test Comment 2");

        board.setComments(List.of(comment1, comment2));

        Category category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("정보공유");
        board.setCategory(category);

        // Convert Board to BoardDTO
        BoardDTO boardDTO = boardService.convertToDTO(board);

        // Assertions
        assertEquals(board.getBoardNo(), boardDTO.getBoardNo());
        assertEquals(board.getBoardTitle(), boardDTO.getBoardTitle());
        assertEquals(board.getBoardContent(), boardDTO.getBoardContent());
        assertEquals(board.getMember().getName(), boardDTO.getMemberName());
        assertEquals(board.getBoardDate(), boardDTO.getBoardDate());
        assertEquals(board.getBoardModifiedDate(), boardDTO.getBoardModifiedDate());
        assertEquals(board.getLikes(), boardDTO.getLikes());
        assertEquals(board.getComments().size(), boardDTO.getCommentCount());

        List<String> expectedCommentContents = board.getComments().stream()
                .map(Comment::getBcContent)
                .collect(Collectors.toList());
        assertEquals(expectedCommentContents, boardDTO.getCommentContents());

        if (board.getCategory() != null) {
            assertEquals(board.getCategory().getCategoryId(), boardDTO.getCategory().getCategoryId());
            assertEquals(board.getCategory().getCategoryName(), boardDTO.getCategory().getCategoryName());
        }
    }
    @Test
    public void testUpdateBoard() {
        when(authentication.getName()).thenReturn("user123");
        Member member = new Member();
        member.setId("user123");
        when(memberRepository.findById("user123")).thenReturn(Optional.of(member));

        Board board = new Board();
        board.setMember(member);
        board.setBoardNo(1);
        when(boardRepository.findById(1)).thenReturn(Optional.of(board));

        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardTitle("Updated Title");
        boardDTO.setBoardContent("Updated Content");

        when(boardRepository.save(any(Board.class))).thenReturn(board);

        BoardDTO updatedBoardDTO = boardService.update(1, boardDTO, authentication);

        assertEquals("Updated Title", updatedBoardDTO.getBoardTitle());
        assertEquals("Updated Content", updatedBoardDTO.getBoardContent());
        assertNotNull(updatedBoardDTO.getBoardModifiedDate());
    }

    @Test
    public void testDeleteBoard() {
        when(authentication.getName()).thenReturn("user123");
        Member member = new Member();
        member.setId("user123");
        when(memberRepository.findById("user123")).thenReturn(Optional.of(member));

        Board board = new Board();
        board.setMember(member);
        board.setBoardNo(1);
        when(boardRepository.findById(1)).thenReturn(Optional.of(board));

        boardService.delete(1, authentication);

        verify(boardRepository, times(1)).delete(board);
        verify(likeService, times(1)).deleteLikesByBoardNo(1);
        verify(commentRepository, times(1)).deleteByBoard_BoardNo(1);
    }
}
