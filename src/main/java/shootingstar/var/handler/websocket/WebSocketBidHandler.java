package shootingstar.var.handler.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shootingstar.var.Service.BidService;
import shootingstar.var.dto.req.BidReqDto;
import shootingstar.var.dto.res.BidInfoResDto;
import shootingstar.var.dto.res.BidResDto;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.jwt.JwtTokenProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketBidHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final BidService bidService;

    private final Set<WebSocketSession> sessions = new HashSet<>();

    private final Map<String, Set<WebSocketSession>> bidSessionMap = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("bid 세션 {} 연결됨", session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        try {
            BidReqDto bidReqDto = getBidReqDto(payload);

            // 엑세스 토큰 검증
            validateAccessToken(bidReqDto);

            // 경매 UUID에 해당하는 map에 session 추가
            String auctionUUID = bidReqDto.getAuctionUUID();
            addSessionToBid(session, auctionUUID);

            if (bidReqDto.getIsBidMessage()) {
                // 응찰 정보 저장
                BidResDto resDto = bidService.participateAuction(getUserUUID(bidReqDto), bidReqDto);
                String messageJson = objectMapper.writeValueAsString(resDto);
                // key가 auctionUUID인 session에 메세지 전송
                broadcastMessage(auctionUUID, messageJson);

            } else {
                BidInfoResDto resDto = bidService.findBidInfo(auctionUUID);
                String messageJson = objectMapper.writeValueAsString(resDto);
                sendMessage(session, messageJson);
            }

        } catch (CustomException e) {
            sendErrorMessage(session, e);

            session.close();
        }
    }

    private BidReqDto getBidReqDto(String payload) {
        BidReqDto bidReqDto;
        try {
            bidReqDto = objectMapper.readValue(payload, BidReqDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_JSON);
        }

        if (bidReqDto.getAuctionUUID() == null || bidReqDto.getAuctionUUID().equals("")) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_AUCTION_UUID);
        } else if (bidReqDto.getIsBidMessage() == null) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_IS_BID_MESSAGE);
        } else if (bidReqDto.getIsBidMessage()) {
            if ((bidReqDto.getPrice() == 0 || bidReqDto.getPrice() == null)) {
                throw new CustomException(ErrorCode.INCORRECT_FORMAT_PRICE);
            }
        }
        return bidReqDto;
    }

    private void validateAccessToken(BidReqDto bidReqDto) {
        log.info("엑세스 토큰 검증");
        String accessToken = bidReqDto.getAccessToken();
        jwtTokenProvider.validateAccessToken(accessToken);
    }

    private void addSessionToBid(WebSocketSession session, String auctionUUID) {
        if (!bidSessionMap.containsKey(auctionUUID)) {
            bidSessionMap.put(auctionUUID, new HashSet<>());
        }
        bidSessionMap.get(auctionUUID).add(session);
    }

    private String getUserUUID(BidReqDto bidReqDto) {
        Authentication authentication = jwtTokenProvider.getAuthenticationFromAccessToken(bidReqDto.getAccessToken());
        return authentication.getName();
    }

    private void broadcastMessage(String chatRoomUUID, String messageJson) throws IOException {
        Set<WebSocketSession> bidSessions = bidSessionMap.get(chatRoomUUID);
        if (bidSessions != null) {
            for (WebSocketSession bidSession : bidSessions) {
                if (bidSession.isOpen()) {
                    sendMessage(bidSession, messageJson);
                }
            }
        }
    }

    private void sendMessage(WebSocketSession session, String messageJson) throws IOException {
        session.sendMessage(new TextMessage(messageJson));
    }

    private void sendErrorMessage(WebSocketSession session, CustomException e) throws IOException {
        Map<String, String> errors = new HashMap<>();
        errors.put("code", e.getErrorCode().getCode());
        errors.put("description", e.getErrorCode().getDescription());
        String errorRes = objectMapper.writeValueAsString(errors);
        sendMessage(session, errorRes);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("bid 세션 {} 연결 끊김", session.getId());
        sessions.remove(session);
    }
}
