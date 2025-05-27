package cn.zh54.json.repair;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 猿大晖@54zh.cn
 * @date 2025/03/25
 */
public class JsonContext {
    public enum ContextValue {
        OBJECT_KEY,
        OBJECT_VALUE,
        ARRAY
    }

    private Set<ContextValue> context;
    private ContextValue current;

    public JsonContext() {
        this.context = new HashSet<>();
        this.current = null;
    }

    public void set(ContextValue value) {
        this.context.add(value);
        this.current = value;
    }

    public void reset() {
        this.context.clear();
        this.current = null;
    }

    public boolean isEmpty() {
        return context.isEmpty();
    }

    public ContextValue getCurrent() {
        return current;
    }

    public Set<ContextValue> getContext() {
        return new HashSet<>(context);
    }
}
