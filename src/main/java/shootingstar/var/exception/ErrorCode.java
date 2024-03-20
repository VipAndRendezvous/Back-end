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
    ILLEGAL_ACCESS_TOKEN(FORBIDDEN, "0105", "Claim이 빈 Access Token 입니다."),
    INVALID_REFRESH_TOKEN(FORBIDDEN, "0106", "잘못된 Refresh Token 입니다."),
    EXPIRED_REFRESH_TOKEN(FORBIDDEN, "0107", "만료된 Refresh Token 입니다."),
    UNSUPPORTED_REFRESH_TOKEN(FORBIDDEN, "0108", "지원하지 않는 Refresh Token 입니다."),
    ILLEGAL_REFRESH_TOKEN(FORBIDDEN, "0109", "Claim이 빈 Refresh Token 입니다."),

    KAKAO_AUTHENTICATION_ERROR(UNAUTHORIZED, "0110", "카카오로부터 ACCESS 토큰 획득에 실패했습니다."),
    KAKAO_CONNECT_FAILED_TOKEN_ENDPOINT(SERVICE_UNAVAILABLE, "0111", "카카오 토큰 엔드포인트와 통신에 실패하였습니다."),
    KAKAO_FAILED_GET_USERINFO_ERROR(UNAUTHORIZED, "0112", "카카오로부터 사용자 정보를 가져오지 못했습니다."),
    KAKAO_CONNECT_FAILED_USERINFO_ENDPOINT(UNAUTHORIZED, "0112", "카카오 사용자 정보 엔드포인트와 통신에 실패하였습니다."),


    NOT_FOUND_END_POINT(NOT_FOUND, "0200", "존재하지 않는 접근입니다."),

    INCORRECT_FORMAT_EMAIL(BAD_REQUEST, "1001", "잘못된 형식의 이메일입니다."),
    INCORRECT_FORMAT_CODE(BAD_REQUEST, "1002", "잘못된 형식의 인증코드입니다."),
    INCORRECT_FORMAT_NICKNAME(BAD_REQUEST, "1003", "잘못된 형식의 닉네임입니다."),

    AUTH_ERROR_EMAIL(UNAUTHORIZED, "1101", "잘못된 키 혹은 잘못(만료) 된 인증 코드입니다."),

    USER_NOT_FOUND(NOT_FOUND, "1201", "존재하지 않는 사용자입니다."), // 로그인을 제외한 사용자 확인에서 발생하는 오류
    AUCTION_ACCESS_DENIED(FORBIDDEN, "2100", "접근 권한이 없습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "2200", "존재하지 않는 경매입니다."),
    AUCTION_CONFLICT(CONFLICT, "2300", "이미 처리된 경매입니다."),
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
