package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoopBackSwiftClientCodegen extends DefaultCodegen implements CodegenConfig {

    private static final String USE_REALM = "useRealm";

    private static final String REALM_PRIMARY_KEY = "realmPrimaryKey";

    private static final String GENERATE_DATA_API = "generateDataAPI";

    private static final String PROJECT_NAME = "projectName";

    private static final String PROJECT_LICENSE = "projectLicense";

    private static final String NAME = "LoopBackSwift";

    private static final String HELP = "Generates a client library for the LoopBackSwift framework.";

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{[a-zA-Z_]+\\}");

    private String projectName = "LoopBackSwiftClient";

    private String sourceFolder = projectName;

    private static int anonymousApisCount = 0;

    private String modelId;

    private boolean useRealm = false;

    private boolean generateDataAPI = false;

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

        embeddedTemplateDir = templateDir = NAME;
        apiPackage = File.separator + "APIs";
        modelPackage = File.separator + "Models";

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
                        "required", "right", "set", "Type", "unowned", "weak", "realm", "hash", "description"
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
        typeMapping.put("int64", "Int64");
        typeMapping.put("long", "Int");
        typeMapping.put("integer", "Int64");
        typeMapping.put("Integer", "Int");
        typeMapping.put("float", "Float");
        typeMapping.put("number", "Int64");
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

        if (!additionalProperties.containsKey(GENERATE_DATA_API)) {
            additionalProperties.put(GENERATE_DATA_API, false);
        } else {
            additionalProperties.put(GENERATE_DATA_API, additionalProperties.get(GENERATE_DATA_API).equals("true"));
        }

        useRealm = (Boolean) additionalProperties.get(USE_REALM);

        generateDataAPI = (Boolean) additionalProperties.get(GENERATE_DATA_API);

        modelId = additionalProperties.get(REALM_PRIMARY_KEY).toString();

        supportingFiles.add(new SupportingFile("AuthenticationMethod.mustache", authFileFolder(), "AuthenticationMethod.swift"));
        supportingFiles.add(new SupportingFile("APIKey.mustache", authFileFolder(), "APIKey.swift"));
        supportingFiles.add(new SupportingFile("RequestAuthenticator.mustache", authFileFolder(), "RequestAuthenticator.swift"));
        supportingFiles.add(new SupportingFile("BaseAPI.mustache", utilFileFolder(), "API.swift"));
        supportingFiles.add(new SupportingFile("APIError.mustache", utilFileFolder(), "APIError.swift"));
        supportingFiles.add(new SupportingFile("RxAlamofireObjectMapping.mustache", utilFileFolder(), "RxAlamofireObjectMapping.swift"));
        supportingFiles.add(new SupportingFile("Primitives+URLEscapedString.mustache", utilFileFolder(), "Primitives+URLEscapedString.swift"));
        supportingFiles.add(new SupportingFile("NSData+JSON.mustache", utilFileFolder(), "NSData+JSON.swift"));
        supportingFiles.add(new SupportingFile("Int64+_ObjectiveCBridgeable.mustache", utilFileFolder(), "Int64+_ObjectiveCBridgeable.swift"));
        supportingFiles.add(new SupportingFile("ISO8601ExtendedDateTransform.mustache", utilFileFolder(), "ISO8601ExtendedDateTransform.swift"));
        supportingFiles.add(new SupportingFile("Podspec.mustache", "", projectName + ".podspec"));
        supportingFiles.add(new SupportingFile("LICENSE.mustache", "", "LICENSE"));
        supportingFiles.add(new SupportingFile("NSDate+toString.mustache", utilFileFolder(), "NSDate+toString.swift"));
        if (useRealm) {
            supportingFiles.add(new SupportingFile("RealmListTransform.mustache", realmFileFolder(), "RealmListTransform.swift"));
            supportingFiles.add(new SupportingFile("RealmOptionalTransform.mustache", realmFileFolder(), "RealmOptionalTransform.swift"));
            supportingFiles.add(new SupportingFile("RealmWrappers.mustache", realmFileFolder(), "RealmWrappers.swift"));
            supportingFiles.add(new SupportingFile("RealmInt64ListTransform.mustache", realmFileFolder(), "RealmInt64ListTransform.swift"));

            typeMapping.put("object", "NSData");
        }

        if (generateDataAPI) {
            topLevelTemplateFiles.put("DataAPI.mustache", ".swift");

            supportingFiles.add(new SupportingFile("UnsupportedOperation.mustache", utilFileFolder(), "UnsupportedOperation.swift"));
            supportingFiles.add(new SupportingFile("UnsupportedModelType.mustache", utilFileFolder(), "UnsupportedModelType.swift"));
        }
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
    public String topLevelAPIFileFolder() {
        return getOutputFolder();
    }

    @Override
    public String topLevelAPIFilename(String templateName) {
        String suffix = topLevelAPITemplateFiles().get(templateName);
        return topLevelAPIFileFolder() + "/DataAPI" + suffix;
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

                // When using Realm, if the primary key data type is Double, change it to Int.
                if (id.datatype.equals("Double")) {
                    id.datatype = "Int";
                }
            }

            for (CodegenProperty var: model.vars) {
                if (var.isRaw != null && var.isRaw) {
                    model.rawVars.add(var);
                }
            }

            int l = model.rawVars.size();
            if (l > 0) {
                for (int i = 0; i < l - 1; ++i) {
                    model.rawVars.get(i).hasMoreRaw = true;
                }
                model.rawVars.get(l - 1).hasMoreRaw = false;
            }
        }

        return objs;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        // For some properties, property.isDouble is not true even if property.datatype is Double.
        if (property.datatype.equals("Double")) {
            property.isDouble = true;
        }

        if (property.datatype.equals("Int") || property.datatype.equals("Int64")) {
            property.isInteger = true;
        }

        if (property.datatype.equals("NSData")) {
            property.isRaw = true;
        }

        property.isNumeric = isTrue(property.isInteger) || isTrue(property.isDouble)
                || isTrue(property.isFloat) || isTrue(property.isLong)
                || isTrue(property.isBoolean);

        if (useRealm && property.name.equals(modelId)) {
            property.required = true;
            property.isId = true;
        }
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
        if (!property.getRequired()) {
            return "nil";
        }

        if (property instanceof AbstractNumericProperty) {
            return "0";
        }

        if (property instanceof BooleanProperty) {
            return "false";
        }

        if (property instanceof StringProperty) {
            return "\"\"";
        }

        if (property instanceof ArrayProperty) {
            return "[:]";
        }

        if (property instanceof MapProperty) {
            return "[:]";
        }

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

        if (useRealm && property.name.equals(modelId)) {
            property.required = true;
            property.defaultValue = "0";
        }

        if (property.baseType.equals("Int64")) {
            property.isPrimitiveType = true;
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
    public String toVarName(String s) {
        String name = s.charAt(0) == '_' ? '_' + camelize(super.toVarName(s.substring(1)), true) : camelize(super.toVarName(s), true);

        return isReservedWord(name) ? escapeReservedWord(name) : name;
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

        String resourcePath = generateDataAPI ? "/" + operations.get(0).path.split("/")[1] : null;
        String countReturnType = "Int";
        String deleteReturnType = "Bool";

        for (CodegenOperation operation: operations) {
            if (resourcePath != null) {
                if (operation.path.equals(resourcePath)) {
                    operation.path = null;

                } else {
                    String[] pathParts = operation.path.split(resourcePath);

                    if (pathParts.length > 1) {
                        operation.path = pathParts[1].substring(1);
                    }
                }
            }

            operation.successfulResponse = findSuccessfulResponse(operation.responses);

            if (operation.successfulResponse.containerType != null && operation.successfulResponse.containerType.equals("array")) {
                operation.successfulResponse.isListContainer = true;
            }

            if (generateDataAPI) {
                if (operation.operationId.equals("create")) {
                    api.put("hasCreate", true);
                }

                if (operation.operationId.equals("upsert")) {
                    api.put("hasUpsert", true);
                }

                if (operation.operationId.equals("findById")) {
                    api.put("hasFindById", true);
                }

                if (operation.operationId.equals("findOne")) {
                    api.put("hasFindOne", true);
                }

                if (operation.operationId.equals("find")) {
                    api.put("hasFind", true);
                }

                if (operation.operationId.equals("count")) {
                    api.put("hasCount", true);
                    countReturnType = operation.successfulResponse.dataType;
                }

                if (operation.operationId.equals("deleteById")) {
                    api.put("hasDeleteById", true);
                    deleteReturnType = operation.successfulResponse.dataType;
                }
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

        if (property.required != null && property.required) {
            property.defaultValue = property.datatypeWithEnum + "(rawValue: \"" + property.defaultValue + "\")!";
        } else {
            property.defaultValue = "nil";
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
            builder.append(toVarName(param));
//            builder.append(".URLEscapedString");
            builder.append(")");

            cursor = matcher.end();
        }

        String after = path.substring(cursor);
        builder.append(after);

        return builder.toString();
    }

    private String getDefaultValue(CodegenProperty property) {
        if (property.required == null) {
            return "nil";
        }

        if (!property.required) {
            return "nil";
        }

        String type = property.datatype;

        if (type.equals("Int") || type.equals("Double")
                || type.equals("Float")) {
            return "0";
        }

        if (type.equals("Bool")) {
            return "false";
        }

        if (type.equals("String")) {
            return "\"\"";
        }

        if (property.isListContainer != null && property.isListContainer) {
            return "[:]";
        }

        if (property.isMapContainer != null && property.isMapContainer) {
            return "[:]";
        }

        return "nil";
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

    private String realmFileFolder() {
        return projectName + File.separator + "Realm";
    }

    private String repositoriesFileFolder() { return projectName + File.separator + "Repositories"; }

    private boolean isTrue(Boolean b) {
        return b != null && b;
    }
}
