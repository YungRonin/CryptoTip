package app.cryptotip.cryptotip.app.json;

import com.gani.lib.json.GJsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyJsonObject extends GJsonObject {

    public MyJsonObject() {
        super();
    }

    public MyJsonObject(String str) throws JSONException {
        super(str);
    }

    public MyJsonObject(JSONObject backend) {
        super(backend);
    }

    public MyJsonObject(GJsonObject backend) {
        super(backend);
    }

    @Override
    protected MyJsonObject createObject(JSONObject object) {
        return new MyJsonObject(object);
    }

    @Override
    protected MyJsonArray createArray(JSONArray array) {
        return new MyJsonArray(array);
    }
}
