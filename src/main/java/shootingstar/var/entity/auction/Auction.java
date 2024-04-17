package shootingstar.var.entity.auction;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import shootingstar.var.entity.BaseTimeEntity;
import shootingstar.var.entity.Bid;
import shootingstar.var.entity.User;
import shootingstar.var.entity.ticket.Ticket;
import shootingstar.var.enums.type.AuctionType;

@Entity
@Getter
@NoArgsConstructor
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auctionId;

    private String auctionUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;


    @Min(value = 100000)
    private long minBidAmount;

    @NotNull
    private LocalDateTime meetingDate;

    @NotBlank
    private String meetingLocation;

    @Column(columnDefinition = "LONGTEXT")
    @NotBlank
    private String meetingInfoText;

    @Column(columnDefinition = "LONGTEXT")
    @NotBlank
    private String meetingPromiseText;

    private String meetingInfoImg;

    private String meetingPromiseImg;

    private long currentHighestBidAmount;

    private String currentHighestBidderUUID;

    private long bidCount;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private AuctionType auctionType;

    private LocalDateTime auctionCloseTime;

    private boolean isExtended;

    @OneToMany(mappedBy = "auction")
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "auction")
    private Ticket ticket;

    @Builder
    public Auction(User user, long minBidAmount, LocalDateTime meetingDate, String meetingLocation,
                   String meetingInfoText, String meetingPromiseText, String meetingInfoImg, String meetingPromiseImg, LocalDateTime auctionCloseTime) {
        this.auctionUUID = UUID.randomUUID().toString();
        this.user = user;
        this.minBidAmount = minBidAmount;
        this.meetingDate = meetingDate;
        this.meetingLocation = meetingLocation;
        this.meetingInfoText = meetingInfoText;
        this.meetingPromiseText = meetingPromiseText;
        this.meetingInfoImg = meetingInfoImg;
        this.meetingPromiseImg = meetingPromiseImg;
        this.auctionType = AuctionType.PROGRESS;
        this.auctionCloseTime = auctionCloseTime;
        this.isExtended = false;
    }

    public void changeAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    // 경매의 주최자가 맞는지 확인하는 메서드
    public boolean isOwner(String userUUID) {
        return this.user.getUserUUID().equals(userUUID);
    }

    // 경매가 진행 중인지 확인하는 메서드
    public boolean isProgress() {
        return AuctionType.PROGRESS.equals(this.auctionType);
    }

    public void increaseBidCount() {
        this.bidCount++;
    }

    public void changeCurrentHighestBidderUUID(String userUUID) {
        this.currentHighestBidderUUID = userUUID;
    }

    public void changeCurrentHighestBidAmount(long price) {
        this.currentHighestBidAmount = price;
    }

    public void changeAuctionCloseTime(LocalDateTime auctionCloseTime) {
        this.auctionCloseTime = auctionCloseTime;
    }

    public void changeIsExtended(boolean isExtended) {
        this.isExtended = isExtended;
    }
}
