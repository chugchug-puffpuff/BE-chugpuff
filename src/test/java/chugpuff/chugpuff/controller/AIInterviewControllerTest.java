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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
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
    void testSaveInterview() throws Exception {
        // 테스트할 AIInterviewDTO 생성
        AIInterviewDTO aiInterviewDTO = new AIInterviewDTO();
        aiInterviewDTO.setUser_id(1L);
        aiInterviewDTO.setInterviewType("Technical");
        aiInterviewDTO.setFeedbackType("Full");
        aiInterviewDTO.setF_feedback("Sample feedback");

        // 테스트용 Member 생성
        Member member = Member.builder()
                .user_id(1L)
                .id("user123")
                .password("password123")
                .name("John Doe")
                .job("Developer")
                .jobKeyword("Java, Spring")
                .email("john.doe@example.com")
                .isAbove15(true)
                .privacyPolicyAccepted(true)
                .recordingAccepted(true)
                .build();

        // 테스트용 AIInterview 생성
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L); // AIInterviewNo 필드를 설정합니다.
        aiInterview.setMember(member);
        aiInterview.setInterviewType("Technical");
        aiInterview.setFeedbackType("Full");

        // Mock 설정
        given(memberService.getMemberByUser_id(1L)).willReturn(Optional.of(member)); // Long 타입 사용
        given(aiInterviewService.saveInterview(any(AIInterview.class))).willReturn(aiInterview);

        // POST 요청 테스트
        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aiInterviewDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['aiinterviewNo']").value(1L)); // JSON 경로를 대문자로 수정
    }

    @Test
    @WithMockUser
    void testStartInterview() throws Exception {
        Member member = new Member();
        member.setUser_id(1L);
        member.setId("user123");

        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setMember(member);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        doNothing().when(aiInterviewService).startInterview(eq(1L), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/start"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testStopInterview() throws Exception {
        doNothing().when(timerService).stopTimer();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/stop"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testStopRecording() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        doNothing().when(aiInterviewService).stopAudioCapture();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/stop-recording"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recording stopped and saved."));
    }

    @Test
    @WithMockUser
    void testProcessAudioResponse() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);
        aiInterview.setFeedbackType("즉시 피드백");

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(externalAPIService.callSTT(anyString())).willReturn("Transcribed text");
        given(aiInterviewService.getSelfIntroductionContentForInterview(any())).willReturn("Self introduction");
        given(aiInterviewService.getChatGPTQuestion(any(), any(), any(), any())).willReturn("Next question");
        given(aiInterviewService.getChatGPTFeedback(any(), any())).willReturn("Feedback");

        MockMultipartFile mockAudioFile = new MockMultipartFile("audioFile", "test.wav", "audio/wav", "dummy data".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/aiinterview/1/process-audio-response")
                        .file(mockAudioFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Feedback"));
    }

    @Test
    @WithMockUser
    void testConvertTextToSpeech() throws Exception {
        String text = "Hello World";
        String audioFilePath = "output.mp3";

        given(externalAPIService.callTTS(text)).willReturn(audioFilePath);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello World\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testEndInterview() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        doNothing().when(aiInterviewService).endInterview(any(AIInterview.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/aiinterview/1/end"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetInterviewById() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        AIInterviewDTO aiInterviewDTO = new AIInterviewDTO();
        aiInterviewDTO.setUser_id(1L);
        aiInterviewDTO.setInterviewType("Technical");

        given(aiInterviewService.getInterviewById(1L)).willReturn(aiInterview);
        given(aiInterviewService.convertToDTO(any(AIInterview.class))).willReturn(aiInterviewDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/aiinterview/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(1L))
                .andExpect(jsonPath("$.interviewType").value("Technical"));
    }

    @Test
    @WithMockUser
    void testGetInterviewsByMember() throws Exception {
        AIInterview aiInterview = new AIInterview();
        aiInterview.setAIInterviewNo(1L);

        given(aiInterviewService.findByMemberId("user123")).willReturn(List.of(aiInterview));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/aiinterview/id/user123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteInterview() throws Exception {
        doNothing().when(aiInterviewService).deleteInterviewById(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/aiinterview/1"))
                .andExpect(status().isNoContent());
    }
}
