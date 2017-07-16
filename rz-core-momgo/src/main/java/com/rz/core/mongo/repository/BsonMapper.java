package com.rz.core.mongo.repository;

import com.rz.core.Assert;
import com.rz.core.RZHelper;
import com.rz.core.mongo.builder.MongoSort;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by renjie.zhang on 7/13/2017.
 */
class BsonMapper {
    public static <T> T toObject(Document document, Class<T> clazz) {
        if (null == document) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        PoDefinition<T> poDefinition = SourcePool.getPoDefinition(clazz);

        if (document.containsKey(PoFieldDefinition.MONGO_ID_FIELD_NAME) && !document.containsKey(poDefinition.getIdField().getItem1())) {
            document.put(poDefinition.getIdField().getItem1(), document.get("_id"));
        }
        String json = com.mongodb.util.JSON.serialize(document);

        return com.alibaba.fastjson.JSON.parseObject(json, clazz);
    }

    public static <T> List<T> toObject(Iterator<Document> documents, Class<T> clazz) {
        if (null == documents) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        List<T> pos = new ArrayList<>();
        while (documents.hasNext()) {
            T po = BsonMapper.toObject(documents.next(), clazz);
            if (null != po) {
                pos.add(po);
            }
        }

        return pos;
    }

    public static <T> Map toMap(Document document, Class<T> clazz, String[] fieldNames) {
        Map<String, Object> map = new HashMap<>();
        if (RZHelper.isEmptyCollection(fieldNames)) {
            return map;
        }

        T po = BsonMapper.toObject(document, clazz);

        PoDefinition<T> poDefinition = SourcePool.getPoDefinition(clazz);
        for (String fieldName : fieldNames) {
            if (poDefinition.containsField(fieldName)) {
                PoFieldDefinition<T> poFieldDefinition = poDefinition.getField(fieldName);
                if (poFieldDefinition.isCanGet()) {
                    try {
                        map.put(fieldName, poFieldDefinition.getValue(po));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return map;
    }

    public static <T> List<Map> toMap(Iterator<Document> documents, Class<T> clazz, String[] fieldNames) {
        List<Map> maps = new ArrayList<>();
        if (RZHelper.isEmptyCollection(fieldNames)) {
            return maps;
        }
        if (null == documents) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        while (documents.hasNext()) {
            Map map = BsonMapper.toMap(documents.next(), clazz, fieldNames);
            if (null != map) {
                maps.add(map);
            }
        }

        return maps;
    }

    public static <T> Document toDocument(T po) {
        if (null == po) {
            return null;
        }

        String json = com.alibaba.fastjson.JSON.toJSONString(po);
        Document document = Document.parse(json);

        return BsonMapper.formatId(document, po.getClass());
    }

    public static <T> List<Document> toDocument(List<T> pos) {
        if (null == pos) {
            return null;
        }

        List<Document> documents = new ArrayList<>();
        for (Object po : pos) {
            Document document = BsonMapper.toDocument(po);
            if (null != document) {
                documents.add(document);
            }
        }

        return documents;
    }

    public static <T> Document toDocument(Map<String, Object> values, Class<T> clazz) {
        Assert.isNotNull(clazz, "clazz");
        if (null == values) {
            return null;
        } else if (values.isEmpty()) {
            return new Document();
        }

        Document document = new Document(values);
        PoDefinition poDefinition = SourcePool.getPoDefinition(clazz);
        String idFieldName = (String) poDefinition.getIdField().getItem1();
        document.remove(idFieldName);
        List<String> removedFieldNames = values.keySet().stream().filter(o -> !poDefinition.containsField(o)).collect(Collectors.toList());
        for (String removedFieldName : removedFieldNames) {
            document.remove(removedFieldName);
        }

        return document;
    }

    public static <T> Document formatId(Document document, Class<T> clazz) {
        if (null == document) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        PoDefinition<T> poDefinition = SourcePool.getPoDefinition(clazz);

        if (document.containsKey(poDefinition.getIdField().getItem1()) && !PoFieldDefinition.MONGO_ID_FIELD_NAME.equals(poDefinition.getIdField().getItem1())) {
            document.put(PoFieldDefinition.MONGO_ID_FIELD_NAME, document.get(poDefinition.getIdField().getItem1()));
            document.remove(poDefinition.getIdField().getItem1());
        }

        return document;
    }

    public static <T> String[] formatId(String[] feildNames, Class<T> clazz) {
        if (null == feildNames) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        PoDefinition<T> poDefinition = SourcePool.getPoDefinition(clazz);

        Set<String> set = new HashSet<>(Arrays.asList(feildNames));
        if (set.contains(poDefinition.getIdField().getItem1()) && !PoFieldDefinition.MONGO_ID_FIELD_NAME.equals(poDefinition.getIdField().getItem1())) {
            set.add(PoFieldDefinition.MONGO_ID_FIELD_NAME);
            set.remove(poDefinition.getIdField().getItem1());
        }

        return set.toArray(new String[set.size()]);
    }

    public static <T> List<MongoSort> formatId(List<MongoSort> mongoSorts, Class<T> clazz) {
        if (null == mongoSorts) {
            return null;
        }
        Assert.isNotNull(clazz, "clazz");

        PoDefinition<T> poDefinition = SourcePool.getPoDefinition(clazz);

        for (MongoSort mongoSort : mongoSorts) {
            if (null != mongoSort && !StringUtils.isBlank(mongoSort.getFeildName())) {
                if (mongoSort.getFeildName().equals(poDefinition.getIdField().getItem1())
                        && !PoFieldDefinition.MONGO_ID_FIELD_NAME.equals(poDefinition.getIdField().getItem1())) {
                    mongoSort.setFeildName(PoFieldDefinition.MONGO_ID_FIELD_NAME);
                }
            }
        }

        return mongoSorts;
    }
}