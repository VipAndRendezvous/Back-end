package shootingstar.var.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import shootingstar.var.entity.auction.Auction;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {
    Optional<Auction> findByAuctionUUID(String auctionUUID);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.auctionUUID = :auctionUUID")
    Optional<Auction> findByAuctionUUIDWithPessimisticLock(String auctionUUID);
}
