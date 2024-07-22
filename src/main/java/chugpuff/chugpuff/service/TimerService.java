package chugpuff.chugpuff.service;

import org.springframework.stereotype.Service;

@Service
public class TimerService {

    private boolean isPaused = false;
    private boolean isStopped = false;

    public void startTimer(long duration, Runnable onFinish) {
        new Thread(() -> {
            long endTime = System.currentTimeMillis() + duration;
            while (System.currentTimeMillis() < endTime) {
                if (isStopped) break;
                if (!isPaused) {
                    // Update the timer display
                    // ...
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!isStopped) onFinish.run();
        }).start();
    }

    public void pauseTimer() {
        isPaused = true;
    }

    public void resumeTimer() {
        isPaused = false;
    }

    public void stopTimer() {
        isStopped = true;
    }
}
