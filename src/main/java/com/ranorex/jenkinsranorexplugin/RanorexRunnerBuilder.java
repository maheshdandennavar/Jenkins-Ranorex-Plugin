package com.ranorex.jenkinsranorexplugin;

import com.ranorex.jenkinsranorexplugin.util.CmdArgument;
import com.ranorex.jenkinsranorexplugin.util.FileUtil;
import com.ranorex.jenkinsranorexplugin.util.RanorexParameter;
import com.ranorex.jenkinsranorexplugin.util.StringUtil;
import hudson.*;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;

public class RanorexRunnerBuilder extends Builder implements SimpleBuildStep {

    private static final String ZIPPED_REPORT_EXTENSION = ".rxzlog";
    private static final String ARGUMENT_SEPARATOR = "\t\r\n;";
    /*
     * Builder GUI Fields
     */
    @CheckForNull
    private String rxTestSuiteFilePath;
    @CheckForNull
    private String rxRunConfiguration;
    @CheckForNull
    private String rxReportDirectory;
    @CheckForNull
    private String rxReportFile;
    @CheckForNull
    private String rxReportExtension;
    @CheckForNull
    private boolean rxJUnitReport;
    @CheckForNull
    private boolean rxZippedReport;
    @CheckForNull
    private String rxZippedReportDirectory;
    @CheckForNull
    private String rxZippedReportFile;
    @CheckForNull
    private String rxGlobalParameter;
    @CheckForNull
    private String cmdLineArgs;
    @CheckForNull
    private boolean rxTestRail;
    @CheckForNull
    private String rxTestRailUser;
    @CheckForNull
    private String rxTestRailPassword;
    @CheckForNull
    private String rxTestRailRID;
    @CheckForNull
    private String rxTestRailRunName;

    /*
     * Other Variables
     */
    private String rxExecuteableFile;
    private String workSpace;
    private String usedRxReportDirectory;
    private String usedRxReportFile;
    private String usedRxZippedReportDirectory;
    private String usedRxZippedReportFile;
    private ArgumentListBuilder jArguments;
    private PrintStream logger;

    /**
     * When this builder is created in the project configuration step, the builder
     * object will be created
     **/
    @DataBoundConstructor
    public RanorexRunnerBuilder() {
    }

    /**
     * When this builder is created in the project configuration step, the builder
     * object will be created from the strings below
     *
     * @param rxTestSuiteFilePath     The name/location of the Ranorex Test Suite /
     *                                Ranorex Test Exe File
     * @param rxRunConfiguration      The Ranorex Run configuration which will be
     *                                executed
     * @param rxReportDirectory       The directory where the Ranorex Report should
     *                                be saved
     * @param rxReportFile            The name of the Ranorex Report
     * @param rxReportExtension       The extension of your Ranorex Report
     * @param rxJUnitReport           If true, a JUnit compatible Report will be
     *                                saved
     * @param rxZippedReport          If true, the report will also be saved as
     *                                RXZLOG
     * @param rxZippedReportDirectory The directory where the Ranorex Zipped Report
     *                                should be saved
     * @param rxZippedReportFile      The name of the zipped Ranorex Report
     * @param rxTestRail              True when test rail is used
     * @param rxTestRailUser          Username for connecting to TestRail
     * @param rxTestRailPassword      Password for connecting to TestRail
     * @param rxTestRailRID           TestRail RID
     * @param rxTestRailRunName       TestRail run name
     * @param rxGlobalParameter       Global test suite parameters
     * @param cmdLineArgs             Additional CMD line arguments
     */
    @Deprecated
    public RanorexRunnerBuilder(String rxTestSuiteFilePath, String rxRunConfiguration, String rxReportDirectory,
                                String rxReportFile, String rxReportExtension, Boolean rxJUnitReport, Boolean rxZippedReport,
                                String rxZippedReportDirectory, String rxZippedReportFile, Boolean rxTestRail, String rxTestRailUser,
                                String rxTestRailPassword, String rxTestRailRID, String rxTestRailRunName, String rxGlobalParameter,
                                String cmdLineArgs) {
        this.rxTestSuiteFilePath = rxTestSuiteFilePath.trim();
        this.rxRunConfiguration = rxRunConfiguration.trim();
        this.rxReportDirectory = rxReportDirectory.trim();
        this.rxReportFile = rxReportFile.trim();
        this.rxReportExtension = rxReportExtension.trim();
        this.rxJUnitReport = rxJUnitReport;
        this.rxZippedReport = rxZippedReport;
        this.rxZippedReportDirectory = rxZippedReportDirectory.trim();
        this.rxZippedReportFile = rxZippedReportFile.trim();
        this.rxTestRail = rxTestRail;
        this.rxTestRailUser = rxTestRailUser.trim();
        this.rxTestRailPassword = rxTestRailPassword.trim();
        this.rxTestRailRID = rxTestRailRID.trim();
        this.rxTestRailRunName = rxTestRailRunName.trim();
        this.rxGlobalParameter = rxGlobalParameter.trim();
        this.cmdLineArgs = cmdLineArgs.trim();

    }

    @CheckForNull
    public String getRxTestSuiteFilePath() {
        return this.rxTestSuiteFilePath;
    }

    @CheckForNull
    public String getRxRunConfiguration() {
        return this.rxRunConfiguration;
    }

    @CheckForNull
    public String getRxReportDirectory() {
        return this.rxReportDirectory;
    }

    @CheckForNull
    public String getRxReportFile() {
        return this.rxReportFile;
    }

    @CheckForNull
    public String getRxReportExtension() {
        return this.rxReportExtension;
    }

    @CheckForNull
    public Boolean getRxJUnitReport() {
        return this.rxJUnitReport;
    }

    @CheckForNull
    public Boolean getRxZippedReport() {
        return this.rxZippedReport;
    }

    @CheckForNull
    public String getRxZippedReportDirectory() {
        return this.rxZippedReportDirectory;
    }

    @CheckForNull
    public String getRxZippedReportFile() {
        return this.rxZippedReportFile;
    }

    @CheckForNull
    public Boolean getRxTestRail() {
        return this.rxTestRail;
    }

    @CheckForNull
    public String getRxTestRailUser() {
        return this.rxTestRailUser;
    }

    @CheckForNull
    public String getRxTestRailPassword() {
        return this.rxTestRailPassword;
    }

    @CheckForNull
    public String getRxTestRailRID() {
        return this.rxTestRailRID;
    }

    @CheckForNull
    public String getRxTestRailRunName() {
        return this.rxTestRailRunName;
    }

    @CheckForNull
    public String getRxGlobalParameter() {
        return this.rxGlobalParameter;
    }

    @CheckForNull
    public String getCmdLineArgs() {
        return this.cmdLineArgs;
    }

    @DataBoundSetter
    public void setRxTestSuiteFilePath(@CheckForNull String rxTestSuiteFilePath) {
        this.rxTestSuiteFilePath = Util.fixNull(rxTestSuiteFilePath);
    }

    @DataBoundSetter
    public void setRxRunConfiguration(@CheckForNull String rxRunConfiguration) {
        this.rxRunConfiguration = Util.fixNull(rxRunConfiguration);
    }

    @DataBoundSetter
    public void setRxReportDirectory(@CheckForNull String rxReportDirectory) {
        this.rxReportDirectory = Util.fixNull(rxReportDirectory);
    }

    @DataBoundSetter
    public void setRxReportFile(@CheckForNull String rxReportFile) {
        this.rxReportFile = Util.fixNull(rxReportFile);
    }

    @DataBoundSetter
    public void setRxReportExtension(@CheckForNull String rxReportExtension) {
        this.rxReportExtension = Util.fixNull(rxReportExtension);
    }

    @DataBoundSetter
    public void setRxJUnitReport(@CheckForNull Boolean rxJUnitReport) {
        this.rxJUnitReport = BooleanUtils.isTrue(rxJUnitReport);
    }

    @DataBoundSetter
    public void setRxZippedReport(Boolean rxZippedReport) {
        this.rxZippedReport = BooleanUtils.isTrue(rxZippedReport);
    }

    @DataBoundSetter
    public void setRxZippedReportDirectory(@CheckForNull String rxZippedReportDirectory) {
        this.rxZippedReportDirectory = Util.fixNull(rxZippedReportDirectory);
    }

    @DataBoundSetter
    public void setRxZippedReportFile(@CheckForNull String rxZippedReportFile) {
        this.rxZippedReportFile = Util.fixNull(rxZippedReportFile);
    }

    @DataBoundSetter
    public void setRxTestRail(Boolean rxTestRail) {
        this.rxTestRail = BooleanUtils.isTrue(rxTestRail);
    }

    @DataBoundSetter
    public void setRxTestRailUser(@CheckForNull String rxTestRailUser) {
        this.rxTestRailUser = Util.fixNull(rxTestRailUser);
    }

    @DataBoundSetter
    public void setRxTestRailPassword(@CheckForNull String rxTestRailPassword) {
        this.rxTestRailPassword = Util.fixNull(rxTestRailPassword);
    }

    @DataBoundSetter
    public void setRxTestRailRID(@CheckForNull String rxTestRailRID) {
        this.rxTestRailRID = Util.fixNull(rxTestRailRID);
    }

    @DataBoundSetter
    public void setRxTestRailRunName(@CheckForNull String rxTestRailRunName) {
        this.rxTestRailRunName = Util.fixNull(rxTestRailRunName);
    }

    @DataBoundSetter
    public void setRxGlobalParameter(@CheckForNull String rxGlobalParameter) {
        this.rxGlobalParameter = Util.fixNull(rxGlobalParameter);
    }

    @DataBoundSetter
    public void setCmdLineArgs(@CheckForNull String CmdLineArgs) {
        this.cmdLineArgs = Util.fixNull(CmdLineArgs);
    }

    /**
     * Runs the step over the given build and reports the progress to the listener
     *
     * @param run       Run instance
     * @param workspace Current workspace
     * @param launcher  Starts a process
     * @param listener  Receives events that happen during a build
     * @throws IOException          - If the build is interrupted by the user (in an
     *                              attempt to abort the build.) Normally the
     *                              BuildStep implementations may simply forward the
     *                              exception it got from its lower-level functions.
     * @throws InterruptedException - If the implementation wants to abort the
     *                              processing when an IOException happens, it can
     *                              simply propagate the exception to the caller.
     *                              This will cause the build to fail, with the
     *                              default error message. Implementations are
     *                              encouraged to catch IOException on its own to
     *                              provide a better error message, if it can do so,
     *                              so that users have better understanding on why
     *                              it failed.
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) throws InterruptedException, IOException {
        EnvVars env;
        FilePath wSpace;

        if (run instanceof AbstractBuild) {
            wSpace = ((AbstractBuild<?, ?>) run).getWorkspace();
            env = run.getEnvironment(listener);
            env.overrideAll(((AbstractBuild<?, ?>) run).getBuildVariables());
        } else {
            wSpace = workspace;
            env = new EnvVars();
        }

        jArguments = new ArgumentListBuilder("cmd.exe", "/C");
        workSpace = FileUtil.getRanorexWorkingDirectory(wSpace, rxTestSuiteFilePath).getRemote();
        workSpace = StringUtil.appendBackslash(workSpace);
        logger = listener.getLogger();

        if (! StringUtil.isNullOrSpace(rxTestSuiteFilePath)) {
            rxExecuteableFile = FileUtil.getExecutableFromTestSuite(rxTestSuiteFilePath);
            jArguments.add(rxExecuteableFile);
            // Ranorex Run Configuration
            if (! StringUtil.isNullOrSpace(rxRunConfiguration)) {
                jArguments.add("/runconfig:" + rxRunConfiguration);
            }

            // Ranorex Reportdirectory
            if (! StringUtil.isNullOrSpace(rxReportDirectory)) {
                logger.println("Reportpath to merge. Base: " + workSpace + " Relative: " + rxReportDirectory);
                usedRxReportDirectory = FileUtil.getAbsoluteReportDirectory(workSpace, rxReportDirectory);
                logger.println("Merged path: " + usedRxReportDirectory);
            } else {
                usedRxReportDirectory = workSpace;
            }
            usedRxReportDirectory = StringUtil.appendBackslash(usedRxReportDirectory);

            // ReportFilename
            if (! StringUtil.isNullOrSpace(rxReportFile)) {
                if (! FileUtil.isAbsolutePath(rxReportFile)) {
                    usedRxReportFile = FileUtil.removeFileExtension(rxReportFile);
                } else {
                    logger.println("'" + rxReportFile + "' is not a valid Ranorex Report filename");
                    return;
                }
            } else {
                usedRxReportFile = "%S_%Y%M%D_%T";
            }
            jArguments.add("/reportfile:" + usedRxReportDirectory + usedRxReportFile + "." + rxReportExtension);

            // JUnit compatible Report
            if (BooleanUtils.isTrue(rxJUnitReport)) {
                jArguments.add("/junit");
            }

            // Compressed copy of Ranorex report
            if (BooleanUtils.isTrue(rxZippedReport)) {
                jArguments.add("/zipreport");
                // Zipped Ranorex Reportdirectory
                if (! StringUtil.isNullOrSpace(rxZippedReportDirectory)) {
                    usedRxZippedReportDirectory = FileUtil.getAbsoluteReportDirectory(workSpace,
                            rxZippedReportDirectory);
                } else {
                    usedRxZippedReportDirectory = workSpace;
                }
                usedRxZippedReportDirectory = StringUtil.appendBackslash(usedRxZippedReportDirectory);

                // Zipped Report File Name
                if (! StringUtil.isNullOrSpace(rxZippedReportFile)) {
                    if (! FileUtil.isAbsolutePath(rxZippedReportFile)) {
                        usedRxZippedReportFile = FileUtil.removeFileExtension(rxZippedReportFile);
                    } else {
                        logger.println("'" + rxZippedReportFile + "' is not a valid Ranorex Report filename");
                        return;
                    }
                } else {
                    usedRxZippedReportFile = usedRxReportFile;
                }

                jArguments.add("/zipreportfile:" + usedRxZippedReportDirectory + usedRxZippedReportFile
                        + ZIPPED_REPORT_EXTENSION);
            }

            // Test Rail
            if (BooleanUtils.isTrue(rxTestRail)) {
                jArguments.add("/testrail");
                if (! StringUtil.isNullOrSpace(rxTestRailUser) && ! StringUtil.isNullOrSpace(rxTestRailPassword)) {
                    jArguments.addMasked("/truser=" + rxTestRailUser);
                    jArguments.addMasked("/trpass=" + rxTestRailPassword);
                } else {
                    logger.println("Testrail username and password are required");
                    return;
                }
                if (! StringUtil.isNullOrSpace(rxTestRailRID)) {
                    jArguments.add("/trrunid=" + rxTestRailRID);
                }
                if (! StringUtil.isNullOrSpace(rxTestRailRunName)) {
                    jArguments.add("/trrunname=" + rxTestRailRunName);
                }
            }

            // Parse Global Parameters
            if (! StringUtil.isNullOrSpace(rxGlobalParameter)) {
                for (String param : StringUtil.splitBy(rxGlobalParameter, ARGUMENT_SEPARATOR)) {
                    try {
                        RanorexParameter rxParam = new RanorexParameter(param);
                        rxParam.trim();
                        jArguments.add(rxParam.toString());
                    } catch (Exception e) {
                        System.out.println("[INFO] [RanorexRunnerBuilder] Parameter '" + param + "' will be ignored");
                    }
                }
            }

            // Additional cmd arguments
            if (! StringUtil.isNullOrSpace(cmdLineArgs)) {
                for (String argument : StringUtil.splitBy(cmdLineArgs, ARGUMENT_SEPARATOR)) {
                    try {
                        CmdArgument arg = new CmdArgument(argument);
                        jArguments.add(arg.toString());
                    } catch (Exception e) {
                        System.out
                                .println("[INFO] [RanorexRunnerBuilder] Argument '" + argument + "' will be ignored ");
                    }
                }
            }
            // Summarize Output
            if (getDescriptor().isUseSummarize()) {
                logger.println("\n*************Start of Ranorex Summary*************");
                logger.println("Current Plugin version:\t\t" + getClass().getPackage().getImplementationVersion());
                logger.println("Ranorex Working Directory:\t" + workSpace);
                logger.println("Ranorex test suite file:\t" + rxTestSuiteFilePath);
                logger.println("Ranorex test exe file:\t\t" + rxExecuteableFile);
                logger.println("Ranorex run configuration:\t" + rxRunConfiguration);
                logger.println("Ranorex report directory:\t" + usedRxReportDirectory);
                logger.println("Ranorex report filename:\t" + usedRxReportFile);
                logger.println("Ranorex report extension:\t" + rxReportExtension);
                logger.println("Junit-compatible report:\t" + rxJUnitReport);
                logger.println("Ranorex report compression:\t" + rxZippedReport);
                if (rxZippedReport) {
                    logger.println("\tRanorex zipped report dir:\t" + usedRxZippedReportDirectory);
                    logger.println("\tRanorex zipped report file:\t" + usedRxZippedReportFile);
                }
                logger.println("Ranorex Test Rail Integration:\t" + rxTestRail);
                if (rxTestRail) {
                    logger.println("\tRanorex Test Rail User:\t\t" + rxTestRailUser);
                    logger.println("\tRanorex Test Rail Password:\t" + "*****************");
                    logger.println("\tRanorex Test Rail Run ID:\t" + rxTestRailRID);
                    logger.println("\tRanorex Test Rail Run Name:\t" + rxTestRailRunName);
                }
                logger.println("Ranorex global parameters:");
                if (! StringUtil.isNullOrSpace(rxGlobalParameter)) {
                    for (String param : StringUtil.splitBy(rxGlobalParameter, ARGUMENT_SEPARATOR)) {
                        try {
                            RanorexParameter rxParam = new RanorexParameter(param);
                            rxParam.trim();
                            logger.println("\t*" + rxParam.toString());
                        } catch (Exception e) {
                            logger.println("\t!" + param + " will be ignored");
                        }
                    }
                } else {
                    logger.println("\t*No global parameters entered");
                }
                logger.println("Command line arguments:");
                if (! StringUtil.isNullOrSpace(cmdLineArgs)) {
                    for (String argument : StringUtil.splitBy(cmdLineArgs, ARGUMENT_SEPARATOR)) {
                        try {
                            CmdArgument arg = new CmdArgument(argument);
                            arg.trim();
                            logger.println("\t*" + arg.toString());
                        } catch (Exception e) {
                            logger.println("\t!" + argument + " will be ignored ");
                        }
                    }
                } else {
                    logger.println("\t*No command line arguments entered");
                }
                logger.println("*************End of Ranorex Summary*************\n");
            }

            FilePath currentWorkspace = FileUtil.getRanorexWorkingDirectory(workspace, rxTestSuiteFilePath);
            logger.println("Executing : " + jArguments.toString());
            try {
                int r = launcher.launch().cmds(jArguments).envs(env).stdout(listener).pwd(currentWorkspace).join();

                if (r != 0) {
                    run.setResult(Result.FAILURE);
                }
            } catch (Exception e) {
                e.printStackTrace(listener.fatalError("execution failed"));
                run.setResult(Result.FAILURE);
            }
        } else {
            logger.println("No TestSuite file given");
        }
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();

    }

    @Symbol ("ranorex")
    @Extension // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /*
         * Configure Variables
         */
        private boolean useSummarize;

        /**
         * In order to load the persisted global configuration, you have to call load()
         * in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        // Check Report Directory
        public FormValidation doCheckRxReportDirectory(@QueryParameter String value) {
            if (! StringUtil.isNullOrSpace(value)) {
                return FormValidation.ok();
            } else {
                return FormValidation.warning("Current Ranorex Working directory will be used");
            }
        }

        // Check Report Filename
        public FormValidation doCheckRxReportFile(@QueryParameter String value) {
            if (! StringUtil.isNullOrSpace(value) && ! FileUtil.isAbsolutePath(value)) {
                return FormValidation.ok();
            } else if (FileUtil.isAbsolutePath(value)) {
                return FormValidation.error("'" + value + "' is not a valid Ranorex Report filename");
            } else {
                return FormValidation.warning("'%S_%Y%M%D_%T' will be used");
            }
        }

        // Check Zipped Report Directory
        public FormValidation doCheckRxZippedReportDirectory(@QueryParameter String value) {
            if (! StringUtil.isNullOrSpace(value)) {
                return FormValidation.ok();
            } else {
                return FormValidation.warning("Current Ranorex Working directory will be used");
            }
        }

        // Check Zipped Report Filename
        public FormValidation doCheckRxZippedReportFile(@QueryParameter String value,
                                                        @QueryParameter String rxReportFile) {
            if (! StringUtil.isNullOrSpace(value) && ! FileUtil.isAbsolutePath(value)) {
                return FormValidation.ok();
            } else if (FileUtil.isAbsolutePath(value)) {
                return FormValidation.error("'" + value + "' is not a valid Ranorex Report filename");
            } else if ((StringUtil.isNullOrSpace(value) && StringUtil.isNullOrSpace(rxReportFile))
                    || (StringUtil.isNullOrSpace(value) && FileUtil.isAbsolutePath(rxReportFile))) {
                return FormValidation.warning("'%S_%Y%M%D_%T' will be used");
            } else {
                return FormValidation.warning("'" + rxReportFile + "' will be used");
            }
        }

        // Check Test Rail Username
        public FormValidation doCheckRxTestRailUser(@QueryParameter String value) {
            if (! StringUtil.isNullOrSpace(value)) {
                return FormValidation.ok();
            }
            return FormValidation.error("Username is required");

        }

        // Check Test Rail Password
        public FormValidation doCheckRxTestRailPassword(@QueryParameter String value) {
            if (! StringUtil.isNullOrSpace(value)) {
                return FormValidation.ok();
            }
            return FormValidation.error("Password is required");
        }

        @SuppressWarnings ("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         *
         * @return a human readable string that is shown in the DropDown Menu
         */
        @Override
        public String getDisplayName() {
            return "Run a Ranorex test suite";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            useSummarize = formData.getBoolean("useSummarize");
            save();
            return super.configure(req, formData); // To change body of generated methods, choose Tools | Templates.
        }

        public boolean isUseSummarize() {
            return useSummarize;
        }
    }
}
