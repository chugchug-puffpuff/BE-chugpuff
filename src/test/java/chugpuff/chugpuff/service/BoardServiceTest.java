package chugpuff.chugpuff.service;

import chugpuff.chugpuff.controller.BoardController;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.repository.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private LikeService likeService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveBoard() {
        // Given
        Board board = new Board();
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
        Category category = new Category();
        category.setCategoryName("취업고민");

        when(categoryService.findCategoryByName(anyString())).thenReturn(category);
        when(boardRepository.save(any(Board.class))).thenReturn(board);

        // When
        Board savedBoard = boardService.save(board);

        // Then
        assertNotNull(savedBoard);
        assertEquals("Test Title", savedBoard.getBoardTitle());
        assertEquals("Test Content", savedBoard.getBoardContent());
        verify(boardRepository, times(1)).save(board);
    }

    @Test
    public void testUpdateBoard() {
        // Given
        Board board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Updated Title");
        board.setBoardContent("Updated Content");

        when(boardRepository.save(any(Board.class))).thenReturn(board);

        // When
        Board updatedBoard = boardService.update(board);

        // Then
        assertNotNull(updatedBoard);
        assertEquals("Updated Title", updatedBoard.getBoardTitle());
        assertEquals("Updated Content", updatedBoard.getBoardContent());
        verify(boardRepository, times(1)).save(board);
    }

    @Test
    public void testDeleteBoard() {
        // Given
        int boardNo = 1;

        doNothing().when(boardRepository).deleteById(boardNo);

        // When
        boardService.delete(boardNo);

        // Then
        verify(boardRepository, times(1)).deleteById(boardNo);
    }

    @Test
    public void testFindById() {
        // Given
        int boardNo = 1;
        Board board = new Board();
        board.setBoardNo(boardNo);

        when(boardRepository.findById(boardNo)).thenReturn(Optional.of(board));

        // When
        Optional<Board> foundBoard = boardService.findById(boardNo);

        // Then
        assertTrue(foundBoard.isPresent());
        assertEquals(boardNo, foundBoard.get().getBoardNo());
        verify(boardRepository, times(1)).findById(boardNo);
    }

    @Test
    public void testFindAll() {
        // Given
        Board board1 = new Board();
        Board board2 = new Board();

        when(boardRepository.findAll()).thenReturn(Arrays.asList(board1, board2));

        // When
        List<Board> boards = boardService.findAll();

        // Then
        assertEquals(2, boards.size());
        verify(boardRepository, times(1)).findAll();
    }

    @Test
    public void testSearchByKeyword() {
        // Given
        Board board1 = new Board();
        board1.setBoardTitle("Test Title 1");
        board1.setBoardContent("Test Content 1");

        Board board2 = new Board();
        board2.setBoardTitle("Test Title 2");
        board2.setBoardContent("Another Content");

        when(boardRepository.findByBoardTitleContainingOrBoardContentContaining("Test", "Test"))
                .thenReturn(Arrays.asList(board1, board2));

        // When
        List<Board> result = boardService.searchByKeyword("Test");

        // Then
        assertEquals(2, result.size());
        verify(boardRepository, times(1)).findByBoardTitleContainingOrBoardContentContaining("Test", "Test");
    }
}