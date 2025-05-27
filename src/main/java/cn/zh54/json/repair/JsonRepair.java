package cn.zh54.json.repair;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * @author 猿大晖@54zh.cn
 * @date 2025/03/25
 */
public class JsonRepair {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 修复异常的 JSON 字符串
     * @param jsonStr 需要修复的 JSON 字符串
     * @param returnObjects 是否返回解析后的对象而不是字符串
     * @param skipJsonLoads 是否跳过使用标准 JSON 解析器的验证
     * @param logging 是否记录修复过程的日志
     * @param ensureAscii 是否确保输出的字符串只包含 ASCII 字符
     * @param streamStable 是否保持流式 JSON 的稳定性
     * @return 修复后的 JSON 字符串或对象
     */
    @SuppressWarnings("unchecked")
    public static Object repairJson(
            String jsonStr,
            boolean returnObjects,
            boolean skipJsonLoads,
            boolean logging,
            boolean ensureAscii,
            boolean streamStable
    ) {
        JsonParser parser = new JsonParser(jsonStr, null, logging, 0, streamStable);
        Object parsedJson;

        if (skipJsonLoads) {
            parsedJson = parser.parse();
        } else {
            try {
                parsedJson = objectMapper.readValue(jsonStr, Object.class);
            } catch (IOException e) {
                parsedJson = parser.parse();
            }
        }

        // 如果需要返回对象或需要日志，直接返回解析结果
        if (returnObjects || logging) {
            return parsedJson;
        }

        // 如果解析结果是空字符串，直接返回空字符串
        if ("".equals(parsedJson)) {
            return "";
        }

        try {
            return objectMapper.writeValueAsString(parsedJson);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    /**
     * 修复异常的 JSON 字符串（使用默认参数）
     * @param jsonStr 需要修复的 JSON 字符串
     * @return 修复后的 JSON 字符串
     */
    public static String repairJson(String jsonStr) {
        return (String) repairJson(jsonStr, false, false, false, true, false);
    }

    /**
     * 加载并修复 JSON 字符串，返回解析后的对象
     * @param jsonStr 需要修复的 JSON 字符串
     * @param skipJsonLoads 是否跳过使用标准 JSON 解析器的验证
     * @param logging 是否记录修复过程的日志
     * @param streamStable 是否保持流式 JSON 的稳定性
     * @return 修复并解析后的对象
     */
    public static Object loads(
            String jsonStr,
            boolean skipJsonLoads,
            boolean logging,
            boolean streamStable
    ) {
        return repairJson(jsonStr, true, skipJsonLoads, logging, true, streamStable);
    }
}
