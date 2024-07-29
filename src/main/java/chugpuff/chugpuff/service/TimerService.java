package chugpuff.chugpuff.service;

import org.springframework.stereotype.Service;

@Service
public class TimerService {

    private boolean isPaused = false;
    private boolean isStopped = false;
    private long remainingTime;
    private Thread timerThread;

    // 타이머 시작
    public void startTimer(long duration, Runnable onFinish) {
        isPaused = false;
        isStopped = false;
        remainingTime = duration;

        timerThread = new Thread(() -> {
            long endTime = System.currentTimeMillis() + duration;
            while (System.currentTimeMillis() < endTime && !isStopped) {
                if (!isPaused) {
                    remainingTime = endTime - System.currentTimeMillis();
                    // 타이머 상태 업데이트 (필요 시 UI 업데이트 로직 추가)
                    System.out.println("Remaining time: " + remainingTime / 1000 + " seconds");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!isStopped) onFinish.run();
        });
        timerThread.start();
    }

    // 타이머 종료
    public void stopTimer() {
        isStopped = true;
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }
}
