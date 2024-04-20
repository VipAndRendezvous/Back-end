package shootingstar.var.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingTimeSaveReqDto {
    @NotBlank
    private String ticketUUID;

//    @NotBlank
//    private LocalDateTime startMeetingTime;
}
