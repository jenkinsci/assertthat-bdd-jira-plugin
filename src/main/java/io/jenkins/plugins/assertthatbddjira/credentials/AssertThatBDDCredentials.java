package io.jenkins.plugins.assertthatbddjira.credentials;

import com.cloudbees.plugins.credentials.Credentials;
import hudson.util.Secret;

public interface AssertThatBDDCredentials extends Credentials {
    
    String getDescription();
    
    String getAccessKey();
    
    Secret getSecretKey();

    Secret getToken();
}

