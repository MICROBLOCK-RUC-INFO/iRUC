package com.example.gqlpcomposer.exception;

/**
 * GraphQL+ 解析异常
 */
public class GqlpParseException extends RuntimeException {

    private final int lineNumber;
    private final String fileName;

    public GqlpParseException(String message) {
        super(message);
        this.lineNumber = -1;
        this.fileName = null;
    }

    public GqlpParseException(String message, String fileName, int lineNumber) {
        super(String.format("解析错误在文件 %s 第 %d 行: %s", fileName, lineNumber, message));
        this.lineNumber = lineNumber;
        this.fileName = fileName;
    }

    public GqlpParseException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = -1;
        this.fileName = null;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getFileName() {
        return fileName;
    }
}
