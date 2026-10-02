package com.tenco.spring_blog.util;

// 날짜 시간 관련된 유틸리티 클래스
//static 메서드로 구성하여 객체 생성 없이 바로 사용 가능

import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Date;
import java.sql.Timestamp;

public class MyDateUtil {

    // Timestamp를 원하는 포맷의 문자열로 변환
    public static String timestampFormat(Timestamp timestamp) {
        Date currentDate = new Date(timestamp.getTime());

        return DateFormatUtils.format(currentDate, "yyyy-MM-dd HH:mm");
    }
}
