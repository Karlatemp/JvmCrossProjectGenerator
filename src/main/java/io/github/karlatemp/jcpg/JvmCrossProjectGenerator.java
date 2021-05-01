package io.github.karlatemp.jcpg;

import com.google.gson.Gson;
import io.github.karlatemp.jcpg.generator.DirBasedGenerator;
import io.github.karlatemp.jcpg.generator.ObserverGenerator;
import io.github.karlatemp.jcpg.generator.StdoutGenerator;
import io.github.karlatemp.jcpg.generator.ZippedProjectGenerator;
import org.fusesource.jansi.Ansi;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresource.ITemplateResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("CodeBlock2Expr")
public class JvmCrossProjectGenerator {
    public static ITemplateEngine TEMPLATE_ENGINE;
    public static Terminal TERMINAL;
    public static LineReader LINE_READER;

    public static class JCompleter {
        public static final Completer
                READ_BOOLEAN = new StringsCompleter("true", "false", "yes", "no", "Y", "N"),
                FILE = new Completers.FileNameCompleter(),
                EMP = new NullCompleter();
        private static final Map<String, Boolean> BOOLEAN_TABLE = Map.of(
                "true", true,
                "false", false,
                "yes", true,
                "no", false,
                "Y", true,
                "y", true,
                "N", false,
                "n", false
        );

        public static String readLineWithCompleter(Completer c, String prompt) {
            DelegatingCompleter.INSTANCE.delegate = c;
            String l = readLine(prompt);
            DelegatingCompleter.INSTANCE.delegate = null;
            return l;
        }

        public static boolean readBoolean(String prompt) {
            String rs = readLineWithCompleter(READ_BOOLEAN, prompt + " [Y/N]");
            return BOOLEAN_TABLE.getOrDefault(rs, false);
        }

        public static String readFile(String prompt) {
            return readLineWithCompleter(FILE, prompt);
        }

        public static String readLine(String prompt) {
            return LINE_READER.readLine(prompt + "> ");
        }

        public static String readLineOrDefault(String prompt, String def) {
            String s = readLine(prompt);
            if (s.isBlank()) {
                return def;
            }
            return s;
        }
    }

    public static class DelegatingCompleter implements Completer {
        public static final DelegatingCompleter INSTANCE = new DelegatingCompleter();
        public Completer delegate;

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            Completer d = this.delegate;
            if (d != null) d.complete(reader, line, candidates);
        }
    }

    private static void initializeTerminal() throws Exception {
        TERMINAL = TerminalBuilder.terminal();
        LINE_READER = LineReaderBuilder.builder()
                .terminal(TERMINAL)
                .completer(DelegatingCompleter.INSTANCE)
                .history(null)
                .option(LineReader.Option.USE_FORWARD_SLASH, true)
                .build();

        org.slf4j.LoggerFactory.getLogger(JvmCrossProjectGenerator.class);
        // Wait slf4j error printed
        Thread.sleep(1500);
    }

    private static void initializeEngine() {
        TemplateEngine engine;
        TEMPLATE_ENGINE = engine = new TemplateEngine();
        engine.setTemplateResolver(new ITemplateResolver() {
            final ClassLoader ccl = getClass().getClassLoader();

            @Override
            public String getName() {
                return "TMS";
            }

            @Override
            public Integer getOrder() {
                return 0;
            }

            @Override
            public TemplateResolution resolveTemplate(
                    IEngineConfiguration configuration,
                    String ownerTemplate,
                    String template,
                    Map<String, Object> templateResolutionAttributes
            ) {
                String n = "jcpg-templates/" + template + ".txt";
                if (ccl.getResource(n) != null) {
                    return new TemplateResolution(
                            new ITemplateResource() {
                                @Override
                                public String getDescription() {
                                    return null;
                                }

                                @Override
                                public String getBaseName() {
                                    return template;
                                }

                                @Override
                                public boolean exists() {
                                    return true;
                                }

                                @Override
                                public Reader reader() throws IOException {
                                    return new InputStreamReader(
                                            Objects.requireNonNull(ccl.getResourceAsStream(n), "Assertion error: `" + n + "` is null"),
                                            StandardCharsets.UTF_8
                                    );
                                }

                                @Override
                                public ITemplateResource relative(String relativeLocation) {
                                    throw new UnsupportedOperationException();
                                }
                            },
                            TemplateMode.TEXT,
                            AlwaysValidCacheEntryValidity.INSTANCE
                    );
                }
                return null;
            }
        });
    }

    public static GeneratingSettings readSettings() throws Exception {
        LINE_READER.printAbove(Ansi.ansi().fgBrightYellow().a("JvmCrossProjectGenerator ").reset().a("by Karlatemp").toString());
        if (JCompleter.readBoolean("Has Local settings?")) {
            String file = JCompleter.readFile("Settings Location");
            LINE_READER.printAbove(
                    Ansi.ansi().a("Loading Settings from ").fgBlue().a(file).reset().toString()
            );
            try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                return new Gson().fromJson(reader, GeneratingSettings.class);
            }
        }
        GeneratingSettings settings = new GeneratingSettings();
        settings.output = JCompleter.readLine("output location");
        LINE_READER.printAbove(Ansi.ansi()
                .a("Project will save to ").fgBrightYellow().a(settings.output)
                .reset().toString()
        );
        settings.projectName = JCompleter.readLine("Project name");
        settings.projectVersion = JCompleter.readLine("Project version");
        settings.projectGroupId = JCompleter.readLine("Project group id");
        settings.mainPackageName = JCompleter.readLine("Application package name");
        settings.kotlinScript = JCompleter.readBoolean("Use Kotlin Script build script?");
        if (JCompleter.readBoolean("Need put source files in a submodule?")) {
            settings.mainModuleName = settings.mainModulePath = JCompleter.readLine("Main module name");
            if (JCompleter.readBoolean("Need specify the location of the main module?")) {
                settings.mainModulePath = JCompleter.readLine("Location of main module (Relative to the generated project)");
            }
        }
        settings.nativePath = JCompleter.readLine("Native subproject location (Relative to the generated project)");
        settings.nativeLibName = JCompleter.readLine("Native subproject artifact name (like 'nativelib')");
        settings.nativeResourceLocation = JCompleter.readLineOrDefault(
                "Native lib packed jar location (`Enter` for default value)",
                "META-INF/native/" + settings.projectName + "/"
        );
        settings.genLibLoader = JCompleter.readBoolean("Generate NativeLibLoader.java?");
        settings.genGithubWorkflow = JCompleter.readBoolean("Generate Github Workflow?");
        if (settings.genGithubWorkflow) {
            settings.scriptsLocation = JCompleter.readLineOrDefault(
                    "The scripts location (`Enter` for default)",
                    ".scripts"
            );
            settings.githubBuildArchivePath = JCompleter.readLineOrDefault(
                    "Github Temperature using direction (`Enter` for default)",
                    "temp/archives"
            );
        }
        if (JCompleter.readBoolean("Save settings to local?")) {
            String output = JCompleter.readFile("Location");
            File parentFile = new File(output).getParentFile();
            if (parentFile != null) parentFile.mkdirs();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(output), StandardCharsets.UTF_8
            ))) {
                new Gson().toJson(settings, writer);
            }
        }
        return settings;
    }

    public static void readOptions(GeneratingSettings settings) throws Exception {

        settings.projectName = "Test";
        settings.kotlinScript = false;
        settings.mainModuleName = settings.mainModulePath = "main";
        settings.projectGroupId = "org.example.generated";
        settings.projectVersion = "1.0.0";
        settings.nativePath = "native";
        settings.nativeLibName = "testing";
        settings.mainPackageName = "org.example.nativetest";
        settings.nativeResourceLocation = "META-INF/native/project/";
        settings.genLibLoader = true;
        settings.genGithubWorkflow = true;
    }

    private static final ClassLoader CCL = JvmCrossProjectGenerator.class.getClassLoader();

    private static void copyResource(String name, String to, ProjectGenerator generator) throws Exception {
        generator.writeFile(to, stream -> {
            try (InputStream rs = CCL.getResourceAsStream("jcpg-resources/" + name)) {
                assert rs != null;
                rs.transferTo(stream);
            }
        });
    }

    public static void doGen(ProjectGenerator generator, GeneratingSettings settings) throws Exception {

        var context = new Context();
        context.setVariable("settings", settings);

        { // gradle scripts
            generator.writeFileW("settings.gradle" + (
                    settings.kotlinScript ? ".kts" : ""
            ), writer -> {
                TEMPLATE_ENGINE.process("gradle/settings", context, writer);
                if (settings.genGithubWorkflow) {
                    writer.append("\ninclude(\":ci-release-helper\")\n");
                }
            });
            generator.writeFileW("build.gradle" + (
                    settings.kotlinScript ? ".kts" : ""
            ), writer -> {
                TEMPLATE_ENGINE.process("gradle/build", context, writer);
            });
            copyResource("gradlew.sh", "gradlew", generator);
            copyResource("gradle/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar", generator);
            copyResource("gradle/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.properties", generator);
            if (settings.mainModulePath != null) {
                generator.writeFileW(settings.mainModulePath + "/build.gradle" + (
                        settings.kotlinScript ? ".kts" : ""
                ), writer -> {
                    TEMPLATE_ENGINE.process("gradle/submain.build", context, writer);
                });
            }
        }
        { // native
            generator.writeFileW(settings.nativePath + "/CMakeLists.txt", writer -> {
                TEMPLATE_ENGINE.process("native/CMakeList", context, writer);
            });
            {
                // includes
                File includes = null, tmp;
                var kit = new Object() {
                    String removeJreSuffix(String javahome) {
                        javahome = javahome.replace('\\', '/');
                        if (javahome.endsWith("/")) {
                            javahome = javahome.substring(0, javahome.length() - 1);
                        }
                        if (javahome.endsWith("/jre")) {
                            javahome = javahome.substring(0, javahome.length() - 4);
                        }
                        return javahome;
                    }
                };
                String javaHome = kit.removeJreSuffix(System.getProperty("java.home"));
                tmp = new File(javaHome, "include");
                if (tmp.isDirectory()) {
                    includes = tmp;
                } else {
                    javaHome = kit.removeJreSuffix(System.getenv("JAVA_HOME"));
                    tmp = new File(javaHome, "include");
                    if (tmp.isDirectory()) {
                        includes = tmp;
                    }
                }
                if (includes == null) {
                    System.err.println("WARNING: Can't found includes.");
                } else {
                    for (File f : Optional.ofNullable(
                            includes.listFiles()
                    ).orElse(new File[0])) {
                        if (f.isFile()) {
                            generator.writeFileWB(settings.nativePath + "/includes/jni/" + f.getName(), writer -> {
                                try (BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
                                    while (true) {
                                        String line = is.readLine();
                                        if (line == null) break;
                                        writer.append(line).append('\n');
                                    }
                                }
                            });
                        }
                    }
                    copyResource("native-includes/unix/jni_md.h", settings.nativePath + "/includes/jni/unix/jni_md.h", generator);
                    copyResource("native-includes/win32/jni_md.h", settings.nativePath + "/includes/jni/win32/jni_md.h", generator);
                }
            }
            generator.writeFileW(settings.nativePath + "/src/lib.cpp", writer -> {
                writer.append("#include <jni.h>\n");
            });
        }
        { // java classes
            String prefix = (settings.mainModuleName == null ? "" : settings.mainModuleName + '/')
                    + "src/main/java/" + settings.mainPackageName.replace('.', '/') + '/';
            if (settings.genLibLoader) { // Lib loader
                generator.writeFileW(prefix + "NativeLibLoader.java", writer -> {
                    TEMPLATE_ENGINE.process("classes/loader", context, writer);
                });
            }
        }

        if (settings.genGithubWorkflow) {
            generator.writeFileW(settings.scriptsLocation + "/setup_docker.sh", writer -> {
                TEMPLATE_ENGINE.process("workflow/docker", context, writer);
            });
            generator.writeFileW(settings.scriptsLocation + "/build.sh", writer -> {
                TEMPLATE_ENGINE.process("workflow/build", context, writer);
            });
            generator.writeFileW(".github/workflows/build.yml", writer -> {
                TEMPLATE_ENGINE.process("workflow/workflow", context, writer);
            });
            generator.writeFileW("ci-release-helper/build.gradle", writer -> {
                TEMPLATE_ENGINE.process("gradle/ci-release-helper", context, writer);
            });
        }
        generator.writeFileW(".gitignore", writer -> {
            writer.append(".idea").append('\n');
            writer.append(".gradle").append('\n');
            writer.append("*.class").append('\n');
            writer.append("*.zip").append('\n');
            writer.append("/").append(settings.nativePath).append("/cmake-build-debug").append('\n');
            writer.append("build/").append('\n');
            if (settings.genGithubWorkflow) {
                writer.append('/').append(settings.githubBuildArchivePath).append('\n');
            }
        });
        // release
        if (generator instanceof Closeable) {
            ((Closeable) generator).close();
        } else if (generator instanceof AutoCloseable) {
            ((AutoCloseable) generator).close();
        }
    }

    private static void test0() throws Exception {
        var settings = new GeneratingSettings();
        readOptions(settings);
//        ProjectGenerator generator = new StdoutGenerator(false);
//        ProjectGenerator generator = new ZippedProjectGenerator(StandardCharsets.UTF_8, new File("generated/g.zip"));
        ProjectGenerator generator = new DirBasedGenerator(StandardCharsets.UTF_8, new File("generated/oot"));
        doGen(generator, settings);

        StdoutGenerator g1;
        ZippedProjectGenerator z1;
        DirBasedGenerator d1;
    }

    public static void main(String[] args) throws Exception {
        initializeEngine();
        initializeTerminal();
        GeneratingSettings settings = readSettings();
        doGen(new ObserverGenerator(new DirBasedGenerator(
                StandardCharsets.UTF_8,
                new File(settings.output)
        )), settings);
    }
}
