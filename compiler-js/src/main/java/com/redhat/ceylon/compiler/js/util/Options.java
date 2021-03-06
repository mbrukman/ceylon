package com.redhat.ceylon.compiler.js.util;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import com.redhat.ceylon.common.config.DefaultToolOptions;
import com.redhat.ceylon.common.log.Logger;
import com.redhat.ceylon.compiler.js.DiagnosticListener;
import com.redhat.ceylon.compiler.typechecker.analyzer.Warning;

/** Represents all the options for compiling.
 * 
 * @author Enrique Zamudio
 */
public class Options {

    private File cwd;
    private List<String> repos = new ArrayList<String>();
    private String systemRepo;
    private String user;
    private String pass;
    private List<File> srcDirs = new ArrayList<File>();
    private List<File> resourceDirs = new ArrayList<File>();
    private String resourceRoot = DefaultToolOptions.getCompilerResourceRootName();
    private String outRepo = DefaultToolOptions.getCompilerOutputRepo();
    private boolean optimize = true;
    private boolean modulify = true;
    private boolean comment = true;
    private String verbose;
    private boolean profile;
    private boolean help;
    private boolean version;
    private boolean stdin;
    private boolean gensrc = true;
    private boolean offline;
    private boolean srcmap;
    private boolean minify;
    private String encoding = System.getProperty("file.encoding");
    private String includeDependencies;
    private Logger logger;
    private Writer outWriter;
    private DiagnosticListener diagnosticListener;
    private EnumSet<Warning> suppwarns;

    /** Find all the repos specified in the argument list (pairs of "-rep x").
     * @param args The argument list from which to parse repositories
     * @param remove If true, removes found repos from arguments, otherwise leaves list intact.
     * @return The list of found repositories. */
    public static List<String> findRepos(List<String> args, boolean remove) {
        ArrayList<String> repos = new ArrayList<String>(args.size() / 2);
        for (Iterator<String> iter = args.iterator(); iter.hasNext();) {
            String s = iter.next();
            if ("-rep".equals(s)) {
                if (remove) {
                    iter.remove();
                }
                if (iter.hasNext()) {
                    s = iter.next();
                    repos.add(s);
                    if (remove) {
                        iter.remove();
                    }
                }
            }
        }
        return repos;
    }

    /** Finds the value for an option that requires value. Can remove it from the original list if needed.
     * @param optionName The name for the option (usually starts with "-")
     * @param args The list of arguments where to look for the option and its value
     * @param remove If true, removes the option and its value from the list.
     * @return The value for the specified option, or null if not found. */
    public static String findOptionValue(String optionName, List<String> args, boolean remove) {
        int idx = args.indexOf(optionName);
        if (idx >=0 && idx < args.size() - 2 && !args.get(idx+1).startsWith("-")) {
            if (remove) {
                args.remove(idx);
                return args.remove(idx);
            } else {
                return args.get(idx+1);
            }
        }
        return null;
    }

    /** Finds the specified option among the arguments, and removes it if needed.
     * @param name The option name (usually starts with "-")
     * @param args The list of arguments where to look for the option
     * @param remove If true, removes the option from the arguments.
     * @return true if the option was found, false otherwise. */
    public static boolean findOption(String name, List<String> args, boolean remove) {
        int idx = args.indexOf(name);
        if (idx >= 0) {
            if (remove) {
                args.remove(idx);
            }
            return true;
        }
        return false;
    }

    /** Returns the current working directory (default = null) */
    public File getCwd() {
        return cwd;
    }
    
    public Options cwd(File cwd) {
        this.cwd = cwd;
        return this;
    }

    /** Returns the list of repositories that were parsed from the command line. */
    public List<String> getRepos() {
        return repos;
    }
    
    public Options addRepo(String repo) {
        repos.add(repo);
        return this;
    }
    
    public Options repos(List<String> repos) {
        this.repos.addAll(repos);
        return this;
    }
    
    /** Returns the system repository (default = null) */
    public String getSystemRepo() {
        return systemRepo;
    }

    public Options systemRepo(String systemRepo) {
        this.systemRepo = systemRepo;
        return this;
    }

    public boolean getOffline() {
        return offline;
    }
    public Options offline(boolean flag) {
        offline=flag;
        return this;
    }

    public String getUser() {
        return user;
    }
    
    public Options user(String user) {
        this.user = user;
        return this;
    }

    public String getPass() {
        return pass;
    }
    
    public Options pass(String pass) {
        this.pass = pass;
        return this;
    }

    /** Returns a list of the source directories. By default it's just one, "source". */
    public List<File> getSrcDirs() {
        return srcDirs;
    }
    
    public Options addSrcDir(String src) {
        srcDirs.add(new File(src));
        return this;
    }

    public Options addSrcDir(File src) {
        srcDirs.add(src);
        return this;
    }

    public Options sourceDirs(List<File> srcs) {
        srcDirs.addAll(srcs);
        return this;
    }

    public String getOutRepo() {
        return outRepo;
    }
    
    public Options outRepo(String outRepo) {
        this.outRepo = outRepo;
        return this;
    }

    public boolean isOptimize() {
        return optimize;
    }
    
    public Options optimize(boolean optimize) {
        this.optimize = optimize;
        return this;
    }

    public boolean isModulify() {
        return modulify;
    }
    
    public Options modulify(boolean modulify) {
        this.modulify = modulify;
        return this;
    }

    public boolean isMinify() {
        return minify;
    }
    public Options minify(boolean flag) {
        minify=flag;
        return this;
    }

    public boolean isSourceMaps() {
        return srcmap;
    }
    public Options sourceMaps(boolean flag) {
        srcmap=flag;
        return this;
    }

    @Deprecated
    public boolean isIndent() {
        return false;
    }
    @Deprecated
    public Options indent(boolean indent) {
        return this;
    }

    public boolean isComment() {
        return comment;
    }
    
    public Options comment(boolean comment) {
        this.comment = comment;
        return this;
    }

    public String getVerbose() {
        return verbose;
    }
    
    public Options verbose(String verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isProfile() {
        return profile;
    }
    
    public Options profile(boolean profile) {
        this.profile = profile;
        return this;
    }

    public boolean isVersion() {
        return version;
    }
    
    public Options version(boolean version) {
        this.version = version;
        return this;
    }

    public boolean isStdin() {
        return stdin;
    }
    
    public Options stdin(boolean stdin) {
        this.stdin = stdin;
        return this;
    }

    public boolean isHelp() {
        return help;
    }
    
    public Options help(boolean help) {
        this.help = help;
        return this;
    }

    /** The character encoding to use when reading source files. */
    public String getEncoding() {
        return encoding;
    }
    
    public Options encoding(String encoding) {
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        this.encoding = encoding;
        return this;
    }

    public String getIncludeDependencies() {
        return includeDependencies;
    }
    
    public Options includeDependencies(String includeDependencies) {
        this.includeDependencies = includeDependencies;
        return this;
    }

    /** Sets the option to generate the source archive or skip it.
     * The default is to generate it, but it can be set to false
     * for example when doing joint compilation with another backend
     * that will generate .src archives. */
    public Options generateSourceArchive(boolean flag) {
        gensrc = flag;
        return this;
    }
    
    /** Tells whether to generate the .src archive (default true). */
    public boolean isGenerateSourceArchive() {
        return gensrc;
    }
    
    // Returns true if --verbose or --verbose=all has been passed on the command line
    public boolean isVerbose() {
        return hasVerboseFlag("");
    }

    // Returns true if one of the argument passed matches one of the flags given to
    // --verbose=... on the command line or if one of the flags is "all"
    public boolean hasVerboseFlag(String flag) {
        if (verbose == null) {
            return false;
        }
        if (verbose.isEmpty()) {
            return true;
        }
        List<String> lst = Arrays.asList(verbose.split(","));
        if (lst.contains("all")) {
            return true;
        }
        return lst.contains(flag);
    }

    /** Sets the name of the special folder whose contents will and up in the root of the resulting resource zip. */
    public Options resourceRootName(String resourceRootName) {
        resourceRoot = resourceRootName;
        return this;
    }
    public String getResourceRootName() {
        return resourceRoot;
    }

    /** Sets the list of directories where the resources come from. This is to properly pack the paths. */
    public Options resourceDirs(List<File> value) {
        resourceDirs.clear();
        resourceDirs.addAll(value);
        return this;
    }
    public List<File> getResourceDirs() {
        return resourceDirs;
    }

    public Logger getLogger() {
        return logger;
    }

    public Options logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public Writer getOutWriter() {
        return outWriter;
    }

    public Options outWriter(Writer outWriter) {
        this.outWriter = outWriter;
        return this;
    }

    public DiagnosticListener getDiagnosticListener() {
        return diagnosticListener;
    }

    public Options diagnosticListener(DiagnosticListener diagnosticListener) {
        this.diagnosticListener = diagnosticListener;
        return this;
    }

    public Options suppressWarnings(EnumSet<Warning> value) {
        suppwarns = value;
        return this;
    }
    public EnumSet<Warning> getSuppressWarnings() {
        return suppwarns;
    }

}
