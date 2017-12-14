package org.daisy.maven.xproc.pipeline;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.base.Supplier;

import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;

import org.daisy.maven.xproc.api.XProcExecutionException;

import org.daisy.pipeline.job.JobMonitorFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "org.daisy.maven.xproc.pipeline.DaisyPipeline2",
	service = { org.daisy.maven.xproc.api.XProcEngine.class }
)
public class DaisyPipeline2 implements org.daisy.maven.xproc.api.XProcEngine {
	
	private XProcEngine engine;
	private JobMonitorFactory jobMonitorFactory;
	
	@Reference(
		name = "XProcEngine",
		unbind = "-",
		service = XProcEngine.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setXProcEngine(XProcEngine engine) {
		this.engine = engine;
	}
	
	@Reference(
		name = "JobMonitorFactory",
		unbind = "-",
		service = JobMonitorFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setJobMonitorFactory(final JobMonitorFactory jobMonitorFactory) {
		this.jobMonitorFactory = jobMonitorFactory;
	}
	
	@Activate
	protected void activate() {}
	
	public void setCatalog(File catalog) {
		if (catalog != null)
			throw new UnsupportedOperationException("Setting catalog file not supported.");
	}
	
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters)
			throws XProcExecutionException {
		run(pipeline, inputs, outputs, options, parameters, null);
	}
	
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters,
	                Map<String,?> context)
			throws XProcExecutionException {
		try {
			XProcPipeline xprocPipeline = engine.load(new URI(pipeline));
			XProcInput.Builder inputBuilder = new XProcInput.Builder();
			if (inputs != null)
				for (String port : inputs.keySet())
					for (String document : inputs.get(port))
						inputBuilder.withInput(port, new LazySaxSourceProvider(document));
			if (options != null)
				for (String name : options.keySet())
					inputBuilder.withOption(new QName("", name), options.get(name));
			if (parameters != null)
				for (String port : parameters.keySet())
					for (String name : parameters.get(port).keySet())
						inputBuilder.withParameter(port, new QName("", name), parameters.get(port).get(name));
			XProcResult results; {
				String jobId = context == null ? null : (String)context.get("XPROCSPEC_TEST_ID");
				if (jobId != null) {
					MessageEventListener listener = new MessageEventListener(jobId, jobMonitorFactory);
					try {
						Properties props = new Properties();
						props.setProperty("JOB_ID", jobId);
						results = xprocPipeline.run(inputBuilder.build(), null, props);
					} finally {
						listener.close();
					}
				} else {
					results = xprocPipeline.run(inputBuilder.build());
				}
			}
			XProcOutput.Builder outputBuilder = new XProcOutput.Builder();
			for (XProcPortInfo info : xprocPipeline.getInfo().getOutputPorts()) {
				String port = info.getName();
				outputBuilder.withOutput(port, (outputs != null) && outputs.containsKey(port) ?
				                         new LazySaxResultProvider(outputs.get(port)) :
				                         new DevNullStreamResultProvider()); }
			results.writeTo(outputBuilder.build()); }
		catch (Exception e) {
			if (e instanceof XProcErrorException) {
				throw new XProcExecutionException("DAISY Pipeline failed to execute XProc\n" + e.toString(), e);
			} else
				throw new XProcExecutionException("DAISY Pipeline failed to execute XProc", e);
		}
	}
	
	private static class DevNullStreamResultProvider implements Supplier<Result> {
		private static final Result result = new StreamResult(
			new OutputStream() {
				@Override public void write(byte[] b, int off, int len) throws IOException {}
				@Override public void write(byte[] b) throws IOException {}
				@Override public void write(int b) throws IOException {}});
		public Result get() {
			return result;
		}
	}
}
