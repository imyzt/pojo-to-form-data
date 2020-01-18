package top.imyzt.plugins;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author imyzt
 * @date 2020/01/17
 * @description 时间日期枚举
 */
public enum DateTimeEnum {

    /**
     * 日期
     */
    DATE(){
        @Override
        public String format(LocalDateTime localDateTime) {
            return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    },
    /**
     * 时间
     */
    TIME(){
        @Override
        public String format(LocalDateTime localDateTime) {
            return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    },
    /**
     * 日期时间
     */
    DATE_TIME(){
        @Override
        public String format(LocalDateTime localDateTime) {
            return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }


    ;

    public String format(LocalDateTime localDateTime) {
        return null;
    }
}

