package vn.com.routex.hub.payment.service.infrastructure.persistence.utils;


import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateTimeUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public String now() {
        return OffsetDateTime.now(ZoneOffset.ofHours(7)).format(FORMATTER);
    }
}
