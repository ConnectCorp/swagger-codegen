package io.swagger.codegen;

import io.swagger.models.ExternalDocs;

import java.util.*;

public class CodegenModel {
    public String id;
    public String parent, parentSchema;
    public String name, classname, description, classVarName, modelJson, dataType;
    public String unescapedDescription;
    public String defaultValue;
    public List<CodegenProperty> vars = new ArrayList<CodegenProperty>();
    public List<CodegenProperty> rawVars = new ArrayList<CodegenProperty>();
    public List<String> allowableValues;

    // list of all required parameters
    public Set<String> mandatory = new HashSet<String>();
    
    public Set<String> imports = new TreeSet<String>();
    public Boolean hasVars, emptyVars, hasMoreModels, hasEnums, isEnum;
    public ExternalDocs externalDocs;

    public Map<String, Object> vendorExtensions;

    public boolean hasId() {
        if (id == null ||  id.isEmpty()) {
            return false;
        }

        for (CodegenProperty var: vars) {
            if (var.name.equals(id)) {
                return true;
            }
        }

        return false;
    }

    public CodegenProperty getId() {
        for (CodegenProperty var: vars) {
            if (var.name.equals(id)) {
                return var;
            }
        }

        return null;
    }

    public boolean hasRawVars() {
        return !rawVars.isEmpty();
    }
}
