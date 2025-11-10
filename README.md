# Java Utils Toolkit

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java: 8+](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.oracle.com/java/)
[![Build Passing](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/yourusername/java-utils-toolkit/actions)

基于Java的常用工具类集合，专注于后端开发痛点解决：PDF生成、日期处理、加密工具、文件操作等。开箱即用，支持Maven集成，附带完整Demo。适用于Spring Boot、Android等场景。

## ✨ 特性

- **PDF模板填充**：iText7驱动，支持{{key}}占位符精确定位、多页批量生成、中文无乱码（自动系统字体）。
- **日期工具**：LocalDateTime封装，跨时区转换、节日计算、一键格式化。
- **加密助手**：AES/RSA加密解密，Base64/Hash一站式。
- **文件操作**：Excel导入导出（Apache POI）、ZIP压缩、路径安全校验。
- **零依赖扩展**：纯Java核心 + 少量成熟库（如iText7），易部署。
- **Demo驱动**：每个工具类配JUnit测试 + 主类运行示例。

## 📦 快速开始

### 1. 克隆仓库
```bash
git clone https://github.com/yourusername/java-utils-toolkit.git
cd java-utils-toolkit
```

### 2. Maven依赖（可选，自建项目集成）
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>utils-toolkit</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 运行Demo
- PDF工具：`mvn exec:java -Dexec.mainClass="com.example.util.test.FillPdfDemo7"`
- 日期工具：`mvn exec:java -Dexec.mainClass="com.example.util.test.DateUtilDemo"`
- 完整列表见 `/src/main/java/com/example/util/test/` 包。

### 4. 构建 & 测试
```bash
mvn clean install  # 构建JAR
mvn test           # 运行JUnit测试
```

## 🛠️ 工具类概览

| 工具类 | 描述 | 示例用法 |
|--------|------|----------|
| `PdfUtil7` | PDF模板填充（多页、中文支持） | `PdfUtil7.generate(template, output, dataList, "mac");` |
| `DateUtils` | 日期格式化/计算 | `DateUtils.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm");` |
| `CryptoUtils` | 加密/解密 | `CryptoUtils.aesEncrypt("text", "key");` |
| `FileUtils` | 文件/Excel操作 | `FileUtils.exportExcel(list, "output.xlsx");` |

详细API文档：Javadoc生成在 `/target/site/apidocs/`。

## 🎯 示例：PDF生成Demo

假设模板 `template_final.pdf` 含 `{{responsible}}`、`{{a}}` 等占位符：

```java
// src/main/java/com/example/util/test/FillPdfDemo7.java
package com.example.util.test;

import com.example.util.PdfUtil7;
import java.util.*;

public class FillPdfDemo7 {
    public static void main(String[] args) {
        try {
            String template = "src/main/resources/template_final.pdf";
            String output = "src/main/resources/output/final_multi_pages.pdf";

            List<Map<String, String>> dataList = new ArrayList<>();
            // 添加多页数据...
            Map<String, String> page1 = new HashMap<>();
            page1.put("responsible", "张三");
            page1.put("a", "100");
            dataList.add(page1);

            PdfUtil7.generate(template, output, dataList, "win");  // 支持 mac/win/linux
            System.out.println("PDF生成成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

运行后，输出3页PDF，每页填充不同数据，中文完美渲染。

## 🤝 贡献指南

1. Fork仓库，创建feature分支。
2. 提交PR：描述变更 + 测试用例。
3. 遵循[代码规范](CONTRIBUTING.md)（Google Java Style）。

欢迎PR新工具类！如需讨论，[Issues](https://github.com/yourusername/java-utils-toolkit/issues)。

## 📄 许可证

本项目采用 [MIT License](LICENSE)。免费商用，保留版权声明。

## 👏 鸣谢

- iText7：PDF核心库。
- Apache POI：Excel支持。
- 社区贡献者：感谢所有Star & Fork！

---

⭐ **Star这个仓库，支持更多工具开发！**  
🐛 **发现Bug？** [提交Issue](https://github.com/yourusername/java-utils-toolkit/issues/new)  
📞 **联系我**：your.email@example.com

[返回顶部](#java-utils-toolkit) | [Changelog](CHANGELOG.md) | [Wiki](https://github.com/yourusername/java-utils-toolkit/wiki)