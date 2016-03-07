package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenType;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoyaSwiftClientCodegen extends SwiftCodegen {
    private final static String NAME = "moya";

    private final static String HELP = "Generates a client library for the Moya framework.";

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{[a-zA-Z_]+\\}");

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getHelp() {
        return HELP;
    }

    public MoyaSwiftClientCodegen() {
        super();

        embeddedTemplateDir = templateDir = "moya";
    }

    @Override
    public String toOperationId(String operationId) {
        String id = super.toOperationId(operationId);

        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);

        op.path = normalizePath(op.path);

        return op;
    }

    private String normalizePath(String path) {
        StringBuilder builder = new StringBuilder();

        Matcher matcher = PATH_PARAM_PATTERN.matcher(path);
        int cursor = 0;

        while (matcher.find()) {
            String before = path.substring(cursor, matcher.start());
            builder.append(before);

            String param = matcher.group().substring(1, matcher.group().length() - 1);
            builder.append("\\(");
            builder.append(param);
            builder.append(")");

            cursor = matcher.end();
        }

        String after = path.substring(cursor);
        builder.append(after);

        return builder.toString();
    }
}
