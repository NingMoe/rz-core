package com.rz.core.mongo.repository;

import com.mongodb.MongoException;
import com.rz.core.Assert;
import com.rz.core.mongo.annotation.MongoId;
import com.rz.core.mongo.annotation.MongoIndex;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by Runky on 7/16/2017.
 */
public class PoFieldDefinition<T> {
    public static final String ID_FIELD_NAME = "id";
    public static final String MONGO_ID_FIELD_NAME = "_id";

    private Class<T> clazz;
    private Field field;
    private String name;
    private Method setterMethod;
    private Method getterMethod;
    private boolean isId;
    private boolean isIndex;
    private boolean canSet;
    private boolean canGet;

    public Class<T> getClazz() {
        return clazz;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isIndex() {
        return isIndex;
    }

    public boolean isCanSet() {
        return canSet;
    }

    public boolean isCanGet() {
        return canGet;
    }

    public PoFieldDefinition(Class<T> clazz, Field field) {
        Assert.isNotNull(clazz, "clazz");
        Assert.isNotNull(field, "field");

        this.clazz = clazz;
        this.field = field;
        this.name = field.getName();
        this.isId = false;
        this.isIndex = false;
        this.canSet = false;
        this.canGet = false;

        try {
            this.setterMethod = new PropertyDescriptor(field.getName(), clazz).getWriteMethod();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        try {
            this.getterMethod = new PropertyDescriptor(field.getName(), clazz).getReadMethod();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (MongoId.class == annotation.annotationType()
                    || PoFieldDefinition.ID_FIELD_NAME.equals(this.name)
                    || PoFieldDefinition.MONGO_ID_FIELD_NAME.equals(this.name)) {
                this.isId = true;
                this.isIndex = true;
            } else if (MongoIndex.class == annotation.annotationType()) {
                this.isIndex = true;
            }
        }

        if (null != this.setterMethod && Modifier.isPublic(this.setterMethod.getModifiers())) {
            this.canSet = true;
        }
        if (null != this.getterMethod && Modifier.isPublic(this.getterMethod.getModifiers())) {
            this.canGet = true;
        }
        if (Modifier.isPublic(this.field.getModifiers())) {
            this.canSet = true;
            this.canGet = true;
        }
    }

    public Object getValue(Object instance) throws InvocationTargetException, IllegalAccessException {
        Assert.isNotNull(instance, "instance");

        if (this.canGet) {
            if (null == this.getterMethod) {
                return this.field.get(instance);
            } else {
                return this.getterMethod.invoke(instance);
            }
        } else {
            throw new MongoException(String.format("The field [%s] cannot get value.", this.name));
        }
    }
}