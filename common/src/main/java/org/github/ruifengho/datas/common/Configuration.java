package org.github.ruifengho.datas.common;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class Configuration {

    private Object root;

    public Configuration(String json) {
        try {
            this.root = JSON.parse(json);
        } catch (Exception e) {
            log.error("解析json错误");
            throw e;
        }
    }

    public Configuration clone() {
        return Configuration.from(toJSONString(this.getInternal()));
    }

    public static Configuration from(File file) {
        try {
            String json = IoUtil.read(new FileInputStream(file), "utf-8");
            return Configuration.from(json);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static Configuration from(InputStream in) {
        try {
            String json = IoUtil.read(in, "utf-8");
            return Configuration.from(json);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static Configuration from(String json) {
        return new Configuration(json);
    }

    public Configuration getConfiguration(final String path) {
        Object object = this.findObject(path);
        if (null == object) {
            return null;
        }
        return Configuration.from(Configuration.toJSONString(object));
    }

    public Configuration merge(Configuration another, boolean forceMerge) {
        Set<String> keys = another.getKeys();
        for (String path : keys) {
            Object obj = findObject(path);
            if (forceMerge) {
                set(path, another.findObject(path));
            } else if (null == obj) {
                set(path, another.findObject(path));
            }
        }
        return this;
    }

    public void setList(String path, Collection<?> collection) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(collection);
        Assert.isTrue(isMapPath(path));
        String tmp = path + "[%d]";
        Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            this.set(String.format(tmp, index++), next);
        }
    }

    /**
     * 路径追加元素
     *
     * @param path
     * @param object
     */
    public void listAppend(String path, Object object) {
        Object value = findObject(path);
        if (null != value && value instanceof List) {
            List list = (List) value;
            int index = list.size();
            list = expandList(list, index + 1);
            this.set(path + "[" + index + "]", object);
        } else {
            setList(path, Arrays.asList(object));
        }
    }

    public String getString(String path) {
        Objects.requireNonNull(path);
        Object value = findObject(path);
        if (null != value) {
            return value.toString();
        }
        return null;
    }

    public Integer getInt(String path) {
        String str = getString(path);
        return Integer.valueOf(str);
    }

    public Character getChar(String path) {
        String str = getString(path);
        return str.charAt(0);
    }

    public Boolean getBool(String path) {
        String str = getString(path);
        if (StrUtil.equals("true", str, true)) {
            return true;
        }
        if (StrUtil.equals("false", str, true)) {
            return false;
        }
        throw new RuntimeException("非boolean数据");
    }

    public <T> T get(String path) {
        Objects.requireNonNull(path);
        Object value = findObject(path);
        if (null != value) {
            return (T) value;
        }
        return null;
    }


    /**
     * a.b[0].c-> a,b,[0],c
     *
     * @param path
     * @return
     */
    public Object findObject(String path) {
        Objects.requireNonNull(path);
        try {
            String[] paths = splitPath(path);
            Object target = this.root;
            for (String splitPath : paths) {
                if (isMapPath(splitPath)) {
                    target = findInMap(target, splitPath);
                } else {
                    target = findInList(target, splitPath);
                }
            }
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    private String[] splitPath(String path) {
        String[] paths = StrUtil.split(StrUtil.replace(path, "[", ".["), ".");
        return paths;
    }

    private Object findInList(Object target, String path) {
        Objects.requireNonNull(path);
        if (target == null) {
            return null;
        }
        Assert.isTrue(target instanceof List, "{}路径不匹配具体类型", path);
        List<Object> list = (List) target;
        Integer index = getIndex(path);
        return list.get(index);
    }

    private Integer getIndex(String path) {
        return Integer.parseInt(path.replace("[", "").replace("]", ""));
    }

    private Object findInMap(Object target, String path) {
        Objects.requireNonNull(path);
        if (target == null) {
            return null;
        }
        Map<String, Object> map = (Map) target;
        return map.get(path);
    }

    private boolean isMapPath(String splitPath) {
        return null != splitPath && !splitPath.contains("[");
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();
        recursiveGetKey(this.root, "", keys);
        return keys;
    }

    public void setObject(String path, Object value) {

        Object newRoot = recursiveSetObject(this.root, splitPath(path), 0, value);
        this.root = newRoot;

    }

    private Object recursiveSetObject(Object current, String[] splitPath, int currentIndex, Object value) {

        String path = splitPath[currentIndex];

        boolean isMapPath = isMapPath(path);

        if (isMapPath) {
            Map<String, Object> map = null;
            if (current instanceof Map) {
                map = (Map) current;
                boolean contains = map.containsKey(path);
                if (!contains) {
                    map.put(path, buildObject(splitPath, currentIndex + 1, value));
                } else {
                    current = map.get(path);
                    map.put(path, recursiveSetObject(current, splitPath, currentIndex + 1, value));
                }
            } else {
                map = new HashMap<>();
                map.put(path, buildObject(splitPath, currentIndex + 1, value));
            }
            return map;
        } else {
            Integer index = getIndex(path);
            List<Object> list = null;
            if (current instanceof List) {
                list = expandList((List) current, index + 1);
            } else {
                list = expandList(new ArrayList<>(), index + 1);
            }
            Object obj = list.get(index);
            if (null != obj) {
                list.set(index, recursiveSetObject(obj, splitPath, currentIndex + 1, value));
            } else {
                list.set(index, buildObject(splitPath, currentIndex + 1, value));
            }
            return list;
        }
    }

    private Object buildObject(String[] splitPath, int currentIndex, Object value) {
        boolean isEnd = splitPath.length == currentIndex;
        if (isEnd) {
            if (value instanceof Configuration) {
                return ((Configuration) value).getInternal();
            }
            return value;
        }

        String path = splitPath[currentIndex];

        if (isMapPath(path)) {
            Map<String, Object> map = new HashMap<>();
            map.put(path, buildObject(splitPath, currentIndex + 1, value));
            return map;
        } else {
            Integer index = getIndex(path);
            List<Object> list = expandList(new ArrayList<>(), index + 1);
            list.set(index, buildObject(splitPath, currentIndex + 1, value));
            return list;

        }

    }

    private List<Object> expandList(List<Object> list, Integer finalSize) {
        if (list.size() < finalSize) {
            Integer tries = finalSize - list.size();
            while (tries-- > 0) {
                list.add(null);
            }
        }
        return list;
    }


    public Object set(final String path, final Object object) {

        Object result = this.findObject(path);

        setObject(path, object);

        return result;
    }

    private void recursiveGetKey(Object current, String path, Set<String> keys) {
        boolean isRoot = (null == path || path.length() == 0);
        boolean isMap = current instanceof Map;
        boolean isList = current instanceof List;
        if (!(isMap || isList)) {
            keys.add(path);
            return;
        }

        if (isMap) {
            Map<String, Object> map = (Map) current;
            Set<String> subKeys = map.keySet();
            subKeys.forEach(key -> {
                if (isRoot) {
                    recursiveGetKey(map.get(key), key, keys);
                } else {
                    recursiveGetKey(map.get(key), path + "." + key, keys);
                }
            });
            return;
        }
        if (isList) {
            List list = (List) current;
            for (int i = 0; i < list.size(); i++) {
                recursiveGetKey(list.get(i), path + String.format("[%d]", i), keys);
            }
            return;
        }
    }

    private static String toJSONString(final Object object) {
        return JSON.toJSONString(object);
    }

    public String beautify() {
        return JSON.toJSONString(this.getInternal(),
                SerializerFeature.PrettyFormat);
    }

    public Object getInternal() {
        return this.root;
    }

    @Override
    public String toString() {
        return toJSONString(root);
    }

    public static void main(String[] args) {
        Configuration one = Configuration.from("{\"job\":{\"setting\":{\"speed\":{\"channel\":3},\"errorLimit\":{\"record\":0,\"percentage\":0.02}},\"content\":[{\"reader\":{\"name\":\"mysqlreader\",\"parameter\":{\"username\":\"root\",\"password\":\"root\",\"column\":[\"id\",\"name\"],\"splitPk\":\"db_id\",\"connection\":[{\"table\":[\"table\"],\"jdbcUrl\":[\"jdbc:mysql://127.0.0.1:3306/database\"]}]}},\"writer\":{\"name\":\"streamwriter\",\"parameter\":{\"print\":true}}}]}}");
//        System.out.println(from.getKeys());
//        System.out.println(from.getConfiguration("job.content[0]").beautify());


        Configuration two = Configuration.from("{\"job\":{\"setting\":{\"speed\":{\"channel\":3},\"errorLimit\":{\"record\":0,\"percentage\":0.02}},\"content\":[{\"reader\":{\"name\":\"mysqlreader\",\"parameter\":{\"username\":\"root\",\"password\":\"root\",\"column\":[\"user\"],\"splitPk\":\"db_id\",\"connection\":[{\"table\":[\"table\"],\"jdbcUrl\":[\"jdbc:mysql://127.0.0.1:3306/database\"]}]}},\"writer\":{\"name\":\"streamwriter\",\"parameter\":{\"print\":true}}}]}}");
        //      from.set("abc.a.b.c[0]", "123");
//        System.err.println(from.beautify());
//        from.set("abc.a.b.c", "123");
//        System.err.println(from.beautify());
//        from.set("abc.a.b.c[2].d", "123");
//        from.set("abc.a.b.c[3].d", "123");
        // from.set("abc.a.b", new ArrayList<>());
        //      from.set("abc.a.b.c[1]", 456L);
        //     from.set("abc.a.b.d[0].1", "123");
        // from.set("abc.a.b.c[0].e", null);
//        System.out.println(from.getKeys());
//        System.out.println(from.beautify());
//        List<Character> its = from.get("abc.a.b.c");
//        System.out.println(its);
//        long conf = from.get("abc.a.b.c[1]");
//        System.out.println(conf);
        //System.err.println(from.getConfiguration("abc.a.b").beautify());
        System.out.println(one.merge(two, true).beautify());

    }
}
