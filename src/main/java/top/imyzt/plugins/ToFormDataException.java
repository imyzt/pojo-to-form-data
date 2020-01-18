package top.imyzt.plugins;

/**
 * @author imyzt
 * @date 2020/01/17
 * @description 描述信息
 */
public class ToFormDataException extends RuntimeException {

    private final String msg;

    public ToFormDataException(String message) {
        super(message);
        this.msg = message;
    }

    public String getMsg() {
        return msg;
    }
}
