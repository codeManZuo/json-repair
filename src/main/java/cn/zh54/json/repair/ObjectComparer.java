package cn.zh54.json.repair;

import java.util.List;
import java.util.Map;

/**
 * @author 猿大晖@54zh.cn
 * @date 2025/03/25
 */
public class ObjectComparer {
    public static boolean isSameObject(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null) {
            return obj1 == obj2;
        }

        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }

        if (obj1 instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map1 = (Map<String, Object>) obj1;
            @SuppressWarnings("unchecked")
            Map<String, Object> map2 = (Map<String, Object>) obj2;

            if (map1.size() != map2.size()) {
                return false;
            }

            for (Map.Entry<String, Object> entry : map1.entrySet()) {
                if (!map2.containsKey(entry.getKey()) ||
                    !isSameObject(entry.getValue(), map2.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        }

        if (obj1 instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list1 = (List<Object>) obj1;
            @SuppressWarnings("unchecked")
            List<Object> list2 = (List<Object>) obj2;

            if (list1.size() != list2.size()) {
                return false;
            }

            for (int i = 0; i < list1.size(); i++) {
                if (!isSameObject(list1.get(i), list2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        return obj1.equals(obj2);
    }
}
