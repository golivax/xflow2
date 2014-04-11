package br.usp.ime.lapessc.xflow2.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class XFlowConfig {

	private static XFlowConfig instance = new XFlowConfig();
	
	private DatabaseConfig databaseConfig;
	private VCSConfig vcsConfig;
	
	private XFlowConfig(){

		try{
			
			Configuration config = 
					new PropertiesConfiguration("xflow.properties");
	
			this.databaseConfig = createDatabaseConfig(config);
			this.vcsConfig = createVCSConfig(config);
					
		}catch(ConfigurationException e){
			e.printStackTrace();
		}

	}
	
	public static XFlowConfig getInstance(){
		return instance;
	}
	
	public DatabaseConfig getDBConfig(){
		return databaseConfig;
	}
	
	public VCSConfig getVCSConfig(){
		return vcsConfig;
	}
	
	private DatabaseConfig createDatabaseConfig(Configuration config){
		String db_url= config.getString("db_url");
		String db_port = config.getString("db_port");
		String db_user = config.getString("db_user");
		String db_password = config.getString("db_password");
		String db_database = config.getString("db_database");
		
		return new DatabaseConfig(db_url, db_port, db_user, db_password, 
				db_database);
	}
	
	private VCSConfig createVCSConfig(Configuration config) {
		String vcs_url= config.getString("vcs_url");
		String vcs_user = config.getString("vcs_user");
		String vcs_password = config.getString("vcs_password");
		
		return new VCSConfig(vcs_url, vcs_user, vcs_password);
	}
	
}
