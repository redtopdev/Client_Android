package com.redtop.engaze.common.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonParser {

    private Gson gson;

    public JsonParser() {

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    public <TObject> TObject deserialize(String jsonStr, Class<TObject> classT) {
        return gson.fromJson(jsonStr, classT);
    }

    public  <TObject> String Serialize(TObject object){
        return  gson.toJson(object);
    }
}
