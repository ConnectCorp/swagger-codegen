package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoopBackSwiftClientCodegen extends DefaultCodegen implements CodegenConfig {

    private static final String USE_REALM = "useRealm";

    private static final String REALM_PRIMARY_KEY = "realmPrimaryKey";

    private static final String PROJECT_NAME = "projectName";

    private static final String PROJECT_LICENSE = "projectLicense";

    private static final String NAME = "LoopBackSwift";

    private static final String HELP = "Generates a client library for the LoopBackSwift framework.";

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{[a-zA-Z_]+\\}");

    private String projectName = "LoopBackSwiftClient";

    private String sourceFolder = projectName;

    private static int anonymousApisCount = 0;

    private String modelId;

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

    public LoopBackSwiftClientCodegen() {
        super();

        outputFolder = "generated-code" + File.separator + NAME;
        modelTemplateFiles.put("model.mustache", ".swift");
        apiTemplateFiles.put("api.mustache", ".swift");
        repositoryTemplateFiles.put("repository.mustache", ".swift");

        embeddedTemplateDir = templateDir = NAME;
        apiPackage = File.separator + "APIs";
        modelPackage = File.separator + "Models";
        repositoryPackage = File.separator + "Repositories";

        setupLanguageSpecificPrimitives();
        setupDefaultIncludes();
        setupReservedWordsLowerCase();
        setupTypeMapping();
        setupImportMapping();
    }

    private void setupLanguageSpecificPrimitives() {
        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "Int",
                        "Float",
                        "Double",
                        "Bool",
                        "Void",
                        "String",
                        "Character",
                        "AnyObject"
                )
        );
    }

    private void setupDefaultIncludes() {
        defaultIncludes = new HashSet<String>(
                Arrays.asList(
                        "NSDate",
                        "Array",
                        "Dictionary",
                        "Set",
                        "Any",
                        "Empty",
                        "AnyObject"
                )
        );
    }

    private void setupReservedWordsLowerCase() {
        setReservedWordsLowerCase(
                Arrays.asList(
                        "class", "break", "as", "associativity", "deinit", "case", "dynamicType", "convenience", "enum", "continue",
                        "false", "dynamic", "extension", "default", "is", "didSet", "func", "do", "nil", "final", "import", "else",
                        "self", "get", "init", "fallthrough", "Self", "infix", "internal", "for", "super", "inout", "let", "if",
                        "true", "lazy", "operator", "in", "COLUMN", "left", "private", "return", "FILE", "mutating", "protocol",
                        "switch", "FUNCTION", "none", "public", "where", "LINE", "nonmutating", "static", "while", "optional",
                        "struct", "override", "subscript", "postfix", "typealias", "precedence", "var", "prefix", "Protocol",
                        "required", "right", "set", "Type", "unowned", "weak", "realm"
                )
        );
    }

    private void setupTypeMapping() {
        typeMapping = new HashMap<String, String>();

        typeMapping.put("array", "Array");
        typeMapping.put("List", "Array");
        typeMapping.put("map", "Dictionary");
        typeMapping.put("date", "NSDate");
        typeMapping.put("Date", "NSDate");
        typeMapping.put("DateTime", "NSDate");
        typeMapping.put("boolean", "Bool");
        typeMapping.put("string", "String");
        typeMapping.put("char", "Character");
        typeMapping.put("short", "Int");
        typeMapping.put("int", "Int");
        typeMapping.put("long", "Int");
        typeMapping.put("integer", "Int");
        typeMapping.put("Integer", "Int");
        typeMapping.put("float", "Float");
        typeMapping.put("number", "Double");
        typeMapping.put("double", "Double");
        typeMapping.put("object", "AnyObject");
        typeMapping.put("file", "NSURL");
        typeMapping.put("binary", "[UInt8]");
    }

    private void setupImportMapping() {
        importMapping = new HashMap<String, String>();
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (!additionalProperties.containsKey(USE_REALM)) {
            additionalProperties.put(USE_REALM, false);
        } else {
            additionalProperties.put(USE_REALM, additionalProperties.get(USE_REALM).equals("true"));
        }

        if (!additionalProperties.containsKey(REALM_PRIMARY_KEY)) {
            additionalProperties.put(REALM_PRIMARY_KEY, "id");
        }

        if (!additionalProperties.containsKey(PROJECT_NAME)) {
            additionalProperties.put(PROJECT_NAME, projectName);
        } else {
            projectName = sourceFolder = additionalProperties.get(PROJECT_NAME).toString();
        }

        if (!additionalProperties.containsKey(PROJECT_LICENSE)) {
            additionalProperties.put(PROJECT_LICENSE, "The MIT License (MIT)");
        }

        modelId = additionalProperties.get(REALM_PRIMARY_KEY).toString();

        supportingFiles.add(new SupportingFile("AuthenticationMethod.mustache", authFileFolder(), "AuthenticationMethod.swift"));
        supportingFiles.add(new SupportingFile("APIKey.mustache", authFileFolder(), "APIKey.swift"));
        supportingFiles.add(new SupportingFile("RequestAuthenticator.mustache", authFileFolder(), "RequestAuthenticator.swift"));

        supportingFiles.add(new SupportingFile("BaseAPI.mustache", utilFileFolder(), "API.swift"));
        supportingFiles.add(new SupportingFile("APIError.mustache", utilFileFolder(), "APIError.swift"));
        supportingFiles.add(new SupportingFile("RxAlamofireObjectMapping.mustache", utilFileFolder(), "RxAlamofireObjectMapping.swift"));
        supportingFiles.add(new SupportingFile("ConnectivityManager.mustache", utilFileFolder(), "ConnectivityManager.swift"));
        supportingFiles.add(new SupportingFile("RealmManager.mustache", utilFileFolder(), "RealmManager.swift"));
        supportingFiles.add(new SupportingFile("RepositoryError.mustache", utilFileFolder(), "RepositoryError.swift"));
        supportingFiles.add(new SupportingFile("Query+toNSPredicate.mustache", utilFileFolder(), "Query+toNSPredicate.swift"));
        supportingFiles.add(new SupportingFile("String+URLEscapedString.mustache", utilFileFolder(), "String+URLEscapedString.swift"));
        supportingFiles.add(new SupportingFile("Double+URLEscapedString.mustache", utilFileFolder(), "Double+URLEscapedString.swift"));

        supportingFiles.add(new SupportingFile("Podspec.mustache", "", projectName + ".podspec"));
        supportingFiles.add(new SupportingFile("LICENSE.mustache", "", "LICENSE"));
    }

    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;
    }

    @Override
    public String modelFileFolder() {
        return getOutputFolder() + modelPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiFileFolder() {
        return getOutputFolder() + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String repositoryFileFolder() {
        return getOutputFolder() + repositoryPackage.replace('.', File.separatorChar);
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objects) {
        HashMap<String, Object> api  = (HashMap<String, Object>) objects.get("operations");

        postProcessOperations(api, (ArrayList<CodegenOperation>) api.get("operation"));

        return objects;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        ArrayList<HashMap<String, Object>> models = (ArrayList<HashMap<String, Object>>) objs.get("models");

        for (HashMap<String, Object> modelData: models) {
            CodegenModel model = (CodegenModel) modelData.get("model");
            model.id = modelId;

            if (model.hasId()) {
                CodegenProperty id = model.getId();

                if (id.datatype.equals("Double")) {
                    id.datatype = "Int";
                }
            }
        }

        return objs;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        property.isNumeric = isTrue(property.isInteger) || isTrue(property.isDouble)
                || isTrue(property.isFloat) || isTrue(property.isLong)
                || isTrue(property.isBoolean);
    }

    @Override
    public String getTypeDeclaration(Property property) {
        if (property instanceof ArrayProperty) {
            ArrayProperty array = (ArrayProperty) property;

            return "[" + getTypeDeclaration(array.getItems()) + "]";
        }

        if (property instanceof MapProperty) {
            MapProperty map = (MapProperty) property;

            return "[String:" + getTypeDeclaration(map.getAdditionalProperties()) + "]";
        }

        return super.getTypeDeclaration(property);
    }

    @Override
    public String getSwaggerType(Property property) {
        String swaggerType = super.getSwaggerType(property);

        if (typeMapping.containsKey(swaggerType)) {
            swaggerType = typeMapping.get(swaggerType);
        }

        return toModelName(swaggerType);
    }

    @Override
    public String toDefaultValue(Property property) {
        return "nil";
    }

    @Override
    public String toInstantiationType(Property property) {
        if (property instanceof ArrayProperty) {
            ArrayProperty array = (ArrayProperty) property;

            return "[" + getSwaggerType(array.getItems()) + "]";
        }

        if (property instanceof MapProperty) {
            MapProperty map = (MapProperty) property;

            return "[String:" + getSwaggerType(map.getAdditionalProperties()) + "]";
        }

        return null;
    }

    @Override
    public CodegenProperty fromProperty(String name, Property p) {
        CodegenProperty property = super.fromProperty(name, p);

        if (property.isEnum) {
            handleEnumProperty(name, property);
        }

        return property;
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "AnonymousAPI" + (++anonymousApisCount);
        }

        return initialCaps(name) + "API";
    }

    @Override
    public String toOperationId(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new RuntimeException("Empty operation id not allowed.");
        }

        if (isReservedWord(id)) {
            throw new RuntimeException("'" + id + "' cannot be used as method name.");
        }

        String[] parts = id.split(Pattern.quote("."));

        return camelize(sanitizeName(parts[parts.length - 1]), true);
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation op, Map<String, Model> definitions, Swagger swagger) {
        CodegenOperation operation = super.fromOperation(normalizePath(path), httpMethod, op, definitions, swagger);

        handleOperationExamples(operation);

        return operation;
    }

    private void postProcessOperations(HashMap<String, Object> api, List<CodegenOperation> operations) {
        if (operations.size() == 0) {
            return;
        }

        String resourcePath = "/" + operations.get(0).path.split("/")[1];
        String countReturnType = "";
        String deleteReturnType = "";

        for (CodegenOperation operation: operations) {
            if (operation.path.equals(resourcePath)) {
                operation.path = null;

            } else {
                String[] pathParts = operation.path.split(resourcePath);

                if (pathParts.length > 1) {
                    operation.path = pathParts[1].substring(1);
                }
            }

            operation.successfulResponse = findSuccessfulResponse(operation.responses);

            if (operation.successfulResponse.containerType != null && operation.successfulResponse.containerType.equals("array")) {
                operation.successfulResponse.isListContainer = true;
            }

            if (operation.operationId.equals("count")) {
                countReturnType = operation.successfulResponse.dataType;
            }

            if (operation.operationId.equals("deleteById")) {
                deleteReturnType = operation.successfulResponse.dataType;
            }
        }

        api.put("resourcePath", resourcePath);
        api.put("countReturnType", countReturnType);
        api.put("deleteReturnType", deleteReturnType);
    }

    private CodegenResponse findSuccessfulResponse(List<CodegenResponse> responses) {
        if (responses.size() == 1 && responses.get(0).isWildcard()) {
            responses.get(0).dataType = "String";

            return responses.get(0);
        }

        for (CodegenResponse response: responses) {
            if (response.code.startsWith("2") && response.dataType != null && !response.primitiveType) {
                return response;
            }
        }

        CodegenResponse stringResponse = new CodegenResponse();
        stringResponse.dataType = "String";

        return stringResponse;
    }

    private void handleOperationExamples(CodegenOperation operation) {
        if (operation.examples != null) {
            List<Map<String, String>> examples = new ArrayList<Map<String, String>>();

            for (Map<String, String> example: operation.examples) {
                if (example.containsKey("contentType") && example.get("contentType").equals("application/json")) {
                    example.put("example", normalizeExampleString(example.get("example")));

                    examples.add(example);
                }
            }

            operation.examples = examples;
        }
    }

    private void handleEnumProperty(String name, CodegenProperty property) {
        List<Map<String, String>> enums = new ArrayList<Map<String, String>>();
        List<String> values = (List<String>) property.allowableValues.get("values");

        for (String value: values) {
            Map<String, String> map = new HashMap<String, String>();

            map.put("enum", toSwiftEnumName(value));
            map.put("raw", value);

            enums.add(map);
        }

        property.allowableValues.put("values", enums);
        property.datatypeWithEnum = StringUtils.left(property.datatypeWithEnum, property.datatypeWithEnum.length() - "Enum".length());

        if (isReservedWord(property.datatypeWithEnum) || name.equals(property.datatypeWithEnum)) {
            property.datatypeWithEnum = escapeReservedWord(property.datatypeWithEnum);
        }
    }

    private String toSwiftEnumName(String name) {
        if (name.matches("[A-Z][a-z0-9]+[a-zA-Z0-9]*")) {
            return name;
        }

        char[] separators = { '-', '_', ' ' };

        return WordUtils.capitalizeFully(StringUtils.lowerCase(name), separators).replaceAll("[-_]", "");
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

    private String normalizeExampleString(String str) {
        return str.replaceAll("\"", "\\\\\"").replaceAll("\\s+", "");
    }

    private String getOutputFolder() {
        return outputFolder + File.separator + sourceFolder;
    }

    private String authFileFolder() {
        return projectName + File.separator + "Auth";
    }

    private String utilFileFolder() {
        return projectName + File.separator + "Util";
    }

    private String repositoriesFileFolder() { return projectName + File.separator + "Repositories"; }

    private boolean isTrue(Boolean b) {
        return b != null && b;
    }
}
