package fykos.fksdb_keycloak_user_provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class FKSDBUserStorageProviderFactory
		implements UserStorageProviderFactory<FKSDBUserStorageProvider> {

	public static final String PROVIDER_NAME = "FKSDB";

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}

	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProviderFactory.class);
	protected Properties properties = new Properties();

	@Override
	public void init(Config.Scope config) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("/users.properties");

		if (is == null) {
			logger.warn("Could not find users.properties in classpath");
		} else {
			try {
				properties.load(is);
			} catch (IOException ex) {
				logger.error("Failed to load users.properties file", ex);
			}
		}
	}

	@Override
	public FKSDBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		return new FKSDBUserStorageProvider(session, model, properties);
	}
}
