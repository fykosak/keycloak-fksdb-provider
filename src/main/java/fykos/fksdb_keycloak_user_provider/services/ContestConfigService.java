package fykos.fksdb_keycloak_user_provider.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jboss.logging.Logger;
import org.keycloak.common.util.EnvUtil;

public class ContestConfigService {
	private static final Logger logger = Logger.getLogger(ContestConfigService.class);

	private Properties properties = new Properties();

	private Map<Integer, String> contestMap = new HashMap<>();
	private Map<String, Integer> domainToContestIdMap = new HashMap<>();

	public ContestConfigService() {
		// init contest map
		contestMap.put(1, "fykos");
		contestMap.put(2, "vyfuk");

		// load config file
		try {
			this.loadConfigFile();
			logger.info(this.properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// map domain to contest
		for (int contestId : contestMap.keySet()) {
			String[] domains = this.getAllowedDomains(contestId);
			for (String domain : domains) {
				if (domainToContestIdMap.containsKey(domain)) {
					throw new RuntimeException("Domain " + domain + " in multiple contests");
				}
				domainToContestIdMap.put(domain, contestId);
			}
		}
	}

	private void loadConfigFile() throws IOException {
		try {
			logger.info(EnvUtil.replace("${jboss.server.config.dir}/domains.properties"));
			File configFile = new File(EnvUtil.replace("${jboss.server.config.dir}/domains.properties"));
			InputStream inputStream = new FileInputStream(configFile);

			this.properties.load(inputStream);
		} catch (FileNotFoundException e) {
			logger.error("Missing domains.properties configuration file");
			throw e;
		} catch (IOException e) {
			logger.error("Failed to read domains properties file");
			throw e;
		}
	}

	public Set<Integer> getContestIds() {
		return contestMap.keySet();
	}

	/**
	 * Returns the contest symbol (fykos/vyfuk) for Contest ID.
	 * If the given ID does not exists, it returns null.
	 */
	public String getContestSymbol(int contestId) {
		return contestMap.get(contestId);
	}

	public String getDomain(String contestSymbol) {
		String domain = properties.getProperty(contestSymbol + ".domains.main");
		if (domain == null) {
			throw new RuntimeException("Config value " + contestSymbol + ".domain.main not specified");
		}
		return domain;
	}

	public String getDomain(int contestId) {
		String contestSymbol = this.getContestSymbol(contestId);
		return this.getDomain(contestSymbol);
	}

	public String[] getAllowedDomains(int contestId) {
		String contestSymbol = this.getContestSymbol(contestId);
		String allDomains = properties.getProperty(contestSymbol + ".domains.all");
		return allDomains.split(",");
	}

	public Integer getContestIdFromDomain(String domain) {
		return domainToContestIdMap.get(domain);
	}
}
