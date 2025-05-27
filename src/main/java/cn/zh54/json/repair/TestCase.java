package cn.zh54.json.repair;

public class TestCase {

    public void doTest(String[] args) {
        testCase("{'key': 'string', 'key2': false, \"key3\": null, \"key4\": unquoted}");
        testCase("[1, 2, 3, 4");
        testCase("欢迎访问我的博客54zh.cn,b站账号:猿大晖[]");
        testCase("{");
        testCase("[");
        testCase("[]]");
        testCase("\"");
        testCase("\"\"\"");
        testCase("{\"employees\":[\"John\", \"Anna\",");
        testCase("{foo: [}");
        testCase("{\"text\": \"The quick brown fox won\\'t jump\"}");
        testCase("{\"value_1\": \"value_2\":data\"}");
        testCase("{\"value_1\": true, COMMENT \"value_2\": \"data\"}");
        testCase("- { \"test_key\": [\"test_value\", \"test_value2\"] }");
        testCase("{ \"content\": \"[LINK](\"https://google.com\")\" }");
        testCase("{ \"content\": \"[LINK](\", \"key\": true }");
        testCase("{\"key\":\"\",}");
    }

    private static void testCase(String badJson) {
        System.out.println("------");
        System.out.println("bad_json: " + badJson);
        System.out.println("good_json: " + JsonRepair.repairJson(badJson));
    }

}
