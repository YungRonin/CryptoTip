package app.cryptotip.cryptotip.app.http;


import com.gani.lib.http.GImmutableParams;

import java.util.Map;

public class MyImmutableParams extends GImmutableParams {
    public static final MyImmutableParams EMPTY = new MyImmutableParams();

    private MyImmutableParams() {
        super();
    }

    @Override
    public MyParams toMutable(){
        return new MyParams(getMap());
    }

    MyImmutableParams(Map<String, Object> initialData) {
        super(initialData);
    }
}
