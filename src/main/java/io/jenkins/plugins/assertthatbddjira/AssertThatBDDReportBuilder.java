package io.jenkins.plugins.assertthatbddjira;

import com.assertthat.plugins.internal.APIUtil;
import com.assertthat.plugins.internal.Arguments;
import com.assertthat.plugins.internal.FileUtil;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.assertthatbddjira.credentials.AssertThatBDDCredentials;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.codehaus.jettison.json.JSONException;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssertThatBDDReportBuilder extends Recorder implements SimpleBuildStep {

    private final String projectId;
    private final String credentialsId;
    private final String jsonReportFolder;
    private final String jsonReportIncludePattern;
    private final String runName;
    private final String type;

    @DataBoundConstructor
    public AssertThatBDDReportBuilder(String projectId, String credentialsId, String jsonReportFolder, String jsonReportIncludePattern, String runName, String type) {
        this.projectId = projectId;
        this.credentialsId = credentialsId;
        this.jsonReportFolder = jsonReportFolder;
        this.jsonReportIncludePattern = jsonReportIncludePattern;
        this.runName = runName;
        this.type = type;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getType() {
        return type;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getJsonReportFolder() {
        return jsonReportFolder;
    }

    public String getJsonReportIncludePattern() {
        return jsonReportIncludePattern;
    }

    public String getRunName() {
        return runName;
    }

    private AssertThatBDDCredentials getAssertThatBDDCredentials(String credentialsId) {
        List<AssertThatBDDCredentials> assertThatBDDCredentialsList = CredentialsProvider.lookupCredentials(AssertThatBDDCredentials.class, Jenkins.getInstance(), ACL.SYSTEM);
        AssertThatBDDCredentials assertThatBDDCredentials = CredentialsMatchers.firstOrNull(assertThatBDDCredentialsList, CredentialsMatchers.allOf(CredentialsMatchers.withId(credentialsId)));
        return assertThatBDDCredentials;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) {
        AssertThatBDDCredentials credentials = getAssertThatBDDCredentials(credentialsId);
        Arguments arguments = new Arguments(
                credentials.getAccessKey(),
                credentials.getSecretKey().getPlainText(),
                projectId,
                runName,
                null,
                jsonReportFolder,
                jsonReportIncludePattern,
                null,
                null,
                null,
                null,
                null,
                type
        );
        APIUtil apiUtil = new APIUtil(arguments.getProjectId(), arguments.getAccessKey(), arguments.getSecretKey(), arguments.getProxyURI(), arguments.getProxyUsername(), arguments.getProxyPassword());

        String[] files;
        try {
            String reportPath = workspace.toURI().getPath();
            if (!arguments.getJsonReportFolder().trim().isEmpty() && !arguments.getJsonReportFolder().trim().replaceAll("/|\\\\","").isEmpty()) {
                reportPath = workspace.toURI().resolve(arguments.getJsonReportFolder()).getPath();
            }
            if (!new File(reportPath).exists()) {
                listener.getLogger().println("[AssertThat BDD] Report folder doesn't exist:  " + reportPath);
                return;
            }
            listener.getLogger().println("[AssertThat BDD] Uploading reports from:  " + reportPath);
            files = new FileUtil().findJsonFiles(new File(reportPath), arguments.getJsonReportIncludePattern(), null);
            if (files.length == 0) {
                listener.getLogger().println("[AssertThat BDD] No files found in '" + reportPath + "' matching pattern '" + arguments.getJsonReportIncludePattern() + "'");
            }
            Long runId = -1L;
            for (String f : files) {
                runId = apiUtil.upload(runId, arguments.getRunName(), (reportPath.endsWith("/") ? reportPath : reportPath + "/") + f, arguments.getType());
            }
        } catch (JSONException | IOException | InterruptedException e) {
            throw new RuntimeException("[AssertThat BDD] Failed to upload report: ", e);
        }

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("assertthatBddReport")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheckProjectId(@QueryParameter String projectId) {
            if (projectId == null || projectId.length() == 0)
                return FormValidation.error(io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDReportBuilder_DescriptorImpl_errors_missingProjectId());
            return FormValidation.ok();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String credentialsId) {
            if (credentialsId == null || credentialsId.length() == 0)
                return FormValidation.error(io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDReportBuilder_DescriptorImpl_errors_missingCredentialsId());
            return FormValidation.ok();
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Jenkins context,
                                                     @QueryParameter String remoteBase) {
            if (context == null || !context.hasPermission(Item.CONFIGURE)) {
                return new StandardListBoxModel();
            }

            List<DomainRequirement> domainRequirements = new ArrayList();
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(
                            CredentialsMatchers.anyOf(
                                    CredentialsMatchers.instanceOf(AssertThatBDDCredentials.class)),
                            CredentialsProvider.lookupCredentials(
                                    StandardCredentials.class,
                                    context,
                                    ACL.SYSTEM,
                                    domainRequirements));
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDReportBuilder_DescriptorImpl_ReportDisplayName();
        }

    }

}
