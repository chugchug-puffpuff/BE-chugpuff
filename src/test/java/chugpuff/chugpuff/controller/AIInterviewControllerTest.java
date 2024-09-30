package chugpuff.chugpuff.controller;

import chugpuff.chugpuff.domain.AIInterview;
import chugpuff.chugpuff.domain.Member;
import chugpuff.chugpuff.dto.AIInterviewDTO;
import chugpuff.chugpuff.service.AIInterviewService;
import chugpuff.chugpuff.service.ExternalAPIService;
import chugpuff.chugpuff.service.MemberService;
import chugpuff.chugpuff.service.TimerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AIInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIInterviewService aiInterviewService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private TimerService timerService;

    @MockBean
    private ExternalAPIService externalAPIService;

    @Test
    @WithMockUser
    void testCreateInterview() throws Exception {
        // Given
        AIInterviewDTO aiInterviewDTO = new AIInterviewDTO();
        aiInterviewDTO.setUser_id(1L);
        aiInterviewDTO.setInterviewType("직무 면접");
        aiInterviewDTO.setFeedbackType("전체 피드백");

        Member member = new Member();
        member.setUser_id(1L);
        member.setId("test1");

        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setMember(member);
        aiInterview.setInterviewType("직무 면접");
        aiInterview.setFeedbackType("전체 피드백");

        ResponseEntity<AIInterview> responseEntity = ResponseEntity.ok(aiInterview);

        // When
        given(memberService.getMemberByUser_id(1L)).willReturn(Optional.of(member));

        given(aiInterviewService.createInterview(any(AIInterviewDTO.class))).willAnswer(invocation -> responseEntity);

        // Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiInterviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiinterviewNo").value(1L));
    }

    @Test
    @WithMockUser
    void testStartInterview() throws Exception {
        Member member = new Member();
        member.setUser_id(1L);
        member.setId("test1");

        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setMember(member);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.startInterview(any(AIInterview.class))).willReturn("첫 질문입니다.");
        given(externalAPIService.callTTS(anyString())).willReturn("http://tts.audio.url");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("첫 질문입니다."))
                .andExpect(jsonPath("$.ttsAudioUrl").value("http://tts.audio.url"));
    }

    @Test
    @WithMockUser
    void testStartInterviewTimer() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(timerService.getRemainingTime()).willReturn(new java.util.HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/start-timer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testStartAnswerRecording() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/answer-start"))
                .andExpect(status().isOk())
                .andExpect(content().string("녹음 시작"));
    }

    @Test
    @WithMockUser
    void testCompleteAnswerRecording() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.completeAnswerRecordingWithAudioUrl(1L)).willReturn(new java.util.HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/answer-complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testConvertAnswerToText() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.convertAnswerToText(any(), anyString())).willReturn(new java.util.HashMap<>());

        MockMultipartFile mockAudioFile = new MockMultipartFile("audioFile", "test.wav", "audio/wav", "dummy data".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/aiinterview/1/convert-answer")
                        .file(mockAudioFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testGenerateFeedback() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setFeedbackType("전체 피드백");

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.generateFullFeedback(any())).willReturn(new java.util.HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/generate-feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testNextQuestion() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.generateNextQuestion(any())).willReturn(new java.util.HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/next-question"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testEndInterview() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.endInterview(any())).willReturn(new java.util.HashMap<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    @WithMockUser
    void testDeleteInterview() throws Exception {
        doNothing().when(aiInterviewService).deleteInterviewById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/aiinterview/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testGetInterviewById() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        AIInterviewDTO aiInterviewDTO = new AIInterviewDTO();
        aiInterviewDTO.setUser_id(1L);
        aiInterviewDTO.setInterviewType("직무 면접");

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.convertToDTO(any())).willReturn(aiInterviewDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/aiinterview/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(1L))
                .andExpect(jsonPath("$.interviewType").value("직무 면접"));
    }

    @Test
    @WithMockUser
    void testGetInterviewsByMember() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.findByMemberId("test1")).willReturn(List.of(aiInterview));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/aiinterview/id/test1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aiinterviewNo").value(1L));
    }
}
