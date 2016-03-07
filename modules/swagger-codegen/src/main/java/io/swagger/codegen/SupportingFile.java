package io.swagger.codegen;

public class SupportingFile {
    public String templateFile;
    public String folder;
    public String destinationFilename;

    public SupportingFile(String templateFile, String folder, String destinationFilename) {
        this.templateFile = templateFile;
        this.folder = folder;
        this.destinationFilename = destinationFilename;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SupportingFile:").append("\n");
        builder.append("\ttemplateFile: ").append(templateFile).append("\n");
        builder.append("\tfolder: ").append(folder).append("\n");
        builder.append("\tdestinationFilename: ").append(destinationFilename).append("\n");

        return builder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SupportingFile)) {
            return false;
        }

        SupportingFile file = (SupportingFile) object;

        return templateFile.equals(file.templateFile)
            && folder.equals(file.folder)
            && destinationFilename.equals(file.destinationFilename);
    }
}