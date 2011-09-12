/*
 * ====================================================================================================================
 * Zanata Initial Setup Script
 * 
 * Edit these variables for Zanata's initial setup.
 * Note: Existing values will get overridden and some elements (e.g. Login Config Xml) will not insert
 * a configuration record for null values.
 * ==================================================================================================================== 
 */
set @LOGIN_CONFIG_URL   = null;
set @AUTH_TYPE          = '';         /* Valid values: INTERNAL, KERBEROS, FEDORA_OPENID, JAAS */

/*
 * ====================================================================================================================
 * End of Configurable elements. Do not edit this script beyond this marker.
 * ====================================================================================================================
 */

/* Change to a custom Login Config Url */
insert ignore into HApplicationConfiguration(creationDate, lastChanged, versionNum, config_key, config_value) 
values(CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0, 'zanata.login-config.url', @LOGIN_CONFIG_URL)
on duplicate key update config_value = @LOGIN_CONFIG_URL, versionNum=versionNum+1, lastChanged=CURRENT_TIMESTAMP();

/* Change the Authentication type. Valid values are: INTERNAL, FEDORA_OPENID, KERBEROS, OTHER */
insert into HApplicationConfiguration(creationDate, lastChanged, versionNum, config_key, config_value) 
values(CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 0, 'zanata.security.authType', @AUTH_TYPE)
on duplicate key update config_value = @AUTH_TYPE, versionNum=versionNum+1, lastChanged=CURRENT_TIMESTAMP();

/* Cleanup */
delete from HApplicationConfiguration where config_key = 'zanata.login-config.url' and config_value = '';