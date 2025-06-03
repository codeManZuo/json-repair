package cn.zh54.json.repair;

public class TestCase {

    public static void doTest(String[] args) {
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
        testCase("[{'transactionDate': '2025-05-31 10:36:55','fromAccount': null,'toAccount': null,'debit': null,'credit': 277300,'balance': 822300,'description': '2025 May 31 10:36:55 31 May 2025 Transfer from CREDPAY FINTECH LIMITED +2,773.00 8,223.00 E-Channel 100033250531103654318487604138','transactionType': 'OTHER'},{'transactionDate': '2025-06-01 18:54:45','fromAccount': null,'toAccount': null,'debit': 65000,'credit': null,'balance': 4850400,'description': '2025 Jun 01");
        testCase("[{\"transactionDate\": \"2025-05-31 10:36:55\",\"fromAccount\": null,\"toAccount\": null,\"debit\": null,\"credit\": 277300,\"balance\": 822300,\"description\": \"2025 May 31 10:36:55 31 May 2025 Transfer from CREDPAY FINTECH LIMITED +2,773.00 8,223.00 E-Channel 100033250531103654318487604138\",\"transactionType\": \"OTHER\"},{\"transactionDate\": \"2025-06-01 18:54:45\",\"fromAccount\": null,\"toAccount\": null,\"debit\": 65000,\"credit\": null,\"balance\": 4850400,\"description\": \"2025 Jun 01");
    }

    private static void testCase(String badJson) {
        System.out.println("------");
        System.out.println("bad_json: " + badJson);
        System.out.println("good_json: " + JsonRepair.repairJson(badJson));
    }

}
