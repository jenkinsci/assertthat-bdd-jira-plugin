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
    
    @NonNull
    private final Secret secretKey;
    
    
    @NonNull
    private final String accessKey;
    
    @DataBoundConstructor
    public AssertThatBDDCredentialsImpl(@CheckForNull String id,
                                @CheckForNull String description,
                                @NonNull String accessKey,
                                @NonNull String secretKey       ) {
        super(id, description);
        this.secretKey = Secret.fromString(secretKey);
        this.accessKey = accessKey;
     
    }
    
    @NonNull
    public Secret getSecretKey() {
        return this.secretKey;
    }
    
    @NonNull
    public String getAccessKey() {
        return this.accessKey;
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
