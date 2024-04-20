package shootingstar.var.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


@Getter
public enum ErrorCode {
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "0000", "알 수 없는 오류가 발생했습니다."),
    INCORRECT_FORMAT(BAD_REQUEST, "0001", "잘못된 입력입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"0002", "제공되지 않는 메서드입니다."),

    AUTHENTICATION_ERROR(UNAUTHORIZED, "0100", "인증에 실패하였습니다."),
    ACCESS_DENIED(FORBIDDEN, "0101", "접근 권한이 없습니다."),
    INVALID_ACCESS_TOKEN(FORBIDDEN, "0102", "잘못된 Access Token 입니다."),
    EXPIRED_ACCESS_TOKEN(FORBIDDEN, "0103", "만료된 Access Token 입니다."),
    UNSUPPORTED_ACCESS_TOKEN(FORBIDDEN, "0104", "지원하지 않는 Access Token 입니다."),
    ILLEGAL_ACCESS_TOKEN(FORBIDDEN, "0105", "권한 정보가 없는 Access Token 입니다."),
    INVALID_REFRESH_TOKEN(FORBIDDEN, "0106", "잘못된 Refresh Token 입니다."),
    EXPIRED_REFRESH_TOKEN(FORBIDDEN, "0107", "만료된 Refresh Token 입니다."),
    UNSUPPORTED_REFRESH_TOKEN(FORBIDDEN, "0108", "지원하지 않는 Refresh Token 입니다."),
    ILLEGAL_REFRESH_TOKEN(FORBIDDEN, "0109", "권한 정보가 없는 Refresh Token 입니다."),

    KAKAO_AUTHENTICATION_ERROR(UNAUTHORIZED, "0110", "카카오로부터 ACCESS 토큰 획득에 실패했습니다."),
    KAKAO_CONNECT_FAILED_TOKEN_ENDPOINT(UNAUTHORIZED, "0111", "카카오 토큰 엔드포인트와 통신에 실패하였습니다."),
    KAKAO_FAILED_GET_USERINFO_ERROR(UNAUTHORIZED, "0112", "카카오로부터 사용자 정보를 가져오지 못했습니다."),
    KAKAO_CONNECT_FAILED_USERINFO_ENDPOINT(UNAUTHORIZED, "0113", "카카오 사용자 정보 엔드포인트와 통신에 실패하였습니다."),
    KAKAO_CONNECT_FAILED_UNLINK_ENDPOINT(UNAUTHORIZED, "0114", "카카오 사용자 연결 해제 엔드포인트와 통신에 실패하였습니다."),
    LOGGED_IN_SOMEWHERE_ELSE(FORBIDDEN, "0115", "다른 장소에서 로그인 되었습니다."),

    NOT_FOUND_END_POINT(NOT_FOUND, "0200", "존재하지 않는 접근입니다."),

    INCORRECT_FORMAT_EMAIL(BAD_REQUEST, "1001", "잘못된 형식의 이메일입니다."),
    INCORRECT_FORMAT_CODE(BAD_REQUEST, "1002", "잘못된 형식의 인증코드입니다."),
    INCORRECT_FORMAT_NICKNAME(BAD_REQUEST, "1003", "잘못된 형식의 닉네임입니다."),
    INCORRECT_FORMAT_KAKAO_ID(BAD_REQUEST, "1004", "잘못된 형식의 카카오 고유번호입니다."),
    INCORRECT_FORMAT_USER_NAME(BAD_REQUEST, "1005", "잘못된 형식의 사용자 이름입니다."),
    INCORRECT_FORMAT_PHONE_NUMBER(BAD_REQUEST, "1006", "잘못된 형식의 휴대폰 번호입니다."),
    INCORRECT_FORMAT_PROFILE_IMG_URL(BAD_REQUEST, "1007", "잘못된 형식의 프로필 이미지 주소입니다."),
    INCORRECT_FORMAT_USER_UUID(BAD_REQUEST, "1008", "잘못된 형식의 사용자 고유번호입니다."),

    AUTH_ERROR_EMAIL(UNAUTHORIZED, "1101", "잘못된 키 혹은 잘못(만료) 된 인증 코드입니다."),
    VALIDATE_ERROR_EMAIL(UNAUTHORIZED, "1102", "인증이 만료되었거나 인증되지 않은 이메일입니다."),
    BANNED_USER(UNAUTHORIZED, "1103", "경고 3회 누적으로 정지된 사용자입니다."),

    USER_NOT_FOUND(NOT_FOUND, "1201", "존재하지 않는 사용자입니다."),

    DUPLICATE_EMAIL(CONFLICT, "1301", "이미 사용중인 이메일입니다."),
    DUPLICATE_NICKNAME(CONFLICT, "1302", "이미 사용중인 닉네임입니다."),
    WITHDRAWAL_ERROR_BY_AUCTION_IN_PROGRESS(CONFLICT, "1303", "현재 진행중인 경매가 존재할 경우 회원탈퇴가 불가능합니다."),
    WITHDRAWAL_ERROR_BY_TICKET_IN_PROGRESS(CONFLICT, "1304", "현재 종료되지 않은 식사권이 존재할 경우 회원탈퇴가 불가능합니다."),
    EXPIRED_SUBSCRIPTION(CONFLICT, "1305", "구독 전이거나 구독이 만료되었습니다."),

    MIN_BID_AMOUNT_INCORRECT_FORMAT(BAD_REQUEST, "2000", "최소입찰금액은 자신의 보유 포인트보다 적어야 합니다."),
    INCORRECT_FORMAT_MIN_BID_AMOUNT(BAD_REQUEST, "2001", "잘못된 형식의 최소 입찰 금액입니다."),
    INCORRECT_FORMAT_MEETING_DATE(BAD_REQUEST, "2002", "잘못된 형식의 식사 날짜입니다."),
    INCORRECT_FORMAT_MEETING_LOCATION(BAD_REQUEST, "2003", "잘못된 형식의 식사 장소입니다."),
    INCORRECT_FORMAT_MEETING_INFO_TEXT(BAD_REQUEST, "2004", "잘못된 형식의 만남에 대한 정보입니다."),
    INCORRECT_FORMAT_MEETING_PROMISE_TEXT(BAD_REQUEST, "2005", "잘못된 형식의 만남에 대한 약속입니다."),
    INCORRECT_FORMAT_AUCTION_UUID(BAD_REQUEST, "2006", "잘못된 형식의 경매 고유번호입니다."),
    INCORRECT_FORMAT_PRICE(BAD_REQUEST, "2007", "잘못된 형식의 입찰 금액입니다."),
    INCORRECT_FORMAT_AUCTION_REPORT_CONTENT(BAD_REQUEST, "2008", "잘못된 형식의 경매 신고 내용입니다."),

    AUCTION_ACCESS_DENIED(FORBIDDEN, "2100", "접근 권한이 없습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "2200", "존재하지 않는 경매입니다."),
    AUCTION_CONFLICT(CONFLICT, "2300", "이미 처리된 경매입니다."),

    EXCHANGE_AMOUNT_INCORRECT_FORMAT(BAD_REQUEST, "3000", "환전 포인트는 보유 포인트보다 적어야 합니다."),
    PORTONE_SERVER_RESPONSE_ERROR(INTERNAL_SERVER_ERROR, "3001", "결제 서버 응답 오류가 발생했습니다."),
    PORTONE_SERVER_DISCONNECTED(INTERNAL_SERVER_ERROR, "3002", "결제 서버와 연결이 끊어졌습니다."),
    INCORRECT_FORMAT_EXCHANGE(BAD_REQUEST, "3003", "잘못된 형식의 환전 신청서 고유번호입니다."),

    PAYMENT_ACCESS_DENIED(FORBIDDEN, "3100", "잘못된 결제 요청입니다."),
    DIFFERENT_ACCOUNT_HOLDER(FORBIDDEN, "3101", "본인 명의의 계좌가 아닙니다."),
    PORTONE_AUTHENTICATION_ERROR(UNAUTHORIZED, "3102", "포트원 ACCESS 토큰 발급에 실패했습니다."),
    PORTONE_PAYMENT_NOT_FOUND(NOT_FOUND, "3200", "해당하는 거래내역이 존재하지 않습니다."),
    EXCHANGE_NOT_FOUND(NOT_FOUND, "3201", "존재하지 않는 환전 신청서입니다."),

    EXCHANGE_ALREADY_HANDLED(CONFLICT, "3300", "이미 승인 또는 반려된 환전 신청서입니다."),

    SCHEDULING_SERVER_ERROR(INTERNAL_SERVER_ERROR, "4000", "알 수 없는 오류가 발생했습니다."),
    TASK_NOT_FOUND(NOT_FOUND, "4200", "존재하지 않는 task입니다."),
    FAIL_TASK_DELETE(NOT_FOUND, "4201", "스케줄링된 task를 취소하는데 실패했습니다."),

    INCORRECT_FORMAT_REVIEW_UUID(BAD_REQUEST, "5001", "잘못된 형식의 리뷰 고유번호입니다."),

    REVIEW_NOT_FOUND(NOT_FOUND, "5201","존재하지 않는 리뷰입니다."),

    REVIEW_ALREADY_HIDDEN(CONFLICT, "5300", "이미 숨겨진 리뷰입니다."),

    INCORRECT_FORMAT_TICKET_UUID(BAD_REQUEST, "6001", "잘못된 형식의 식사권 UUID입니다."),
    INCORRECT_FORMAT_START_MEETING_TIME(BAD_REQUEST, "6002", "잘못된 형식의 만남 시작 시간입니다."),
    INCORRECT_FORMAT_TICKET_REPORT_CONTENT(BAD_REQUEST, "6003", "잘못된 형식의 식사권 신고 내용입니다."),
    INCORRECT_FORMAT_TICKET_REPORT_EVIDENCE_URL(BAD_REQUEST, "6004", "잘못된 형식의 식사권 신고 증거 URL입니다."),
    INCORRECT_FORMAT_REVIEW_CONTENT(BAD_REQUEST, "6005", "잘못된 형식의 리뷰 내용입니다."),
    INCORRECT_FORMAT_REVIEW_RATING(BAD_REQUEST, "6006", "잘못된 형식의 리뷰 점수입니다."),
    MEETING_TIME_NOT_PASSED(BAD_REQUEST, "6007", "만남이 끝나기 전에 리뷰 작성을 할 수 없습니다."),

    TICKET_ACCESS_DENIED(FORBIDDEN, "6100", "접근 권한이 없습니다."),

    TICKET_NOT_FOUND(NOT_FOUND, "6200", "존재하지 않는 식사권입니다."),
    TICKET_MEETING_TIME_NOT_FOUND(NOT_FOUND, "6201", "존재하지 않는 만남 시작 시간입니다."),

    TICKET_MEETING_TIME_CONFLICT(CONFLICT, "6300", "이미 처리된 식사권 만남 시간입니다."),
    TICKET_REPORT_CONFLICT(CONFLICT, "6301", "이미 신고된 식사권입니다."),
    TICKET_CANCEL_CONFLICT(CONFLICT, "6302", "식사 시간이 지난 후에는 취소가 불가능합니다."),
    ALREADY_TICKET_CANCEL_CONFLICT(CONFLICT, "6303", "이미 취소된 식사권입니다."),
    REVIEW_CONFLICT(CONFLICT, "6304", "해당 식사권에 대한 리뷰를 작성한 적이 있습니다."),

    INCORRECT_FORMAT_VIP_INFO_UUID(BAD_REQUEST, "7001", "잘못된 형식의 VIP 정보 고유번호입니다."),
    VIP_AUCTION_SUCCESS_ACCESS_DENIED(FORBIDDEN,"7100", "잘못된 VIP 옥션 조회 경로입니다."),
    VIP_INFO_NOT_FOUND(NOT_FOUND,"7200", "존재하지 않는 VIP 정보입니다."),
    VIP_INFO_DUPLICATE(CONFLICT,"7300","중복된 VIP신청입니다."),
    VIP_INFO_ALREADY_HANDLED(CONFLICT, "7301", "이미 승인 또는 반려된 VIP 정보입니다."),

    INCORRECT_FORMAT_CHAT_ROOM_UUID(BAD_REQUEST, "8001", "잘못된 형식의 채팅방 UUID입니다."),
    INCORRECT_FORMAT_CHAT_REPORT_CONTENT(BAD_REQUEST, "8002", "잘못된 형식의 채팅방 신고 내용입니다."),
    INCORRECT_FORMAT_CHAT_MESSAGE(BAD_REQUEST, "8003", "잘못된 형식의 채팅 메세지입니다."),

    CHAT_ROOM_ACCESS_DENIED(FORBIDDEN, "8100", "접근 권한이 없습니다."),
    CHAT_MESSAGE_ACCESS_DENIED(FORBIDDEN, "8101", "접근 권한이 없습니다."),

    CHAT_ROOM_NOT_FOUND(NOT_FOUND, "8200", "존재하지 않는 채팅방입니다."),

    INCORRECT_FORMAT_ADMIN_ID(BAD_REQUEST, "9001", "잘못된 형식의 관리자 ID입니다."),
    INCORRECT_FORMAT_ADMIN_PASSWORD(BAD_REQUEST, "9002", "잘못된 형식의 관리자 비밀번호 입니다."),
    INCORRECT_FORMAT_ADMIN_NICKNAME(BAD_REQUEST, "9003", "잘못된 형식의 관리자 닉네임입니다."),
    INCORRECT_FORMAT_ADMIN_SECRET_KEY(BAD_REQUEST, "9004", "잘못된 형식의 관리자 비밀 키 입니다."),
    INCORRECT_FORMAT_REVIEW_REPORT_UUID(BAD_REQUEST, "9005", "잘못된 형식의 리뷰 신고 고유번호입니다."),
    INCORRECT_FORMAT_CHAT_REPORT_UUID(BAD_REQUEST, "9006", "잘못된 형식의 채팅 신고 고유번호입니다."),
    INCORRECT_FORMAT_TICKET_REPORT_UUID(BAD_REQUEST, "9007", "잘못된 형식의 식사권 신고 고유번호입니다."),


    ADMIN_LOGIN_FAILED(FORBIDDEN, "9101", "잘못된 관라자 아이디 혹은 패스워드입니다."),

    REVIEW_REPORT_NOT_FOUND(NOT_FOUND, "9200", "존재하지 않는 리뷰 신고입니다."),
    CHAT_REPORT_NOT_FOUND(NOT_FOUND, "9201", "존재하지 않는 채팅 신고입니다."),
    TICKET_REPORT_NOT_FOUND(NOT_FOUND, "9202", "존재하지 않는 식사권 신고입니다."),
    BANNER_NOT_FOUND(NOT_FOUND, "9203", "존재하지 않는 배너입니다."),

    DUPLICATE_ADMIN_ID(CONFLICT, "9301", "해당 관리자 아이디는 사용할 수 없습니다."),
    DUPLICATE_ADMIN_NICKNAME(CONFLICT, "9302", "해당 관리자 닉네임은 사용할 수 없습니다."),
    INCORRECT_VALUE_ADMIN_SECRET_KEY(CONFLICT, "9303", "잘못된 관리자 비밀 키 입니다."),
    ALREADY_BANNED_USER(CONFLICT, "9304", "이미 정지된 사용자입니다."),
    REVIEW_REPORT_ALREADY_HANDLED(CONFLICT, "9305", "이미 승인 혹은 반려된 리뷰 신고입니다."),
    CHAT_REPORT_ALREADY_HANDLED(CONFLICT, "9306", "이미 승인 혹은 반려된 채팅 신고입니다."),
    TICKET_REPORT_ALREADY_HANDLED(CONFLICT, "9307", "이미 승인 혹은 반려된 식사권 신고입니다."),

    WALLET_NOT_FOUND(NOT_FOUND, "10200", "지갑을 찾을 수 없습니다."),

    INCORRECT_FORMAT_JSON(BAD_REQUEST, "11000", "잘못된 형식의 JSON 데이터 입니다."),
    INCORRECT_FORMAT_IS_BID_MESSAGE(BAD_REQUEST, "11001", "잘못된 형식의 isBidMessage 입니다."),
    INCORRECT_FORMAT_IS_CHAT_MESSAGE(BAD_REQUEST, "11002", "잘못된 형식의 isChatMessage 입니다."),

    S3_EMPTY_IMAGE(BAD_REQUEST, "12000", "빈 이미지 파일입니다."),
    S3_UPLOAD_FAILED(INTERNAL_SERVER_ERROR, "12001", "이미지 업로드에 실패하였습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String description;

    ErrorCode(HttpStatus httpStatus, String code, String description) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.description = description;
    }
}
