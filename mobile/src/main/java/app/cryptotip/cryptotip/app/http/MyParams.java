package app.cryptotip.cryptotip.app.http;

import com.gani.lib.http.GParams;

import java.util.Map;

public class MyParams extends GParams<MyParams, MyImmutableParams> {
    public MyParams() {
        super();
    }

    MyParams(Map<String, Object> initialData) {
        super(initialData);
    }

    @Override
    protected MyImmutableParams createImmutable(Map<String, Object> paramMap) {
        return new MyImmutableParams(paramMap);
    }

    public static MyParams create() {
        return new MyParams(); //.put("_detail", "v1") redundant when  url parsed from json
    }

    @Override
    public MyParams copy() {
        return new MyParams(getMap());
    }
}
