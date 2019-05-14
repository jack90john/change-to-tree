import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dto.DataTest;
import dto.FormatData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Designer: jack
 * Date: 2019-05-09
 * Version: 1.0.0
 */
public class ChangeToTree {

    public static void main(String args[]) {
        //模拟地址
        List<DataTest> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            DataTest dataTest = new DataTest();
            dataTest.setId(String.valueOf(i));
            dataTest.setTestData("testData" + i);
            if (i == 0) {
                dataTest.setParentId("0000000000000000000000");
            } else if (i < 6) {
                dataTest.setParentId("0");
            } else if (i < 10) {
                dataTest.setParentId("1");
            } else if (i == 10) {
                dataTest.setParentId("0000000000000000000000");
            } else if (i < 16) {
                dataTest.setParentId("10");
            } else {
                dataTest.setParentId("11");
            }
            list.add(dataTest);
        }
        ChangeToTree test = new ChangeToTree();
        List<FormatData> formatData = test.formatData(list, "id", "parentId", "testData");
        System.out.println(test.changeToTree(formatData));
    }

    public static String getMethodNameUtil(String filedName) {
        return "get" + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
    }


    @SuppressWarnings("unchecked")
    public String changeToTree(List<FormatData> formatDataList) {
        //取出每个地址Id
        Map<String, FormatData> ids = formatDataList.parallelStream()
                .collect(Collectors.groupingBy(FormatData::getId,
                        Collectors.collectingAndThen(Collectors.toList(), value -> value.get(0))));
        List<FormatData> result = ids.entrySet().parallelStream()
                .map(entry -> {
                    FormatData entryValue = entry.getValue();
                    if (ids.containsKey(entryValue.getParentId())) {
                        FormatData parent = ids.get(entryValue.getParentId());
                        if (parent.getChildren() == null) {
                            parent.setChildren(new ArrayList<>(Collections.singletonList(entryValue)));
                        } else {
                            parent.getChildren().add(entryValue);
                        }
                        return null;
                    } else {
                        return entryValue;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        return JSONArray.parseArray(JSONObject.toJSONString(result)).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public <T> List<FormatData> formatData(List<T> tList, String idName, String parentIdName, String... dataNames) {
        return tList.stream()
                .map(t -> {
                    FormatData formatData = new FormatData();
                    try {
                        String idMethodName = getMethodNameUtil(idName);
                        Method idMethod = t.getClass().getDeclaredMethod(idMethodName);
                        String parentIdMethodName = getMethodNameUtil(parentIdName);
                        Method parentIdMethod = t.getClass().getDeclaredMethod(parentIdMethodName);
                        formatData.setId(idMethod.invoke(t).toString());
                        formatData.setParentId(parentIdMethod.invoke(t).toString());
                        if (dataNames.length > 1) {
                            Map<String, Object> data = Arrays.stream(dataNames)
                                    .collect(Collectors.toMap(dataName -> dataName, dataName -> {
                                        try {
                                            String dataMethodName = getMethodNameUtil(dataName);
                                            Method dataMethod = t.getClass().getDeclaredMethod(dataMethodName);
                                            return dataMethod.invoke(t);
                                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                        return new Object();
                                    }));
                            formatData.setData(data);
                        } else {
                            String dataMethodName = getMethodNameUtil(dataNames[0]);
                            Method dataMethod = t.getClass().getDeclaredMethod(dataMethodName);
                            formatData.setData(dataMethod.invoke(t));
                        }

                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return formatData;
                })
                .collect(Collectors.toList());
    }
}