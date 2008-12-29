insert into Person (id, personId, name) values (1, 'admin','Administrator');
insert into Person (id, personId, name) values (2, 'demo', 'Sample User');

insert into Account (id, username, passwordhash, enabled, person_id) values (1, 'admin', 'Eyox7xbNQ09MkIfRyH+rjg==', 1, 1);
insert into Account (id, username, passwordhash, enabled, person_id) values (2, 'demo', '/9Se/pfHeUH8FJ4asBD6jQ==', 1, 2);

insert into AccountRole (id, name, conditional) values (1, 'admin', false);
insert into AccountRole (id, name, conditional) values (2, 'user', false);
insert into AccountRole (id, name, conditional) values (3, 'project-maintainer', true);
insert into AccountRole (id, name, conditional) values (4, 'translation-team-lead', true);
insert into AccountRole (id, name, conditional) values (5, 'translator', true);
insert into AccountRole (id, name, conditional) values (6, 'reviewer', true);

insert into AccountMembership (account_id, member_of) values (1, 1);
insert into AccountMembership (account_id, member_of) values (2, 2);

insert into AccountRoleGroup (role_id, member_of) values (2, 1);

insert into Project (id, uname, name, version) values (1, 'seam', 'Jboss Seam', 0);
insert into Project (id, uname, name, version) values (2, 'webbeans', 'Web Beans', 0);

insert into ProjectSeries (id, projectId, name, version) values (1, 1, '2.0.x', 0);
insert into ProjectSeries (id, projectId, name, version) values (2, 2, 'default', 0);

insert into ProjectTarget (id, projectSeriesId, projectId, name, version) values (1, 1, 1, '2.0.0', 0);
insert into ProjectTarget (id, projectSeriesId, projectId, name, version) values (2, 1, 1, '2.0.1', 0);

insert into ProjectTarget (id, projectSeriesId, projectId, name, version) values (3, 2, 2, '1.0', 0);



insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(1, 0, 'af', 'Afrikaans', 'Afrikaans', '', 'af', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(2, 0, 'af_NA', 'Afrikaans (Namibia)', 'Afrikaans (Namibië)', 'NA', 'af', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(3, 0, 'af_ZA', 'Afrikaans (South Africa)', 'Afrikaans (Suid-Afrika)', 'ZA', 'af', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(4, 0, 'am', 'Amharic', 'አማርኛ', '', 'am', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(5, 0, 'am_ET', 'Amharic (Ethiopia)', 'አማርኛ (ኢትዮጵያ)', 'ET', 'am', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(6, 0, 'ar', 'Arabic', 'العربية', '', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(7, 0, 'ar_AE', 'Arabic (United Arab Emirates)', 'العربية (الامارات العربية المتحدة)', 'AE', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(8, 0, 'ar_BH', 'Arabic (Bahrain)', 'العربية (البحرين)', 'BH', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(9, 0, 'ar_DZ', 'Arabic (Algeria)', 'العربية (الجزائر)', 'DZ', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(10, 0, 'ar_EG', 'Arabic (Egypt)', 'العربية (مصر)', 'EG', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(11, 0, 'ar_IQ', 'Arabic (Iraq)', 'العربية (العراق)', 'IQ', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(12, 0, 'ar_JO', 'Arabic (Jordan)', 'العربية (الأردن)', 'JO', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(13, 0, 'ar_KW', 'Arabic (Kuwait)', 'العربية (الكويت)', 'KW', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(14, 0, 'ar_LB', 'Arabic (Lebanon)', 'العربية (لبنان)', 'LB', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(15, 0, 'ar_LY', 'Arabic (Libya)', 'العربية (ليبيا)', 'LY', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(16, 0, 'ar_MA', 'Arabic (Morocco)', 'العربية (المغرب)', 'MA', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(17, 0, 'ar_OM', 'Arabic (Oman)', 'العربية (عمان)', 'OM', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(18, 0, 'ar_QA', 'Arabic (Qatar)', 'العربية (قطر)', 'QA', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(19, 0, 'ar_SA', 'Arabic (Saudi Arabia)', 'العربية (المملكة العربية السعودية)', 'SA', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(20, 0, 'ar_SD', 'Arabic (Sudan)', 'العربية (السودان)', 'SD', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(21, 0, 'ar_SY', 'Arabic (Syria)', 'العربية (سوريا)', 'SY', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(22, 0, 'ar_TN', 'Arabic (Tunisia)', 'العربية (تونس)', 'TN', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(23, 0, 'ar_YE', 'Arabic (Yemen)', 'العربية (اليمن)', 'YE', 'ar', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(24, 0, 'as', 'Assamese', 'অসমীয়া', '', 'as', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(25, 0, 'as_IN', 'Assamese (India)', 'অসমীয়া (ভাৰত)', 'IN', 'as', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(26, 0, 'az', 'Azerbaijani', 'azərbaycanca', '', 'az', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(27, 0, 'az_Cyrl', 'Azerbaijani (Cyrillic)', 'Азәрбајҹан (kiril)', '', 'az', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(28, 0, 'az_Cyrl_AZ', 'Azerbaijani (Cyrillic, Azerbaijan)', 'Азәрбајҹан (kiril, Азәрбајҹан)', 'AZ', 'az', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(29, 0, 'az_Latn', 'Azerbaijani (Latin)', 'azərbaycanca (latın)', '', 'az', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(30, 0, 'az_Latn_AZ', 'Azerbaijani (Latin, Azerbaijan)', 'azərbaycanca (latın, Azərbaycan)', 'AZ', 'az', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(31, 0, 'be', 'Belarusian', 'беларуская', '', 'be', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(32, 0, 'be_BY', 'Belarusian (Belarus)', 'беларуская (Беларусь)', 'BY', 'be', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(33, 0, 'bg', 'Bulgarian', 'български', '', 'bg', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(34, 0, 'bg_BG', 'Bulgarian (Bulgaria)', 'български (България)', 'BG', 'bg', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(35, 0, 'bn', 'Bengali', 'বাংলা', '', 'bn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(36, 0, 'bn_BD', 'Bengali (Bangladesh)', 'বাংলা (বাংলাদেশ)', 'BD', 'bn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(37, 0, 'bn_IN', 'Bengali (India)', 'বাংলা (ভারত)', 'IN', 'bn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(38, 0, 'ca', 'Catalan', 'català', '', 'ca', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(39, 0, 'ca_ES', 'Catalan (Spain)', 'català (Espanya)', 'ES', 'ca', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(40, 0, 'cs', 'Czech', 'čeština', '', 'cs', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(41, 0, 'cs_CZ', 'Czech (Czech Republic)', 'čeština (Česká republika)', 'CZ', 'cs', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(42, 0, 'cy', 'Welsh', 'Cymraeg', '', 'cy', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(43, 0, 'cy_GB', 'Welsh (United Kingdom)', 'Cymraeg (Prydain Fawr)', 'GB', 'cy', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(44, 0, 'da', 'Danish', 'dansk', '', 'da', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(45, 0, 'da_DK', 'Danish (Denmark)', 'dansk (Danmark)', 'DK', 'da', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(46, 0, 'de', 'German', 'Deutsch', '', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(47, 0, 'de_AT', 'German (Austria)', 'Deutsch (Österreich)', 'AT', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(48, 0, 'de_BE', 'German (Belgium)', 'Deutsch (Belgien)', 'BE', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(49, 0, 'de_CH', 'German (Switzerland)', 'Deutsch (Schweiz)', 'CH', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(50, 0, 'de_DE', 'German (Germany)', 'Deutsch (Deutschland)', 'DE', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(51, 0, 'de_LI', 'German (Liechtenstein)', 'Deutsch (Liechtenstein)', 'LI', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(52, 0, 'de_LU', 'German (Luxembourg)', 'Deutsch (Luxemburg)', 'LU', 'de', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(53, 0, 'el', 'Greek', 'Ελληνικά', '', 'el', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(54, 0, 'el_CY', 'Greek (Cyprus)', 'Ελληνικά (Κύπρος)', 'CY', 'el', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(55, 0, 'el_GR', 'Greek (Greece)', 'Ελληνικά (Ελλάδα)', 'GR', 'el', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(56, 0, 'en', 'English', 'English', '', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(57, 0, 'en_AU', 'English (Australia)', 'English (Australia)', 'AU', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(58, 0, 'en_BE', 'English (Belgium)', 'English (Belgium)', 'BE', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(59, 0, 'en_BW', 'English (Botswana)', 'English (Botswana)', 'BW', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(60, 0, 'en_BZ', 'English (Belize)', 'English (Belize)', 'BZ', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(61, 0, 'en_CA', 'English (Canada)', 'English (Canada)', 'CA', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(62, 0, 'en_GB', 'English (United Kingdom)', 'English (United Kingdom)', 'GB', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(63, 0, 'en_HK', 'English (Hong Kong SAR China)', 'English (Hong Kong SAR China)', 'HK', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(64, 0, 'en_IE', 'English (Ireland)', 'English (Ireland)', 'IE', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(65, 0, 'en_IN', 'English (India)', 'English (India)', 'IN', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(66, 0, 'en_JM', 'English (Jamaica)', 'English (Jamaica)', 'JM', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(67, 0, 'en_MH', 'English (Marshall Islands)', 'English (Marshall Islands)', 'MH', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(68, 0, 'en_MT', 'English (Malta)', 'English (Malta)', 'MT', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(69, 0, 'en_NA', 'English (Namibia)', 'English (Namibia)', 'NA', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(70, 0, 'en_NZ', 'English (New Zealand)', 'English (New Zealand)', 'NZ', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(71, 0, 'en_PH', 'English (Philippines)', 'English (Philippines)', 'PH', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(72, 0, 'en_PK', 'English (Pakistan)', 'English (Pakistan)', 'PK', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(73, 0, 'en_SG', 'English (Singapore)', 'English (Singapore)', 'SG', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(74, 0, 'en_TT', 'English (Trinidad and Tobago)', 'English (Trinidad and Tobago)', 'TT', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(75, 0, 'en_US', 'English (United States)', 'English (United States)', 'US', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(76, 0, 'en_US_POSIX', 'English (United States, Computer)', 'English (United States, Computer)', 'US', 'en', NULL, FALSE, 'POSIX')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(77, 0, 'en_VI', 'English (U.S. Virgin Islands)', 'English (U.S. Virgin Islands)', 'VI', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(78, 0, 'en_ZA', 'English (South Africa)', 'English (South Africa)', 'ZA', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(79, 0, 'en_ZW', 'English (Zimbabwe)', 'English (Zimbabwe)', 'ZW', 'en', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(80, 0, 'eo', 'Esperanto', 'esperanto', '', 'eo', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(81, 0, 'es', 'Spanish', 'español', '', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(82, 0, 'es_AR', 'Spanish (Argentina)', 'español (Argentina)', 'AR', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(83, 0, 'es_BO', 'Spanish (Bolivia)', 'español (Bolivia)', 'BO', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(84, 0, 'es_CL', 'Spanish (Chile)', 'español (Chile)', 'CL', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(85, 0, 'es_CO', 'Spanish (Colombia)', 'español (Colombia)', 'CO', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(86, 0, 'es_CR', 'Spanish (Costa Rica)', 'español (Costa Rica)', 'CR', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(87, 0, 'es_DO', 'Spanish (Dominican Republic)', 'español (República Dominicana)', 'DO', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(88, 0, 'es_EC', 'Spanish (Ecuador)', 'español (Ecuador)', 'EC', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(89, 0, 'es_ES', 'Spanish (Spain)', 'español (España)', 'ES', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(90, 0, 'es_GT', 'Spanish (Guatemala)', 'español (Guatemala)', 'GT', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(91, 0, 'es_HN', 'Spanish (Honduras)', 'español (Honduras)', 'HN', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(92, 0, 'es_MX', 'Spanish (Mexico)', 'español (México)', 'MX', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(93, 0, 'es_NI', 'Spanish (Nicaragua)', 'español (Nicaragua)', 'NI', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(94, 0, 'es_PA', 'Spanish (Panama)', 'español (Panamá)', 'PA', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(95, 0, 'es_PE', 'Spanish (Peru)', 'español (Perú)', 'PE', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(96, 0, 'es_PR', 'Spanish (Puerto Rico)', 'español (Puerto Rico)', 'PR', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(97, 0, 'es_PY', 'Spanish (Paraguay)', 'español (Paraguay)', 'PY', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(98, 0, 'es_SV', 'Spanish (El Salvador)', 'español (El Salvador)', 'SV', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(99, 0, 'es_US', 'Spanish (United States)', 'español (Estados Unidos)', 'US', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(100, 0, 'es_UY', 'Spanish (Uruguay)', 'español (Uruguay)', 'UY', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(101, 0, 'es_VE', 'Spanish (Venezuela)', 'español (Venezuela)', 'VE', 'es', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(102, 0, 'et', 'Estonian', 'eesti', '', 'et', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(103, 0, 'et_EE', 'Estonian (Estonia)', 'eesti (Eesti)', 'EE', 'et', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(104, 0, 'eu', 'Basque', 'euskara', '', 'eu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(105, 0, 'eu_ES', 'Basque (Spain)', 'euskara (Espainia)', 'ES', 'eu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(106, 0, 'fa', 'Persian', 'فارسی', '', 'fa', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(107, 0, 'fa_AF', 'Persian (Afghanistan)', 'دری (افغانستان)', 'AF', 'fa', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(108, 0, 'fa_IR', 'Persian (Iran)', 'فارسی (ایران)', 'IR', 'fa', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(109, 0, 'fi', 'Finnish', 'suomi', '', 'fi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(110, 0, 'fi_FI', 'Finnish (Finland)', 'suomi (Suomi)', 'FI', 'fi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(111, 0, 'fo', 'Faroese', 'føroyskt', '', 'fo', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(112, 0, 'fo_FO', 'Faroese (Faroe Islands)', 'føroyskt (Føroyar)', 'FO', 'fo', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(113, 0, 'fr', 'French', 'français', '', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(114, 0, 'fr_BE', 'French (Belgium)', 'français (Belgique)', 'BE', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(115, 0, 'fr_CA', 'French (Canada)', 'français (Canada)', 'CA', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(116, 0, 'fr_CH', 'French (Switzerland)', 'français (Suisse)', 'CH', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(117, 0, 'fr_FR', 'French (France)', 'français (France)', 'FR', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(118, 0, 'fr_LU', 'French (Luxembourg)', 'français (Luxembourg)', 'LU', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(119, 0, 'fr_MC', 'French (Monaco)', 'français (Monaco)', 'MC', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(120, 0, 'fr_SN', 'French (Senegal)', 'français (Sénégal)', 'SN', 'fr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(121, 0, 'ga', 'Irish', 'Gaeilge', '', 'ga', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(122, 0, 'ga_IE', 'Irish (Ireland)', 'Gaeilge (Éire)', 'IE', 'ga', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(123, 0, 'gl', 'Galician', 'galego', '', 'gl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(124, 0, 'gl_ES', 'Galician (Spain)', 'galego (España)', 'ES', 'gl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(125, 0, 'gu', 'Gujarati', 'ગુજરાતી', '', 'gu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(126, 0, 'gu_IN', 'Gujarati (India)', 'ગુજરાતી (ભારત)', 'IN', 'gu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(127, 0, 'gv', 'Manx', 'Gaelg', '', 'gv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(128, 0, 'gv_GB', 'Manx (United Kingdom)', 'Gaelg (Rywvaneth Unys)', 'GB', 'gv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(129, 0, 'ha', 'Hausa', 'Haoussa', '', 'ha', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(130, 0, 'ha_Latn', 'Hausa (Latin)', 'Haoussa (Latn)', '', 'ha', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(131, 0, 'ha_Latn_NG', 'Hausa (Latin, Nigeria)', 'Haoussa (Latn, Nijeriya)', 'NG', 'ha', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(132, 0, 'haw', 'Hawaiian', 'ʻōlelo Hawaiʻi', '', 'haw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(133, 0, 'haw_US', 'Hawaiian (United States)', 'ʻōlelo Hawaiʻi (ʻAmelika Hui Pū ʻIa)', 'US', 'haw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(134, 0, 'he', 'Hebrew', 'עברית', '', 'he', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(135, 0, 'he_IL', 'Hebrew (Israel)', 'עברית (ישראל)', 'IL', 'he', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(136, 0, 'hi', 'Hindi', 'हिन्दी', '', 'hi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(137, 0, 'hi_IN', 'Hindi (India)', 'हिन्दी (भारत)', 'IN', 'hi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(138, 0, 'hr', 'Croatian', 'hrvatski', '', 'hr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(139, 0, 'hr_HR', 'Croatian (Croatia)', 'hrvatski (Hrvatska)', 'HR', 'hr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(140, 0, 'hu', 'Hungarian', 'magyar', '', 'hu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(141, 0, 'hu_HU', 'Hungarian (Hungary)', 'magyar (Magyarország)', 'HU', 'hu', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(142, 0, 'hy', 'Armenian', 'Հայերէն', '', 'hy', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(143, 0, 'hy_AM', 'Armenian (Armenia)', 'Հայերէն (Հայաստանի Հանրապետութիւն)', 'AM', 'hy', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(144, 0, 'hy_AM_REVISED', 'Armenian (Armenia, Revised Orthography)', 'Հայերէն (Հայաստանի Հանրապետութիւն, REVISED)', 'AM', 'hy', NULL, FALSE, 'REVISED')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(145, 0, 'id', 'Indonesian', 'Bahasa Indonesia', '', 'id', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(146, 0, 'id_ID', 'Indonesian (Indonesia)', 'Bahasa Indonesia (Indonesia)', 'ID', 'id', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(147, 0, 'ii', 'Sichuan Yi', 'ꆈꌠꉙ', '', 'ii', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(148, 0, 'ii_CN', 'Sichuan Yi (China)', 'ꆈꌠꉙ (ꍏꇩ)', 'CN', 'ii', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(149, 0, 'is', 'Icelandic', 'íslenska', '', 'is', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(150, 0, 'is_IS', 'Icelandic (Iceland)', 'íslenska (Ísland)', 'IS', 'is', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(151, 0, 'it', 'Italian', 'italiano', '', 'it', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(152, 0, 'it_CH', 'Italian (Switzerland)', 'italiano (Svizzera)', 'CH', 'it', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(153, 0, 'it_IT', 'Italian (Italy)', 'italiano (Italia)', 'IT', 'it', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(154, 0, 'ja', 'Japanese', '日本語', '', 'ja', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(155, 0, 'ja_JP', 'Japanese (Japan)', '日本語 (日本)', 'JP', 'ja', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(156, 0, 'ka', 'Georgian', 'ქართული', '', 'ka', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(157, 0, 'ka_GE', 'Georgian (Georgia)', 'ქართული (საქართველო)', 'GE', 'ka', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(158, 0, 'kk', 'Kazakh', 'Қазақ', '', 'kk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(159, 0, 'kk_Cyrl', 'Kazakh (Cyrillic)', 'Қазақ (Cyrl)', '', 'kk', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(160, 0, 'kk_Cyrl_KZ', 'Kazakh (Cyrillic, Kazakhstan)', 'Қазақ (Cyrl, Қазақстан)', 'KZ', 'kk', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(161, 0, 'kl', 'Kalaallisut', 'kalaallisut', '', 'kl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(162, 0, 'kl_GL', 'Kalaallisut (Greenland)', 'kalaallisut (Kalaallit Nunaat)', 'GL', 'kl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(163, 0, 'km', 'Khmer', 'ភាសាខ្មែរ', '', 'km', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(164, 0, 'km_KH', 'Khmer (Cambodia)', 'ភាសាខ្មែរ (កម្ពុជា)', 'KH', 'km', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(165, 0, 'kn', 'Kannada', 'ಕನ್ನಡ', '', 'kn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(166, 0, 'kn_IN', 'Kannada (India)', 'ಕನ್ನಡ (ಭಾರತ)', 'IN', 'kn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(167, 0, 'ko', 'Korean', '한국어', '', 'ko', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(168, 0, 'ko_KR', 'Korean (South Korea)', '한국어 (대한민국)', 'KR', 'ko', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(169, 0, 'kok', 'Konkani', 'कोंकणी', '', 'kok', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(170, 0, 'kok_IN', 'Konkani (India)', 'कोंकणी (भारत)', 'IN', 'kok', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(171, 0, 'kw', 'Cornish', 'kernewek', '', 'kw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(172, 0, 'kw_GB', 'Cornish (United Kingdom)', 'kernewek (Rywvaneth Unys)', 'GB', 'kw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(173, 0, 'lt', 'Lithuanian', 'lietuvių', '', 'lt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(174, 0, 'lt_LT', 'Lithuanian (Lithuania)', 'lietuvių (Lietuva)', 'LT', 'lt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(175, 0, 'lv', 'Latvian', 'latviešu', '', 'lv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(176, 0, 'lv_LV', 'Latvian (Latvia)', 'latviešu (Latvija)', 'LV', 'lv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(177, 0, 'mk', 'Macedonian', 'македонски', '', 'mk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(178, 0, 'mk_MK', 'Macedonian (Macedonia)', 'македонски (Македонија)', 'MK', 'mk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(179, 0, 'ml', 'Malayalam', 'മലയാളം', '', 'ml', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(180, 0, 'ml_IN', 'Malayalam (India)', 'മലയാളം (ഇന്ത്യ)', 'IN', 'ml', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(181, 0, 'mr', 'Marathi', 'मराठी', '', 'mr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(182, 0, 'mr_IN', 'Marathi (India)', 'मराठी (भारत)', 'IN', 'mr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(183, 0, 'ms', 'Malay', 'Bahasa Melayu', '', 'ms', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(184, 0, 'ms_BN', 'Malay (Brunei)', 'Bahasa Melayu (Brunei)', 'BN', 'ms', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(185, 0, 'ms_MY', 'Malay (Malaysia)', 'Bahasa Melayu (Malaysia)', 'MY', 'ms', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(186, 0, 'mt', 'Maltese', 'Malti', '', 'mt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(187, 0, 'mt_MT', 'Maltese (Malta)', 'Malti (Malta)', 'MT', 'mt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(188, 0, 'nb', 'Norwegian Bokmål', 'norsk bokmål', '', 'nb', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(189, 0, 'nb_NO', 'Norwegian Bokmål (Norway)', 'norsk bokmål (Norge)', 'NO', 'nb', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(190, 0, 'ne', 'Nepali', 'नेपाली', '', 'ne', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(191, 0, 'ne_IN', 'Nepali (India)', 'नेपाली (भारत)', 'IN', 'ne', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(192, 0, 'ne_NP', 'Nepali (Nepal)', 'नेपाली (नेपाल)', 'NP', 'ne', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(193, 0, 'nl', 'Dutch', 'Nederlands', '', 'nl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(194, 0, 'nl_BE', 'Dutch (Belgium)', 'Nederlands (België)', 'BE', 'nl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(195, 0, 'nl_NL', 'Dutch (Netherlands)', 'Nederlands (Nederland)', 'NL', 'nl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(196, 0, 'nn', 'Norwegian Nynorsk', 'nynorsk', '', 'nn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(197, 0, 'nn_NO', 'Norwegian Nynorsk (Norway)', 'nynorsk (Noreg)', 'NO', 'nn', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(198, 0, 'om', 'Oromo', 'Oromoo', '', 'om', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(199, 0, 'om_ET', 'Oromo (Ethiopia)', 'Oromoo (Itoophiyaa)', 'ET', 'om', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(200, 0, 'om_KE', 'Oromo (Kenya)', 'Oromoo (Keeniyaa)', 'KE', 'om', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(201, 0, 'or', 'Oriya', 'ଓଡ଼ିଆ', '', 'or', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(202, 0, 'or_IN', 'Oriya (India)', 'ଓଡ଼ିଆ (ଭାରତ)', 'IN', 'or', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(203, 0, 'pa', 'Punjabi', 'ਪੰਜਾਬੀ', '', 'pa', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(204, 0, 'pa_Arab', 'Punjabi (Arabic)', 'پنجاب (العربية)', '', 'pa', 'Arab', TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(205, 0, 'pa_Arab_PK', 'Punjabi (Arabic, Pakistan)', 'پنجاب (العربية, پکستان)', 'PK', 'pa', 'Arab', TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(206, 0, 'pa_Guru', 'Punjabi (Gurmukhi)', 'ਪੰਜਾਬੀ (Guru)', '', 'pa', 'Guru', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(207, 0, 'pa_Guru_IN', 'Punjabi (Gurmukhi, India)', 'ਪੰਜਾਬੀ (Guru, ਭਾਰਤ)', 'IN', 'pa', 'Guru', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(208, 0, 'pl', 'Polish', 'polski', '', 'pl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(209, 0, 'pl_PL', 'Polish (Poland)', 'polski (Polska)', 'PL', 'pl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(210, 0, 'ps', 'Pashto', 'پښتو', '', 'ps', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(211, 0, 'ps_AF', 'Pashto (Afghanistan)', 'پښتو (افغانستان)', 'AF', 'ps', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(212, 0, 'pt', 'Portuguese', 'português', '', 'pt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(213, 0, 'pt_BR', 'Portuguese (Brazil)', 'português (Brasil)', 'BR', 'pt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(214, 0, 'pt_PT', 'Portuguese (Portugal)', 'português (Portugal)', 'PT', 'pt', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(215, 0, 'ro', 'Romanian', 'română', '', 'ro', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(216, 0, 'ro_RO', 'Romanian (Romania)', 'română (România)', 'RO', 'ro', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(217, 0, 'ru', 'Russian', 'русский', '', 'ru', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(218, 0, 'ru_RU', 'Russian (Russia)', 'русский (Россия)', 'RU', 'ru', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(219, 0, 'ru_UA', 'Russian (Ukraine)', 'русский (Украина)', 'UA', 'ru', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(220, 0, 'si', 'Sinhala', 'සිංහල', '', 'si', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(221, 0, 'si_LK', 'Sinhala (Sri Lanka)', 'සිංහල (ශ්‍රී ලංකාව)', 'LK', 'si', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(222, 0, 'sk', 'Slovak', 'slovenský', '', 'sk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(223, 0, 'sk_SK', 'Slovak (Slovakia)', 'slovenský (Slovenská republika)', 'SK', 'sk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(224, 0, 'sl', 'Slovenian', 'slovenščina', '', 'sl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(225, 0, 'sl_SI', 'Slovenian (Slovenia)', 'slovenščina (Slovenija)', 'SI', 'sl', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(226, 0, 'so', 'Somali', 'Soomaali', '', 'so', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(227, 0, 'so_DJ', 'Somali (Djibouti)', 'Soomaali (Jabuuti)', 'DJ', 'so', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(228, 0, 'so_ET', 'Somali (Ethiopia)', 'Soomaali (Itoobiya)', 'ET', 'so', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(229, 0, 'so_KE', 'Somali (Kenya)', 'Soomaali (Kiiniya)', 'KE', 'so', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(230, 0, 'so_SO', 'Somali (Somalia)', 'Soomaali (Soomaaliya)', 'SO', 'so', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(231, 0, 'sq', 'Albanian', 'shqipe', '', 'sq', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(232, 0, 'sq_AL', 'Albanian (Albania)', 'shqipe (Shqipëria)', 'AL', 'sq', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(233, 0, 'sr', 'Serbian', 'Српски', '', 'sr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(234, 0, 'sr_Cyrl', 'Serbian (Cyrillic)', 'Српски (Ћирилица)', '', 'sr', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(235, 0, 'sr_Cyrl_BA', 'Serbian (Cyrillic, Bosnia and Herzegovina)', 'Српски (Ћирилица, Босна и Херцеговина)', 'BA', 'sr', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(236, 0, 'sr_Cyrl_ME', 'Serbian (Cyrillic, Montenegro)', 'Српски (Ћирилица, Црна Гора)', 'ME', 'sr', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(237, 0, 'sr_Cyrl_RS', 'Serbian (Cyrillic, Serbia)', 'Српски (Ћирилица, Србија)', 'RS', 'sr', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(238, 0, 'sr_Latn', 'Serbian (Latin)', 'Srpski (Latinica)', '', 'sr', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(239, 0, 'sr_Latn_BA', 'Serbian (Latin, Bosnia and Herzegovina)', 'Srpski (Latinica, Bosna i Hercegovina)', 'BA', 'sr', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(240, 0, 'sr_Latn_ME', 'Serbian (Latin, Montenegro)', 'Srpski (Latinica, Crna Gora)', 'ME', 'sr', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(241, 0, 'sr_Latn_RS', 'Serbian (Latin, Serbia)', 'Srpski (Latinica, Srbija)', 'RS', 'sr', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(242, 0, 'sv', 'Swedish', 'svenska', '', 'sv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(243, 0, 'sv_FI', 'Swedish (Finland)', 'svenska (Finland)', 'FI', 'sv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(244, 0, 'sv_SE', 'Swedish (Sweden)', 'svenska (Sverige)', 'SE', 'sv', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(245, 0, 'sw', 'Swahili', 'Kiswahili', '', 'sw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(246, 0, 'sw_KE', 'Swahili (Kenya)', 'Kiswahili (Kenya)', 'KE', 'sw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(247, 0, 'sw_TZ', 'Swahili (Tanzania)', 'Kiswahili (Tanzania)', 'TZ', 'sw', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(248, 0, 'ta', 'Tamil', 'தமிழ்', '', 'ta', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(249, 0, 'ta_IN', 'Tamil (India)', 'தமிழ் (இந்தியா)', 'IN', 'ta', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(250, 0, 'te', 'Telugu', 'తెలుగు', '', 'te', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(251, 0, 'te_IN', 'Telugu (India)', 'తెలుగు (భారత దేళం)', 'IN', 'te', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(252, 0, 'th', 'Thai', 'ไทย', '', 'th', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(253, 0, 'th_TH', 'Thai (Thailand)', 'ไทย (ไทย)', 'TH', 'th', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(254, 0, 'ti', 'Tigrinya', 'ትግርኛ', '', 'ti', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(255, 0, 'ti_ER', 'Tigrinya (Eritrea)', 'ትግርኛ (ኤርትራ)', 'ER', 'ti', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(256, 0, 'ti_ET', 'Tigrinya (Ethiopia)', 'ትግርኛ (ኢትዮጵያ)', 'ET', 'ti', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(257, 0, 'tr', 'Turkish', 'Türkçe', '', 'tr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(258, 0, 'tr_TR', 'Turkish (Turkey)', 'Türkçe (Türkiye)', 'TR', 'tr', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(259, 0, 'uk', 'Ukrainian', 'українська', '', 'uk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(260, 0, 'uk_UA', 'Ukrainian (Ukraine)', 'українська (Україна)', 'UA', 'uk', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(261, 0, 'ur', 'Urdu', 'اردو', '', 'ur', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(262, 0, 'ur_IN', 'Urdu (India)', 'اردو (بھارت)', 'IN', 'ur', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(263, 0, 'ur_PK', 'Urdu (Pakistan)', 'اردو (پاکستان)', 'PK', 'ur', NULL, TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(264, 0, 'uz', 'Uzbek', 'Ўзбек', '', 'uz', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(265, 0, 'uz_Arab', 'Uzbek (Arabic)', 'اۉزبېک (Arab)', '', 'uz', 'Arab', TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(266, 0, 'uz_Arab_AF', 'Uzbek (Arabic, Afghanistan)', 'اۉزبېک (Arab, افغانستان)', 'AF', 'uz', 'Arab', TRUE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(267, 0, 'uz_Cyrl', 'Uzbek (Cyrillic)', 'Ўзбек (Cyrl)', '', 'uz', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(268, 0, 'uz_Cyrl_UZ', 'Uzbek (Cyrillic, Uzbekistan)', 'Ўзбек (Cyrl, Ўзбекистон)', 'UZ', 'uz', 'Cyrl', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(269, 0, 'uz_Latn', 'Uzbek (Latin)', 'o''zbekcha (Lotin)', '', 'uz', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(270, 0, 'uz_Latn_UZ', 'Uzbek (Latin, Uzbekistan)', 'o''zbekcha (Lotin, Oʿzbekiston)', 'UZ', 'uz', 'Latn', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(271, 0, 'vi', 'Vietnamese', 'Tiếng Việt', '', 'vi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(272, 0, 'vi_VN', 'Vietnamese (Vietnam)', 'Tiếng Việt (Việt Nam)', 'VN', 'vi', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(273, 0, 'zh', 'Chinese', '中文', '', 'zh', NULL, FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(274, 0, 'zh_Hans', 'Chinese (Simplified Han)', '中文 (简体中文)', '', 'zh', 'Hans', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(275, 0, 'zh_Hans_CN', 'Chinese (Simplified Han, China)', '中文 (简体中文, 中国)', 'CN', 'zh', 'Hans', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(276, 0, 'zh_Hans_HK', 'Chinese (Simplified Han, Hong Kong SAR China)', '中文 (简体中文, 中国香港特别行政区)', 'HK', 'zh', 'Hans', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(277, 0, 'zh_Hans_MO', 'Chinese (Simplified Han, Macau SAR China)', '中文 (简体中文, 中国澳门特别行政区)', 'MO', 'zh', 'Hans', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(278, 0, 'zh_Hans_SG', 'Chinese (Simplified Han, Singapore)', '中文 (简体中文, 新加坡)', 'SG', 'zh', 'Hans', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(279, 0, 'zh_Hant', 'Chinese (Traditional Han)', '中文 (繁體中文)', '', 'zh', 'Hant', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(280, 0, 'zh_Hant_HK', 'Chinese (Traditional Han, Hong Kong SAR China)', '中文 (繁體中文, 中華人民共和國香港特別行政區)', 'HK', 'zh', 'Hant', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(281, 0, 'zh_Hant_MO', 'Chinese (Traditional Han, Macau SAR China)', '中文 (繁體中文, 中華人民共和國澳門特別行政區)', 'MO', 'zh', 'Hant', FALSE, '')
insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(282, 0, 'zh_Hant_TW', 'Chinese (Traditional Han, Taiwan)', '中文 (繁體中文, 臺灣)', 'TW', 'zh', 'Hant', FALSE, '')

update Locale set parentId = 1 where id = 2
update Locale set parentId = 1 where id = 3
update Locale set parentId = 4 where id = 5
update Locale set parentId = 6 where id = 7
update Locale set parentId = 6 where id = 8
update Locale set parentId = 6 where id = 9
update Locale set parentId = 6 where id = 10
update Locale set parentId = 6 where id = 11
update Locale set parentId = 6 where id = 12
update Locale set parentId = 6 where id = 13
update Locale set parentId = 6 where id = 14
update Locale set parentId = 6 where id = 15
update Locale set parentId = 6 where id = 16
update Locale set parentId = 6 where id = 17
update Locale set parentId = 6 where id = 18
update Locale set parentId = 6 where id = 19
update Locale set parentId = 6 where id = 20
update Locale set parentId = 6 where id = 21
update Locale set parentId = 6 where id = 22
update Locale set parentId = 6 where id = 23
update Locale set parentId = 24 where id = 25
update Locale set parentId = 26 where id = 27
update Locale set parentId = 27 where id = 28
update Locale set parentId = 26 where id = 29
update Locale set parentId = 29 where id = 30
update Locale set parentId = 31 where id = 32
update Locale set parentId = 33 where id = 34
update Locale set parentId = 35 where id = 36
update Locale set parentId = 35 where id = 37
update Locale set parentId = 38 where id = 39
update Locale set parentId = 40 where id = 41
update Locale set parentId = 42 where id = 43
update Locale set parentId = 44 where id = 45
update Locale set parentId = 46 where id = 47
update Locale set parentId = 46 where id = 48
update Locale set parentId = 46 where id = 49
update Locale set parentId = 46 where id = 50
update Locale set parentId = 46 where id = 51
update Locale set parentId = 46 where id = 52
update Locale set parentId = 53 where id = 54
update Locale set parentId = 53 where id = 55
update Locale set parentId = 56 where id = 57
update Locale set parentId = 56 where id = 58
update Locale set parentId = 56 where id = 59
update Locale set parentId = 56 where id = 60
update Locale set parentId = 56 where id = 61
update Locale set parentId = 56 where id = 62
update Locale set parentId = 56 where id = 63
update Locale set parentId = 56 where id = 64
update Locale set parentId = 56 where id = 65
update Locale set parentId = 56 where id = 66
update Locale set parentId = 56 where id = 67
update Locale set parentId = 56 where id = 68
update Locale set parentId = 56 where id = 69
update Locale set parentId = 56 where id = 70
update Locale set parentId = 56 where id = 71
update Locale set parentId = 56 where id = 72
update Locale set parentId = 56 where id = 73
update Locale set parentId = 56 where id = 74
update Locale set parentId = 56 where id = 75
update Locale set parentId = 75 where id = 76
update Locale set parentId = 56 where id = 77
update Locale set parentId = 56 where id = 78
update Locale set parentId = 56 where id = 79
update Locale set parentId = 81 where id = 82
update Locale set parentId = 81 where id = 83
update Locale set parentId = 81 where id = 84
update Locale set parentId = 81 where id = 85
update Locale set parentId = 81 where id = 86
update Locale set parentId = 81 where id = 87
update Locale set parentId = 81 where id = 88
update Locale set parentId = 81 where id = 89
update Locale set parentId = 81 where id = 90
update Locale set parentId = 81 where id = 91
update Locale set parentId = 81 where id = 92
update Locale set parentId = 81 where id = 93
update Locale set parentId = 81 where id = 94
update Locale set parentId = 81 where id = 95
update Locale set parentId = 81 where id = 96
update Locale set parentId = 81 where id = 97
update Locale set parentId = 81 where id = 98
update Locale set parentId = 81 where id = 99
update Locale set parentId = 81 where id = 100
update Locale set parentId = 81 where id = 101
update Locale set parentId = 102 where id = 103
update Locale set parentId = 104 where id = 105
update Locale set parentId = 106 where id = 107
update Locale set parentId = 106 where id = 108
update Locale set parentId = 109 where id = 110
update Locale set parentId = 111 where id = 112
update Locale set parentId = 113 where id = 114
update Locale set parentId = 113 where id = 115
update Locale set parentId = 113 where id = 116
update Locale set parentId = 113 where id = 117
update Locale set parentId = 113 where id = 118
update Locale set parentId = 113 where id = 119
update Locale set parentId = 113 where id = 120
update Locale set parentId = 121 where id = 122
update Locale set parentId = 123 where id = 124
update Locale set parentId = 125 where id = 126
update Locale set parentId = 127 where id = 128
update Locale set parentId = 129 where id = 130
update Locale set parentId = 130 where id = 131
update Locale set parentId = 132 where id = 133
update Locale set parentId = 134 where id = 135
update Locale set parentId = 136 where id = 137
update Locale set parentId = 138 where id = 139
update Locale set parentId = 140 where id = 141
update Locale set parentId = 142 where id = 143
update Locale set parentId = 143 where id = 144
update Locale set parentId = 145 where id = 146
update Locale set parentId = 147 where id = 148
update Locale set parentId = 149 where id = 150
update Locale set parentId = 151 where id = 152
update Locale set parentId = 151 where id = 153
update Locale set parentId = 154 where id = 155
update Locale set parentId = 156 where id = 157
update Locale set parentId = 158 where id = 159
update Locale set parentId = 159 where id = 160
update Locale set parentId = 161 where id = 162
update Locale set parentId = 163 where id = 164
update Locale set parentId = 165 where id = 166
update Locale set parentId = 167 where id = 168
update Locale set parentId = 169 where id = 170
update Locale set parentId = 171 where id = 172
update Locale set parentId = 173 where id = 174
update Locale set parentId = 175 where id = 176
update Locale set parentId = 177 where id = 178
update Locale set parentId = 179 where id = 180
update Locale set parentId = 181 where id = 182
update Locale set parentId = 183 where id = 184
update Locale set parentId = 183 where id = 185
update Locale set parentId = 186 where id = 187
update Locale set parentId = 188 where id = 189
update Locale set parentId = 190 where id = 191
update Locale set parentId = 190 where id = 192
update Locale set parentId = 193 where id = 194
update Locale set parentId = 193 where id = 195
update Locale set parentId = 196 where id = 197
update Locale set parentId = 198 where id = 199
update Locale set parentId = 198 where id = 200
update Locale set parentId = 201 where id = 202
update Locale set parentId = 203 where id = 204
update Locale set parentId = 204 where id = 205
update Locale set parentId = 203 where id = 206
update Locale set parentId = 206 where id = 207
update Locale set parentId = 208 where id = 209
update Locale set parentId = 210 where id = 211
update Locale set parentId = 212 where id = 213
update Locale set parentId = 212 where id = 214
update Locale set parentId = 215 where id = 216
update Locale set parentId = 217 where id = 218
update Locale set parentId = 217 where id = 219
update Locale set parentId = 220 where id = 221
update Locale set parentId = 222 where id = 223
update Locale set parentId = 224 where id = 225
update Locale set parentId = 226 where id = 227
update Locale set parentId = 226 where id = 228
update Locale set parentId = 226 where id = 229
update Locale set parentId = 226 where id = 230
update Locale set parentId = 231 where id = 232
update Locale set parentId = 233 where id = 234
update Locale set parentId = 234 where id = 235
update Locale set parentId = 234 where id = 236
update Locale set parentId = 234 where id = 237
update Locale set parentId = 233 where id = 238
update Locale set parentId = 238 where id = 239
update Locale set parentId = 238 where id = 240
update Locale set parentId = 238 where id = 241
update Locale set parentId = 242 where id = 243
update Locale set parentId = 242 where id = 244
update Locale set parentId = 245 where id = 246
update Locale set parentId = 245 where id = 247
update Locale set parentId = 248 where id = 249
update Locale set parentId = 250 where id = 251
update Locale set parentId = 252 where id = 253
update Locale set parentId = 254 where id = 255
update Locale set parentId = 254 where id = 256
update Locale set parentId = 257 where id = 258
update Locale set parentId = 259 where id = 260
update Locale set parentId = 261 where id = 262
update Locale set parentId = 261 where id = 263
update Locale set parentId = 264 where id = 265
update Locale set parentId = 265 where id = 266
update Locale set parentId = 264 where id = 267
update Locale set parentId = 267 where id = 268
update Locale set parentId = 264 where id = 269
update Locale set parentId = 269 where id = 270
update Locale set parentId = 271 where id = 272
update Locale set parentId = 273 where id = 274
update Locale set parentId = 274 where id = 275
update Locale set parentId = 274 where id = 276
update Locale set parentId = 274 where id = 277
update Locale set parentId = 274 where id = 278
update Locale set parentId = 273 where id = 279
update Locale set parentId = 279 where id = 280
update Locale set parentId = 279 where id = 281
update Locale set parentId = 279 where id = 282



