package vip.liuw.mdview.framework.response;


public enum ResCode {
    SUCCESS(0, "成功"),
    FAIL(1, "失败"),
    TOKEN_INVALID(2, "token失效或未登录"),
    PERMISSION_DENIED(3, "没有权限"),
    PARAM_INVALID(4, "参数验证失败"),
    SERVER_ERROR(5, "服务器处理异常，请反馈"),
    SERVER_BLOCKING(6, "服务器拥堵，请稍后再试"),
    ;

    public int code;
    public String message;

    ResCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Res res() {
        return Res.ok().code(this);
    }

}
