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
    private MockMvc mockMvc; //MockMvc를 통해 HTTP 요청과 응답 테스트

    @MockBean
    private BoardService boardService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper

    private Board board;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // Mockito 어노테이션 초기화
        board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
        board.setCategoryId(1);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testCreateBoard() throws Exception {
        // Mocking: boardService.save가 호출될 때 board 객체 반환
        when(boardService.save(any(Board.class))).thenReturn(board);

        // HTTP POST 요청을 통해 /api/board 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(post("/api/board").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        // boardService.save가 한 번 호출되었는지 확인
        verify(boardService, times(1)).save(any(Board.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testUpdateBoard() throws Exception {
        // Mocking: boardService.update가 호출될 때 board 객체 반환
        when(boardService.update(any(Board.class))).thenReturn(board);

        // HTTP PUT 요청을 통해 /api/board/{boardNo} 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(put("/api/board/{boardNo}", 1).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        // boardService.update가 한 번 호출되었는지 확인
        verify(boardService, times(1)).update(any(Board.class));
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testDeleteBoard() throws Exception {
        // Mocking: boardService.delete가 호출될 때 아무 것도 하지 않음
        doNothing().when(boardService).delete(1);

        // HTTP DELETE 요청을 통해 /api/board/{boardNo} 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(delete("/api/board/{boardNo}", 1).with(csrf()))
                .andExpect(status().isOk());

        // boardService.delete가 한 번 호출되었는지 확인
        verify(boardService, times(1)).delete(1);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetBoard() throws Exception {
        // Mocking: boardService.findById가 호출될 때 Optional로 감싼 board 객체 반환
        when(boardService.findById(1)).thenReturn(Optional.of(board));

        // HTTP GET 요청을 통해 /api/board/{boardNo} 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(get("/api/board/{boardNo}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));

        // boardService.findById가 한 번 호출되었는지 확인
        verify(boardService, times(1)).findById(1);
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetAllBoards() throws Exception {
        Board board2 = new Board();
        board2.setBoardNo(2);
        board2.setBoardTitle("Title 2");
        board2.setBoardContent("Content 2");
        board2.setCategoryId(2);

        // Mocking: boardService.findAll이 호출될 때 두 개의 board 객체를 포함한 리스트 반환
        when(boardService.findAll()).thenReturn(Arrays.asList(board, board2));

        // HTTP GET 요청을 통해 /api/board 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(get("/api/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardTitle").value("Test Title")) // boardTitle로 변경
                .andExpect(jsonPath("$[0].boardContent").value("Test Content")) // boardContent로 변경
                .andExpect(jsonPath("$[1].boardTitle").value("Title 2")) // boardTitle로 변경
                .andExpect(jsonPath("$[1].boardContent").value("Content 2")); // boardContent로 변경

        // boardService.findAll이 한 번 호출되었는지 확인
        verify(boardService, times(1)).findAll();
    }

    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testSearchBoards() throws Exception {
        Board board2 = new Board();
        board2.setBoardNo(2);
        board2.setBoardTitle("Another Test Title");
        board2.setBoardContent("Another Test Content");
        board2.setCategoryId(2);

    // Mocking: boardService.searchByKeyword가 호출될 때 두 개의 board 객체를 포함한 리스트 반환
        when(boardService.searchByKeyword("Test")).thenReturn(Arrays.asList(board, board2));

        // HTTP GET 요청을 통해 /api/board/search 엔드포인트 호출 및 기대 결과 확인
        mockMvc.perform(get("/api/board/search")
                        .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].boardTitle").value("Test Title")) // boardTitle로 변경
                .andExpect(jsonPath("$[0].boardContent").value("Test Content")) // boardContent로 변경
                .andExpect(jsonPath("$[1].boardTitle").value("Another Test Title")) // boardTitle로 변경
                .andExpect(jsonPath("$[1].boardContent").value("Another Test Content")); // boardContent로 변경

        // boardService.searchByKeyword가 한 번 호출되었는지 확인
        verify(boardService, times(1)).searchByKeyword("Test");
    }
}
