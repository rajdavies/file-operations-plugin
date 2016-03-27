package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;
import java.io.Serializable;

public class FileCreateOperation extends FileOperation implements Serializable { 
	private final String fileName;
	private final String fileContent;
	
	@DataBoundConstructor 
	 public FileCreateOperation(String fileName, String fileContent) { 
		this.fileName = fileName;
		this.fileContent = fileContent;		
	 }

	 public String getFileName()
	 {
		 return fileName;
	 }
	 public String getFileContent()
	 {
		 return fileContent;
	 }
	 
	 public boolean RunOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
				FilePath ws = build.getWorkspace(); 				
				
				try {	
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(fileName), build.getEnvironment(listener).expand(fileContent),build.getEnvironment(listener)));				
				}
				catch (Exception e) {
					e.printStackTrace(listener.getLogger());
					return false;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace(listener.getLogger());
			}	
			return result;
		} 
 
	private static final class TargetFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;
		private final BuildListener listener;
		private final EnvVars environment;
		private final String resolvedFileName;
		private final String resolvedFileContent;
		public TargetFileCallable(BuildListener Listener, String ResolvedFileName, String ResolvedFileContent, EnvVars environment) {
			this.listener = Listener;
			this.resolvedFileName = ResolvedFileName;	
			this.resolvedFileContent = ResolvedFileContent;
			this.environment = environment;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try 
			{				
				FilePath fpWS = new FilePath(ws);
				FilePath fpTL = new FilePath(fpWS, resolvedFileName);
				listener.getLogger().println("Creating file: " + fpTL.getRemote());
				fpTL.write(resolvedFileContent, "UTF-8");
				result = true;
			}
			catch(RuntimeException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				result = false;
			}
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                
		}		
	}
 @Extension public static class DescriptorImpl extends FileOperationDescriptor {
 public String getDisplayName() { return "File Create"; }

 }
}