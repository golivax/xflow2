package br.usp.ime.lapessc.xflow2.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class XFlowConfig {

	private static XFlowConfig instance = new XFlowConfig();
	
	private VCSConfig vcsConfig;
	
	private XFlowConfig(){

		try{
			
			Configuration config = 
					new PropertiesConfiguration("xflow.properties");
	
			this.vcsConfig = createVCSConfig(config);
					
		}catch(ConfigurationException e){
			e.printStackTrace();
		}

	}
	
	public static XFlowConfig getInstance(){
		return instance;
	}
	
	public VCSConfig getVCSConfig(){
		return vcsConfig;
	}
	
	
	private VCSConfig createVCSConfig(Configuration config) {
		String vcs_url= config.getString("vcs_url");
		String vcs_user = config.getString("vcs_user");
		String vcs_password = config.getString("vcs_password");
		
		return new VCSConfig(vcs_url, vcs_user, vcs_password);
	}
	
}
