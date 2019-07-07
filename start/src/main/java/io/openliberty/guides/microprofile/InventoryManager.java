package io.openliberty.guides.microprofile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import io.openliberty.guides.microprofile.util.ReadyJson;
import io.openliberty.guides.microprofile.util.InventoryUtil;

@ApplicationScoped
public class InventoryManager {

    private ConcurrentMap<String, JsonObject> inv = new ConcurrentHashMap<>();

    public JsonObject get(String hostname) {
        JsonObject properties = inv.get(hostname);
        if (properties == null) {
            if (InventoryUtil.responseOk(hostname)) {
                properties = InventoryUtil.getProperties(hostname);
                this.add(hostname, properties);
            } else {
                return ReadyJson.SERVICE_UNREACHABLE.getJson();
            }
        }
        return properties;
    }

    public void add(String hostname, JsonObject systemProps) {
        inv.putIfAbsent(hostname, systemProps);
    }

    public JsonObject list() {
        JsonObjectBuilder systems = Json.createObjectBuilder();
        inv.forEach((host, props) -> {
            JsonObject systemProps = Json.createObjectBuilder()
                                         .add("os.name", props.getString("os.name"))
                                         .add("user.name", props.getString("user.name"))
                                         .build();
            systems.add(host, systemProps);
        });
        systems.add("hosts", systems);
        systems.add("total", inv.size());
        return systems.build();
    }

    public JsonArray getSystems(String url) {
        // inventory content
        JsonObject content = InventoryUtil.buildHostJson("*", url);

        // collecting systems jsons
        JsonArrayBuilder jsonArray = inv.keySet().stream().map(host -> {
            return InventoryUtil.buildHostJson(host, url);
        }).collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add);

        jsonArray.add(content);

        return jsonArray.build();
    }
}