package app.cryptotip.cryptotip.app.json;

import com.gani.lib.json.GJsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyJsonArray extends GJsonArray<MyJsonObject> {

    public MyJsonArray() {
        super(new JSONArray());
    }

    MyJsonArray(JSONArray backend) {
        super(backend);
    }

    public MyJsonArray(MyJsonObject[] elements) throws JSONException {
        super(elements);
    }

    public MyJsonArray(String elements) throws JSONException{
        this(new JSONArray(elements));
    }

    @Override
    protected MyJsonObject[] createArray(int length) {
        return new MyJsonObject[length];
    }

    @Override
    protected MyJsonObject createObject(JSONObject object) {
        return new MyJsonObject(object);
    }
}
