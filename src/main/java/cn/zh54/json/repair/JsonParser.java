package cn.zh54.json.repair;

import java.io.Reader;
import java.util.*;

/**
 * @author 猿大晖@54zh.cn
 * @date 2025/03/25
 */
public class JsonParser {
    private static final List<String> STRING_DELIMITERS = Arrays.asList("\"", "'");
    private static final Set<Character> NUMBER_CHARS = new HashSet<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.', 'e', 'E', '/', ','));

    private String jsonStr;
    private int index;
    private final JsonContext context;
    private final boolean logging;
    private final List<Map<String, String>> logger;
    private final boolean streamStable;

    public JsonParser(String jsonStr, Reader jsonFd, Boolean logging, int jsonFdChunkLength, boolean streamStable) {
        this.jsonStr = jsonStr;
        this.index = 0;
        this.context = new JsonContext();
        this.logging = logging != null && logging;
        this.logger = this.logging ? new ArrayList<>() : null;
        this.streamStable = streamStable;
    }

    public Object parse() {
        Object json = parseJson();
        if (index < jsonStr.length()) {
            log("The parser returned early, checking if there's more json elements");
            List<Object> jsonArray = new ArrayList<>();
            jsonArray.add(json);
            while (index < jsonStr.length()) {
                Object j = parseJson();
                if (!"".equals(j)) {
                    if (ObjectComparer.isSameObject(jsonArray.get(jsonArray.size() - 1), j)) {
                        jsonArray.remove(jsonArray.size() - 1);
                    }
                    jsonArray.add(j);
                }
            }
            if (jsonArray.size() == 1) {
                log("There were no more elements, returning the element without the array");
                json = jsonArray.get(0);
            } else {
                json = jsonArray;
            }
        }
        return logging ? Arrays.asList(json, logger) : json;
    }

    private Object parseJson() {
        while (true) {
            Character ch = getCharAt();
            if (ch == null) {
                return "";
            }
            if (ch == '{') {
                index++;
                return parseObject();
            }
            if (ch == '[') {
                index++;
                return parseArray();
            }
            if (context.getCurrent() == JsonContext.ContextValue.OBJECT_VALUE && ch == '}') {
                log("At the end of an object we found a key with missing value, skipping");
                return "";
            }
            if (!context.isEmpty() && (STRING_DELIMITERS.contains(String.valueOf(ch)) || Character.isLetter(ch))) {
                return parseString();
            }
            if (!context.isEmpty() && (Character.isDigit(ch) || ch == '-' || ch == '.')) {
                return parseNumber();
            }
            if (ch == '#' || ch == '/') {
                return parseComment();
            }
            index++;
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> obj = new LinkedHashMap<>();
        while (getCharAt() != null && getCharAt() != '}') {
            skipWhitespacesAt();

            if (getCharAt() != null && getCharAt() == ':') {
                log("While parsing an object we found a : before a key, ignoring");
                index++;
            }

            context.set(JsonContext.ContextValue.OBJECT_KEY);
            int rollbackIndex = index;

            String key = "";
            while (getCharAt() != null) {
                rollbackIndex = index;
                if (getCharAt() == '[' && key.isEmpty()) {
                    if (!obj.isEmpty()) {
                        String prevKey = new ArrayList<>(obj.keySet()).get(obj.size() - 1);
                        if (obj.get(prevKey) instanceof List) {
                            index++;
                            Object newArray = parseArray();
                            if (newArray instanceof List) {
                                ((List<Object>) obj.get(prevKey)).addAll((List<Object>) newArray);
                                skipWhitespacesAt();
                                if (getCharAt() != null && getCharAt() == ',') {
                                    index++;
                                }
                                skipWhitespacesAt();
                                continue;
                            }
                        }
                    }
                }
                key = String.valueOf(parseString());
                if (key.isEmpty()) {
                    skipWhitespacesAt();
                }
                if (!key.isEmpty() || (key.isEmpty() && (getCharAt() != null && (getCharAt() == ':' || getCharAt() == '}')))) {
                    break;
                }
            }

            if (context.getContext().contains(JsonContext.ContextValue.ARRAY) && obj.containsKey(key)) {
                log("While parsing an object we found a duplicate key, closing the object here and rolling back the index");
                index = rollbackIndex - 1;
                jsonStr = jsonStr.substring(0, index + 1) + "{" + jsonStr.substring(index + 1);
                break;
            }

            skipWhitespacesAt();

            if (getCharAt() == null || getCharAt() == '}') {
                continue;
            }

            if (getCharAt() != ':') {
                log("While parsing an object we missed a : after a key");
            }

            index++;
            context.reset();
            context.set(JsonContext.ContextValue.OBJECT_VALUE);
            Object value = parseJson();

            context.reset();
            obj.put(key, value);

            if (getCharAt() != null && (getCharAt() == ',' || getCharAt() == '\'' || getCharAt() == '"')) {
                index++;
            }

            skipWhitespacesAt();
        }
        index++;
        return obj;
    }

    private List<Object> parseArray() {
        List<Object> arr = new ArrayList<>();
        context.set(JsonContext.ContextValue.ARRAY);

        Character ch = getCharAt();
        while (ch != null && ch != ']' && ch != '}') {
            skipWhitespacesAt();
            Object value = "";

            if (STRING_DELIMITERS.contains(String.valueOf(ch))) {
                int i = 1;
                i = skipToCharacter(ch, i);
                i = skipWhitespacesAt(i, false);
                if (getCharAt(i) == ':') {
                    value = parseObject();
                } else {
                    value = parseString();
                }
            } else {
                value = parseJson();
            }

            if ("".equals(value)) {
                index++;
            } else if ("...".equals(value) && getCharAt(-1) == '.') {
                log("While parsing an array, found a stray '...'; ignoring it");
            } else {
                arr.add(value);
            }

            ch = getCharAt();
            while (ch != null && ch != ']' && (Character.isWhitespace(ch) || ch == ',')) {
                index++;
                ch = getCharAt();
            }
        }

        if (ch != null && ch != ']') {
            log("While parsing an array we missed the closing ], ignoring it");
        }

        index++;
        context.reset();
        return arr;
    }

    private Object parseString() {
        boolean missingQuotes = false;
        char lStringDelimiter = '"';
        char rStringDelimiter = '"';

        Character ch = getCharAt();
        if (ch == '#' || ch == '/') {
            return parseComment();
        }

        while (ch != null && !STRING_DELIMITERS.contains(String.valueOf(ch)) && !Character.isLetterOrDigit(ch)) {
            index++;
            ch = getCharAt();
        }

        if (ch == null) {
            return "";
        }

        if (ch == '\'') {
            lStringDelimiter = rStringDelimiter = '\'';
        } else if (ch == '"') {
            lStringDelimiter = rStringDelimiter = '"';
        } else if (Character.isLetterOrDigit(ch)) {
            if (Character.toLowerCase(ch) == 't' || Character.toLowerCase(ch) == 'f' || Character.toLowerCase(ch) == 'n') {
                Object value = parseBooleanOrNull();
                if (!"".equals(value)) {
                    return value;
                }
            }
            log("While parsing a string, we found a literal instead of a quote");
            missingQuotes = true;
        }

        if (!missingQuotes) {
            index++;
        }

        StringBuilder stringAcc = new StringBuilder();
        boolean escaped = false;

        while (true) {
            ch = getCharAt();
            if (ch == null) {
                break;
            }

            if (escaped) {
                if (ch == 'n') stringAcc.append('\n');
                else if (ch == 'r') stringAcc.append('\r');
                else if (ch == 't') stringAcc.append('\t');
                else if (ch == 'b') stringAcc.append('\b');
                else if (ch == 'f') stringAcc.append('\f');
                else stringAcc.append(ch);
                escaped = false;
                index++;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                index++;
                continue;
            }

            if (!missingQuotes && ch == rStringDelimiter) {
                index++;
                break;
            }

            if (missingQuotes && (ch == ',' || ch == '}' || ch == ']' || Character.isWhitespace(ch))) {
                break;
            }

            stringAcc.append(ch);
            index++;
        }

        return stringAcc.toString();
    }

    private Object parseNumber() {
        StringBuilder numberStr = new StringBuilder();
        Character ch = getCharAt();
        boolean isArray = context.getCurrent() == JsonContext.ContextValue.ARRAY;

        while (ch != null && NUMBER_CHARS.contains(ch) && (!isArray || ch != ',')) {
            numberStr.append(ch);
            index++;
            ch = getCharAt();
        }

        if (numberStr.length() > 0 && "-eE/,".indexOf(numberStr.charAt(numberStr.length() - 1)) >= 0) {
            numberStr.setLength(numberStr.length() - 1);
            index--;
        } else if (getCharAt() != null && Character.isLetter(getCharAt())) {
            index -= numberStr.length();
            return parseString();
        }

        String numStr = numberStr.toString();
        try {
            if (numStr.contains(",")) {
                return numStr;
            }
            if (numStr.contains(".") || numStr.toLowerCase().contains("e")) {
                return Double.parseDouble(numStr);
            }
            return Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            return numStr;
        }
    }

    private Object parseBooleanOrNull() {
        int startingIndex = index;
        Character ch = getCharAt();
        if (ch == null) {
            return "";
        }

        String value = null;
        Boolean result = null;

        char lowerCh = Character.toLowerCase(ch);
        if (lowerCh == 't') {
            value = "true";
            result = true;
        } else if (lowerCh == 'f') {
            value = "false";
            result = false;
        } else if (lowerCh == 'n') {
            value = "null";
            result = null;
        }

        if (value != null) {
            int i = 0;
            while (ch != null && i < value.length() && Character.toLowerCase(ch) == value.charAt(i)) {
                i++;
                index++;
                ch = getCharAt();
            }
            if (i == value.length()) {
                return result;
            }
        }

        index = startingIndex;
        return "";
    }

    private String parseComment() {
        Character ch = getCharAt();
        List<Character> terminationCharacters = new ArrayList<>(Arrays.asList('\n', '\r'));

        if (context.getContext().contains(JsonContext.ContextValue.ARRAY)) {
            terminationCharacters.add(']');
        }
        if (context.getCurrent() == JsonContext.ContextValue.OBJECT_VALUE) {
            terminationCharacters.add('}');
        }
        if (context.getCurrent() == JsonContext.ContextValue.OBJECT_KEY) {
            terminationCharacters.add(':');
        }

        if (ch == '#') {
            StringBuilder comment = new StringBuilder();
            while (ch != null && !terminationCharacters.contains(ch)) {
                comment.append(ch);
                index++;
                ch = getCharAt();
            }
            log("Found line comment: " + comment);
            return "";
        }

        if (ch == '/') {
            Character nextChar = getCharAt(1);
            if (nextChar == '/') {
                StringBuilder comment = new StringBuilder("//");
                index += 2;
                ch = getCharAt();
                while (ch != null && !terminationCharacters.contains(ch)) {
                    comment.append(ch);
                    index++;
                    ch = getCharAt();
                }
                log("Found line comment: " + comment);
                return "";
            }
            if (nextChar == '*') {
                StringBuilder comment = new StringBuilder("/*");
                index += 2;
                while (true) {
                    ch = getCharAt();
                    if (ch == null) {
                        log("Reached end-of-string while parsing block comment; unclosed block comment.");
                        break;
                    }
                    comment.append(ch);
                    index++;
                    if (comment.toString().endsWith("*/")) {
                        break;
                    }
                }
                log("Found block comment: " + comment);
                return "";
            }
            index++;
        }
        return "";
    }

    private Character getCharAt() {
        return getCharAt(0);
    }

    private Character getCharAt(int count) {
        try {
            return jsonStr.charAt(index + count);
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    private int skipWhitespacesAt() {
        return skipWhitespacesAt(0, true);
    }

    private int skipWhitespacesAt(int idx, boolean moveMainIndex) {
        try {
            char ch = jsonStr.charAt(index + idx);
            while (Character.isWhitespace(ch)) {
                if (moveMainIndex) {
                    index++;
                } else {
                    idx++;
                }
                ch = jsonStr.charAt(index + idx);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return idx;
        }
        return idx;
    }

    private int skipToCharacter(char character) {
        return skipToCharacter(character, 0);
    }

    private int skipToCharacter(char character, int idx) {
        try {
            char ch = jsonStr.charAt(index + idx);
            while (ch != character) {
                idx++;
                ch = jsonStr.charAt(index + idx);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return idx;
        }
        return idx;
    }

    private void log(String text) {
        if (!logging) {
            return;
        }
        int window = 10;
        int start = Math.max(index - window, 0);
        int end = Math.min(index + window, jsonStr.length());
        String context = jsonStr.substring(start, end);
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("text", text);
        logEntry.put("context", context);
        logger.add(logEntry);
    }
}
