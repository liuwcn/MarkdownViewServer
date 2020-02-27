package vip.liuw.mdview.framework.response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Getter
@Setter
public class Res implements Serializable {

    @JSONField(serialize = false, deserialize = false)
    @JsonIgnore
    private ResCode resCode;

    private int code;

    private String message;

    private Object data;

    private Res() {
    }

    public static Res ok() {
        return new Res();
    }

    public static Res ok(String message) {
        return ok().message(message);
    }

    public static Res ok(Object data) {
        try {
            return ok().data(data).message("处理成功");
        }catch (Exception e){
            return error("系统错误!");
        }
    }

    public static Res error(ResCode resCode) {
        return ok().code(resCode);
    }

    public static Res error(String message) {
        return ok().code(ResCode.FAIL).message(message);
    }

    public static Res error(Object data) {
        return ok().code(ResCode.FAIL).data(data);
    }

    public Res code(ResCode resCode) {
        this.resCode = resCode;
        this.code = resCode.code;
        if (resCode != ResCode.SUCCESS) {
            this.message = resCode.message;
        }
        return this;
    }

    public Res message(String message) {
        this.message = message;
        return this;
    }

    public Res data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 为data添加元素, data类型为Map
     */
    public Res data(String key, Object value) {
        initMap();
        ((Map) this.data).put(key, value);
        return this;
    }

    /**
     * 为data添加元素, data类型为Map
     */
    public Res data(Map<String, Object> map) {
        initMap();
        ((Map) this.data).putAll(map);
        return this;
    }

    /**
     * 初始化data为Map,要求data未被初始化为非Map对象
     */
    private void initMap() {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }
        Assert.isTrue(this.data instanceof Map, "data已经被初始化为非Map对象");
    }

    public <R> Res then(Predicate<Object> predicate, Function<Res, R> function) {
        return then(this.data, predicate, function);
    }

    /**
     * 当条件成立时,执行function
     */
    public <R> Res then(Object obj, Predicate<Object> predicate, Function<Res, R> function) {
        if (predicate.test(obj)) {
            this.resCode = null;
            this.data = null;
            this.message = null;
            function.apply(this);
        }
        return this;
    }

    public String toJsonString(SerializerFeature... features) {
        return JSON.toJSONString(this, features);
    }

}
