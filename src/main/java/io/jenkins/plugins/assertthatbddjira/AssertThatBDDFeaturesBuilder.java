package io.jenkins.plugins.assertthatbddjira;

import com.assertthat.plugins.internal.APIUtil;
import com.assertthat.plugins.internal.Arguments;
import com.assertthat.plugins.internal.FileUtil;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.assertthatbddjira.credentials.AssertThatBDDCredentials;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;

public class AssertThatBDDFeaturesBuilder extends Builder implements SimpleBuildStep {

    private final String projectId;
    private final String outputFolder;
    private final String mode;
    private final String jql;
    private final String credentialsId;
    private final String proxyURI;
    private final String proxyUsername;
    private final String proxyPassword;

    public String getProxyURI() {
        return proxyURI;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }


    public String getCredentialsId() {
        return credentialsId;
    }
    
    public String getMode() {
        return mode;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public String getOutputFolder() {
        return outputFolder;
    }
    
    
    public String getJql() {
        return jql;
    }
    
    @DataBoundConstructor
    public AssertThatBDDFeaturesBuilder(String projectId, String credentialsId, String outputFolder, String jql, String mode, String proxyURI, String proxyUsername, String proxyPassword) {
        this.projectId = projectId;
        this.outputFolder = outputFolder;
        this.mode = mode;
        this.jql = jql;
        this.credentialsId = credentialsId;
        this.proxyURI = proxyURI;
        this.proxyUsername = proxyUsername;
        this.proxyPassword=proxyPassword;
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
                null,
                outputFolder,
                null,
                null,
                proxyURI,
                proxyUsername,
                proxyPassword,
                mode,
                jql,
                null
        );
    
        APIUtil apiUtil = new APIUtil(arguments.getProjectId(), arguments.getAccessKey(), arguments.getSecretKey(), arguments.getProxyURI(), arguments.getProxyUsername(), arguments.getProxyPassword());
    
        try {
            String outputPath = workspace.toURI().getPath();
            if(!arguments.getOutputFolder().trim().isEmpty()){
                outputPath = workspace.toURI().resolve(arguments.getOutputFolder()).getPath();
            }
            listener.getLogger().println("[AssertThat BDD] Downloading features to:  " + outputPath);
            File inZip = apiUtil.download(new File(outputPath), mode, jql);
            File zip = new FileUtil().unpackArchive(inZip, new File(outputPath));
            boolean deleted = zip.delete();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("[AssertThat BDD] Failed to download features: ", e);
        }
    }

    @Symbol("assertthatBddFeatures")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckProjectId(@QueryParameter String projectId)
                throws IOException, ServletException {
            if (projectId ==null || projectId.length() == 0)
                return FormValidation.error(io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDFeaturesBuilder_DescriptorImpl_errors_missingProjectId());
            return FormValidation.ok();
        }
        public FormValidation doCheckCredentialsId(@QueryParameter String credentialsId) {
            if (credentialsId == null || credentialsId.length() == 0)
                return FormValidation.error(io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDFeaturesBuilder_DescriptorImpl_errors_missingCredentialsId());
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
            return io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDFeaturesBuilder_DescriptorImpl_FeaturesDisplayName();
        }

    }

}
