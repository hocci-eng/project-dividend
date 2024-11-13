package zerobase.dividend.exception.impl;

import org.springframework.http.HttpStatus;
import zerobase.dividend.exception.AbstractException;

import java.net.http.HttpRequest;

public class NoDividendInfo extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.NO_CONTENT.value();
    }

    @Override
    public String getMessage() {
        return "배당금 정보가 없습니다.";
    }
}
