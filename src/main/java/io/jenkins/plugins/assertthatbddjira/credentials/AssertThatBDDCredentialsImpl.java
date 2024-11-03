package io.jenkins.plugins.assertthatbddjira.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import io.jenkins.plugins.assertthatbddjira.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

public class AssertThatBDDCredentialsImpl extends BaseStandardCredentials implements AssertThatBDDCredentials {

    private final Secret secretKey;

    private final String accessKey;

    private final Secret token;
    
    @DataBoundConstructor
    public AssertThatBDDCredentialsImpl(@CheckForNull String id,
                                @CheckForNull String description,
                                 String accessKey,
                                 String secretKey,
                                 String token) {
        super(id, description);
        this.secretKey = Secret.fromString(secretKey);
        this.accessKey = accessKey;
        this.token = Secret.fromString(token);
     
    }

    public Secret getSecretKey() {
        return this.secretKey;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public Secret getToken() {
        return this.token;
    }
    
    @Extension
    public static class DescriptorImpl
            extends CredentialsDescriptor {
        
        /** {@inheritDoc} */
        @Override
        public String getDisplayName() {
            return io.jenkins.plugins.assertthatbddjira.Messages.AssertThatBDDCredentialsImpl_DescriptorImpl_credentialsName();
        }
    }
}
