package chugpuff.chugpuff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerServiceTest {

    private TimerService timerService;

    @BeforeEach
    void setUp() {
        timerService = new TimerService();
    }

    @Test
    void testStartTimer_CallsOnFinish() throws InterruptedException {
        // Given
        Runnable onFinish = mock(Runnable.class);
        long duration = 1000;

        // When
        timerService.startTimer(duration, onFinish);
        Thread.sleep(1500);

        // Then
        verify(onFinish, times(1)).run();
    }

    @Test
    void testStopTimer_BeforeFinish() throws InterruptedException {
        // Given
        Runnable onFinish = mock(Runnable.class);
        long duration = 2000;

        // When
        timerService.startTimer(duration, onFinish);
        Thread.sleep(500);
        timerService.stopTimer();

        // Then
        verify(onFinish, times(0)).run();
    }

    @Test
    void testGetRemainingTime() throws InterruptedException {
        // Given
        Runnable onFinish = mock(Runnable.class);
        long duration = 3000;

        // When
        timerService.startTimer(duration, onFinish);
        Thread.sleep(1000);
        Map<String, Object> remainingTime = timerService.getRemainingTime();

        // Then
        long remainingMinutes = (long) remainingTime.get("minutes");
        long remainingSeconds = (long) remainingTime.get("seconds");
        assertTrue(remainingMinutes >= 0 && remainingMinutes <= 1, "Remaining minutes should be between 0 and 1.");
        assertTrue(remainingSeconds >= 0 && remainingSeconds <= 59, "Remaining seconds should be between 0 and 59.");
    }
}
