package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.dto.BoardDTO;
import chugpuff.chugpuff.dto.CategoryDTO;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.LikeService;
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

    @Mock
    private LikeService likeService;

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
        when(boardService.findAllBoardDTOs()).thenReturn(Collections.emptyList());

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
        when(boardService.findBoardDTOById(anyInt())).thenReturn(boardDTO);

        mockMvc.perform(get("/api/board/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.boardNo").value(1));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testCreateBoard() throws Exception {
        // Create a sample Board object
        Board board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");


        // Mocking the service to return the created board
        when(boardService.save(any(Board.class), any(Authentication.class))).thenReturn(board);

        // Perform POST request and verify the response
        mockMvc.perform(post("/api/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testUpdateBoard() throws Exception {
        // Sample updated BoardDTO
        BoardDTO updateBoardDTO = new BoardDTO();
        updateBoardDTO.setBoardNo(1);
        updateBoardDTO.setBoardTitle("Updated Title");
        updateBoardDTO.setBoardContent("Updated Content");

        // Mocking the service to return the updated board
        when(boardService.update(anyInt(), any(BoardDTO.class), any(Authentication.class)))
                .thenReturn(updateBoardDTO);

        // Perform PUT request and verify the response
        mockMvc.perform(put("/api/board/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBoardDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Updated Title"))
                .andExpect(jsonPath("$.boardContent").value("Updated Content"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testDeleteBoard() throws Exception {
        // Mocking the service to perform deletion without error
        doNothing().when(boardService).delete(anyInt(), any(Authentication.class));

        // Perform DELETE request and verify the response
        mockMvc.perform(delete("/api/board/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetLikesCount() throws Exception {
        // Mocking the service to return a likes count
        when(likeService.getLikesCount(anyInt())).thenReturn(100);

        // Perform GET request and verify the response
        mockMvc.perform(get("/api/board/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100));
    }


    @Test
    @WithMockUser
    public void testToggleLike() throws Exception {
        // Mocking the service to toggle like without error
        doNothing().when(likeService).toggleLike(anyInt());

        // Perform POST request and verify the response
        mockMvc.perform(post("/api/board/1/like"))
                .andExpect(status().isOk());
    }


}
