package io.github.karlatemp.jcpg;

public class GeneratingSettings {
    public String projectGroupId;
    public String projectName;
    public String projectVersion;
    public String nativePath;
    public String nativeLibName;
    public String output;
    public String mainPackageName;
    public String nativeResourceLocation; // META-INF/native/project/
    public String scriptsLocation = ".scripts";
    public String mainModuleName;
    public String mainModulePath;
    public boolean genGithubWorkflow;
    public boolean genLibLoader;
    public boolean kotlinScript;
    public String githubBuildArchivePath = "temp/archives";
}
