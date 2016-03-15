package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

import java.io.File;
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
        outputFolder = "generated-code" + File.separator + "moya";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        supportingFiles.remove(new SupportingFile("AlamofireImplementations.mustache", sourceFolder, "AlamofireImplementations.swift"));
        supportingFiles.remove(new SupportingFile("APIs.mustache", sourceFolder, "APIs.swift"));
        supportingFiles.remove(new SupportingFile("Models.mustache", sourceFolder, "Models.swift"));
        supportingFiles.remove(new SupportingFile("Extensions.mustache", sourceFolder, "Extensions.swift"));
        supportingFiles.remove(new SupportingFile("APIHelper.mustache", sourceFolder, "APIHelper.swift"));
        supportingFiles.remove(new SupportingFile("Cartfile.mustache", "", "Cartfile"));
        supportingFiles.remove(new SupportingFile("Podspec.mustache", "", projectName + ".podspec"));

        sourceFolder = projectName;

        supportingFiles.add(new SupportingFile("Swift+URLEscapedString.mustache", sourceFolder, "String+URLEscapedString.swift"));
        supportingFiles.add(new SupportingFile("ParameterEncoding+encode.mustache", sourceFolder, "ParameterEncoding+encode.swift"));
        supportingFiles.add(new SupportingFile("Parameters.mustache", sourceFolder, "Parameters.swift"));
        supportingFiles.add(new SupportingFile("JsonSerializable.mustache", sourceFolder, "JsonSerializable.swift"));
        supportingFiles.add(new SupportingFile("JsonDeserializers.mustache", sourceFolder, "JsonDeserializers.swift"));
        supportingFiles.add(new SupportingFile("Utils.mustache", sourceFolder, "Utils.swift"));
    }

    @Override
    public String toOperationId(String operationId) {
        return initialCaps(super.toOperationId(operationId));
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation op = super.fromOperation(path, httpMethod, operation, definitions, swagger);

        op.path = normalizePath(op.path);


        if (op.examples != null) {
            for (Map<String, String> example : op.examples) {
                String exampleString = example.get("example");

                example.put("example", normalizeExampleString(exampleString));
            }
        }

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
            builder.append(".URLEscapedString");
            builder.append(")");

            cursor = matcher.end();
        }

        String after = path.substring(cursor);
        builder.append(after);

        return builder.toString();
    }

    private String normalizeExampleString(String exampleString) {
        return exampleString.replaceAll("\"", "\\\\\"").replaceAll("\\s+", "");
    }
}
