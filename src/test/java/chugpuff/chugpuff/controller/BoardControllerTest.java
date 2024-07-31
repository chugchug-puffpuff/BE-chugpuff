package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.dto.CategoryDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.service.BoardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private BoardController boardController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser
    public void testGetAllBoards() throws Exception {
        when(boardService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/board"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

    }

    @Test
    @WithMockUser
    public void testGetBoardById() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardNo(1);
        when(boardService.findById(anyInt())).thenReturn(Optional.of(boardDTO));

        mockMvc.perform(get("/api/board/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardNo").value(1));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testCreateBoard() throws Exception {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardNo(1);
        boardDTO.setBoardTitle("Test Title");
        boardDTO.setBoardContent("Test Content");
        boardDTO.setMemberName("Test Member");
        boardDTO.setBoardDate(LocalDateTime.now());
        boardDTO.setLikes(0);
        boardDTO.setCommentCount(0);
        boardDTO.setCategory(new CategoryDTO(1, "정보공유"));

        // Mocking Board entity
        Board board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
        // Set other fields accordingly

        // Mock the service methods
        when(boardService.save(any(Board.class), any(Authentication.class))).thenReturn(board);
        when(boardService.convertToDTO(any(Board.class))).thenReturn(boardDTO);

        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"))
                .andExpect(jsonPath("$.memberName").value("Test Member"));
    }
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testUpdateBoard() throws Exception {
        // Sample BoardDTO for the update
        BoardDTO updateBoardDTO = new BoardDTO();
        updateBoardDTO.setBoardNo(1);
        updateBoardDTO.setBoardTitle("Updated Title");
        updateBoardDTO.setBoardContent("Updated Content");
        updateBoardDTO.setMemberName("Updated Member");
        updateBoardDTO.setBoardDate(LocalDateTime.now());
        updateBoardDTO.setBoardModifiedDate(LocalDateTime.now());
        updateBoardDTO.setLikes(0);
        updateBoardDTO.setCommentCount(0);
        updateBoardDTO.setCategory(new CategoryDTO(1, "Updated Category"));

        // Mocking service response
        when(boardService.update(anyInt(), any(BoardDTO.class), any(Authentication.class)))
                .thenReturn(updateBoardDTO);

        mockMvc.perform(put("/api/board/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBoardDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Updated Title"))
                .andExpect(jsonPath("$.boardContent").value("Updated Content"))
                .andExpect(jsonPath("$.memberName").value("Updated Member"))
                .andExpect(jsonPath("$.category.categoryName").value("Updated Category"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testDeleteBoard() throws Exception {
        // Mocking service response to do nothing on delete
        doNothing().when(boardService).delete(anyInt(), any(Authentication.class));

        mockMvc.perform(delete("/api/board/1"))
                .andExpect(status().isOk());
    }
}