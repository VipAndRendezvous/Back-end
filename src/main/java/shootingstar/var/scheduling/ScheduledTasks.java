package shootingstar.var.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import shootingstar.var.Service.SchedulerService;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final SchedulerService schedulerService;

    @Scheduled(cron = "0 0 0 * * *")
    public void completeTicket() {
        schedulerService.completeTicket();
    }
}
