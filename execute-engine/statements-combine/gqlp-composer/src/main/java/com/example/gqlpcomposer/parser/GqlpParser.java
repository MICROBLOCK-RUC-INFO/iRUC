package com.example.gqlpcomposer.parser;

import com.example.gqlpcomposer.model.GqlpDocument;

/**
 * GraphQL+ 解析器接口
 */
public interface GqlpParser {

    /**
     * 解析GraphQL+文档
     *
     * @param fileName 文件名
     * @param content 文档内容
     * @return 解析后的文档对象
     */
    GqlpDocument parse(String fileName, String content);
}
