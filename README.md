# eval-template-parse

eval-template-parse 是一个轻量级表达式解析框架，通过 javacc 语法解析，不依赖任何第三方 jar。

## maven 依赖
添加 eval-template-parse 依赖
```xml
<dependency>
    <groupId>cn.veasion</groupId>
    <artifactId>eval-template-parse</artifactId>
    <version>1.0.0</version>
</dependency>
```
## 使用方式介绍
具体代码请见单元测试

### 表达式解析
支持解析非常复杂的表达式，假设有一个JAVA对象为
```js
// 伪代码
let object = {
    order: {
        num: 100,
        field: "num",
        product_amt: 20.25,
        product: {
            user: {
                age: 18,
                index: 1,
                array: [10, 20, 30]
            }
        }
    },
    ext: (s) => {
        if (s === 'random') {
            return Math.random()
        } else if (s === 'date') {
            return new Date().getTime()
        }
        return null
    }
}
```
表达式解析
```
Object result;

// 示例
result = EvalParserUtils.eval(object, "order.num");
// 结果
100

// 示例
result = EvalParserUtils.eval(object, "order['num']");
// 结果
100

// 示例
result = EvalParserUtils.eval(object, "order[order.field]");
// 结果
100

// 示例
result = EvalParserUtils.eval(object, "order.product.user['age']");
// 结果
18

// 示例
result = EvalParserUtils.eval(object, "order.product.user.array[order.product.user.index]");
// 结果
20

// 示例
result = EvalParserUtils.eval(object, "order.field == 'num' && 1=='1'");
// 结果
true

// 示例
result = EvalParserUtils.eval(object, "order[order.field] == 10 && 1 eq 1 && 1 == 2");
// 结果
false

// 示例
result = EvalParserUtils.eval(object, "order.product.user.age > 18 || order.field != 'num'");
// 结果
false

// 示例
result = EvalParserUtils.eval(object, "order[order.field] == 10 ? (1 != 2 ? order.field : order.product.user.age) : 'xxx'");
// 结果
xxx

// 示例
result = EvalParserUtils.eval(object, "ext.random");
// 结果
0.6526022926698725

// 示例
result = EvalParserUtils.eval(object, "ext.random");
// 结果
0.5368150204045047

// 示例，计算结果并且获取每个变量对应的值
Map<String, Object> varMap = new HashMap<>();
String eval = "order[order.field] == 10 ? (1 != 2 ? order.field : order.product.user.age) : 'xxx'";
result = EvalParserUtils.eval(object, eval, varMap);
// result 结果
xxx
// varMap 变量
{
    "order.field": "num",
    "order[order.field]": 100,
    "order.product.user.age": 18
}
```
表达式模板解析
```
String template = "订单数量：${order.num}，用户年龄：${order.product.user.age}，其他：${1>2?'x':(!(order!=null)?'xx':'xxx')}。默认：${ext.xxx|order.xxx|10}";
Map<String, Object> varMap = new HashMap<>();
Object result = EvalParserUtils.template(object, template, varMap);

// result 结果
订单数量：100，用户年龄：18，其他：xxx。默认：10
// varMap 变量
{
    "order.num": 100,
    "order.product.user.age": 18,
    "order": {...},
    "order.xxx|10": 10,
    "ext.xxx|order.xxx|10": 10
} 
```
### 模板解析
模板解析，支持 if/for/eval 表达式。
示例：假设有一个JAVA对象为
```js
// 伪代码
let object = {
    name: "Veasion",
    sex: "男",
    age: 0,
    users: [
        {
            name: "用户1",
            attrMap: {
                "颜色": "红色",
                "爱好": "打球",
                "": "多大"
            }
        },
        {
            name: "用户2",
            attrMap: {
                "颜色": "白色",
                "喜欢": "睡觉",
                "哈哈": ""
            }
        }
    ]
}
```
test.template 模板内容
```
hello! 你好，我是${name}

    很高兴认识你！
<#if sex>性别：${sex}</#if>
<#if age gt 0>
年龄：${age}
<#else>
aaaaa
</#if>
各种字符串测试：awdesfrdf “”哈哈""sads';!efreg35=3469912!%296396-#^(%^__+-)(&$`.,<>?/'"*%~
<#-- 这是注释 --#>
用户列表：
<#for users as item>
姓名：${item.name}
=== 其它属性 ===
<#for item.attrMap as key, value>
<#if key && value>
${key}: ${value}
</#if>
</#for>
</#for>
====================
```
解析
```
InputStream inputStream = getClass().getResourceAsStream("/test.template");
String template = TemplateParserUtils.parseTemplate(object, inputStream);
System.out.println(template);
```
结果
```
hello! 你好，我是Veasion

    很高兴认识你！
性别：男

aaaaa
各种字符串测试：awdesfrdf “”哈哈""sads';!efreg35=3469912!%296396-#^(%^__+-)(&$`.,<>?/'"*%~

用户列表：
姓名：用户1
=== 其它属性 ===
爱好: 打球
颜色: 红色

姓名：用户2
=== 其它属性 ===
颜色: 白色
喜欢: 睡觉


====================
```
### 计算解析

解析并计算各种常规数学公式。

普通公式
```
BigDecimal result;
// 计算
result = CalculatorUtils.calc("-2");
// 结果
-2
// 计算
result = CalculatorUtils.calc("√8");
// 结果
2.8284271247461903
// 计算
result = CalculatorUtils.calc("2^3");
// 结果
8.0
// 计算
result = CalculatorUtils.calc("10%6");
// 结果
4
// 计算
result = CalculatorUtils.calc("1+(-4)");
// 结果
-3
// 计算
result = CalculatorUtils.calc("√(3*3)");
// 结果
3.0
// 计算
result = CalculatorUtils.calc("2×3÷3");
// 结果
2.000000
// 计算
result = CalculatorUtils.calc("1+3√(3*3*3)");
// 结果
3.9999967041649445
// 计算
result = CalculatorUtils.calc("2+5*2-6/3");
// 结果
10.000000
// 计算
result = CalculatorUtils.calc("(1+2+3)^2+4");
// 结果
40.0
// 计算
result = CalculatorUtils.calc("2+5*(2-6*(3+1))/3");
// 结果
-34.666667
// 计算
result = CalculatorUtils.calc("4.99+(5.99+6.99)*1.06^2+√4");
// 结果
21.574328000000002596
```

变量表达式公式，假设有JAVA对象为
```
let object = {
    a: 1,
    b: 2,
    c: 3,
    aa: -1,
    bb: -2,
    cc: -3,
    d: 4.99,
    s: 8,
    "商品金额": 10.25,
    "销售数量": 10,
    "优惠金额": 2,
    order: {
        num: 10,
        field: "num",
        product_amt: 10.25,
        product: {
            user: {
                id: 1,
                age: 18,
                array: [10, 20, 30]
            }
        }
    },
    ext: (s) => {
        if (s === 'random') {
            return Math.random()
        } else if (s === 'date') {
            return new Date().getTime()
        }
        return null
    }
}
```
表达式计算示例：
```
// 计算
result = CalculatorUtils.calc(object, "-b");
// 结果
-2
// 计算
result = CalculatorUtils.calc(object, "√s");
// 结果
2.8284271247461903
// 计算
result = CalculatorUtils.calc(object, "b^c");
// 结果
8.0
// 计算
result = CalculatorUtils.calc(object, "(s+b)%(c*b)");
// 结果
4
// 计算
result = CalculatorUtils.calc(object, "a+(-4)");
// 结果
-3
// 计算
result = CalculatorUtils.calc(object, "√(c*c)");
// 结果
3.0
// 计算
result = CalculatorUtils.calc(object, "b×c÷c");
// 结果
2.000000
// 计算
result = CalculatorUtils.calc(object, "a+c√(c*c*c)");
// 结果
3.9999967041649445
// 计算
result = CalculatorUtils.calc(object, "b+5*b-6/c");
// 结果
10.000000
// 计算
result = CalculatorUtils.calc(object, "(a+b+c)^b+4");
// 结果
40.0
// 计算
result = CalculatorUtils.calc(object, "b+5*(b-6*(c+a))/c");
// 结果
-34.666667
// 计算
result = CalculatorUtils.calc(object, "d+(5.99+6.99)*1.06^b+√(a+c)");
// 结果
21.574328000000002596

// 计算
result = CalculatorUtils.calc(object, "商品金额*销售数量-优惠金额+默认值|5");
// 结果
105.50
// 计算
result = CalculatorUtils.calc(object, "order[order.field] * 2");
// 结果
20
// 计算
result = CalculatorUtils.calc(object, "order.product_amt * order.num + ext.random");
// 结果
102.8576819824998685
// 计算
result = CalculatorUtils.calc(object, "order.product_amt * order.num + ext.random");
// 结果
102.516112118483238236
// 计算
result = CalculatorUtils.calc(object, "order.product.user['age'] + order.product.user.age");
// 结果
36
// 计算
result = CalculatorUtils.calc(object, "order.product.user.array[0] + order.product.user.array[order.product.user.id]");
// 结果
30

// 计算并获取每个变量的结果
Map<String, Object> varMap = new HashMap<>();
result = CalculatorUtils.calc(object, "商品金额*销售数量-优惠金额+默认值|5", varMap);
// result 结果
105.50
// varMap 变量
{
    "销售数量": 10,
    "商品金额": 10.25,
    "默认值|5": 5,
    "优惠金额": 2
}

// 计算并获取每个变量的结果
varMap = new HashMap<>();
result = CalculatorUtils.calc(object, "order.product.user.array[0] + order.product.user.array[order.product.user.id]", varMap);
// result 结果
30
// varMap 变量
{
    "order.product.user.array[0]": 10,
    "order.product.user.id": 1,
    "order.product.user.array[order.product.user.id]": 20
}
```

## 赞助

项目的发展离不开您的支持，请作者喝杯咖啡吧~

![支付宝](https://veasion.oss-cn-shanghai.aliyuncs.com/alipay.png?x-oss-process=image/resize,m_lfit,h_360,w_360)
