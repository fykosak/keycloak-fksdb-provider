package fykos.fksdb_keycloak_user_provider;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class FKSDBUserStorageProviderFactory
		implements UserStorageProviderFactory<FKSDBUserStorageProvider> {

	public static final String PROVIDER_NAME = "FKSDB";
	private static final Logger logger = Logger.getLogger(FKSDBUserStorageProviderFactory.class);

	@Override
	public String getId() {
		return PROVIDER_NAME;
	}

	@Override
	public FKSDBUserStorageProvider create(KeycloakSession session, ComponentModel model) {
		return new FKSDBUserStorageProvider(session, model);
	}

	@Override
	public void init(Config.Scope config) {
		logger.info("FKSDB storage provider created");
	}

	@Override
	public String getHelpText() {
		return "JPA storage provider for FKSDB";
	}
}
