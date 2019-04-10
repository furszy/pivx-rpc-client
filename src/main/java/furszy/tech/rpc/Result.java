package furszy.tech.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigInteger;

public class Result {

    private JsonObject jsonObject;

    public Result(String json) {
        this.jsonObject = new JsonParser().parse(json).getAsJsonObject();
    }

    public String getString(String key){
        return jsonObject.get(key).getAsString();
    }

    public int getInt(String key){
        return jsonObject.get(key).getAsInt();
    }

    public JsonArray getJsonArray(String key) {
        return jsonObject.get(key).getAsJsonArray();
    }

    public BigInteger getBigInteger(String key){
        return jsonObject.get(key).getAsBigInteger();
    }

    public boolean getBool(String key){
        return jsonObject.get(key).getAsBoolean();
    }

    public long getLong(String key){
        return jsonObject.get(key).getAsLong();
    }

    public boolean isError(){
        return jsonObject.has("code") && jsonObject.get("code").getAsInt() < 0;
    }

    public String getErrorMessage(){
        return jsonObject.get("message").getAsString();
    }

    public int getErrorCode(){
        return jsonObject.get("code").getAsInt();
    }


    @Override
    public String toString() {
        return jsonObject.toString();
    }


}
