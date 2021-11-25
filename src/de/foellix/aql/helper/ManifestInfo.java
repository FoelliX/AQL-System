package de.foellix.aql.helper;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;

public class ManifestInfo {
	private static final String LAUNCHER_ACTIVITY_ACTION = "android.intent.action.MAIN";
	private static final String LAUNCHER_ACTIVITY_CATEGORY = "android.intent.category.LAUNCHER";

	private static final String ACTIVITY = "activity";
	private static final String ACTIVITY_ALIAS = "activity-alias";
	private static final String RECEIVER = "receiver";
	private static final String SERVICE = "service";
	private static final String INTENT_FILTER = "intent-filter";
	private static final String ACTION = "action";
	private static final String CATEGORY = "category";
	private static final String DATA = "data";
	private static final String PERMISSION = "uses-permission";
	private static final String ANDROID_NAME = "android:name";
	private static final String ANDROID_HOST = "android:host";
	private static final String ANDROID_PATH = "android:path";
	private static final String ANDROID_PATH_PATTERN = "android:pathPattern";
	private static final String ANDROID_PATH_PREFIX = "android:pathPrefix";
	private static final String ANDROID_PORT = "android:port";
	private static final String ANDROID_SCHEME = "android:scheme";
	private static final String ANDROID_SSP = "android:ssp";
	private static final String ANDROID_SSP_PATTERN = "android:sspPattern";
	private static final String ANDROID_SSP_PREFIX = "android:sspPrefix";
	private static final String ANDROID_TYPE = "android:mimeType";
	private static final String ANDROID_TARGET_ACTIVITY = "android:targetActivity";

	private final String manifest;
	private final String pkgName;
	private final String appName;
	private final Intentfilters activityIntentfilters;
	private final Intentfilters receiverIntentfilters;
	private final Intentfilters serviceIntentfilters;
	private final Permissions permissions;
	private String launcherActivity;

	public ManifestInfo(final String manifest) {
		this.manifest = manifest;
		this.pkgName = Helper.cut(manifest, "package=\"", "\"");
		this.appName = Helper.cut(this.pkgName, ".", Helper.OCCURENCE_LAST);

		this.activityIntentfilters = parseIntentfilters(ACTIVITY);
		this.activityIntentfilters.getIntentfilter().addAll(parseIntentfilters(ACTIVITY_ALIAS).getIntentfilter());
		this.receiverIntentfilters = parseIntentfilters(RECEIVER);
		this.serviceIntentfilters = parseIntentfilters(SERVICE);
		this.permissions = parsePermissions();
		this.launcherActivity = null;
	}

	private Intentfilters parseIntentfilters(String componentType) {
		final Intentfilters aqlIntentfilters = new Intentfilters();

		try {
			final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			final Document resultDocument = domBuilder.parse(new InputSource(new StringReader(this.manifest)));
			resultDocument.getDocumentElement().normalize();

			// Components
			final NodeList components = resultDocument.getElementsByTagName(componentType);
			for (int i = 0; i < components.getLength(); i++) {
				final Element component = (Element) components.item(i);

				final Reference ref = new Reference();
				if (componentType == ACTIVITY_ALIAS) {
					ref.setClassname(component.getAttribute(ANDROID_TARGET_ACTIVITY));
				} else {
					ref.setClassname(component.getAttribute(ANDROID_NAME));
				}
				if (!ref.getClassname().contains(".")) {
					ref.setClassname(this.pkgName + "." + ref.getClassname());
				} else if (ref.getClassname().startsWith(".")) {
					ref.setClassname(this.pkgName + ref.getClassname());
				}

				// Intentfilters
				final NodeList intentfilters = component.getElementsByTagName(INTENT_FILTER);
				for (int j = 0; j < intentfilters.getLength(); j++) {
					final Element intentfilter = (Element) intentfilters.item(j);

					final Intentfilter aqlIntentfilter = new Intentfilter();
					aqlIntentfilter.setReference(ref);

					// Actions
					final NodeList actions = intentfilter.getElementsByTagName(ACTION);
					for (int k = 0; k < actions.getLength(); k++) {
						aqlIntentfilter.getAction()
								.add(actions.item(k).getAttributes().getNamedItem(ANDROID_NAME).getNodeValue());
					}

					// Categories
					final NodeList categories = intentfilter.getElementsByTagName(CATEGORY);
					for (int k = 0; k < categories.getLength(); k++) {
						aqlIntentfilter.getCategory()
								.add(categories.item(k).getAttributes().getNamedItem(ANDROID_NAME).getNodeValue());
					}

					// Datas
					final NodeList datas = intentfilter.getElementsByTagName(DATA);
					for (int k = 0; k < datas.getLength(); k++) {
						final Data data = new Data();
						if (datas.item(k).getAttributes() != null) {
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_HOST) != null) {
								data.setHost(datas.item(k).getAttributes().getNamedItem(ANDROID_HOST).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_PATH) != null) {
								data.setPath(datas.item(k).getAttributes().getNamedItem(ANDROID_PATH).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_PATH_PATTERN) != null) {
								data.setPathPattern(datas.item(k).getAttributes().getNamedItem(ANDROID_PATH_PATTERN)
										.getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_PATH_PREFIX) != null) {
								data.setPathPrefix(
										datas.item(k).getAttributes().getNamedItem(ANDROID_PATH_PREFIX).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_PORT) != null) {
								data.setPort(datas.item(k).getAttributes().getNamedItem(ANDROID_PORT).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_SCHEME) != null) {
								data.setScheme(
										datas.item(k).getAttributes().getNamedItem(ANDROID_SCHEME).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_SSP) != null) {
								data.setSsp(datas.item(k).getAttributes().getNamedItem(ANDROID_SSP).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_SSP_PATTERN) != null) {
								data.setSspPattern(
										datas.item(k).getAttributes().getNamedItem(ANDROID_SSP_PATTERN).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_SSP_PREFIX) != null) {
								data.setSspPrefix(
										datas.item(k).getAttributes().getNamedItem(ANDROID_SSP_PREFIX).getNodeValue());
							}
							if (datas.item(k).getAttributes().getNamedItem(ANDROID_TYPE) != null) {
								data.setType(datas.item(k).getAttributes().getNamedItem(ANDROID_TYPE).getNodeValue());
							}
						}
						aqlIntentfilter.getData().add(data);
					}

					aqlIntentfilters.getIntentfilter().add(aqlIntentfilter);
				}
			}
		} catch (final IOException | ParserConfigurationException | SAXException e) {
			Log.warning("Could not read all intentfilters declared in the manifest!");
		}

		return aqlIntentfilters;
	}

	private Permissions parsePermissions() {
		final Permissions aqlPermissions = new Permissions();

		try {
			final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			final Document resultDocument = domBuilder.parse(new InputSource(new StringReader(this.manifest)));
			resultDocument.getDocumentElement().normalize();

			// Permissions
			final NodeList permissions = resultDocument.getElementsByTagName(PERMISSION);
			for (int i = 0; i < permissions.getLength(); i++) {
				final Element permission = (Element) permissions.item(i);
				final Permission aqlPermission = new Permission();
				aqlPermission.setName(permission.getAttribute(ANDROID_NAME));
				aqlPermissions.getPermission().add(aqlPermission);
			}
		} catch (final IOException | ParserConfigurationException | SAXException e) {
			Log.warning("Could not read all permissions declared in the manifest!");
		}

		return aqlPermissions;
	}

	public String getManifestRAW() {
		return this.manifest;
	}

	public String getPkgName() {
		return this.pkgName;
	}

	public String getAppName() {
		return this.appName;
	}

	public Intentfilters getActivityIntentfilters() {
		return this.activityIntentfilters;
	}

	public Intentfilters getReceiverIntentfilters() {
		return this.receiverIntentfilters;
	}

	public Intentfilters getServiceIntentfilters() {
		return this.serviceIntentfilters;
	}

	public Intentfilters getAllIntentfilters() {
		final Intentfilters all = new Intentfilters();
		all.getIntentfilter().addAll(this.activityIntentfilters.getIntentfilter());
		all.getIntentfilter().addAll(this.serviceIntentfilters.getIntentfilter());
		all.getIntentfilter().addAll(this.receiverIntentfilters.getIntentfilter());
		return all;
	}

	public Permissions getPermissions() {
		return this.permissions;
	}

	public String getLauncherActivityName() {
		if (this.launcherActivity == null) {
			for (final Intentfilter intentfilter : this.activityIntentfilters.getIntentfilter()) {
				if (intentfilter.getAction().contains(LAUNCHER_ACTIVITY_ACTION)
						&& intentfilter.getCategory().contains(LAUNCHER_ACTIVITY_CATEGORY)) {
					this.launcherActivity = intentfilter.getReference().getClassname();
				}
			}
		}
		return this.launcherActivity;
	}
}
