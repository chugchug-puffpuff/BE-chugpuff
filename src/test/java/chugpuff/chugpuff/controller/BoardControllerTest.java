package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.entity.Board;
import chugpuff.chugpuff.entity.Category;
import chugpuff.chugpuff.entity.MemberEntity;
import chugpuff.chugpuff.repository.MemberRepository;
import chugpuff.chugpuff.service.BoardService;
import chugpuff.chugpuff.service.LikeService;
import chugpuff.chugpuff.service.CategoryService;
import chugpuff.chugpuff.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private Board board;
    private Member member;
    private Category category;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setUser_id(1L);
        member.setName("Test User");

        category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Test Category");

        board = new Board();
        board.setBoardNo(1);
        board.setBoardTitle("Test Title");
        board.setBoardContent("Test Content");
        board.setBoardDate(LocalDateTime.now());
        board.setBoardmodifiedDate(LocalDateTime.now());
        board.setLikes(0);
        board.setMember(member);
        board.setCategory(category);

        // MockMvc with security setup
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testCreateBoard() throws Exception {
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));
        when(boardService.save(any(Board.class))).thenReturn(board);

        mockMvc.perform(post("/api/board").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("userId", "1")
                        .content(objectMapper.writeValueAsString(board)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(board)));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testGetBoardById() throws Exception {
        when(boardService.findById(anyInt())).thenReturn(Optional.of(board));

        mockMvc.perform(get("/api/board/{boardNo}", 1).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardNo").value(1))
                .andExpect(jsonPath("$.boardTitle").value("Test Title"))
                .andExpect(jsonPath("$.boardContent").value("Test Content"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testUpdateBoard() throws Exception {
        Board updatedBoard = new Board();
        updatedBoard.setBoardNo(1);
        updatedBoard.setBoardTitle("Updated Title");
        updatedBoard.setBoardContent("Updated Content");
        updatedBoard.setBoardDate(LocalDateTime.now());
        updatedBoard.setBoardmodifiedDate(LocalDateTime.now());
        updatedBoard.setLikes(0);
        updatedBoard.setMember(member);
        updatedBoard.setCategory(category);

        when(boardService.update(any(Board.class))).thenReturn(updatedBoard);

        mockMvc.perform(put("/api/board/{boardNo}", 1).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBoard)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedBoard)));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDeleteBoard() throws Exception {
        doNothing().when(boardService).delete(anyInt());

        mockMvc.perform(delete("/api/board/{boardNo}", 1).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testGetAllBoards() throws Exception {
        when(boardService.findAll()).thenReturn(List.of(board));

        mockMvc.perform(get("/api/board")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(board))));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testSearchBoardsByKeyword() throws Exception {
        when(boardService.searchByKeyword("Test")).thenReturn(List.of(board));

        mockMvc.perform(get("/api/board/search")
                        .param("keyword", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(board))));
    }
}

