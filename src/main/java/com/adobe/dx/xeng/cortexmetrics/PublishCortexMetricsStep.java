package com.adobe.dx.xeng.cortexmetrics;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.YesNoMaybe;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author saville
 */
public class PublishCortexMetricsStep extends Step {

    private String url;
    private Secret bearerToken;
    private String namespace;
    private Map<String, String> labels = new HashMap<>();

    public Map<String, String> getLabels() {
        return labels;
    }

    @DataBoundSetter
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getUrl() {
        return url;
    }

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    public Secret getBearerToken() {
        return bearerToken;
    }

    @DataBoundSetter
    public void setBearerToken(String bearerToken) {
        this.bearerToken = Secret.fromString(bearerToken);
    }

    public String getNamespace() {
        return namespace;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @DataBoundConstructor
    public PublishCortexMetricsStep() {}

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new Execution(url, bearerToken, namespace, labels, stepContext);
    }

    static class Execution extends SynchronousStepExecution<Void> {
        private static final long serialVersionUID = 1L;

        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient String url;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient Secret bearerToken;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient String namespace;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final Map<String, String> labels;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final Run<?, ?> run;
        @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Only used when starting.")
        private transient final TaskListener taskListener;

        Execution(String url, Secret bearerToken, String namespace,
                  Map<String, String> labels, final @Nonnull StepContext context)
                throws IOException, InterruptedException {
            super(context);
            this.url = url;
            this.bearerToken = bearerToken;
            this.namespace = namespace;
            this.labels = labels;
            this.run = context.get(Run.class);
            this.taskListener = context.get(TaskListener.class);
        }

        @Override
        protected Void run() throws Exception {
            try {
                CortexPublisher publisher = new CortexPublisher(run, url, bearerToken, namespace, labels);
                publisher.send(taskListener);
            } catch(Exception e) {
                taskListener.getLogger().println("Failed to send metrics to Cortex:");
                e.printStackTrace(taskListener.getLogger());
            }
            return null;
        }
    }

    @Extension(dynamicLoadable = YesNoMaybe.YES, optional = true)
    public static class DescriptorImpl extends StepDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getFunctionName() {
            return "publishCortexMetrics";
        }

        /** {@inheritDoc} */
        @Override
        public String getDisplayName() {
            return "Publish metrics to Cortex";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> requiredContext = new HashSet<>();
            requiredContext.add(TaskListener.class);
            requiredContext.add(Run.class);
            return requiredContext;
        }
    }
}
