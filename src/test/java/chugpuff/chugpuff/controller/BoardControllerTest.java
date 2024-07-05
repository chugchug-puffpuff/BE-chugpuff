package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.CategoryService;
import chugpuff.chugpuff.service.LikeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Board board;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testCreateBoard() throws Exception {
        when(boardService.save(any(Board.class))).thenReturn(board);

        mockMvc.perform(post("/api/board").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardTitle\":\"Test Title\",\"boardContent\":\"Test Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        verify(boardService, times(1)).save(any(Board.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testUpdateBoard() throws Exception {
        when(boardService.update(any(Board.class))).thenReturn(board);

        mockMvc.perform(put("/api/board/{boardNo}", 1).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"boardTitle\":\"Test Title\",\"boardContent\":\"Test Content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        verify(boardService, times(1)).update(any(Board.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testDeleteBoard() throws Exception {
        doNothing().when(boardService).delete(1);

        mockMvc.perform(delete("/api/board/{boardNo}", 1).with(csrf()))
                .andExpect(status().isOk());

        verify(boardService, times(1)).delete(1);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetBoard() throws Exception {
        when(boardService.findById(1)).thenReturn(Optional.of(board));

        mockMvc.perform(get("/api/board/{boardNo}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        verify(boardService, times(1)).findById(1);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetAllBoards() throws Exception {
        Board board2 = new Board();
        board2.setBoardNo(2);
        board2.setBoardTitle("Title 2");
        board2.setBoardContent("Content 2");

        when(boardService.findAll()).thenReturn(Arrays.asList(board, board2));

        mockMvc.perform(get("/api/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardTitle").value("Test Title"))
                .andExpect(jsonPath("$[1].boardTitle").value("Title 2"));

        verify(boardService, times(1)).findAll();
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testSearchBoards() throws Exception {
        Board board2 = new Board();
        board2.setBoardNo(2);
        board2.setBoardTitle("Title 2");
        board2.setBoardContent("Content 2");

        when(boardService.searchByKeyword("Test")).thenReturn(Arrays.asList(board, board2));

        mockMvc.perform(get("/api/board/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardTitle").value("Test Title"))
                .andExpect(jsonPath("$[1].boardTitle").value("Title 2"));

        verify(boardService, times(1)).searchByKeyword("Test");
    }
}
