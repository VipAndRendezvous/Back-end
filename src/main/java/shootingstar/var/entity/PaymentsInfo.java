package shootingstar.var.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentsInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String paymentUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    @NotNull
    private User user;

    @NotNull
    private Long paymentAmount;

    public PaymentsInfo(User user, Long paymentAmount) {
        this.paymentUUID = UUID.randomUUID().toString();
        this.user = user;
        this.paymentAmount = paymentAmount;
    }
}
