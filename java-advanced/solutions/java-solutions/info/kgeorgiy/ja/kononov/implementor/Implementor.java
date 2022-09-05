package info.kgeorgiy.ja.kononov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;

/**
 * @author Vladimir Kononov
 * <p>
 * This class implements {@link JarImpler}
 */
public class Implementor implements JarImpler, Impler {

    /**
     * Empty line for generating code
     */
    private static final String EMPTY_LINE = System.lineSeparator();

    /**
     * Whitespace for generating code
     */
    private static final String SPACE = " ";

    /**
     * 4 Whitespaces for generating code beauty code
     */
    private static final String TAB = "    ";

    /**
     * ';' for generating code
     */
    private static final String SEMICOLON = ";";

    /**
     * Additional suffix for creating implementing interface
     */
    private static final String IMPL = "Impl";

    /**
     * '{' for generating code
     */
    private static final String LEFT_BRACE = "{";

    /**
     * '}' for generating code
     */
    private static final String RIGHT_BRACE = "}";

    /**
     * Class extension for file
     */
    private static final String CLASS = ".class";

    /**
     * java extension for file
     */
    private static final String JAVA = ".java";

    /**
     * Main used for selects the option to run the program.
     * <p>
     * {@link Implementor} runs in 2 ways:
     * <ul>
     *     <li>with 2 args: run {@link #implement(Class, Path) with this arguments}</li>
     *     <li>with 3 args: run {@link #implementJar(Class, Path) with second and third argument}</li>
     * </ul>
     * If args are incorrect: Error is displayed on the console
     *
     * @param args command line arguments to run the program
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3) || args[0] == null || args[1] == null) {
            System.err.println("Arguments should contain 2 or 3not null args");
            return;
        }

        Implementor implementor = new Implementor();
        try {
            if (args.length == 3) {
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            } else {
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Can't find this Class " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Catch Implementor Exception " + e.getMessage());
        }
    }

    /**
     * Function that converts a string to a unicode string.
     *
     * @param s {@link String} to unicode
     * @return converted to unicode {@link String}
     */
    private static String convertStringToUnicode(String s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char nextSym = s.charAt(i);
            if (s.charAt(i) < 128) {
                builder.append(nextSym);
            } else {
                builder.append(String.format("\\u%04X", (int) nextSym));
            }
        }
        return builder.toString();
    }

    /**
     * Returns absolute {@link Path} to the token with extension and converts "." to separator char.
     *
     * @param token {@link Class} path to which is required
     * @param path  {@link Path} directory where {@code token} is located
     * @param dop   {@link String} Additional line to the file path with its extension
     * @return full {@link Path} to this {@code token}
     */
    private static Path getAbsolutePath(Class<?> token, Path path, String dop) {
        return path.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + dop);
    }

    /**
     * Compile java file {@code token} to {@code .class} file.
     *
     * @param token   {@link Class} java file to compile
     * @param tempDir {@link Path} temporary directory where to save the compiled class
     * @throws ImplerException if exit code {@code compiler.run} != 0
     */
    private static void compile(final Class<?> token, final Path tempDir) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String classpath = tempDir + File.pathSeparator + getClassPath(token);
        int resCode = compiler.run(null, null, null, "-classpath",
                classpath,
                getAbsolutePath(token, tempDir, IMPL + JAVA).toString());

        if (resCode != 0) {
            throw new ImplerException("exit code != 0 ");
        }
    }

    /**
     * Returns ClassPath to {@code token}.
     *
     * @param token {@link Class} to get him classpath
     * @return {@link String} classpath to {@code token}
     * @throws AssertionError if can't resolve token's URI
     */
    private static String getClassPath(Class<?> token) {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates {@code .jar} file with compiled implementing interface.
     * Created class with same name of class{@code token} + {@code IMPL}
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if can't generate implementation file because one or more of these reasons:
     *                         <ul>
     *                             <li>1) {@code token} == null</li>
     *                             <li>2) Can't recognise and create {@link Path} of {@code jarFile}</li>
     *                             <li>3) {}{@link #compile(Class, Path)}} can't compile file</li>
     *                             <li>4) {@link IOException} with writing</li>
     *                             <li>5) {{@link #implement(Class, Path)}} can't implement interface</li>
     *                         </ul>
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null) {
            throw new ImplerException("token is null");
        }
        Path tempDir = jarFile.getParent().resolve("tmp");
        implement(token, tempDir);
        compile(token, tempDir);
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))) {
            writer.putNextEntry(new ZipEntry(getNameWithPackage(token, IMPL + CLASS)));
            Files.copy(getAbsolutePath(token, tempDir, IMPL + CLASS), writer);
        } catch (IOException e) {
            throw new ImplerException("couldn't write to jar file " + e.getMessage());
        }
    }

    /**
     * Returns path from source to Class.
     *
     * @param token class path from source to which is required
     * @param dop   additional suffix and file extension for {@code token}
     * @return {@link String} token's package name (with converted "." to "/") + token name + dop
     */
    private static String getNameWithPackage(Class<?> token, String dop) {
        return token.getPackageName().replace('.', '/')
                .concat("/" + token.getSimpleName() + dop);

    }


    /**
     * Creates java file - interface that implements {@code token} interface.
     * Generated class will be named - {@code token} name of class + Impl. It will be located in
     * {@code root} directory. All methods will return null/0/false.
     *
     * @param token type token to create implementation for.
     * @param root  root directory where will be located implementing java file.
     * @throws ImplerException if will be one or more of these reasons:
     *                         <ul>
     *                             <li>1) {@code token} is not interface or private</li>
     *                             <li>2) {@code root} is incorrect path</li>
     *                             <li>3) {@link IOException} with writing</li>
     *                             <li>4) {@link IOException} can't create directory {@code root}</li>
     *                         </ul>
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!checkTokenAndRoot(token, root)) {
            throw new ImplerException("This token must be Public Interface and root must be not null");
        }

        root = getAbsolutePath(token, root, IMPL + JAVA);

        try {
            Path parentDir = root.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't access to directory file" + e.getMessage());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            writer.write(convertStringToUnicode(createHead(token)
                    + createMethods(token)
                    + EMPTY_LINE
                    + RIGHT_BRACE));
        } catch (IOException e) {
            throw new ImplerException("Can't write into Impl file " + e.getMessage());
        }
    }

    /**
     * Checks that token is not private and token is interface and root != null.
     *
     * @param token Checking java {@link Class}
     * @param root  checking {@link Path}
     * @return True | False
     */
    private boolean checkTokenAndRoot(Class<?> token, Path root) {
        return token != null && token.isInterface()
                && !Modifier.isPrivate(token.getModifiers())
                && root != null;
    }

    /**
     * Return Generated String with generated methods for class.
     *
     * @param token {@link Class} interface on the basis of which methods are created
     * @return {@link String} with created methods for Impl file
     */
    private String createMethods(Class<?> token) {
        return Arrays.stream(token.getMethods()).map(this::createMethod).collect(Collectors.joining());
    }

    /**
     * Generate method by its properties and returns correct string with method for java file.
     *
     * @param method {@link Method} that should be generated
     * @return {@link String} with this {@code method}
     */
    private String createMethod(Method method) {
        int mod = method.getModifiers();
        if (Modifier.isStatic(mod) || Modifier.isPrivate(mod)) return "";
        return TAB
                + EMPTY_LINE
                + TAB
                + "public "
                + method.getReturnType().getCanonicalName() + SPACE
                + method.getName() + SPACE
                + generateParametersForMethod(method) + SPACE
                + LEFT_BRACE
                + EMPTY_LINE + TAB + TAB
                + "return "
                + createReturn(method)
                + SEMICOLON + EMPTY_LINE
                + TAB + RIGHT_BRACE
                + EMPTY_LINE;
    }

    /**
     * Create correct return for method.
     * Returns:
     * <ul>
     *     <li>1) "" for void method</li>
     *     <li>2) false for boolean</li>
     *     <li>3) 0 for primitive type</li>
     *     <li>4) null else</li>
     * </ul>
     *
     * @param method {@link Method} that need these generate
     * @return {@link String} - return + "returning object"
     */
    private String createReturn(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.getSimpleName().equals("void")) {
            return "";
        }

        if (returnType.getSimpleName().equals("boolean")) {
            return "false";
        }

        if (returnType.isPrimitive()) {
            return "0";
        }

        return "null";
    }

    /**
     * Generated correct {@link String} in method declaration in brackets.
     *
     * @param method {@link Method} for what generating
     * @return {@link String} (types + args)
     */
    private String generateParametersForMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();

        return IntStream.range(0, parameters.length)
                .mapToObj(i -> parameters[i].getCanonicalName() + " a" + i)
                .collect(Collectors.joining(",", "(", ")"));
    }

    /**
     * Generating Head of Impl file with package and declaration class.
     *
     * @param token {@link Class} According to the template of which interface the generation should be
     * @return {@link String} head if Impl
     */
    private String createHead(Class<?> token) {
        return getPackage(token)
                + "public class "
                + token.getSimpleName()
                + IMPL + SPACE
                + "implements "
                + token.getCanonicalName() + SPACE
                + LEFT_BRACE
                + EMPTY_LINE;
    }

    /**
     * Returns package name of this {@link Class} {@code token}.
     * Returns "" if {@code token} haven't package
     *
     * @param token {@link Class} for getting package
     * @return "" | correct {@link String} like in java file with declaration package
     */
    private String getPackage(Class<?> token) {
        if (token.getPackage() == null || token.getPackageName().equals("")) {
            return EMPTY_LINE;
        }

        return "package "
                + token.getPackageName()
                + SEMICOLON
                + EMPTY_LINE;
    }


}
