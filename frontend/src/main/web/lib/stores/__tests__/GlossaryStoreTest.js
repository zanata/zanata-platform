jest.dontMock('../GlossaryStore')
  .dontMock('lodash')
  .dontMock('moment')
  .dontMock('moment-range')
  .dontMock('../../utils/GlossaryHelper')
  .dontMock('../../utils/StringUtils')
  .dontMock('../../utils/DateHelper');


describe('GlossaryStoreTest', function() {
  var baseUrl = 'http://localhost/base';
  var user = {"username": "test-user", "email":"zanata@zanata.org", "name": "admin-name", "loggedIn": "true", "imageUrl":"//www.gravatar.com/avatar/dda6e90e3f2a615fb8b31205e8b4894b?d=mm&r=g&s=115", "languageTeams": "English, French, German, Yodish (Yoda English)"}
  var data = {"permission":{"updateGlossary":true, "insertGlossary":true}, "dev": "true", "profileUser" : {"username": "test-user", "email":"zanata@zanata.org", "name":"admin-name","loggedIn":"true","imageUrl":"//www.gravatar.com/avatar/dda6e90e3f2a615fb8b31205e8b4894b?d=mm&r=g&s=115","languageTeams":"English, French, German, Yodish (Yoda English)"}}
  var _ = require('lodash');

  beforeEach(function() {
    require('../../constants/Configs').baseUrl = baseUrl;
    require('../../constants/Configs').user = user;
    require('../../constants/Configs').data = data;
  });

  it('will load from server if state.locales is empty', function() {
    var zhHans = {
      "locale": {"localeId": "zh-Hans", "displayName": "Chinese (Simplified)", "alias": ""},
      "numberOfTerms": 0
    };
    var zhHant = {
      "locale": {"localeId": "zh-Hant", "displayName": "Chinese (Traditional)", "alias": ""},
      "numberOfTerms": 0
    };
    var respLocaleStats = {
      "srcLocale": {
        "locale": {"localeId": "en-US", "displayName": "English (United States)", "alias": ""},
        "numberOfTerms": 474
      },
      "transLocale" : [zhHans, zhHant]
    };
    var entry1 = {
        "id": '1', "pos": "noun", "description": "desc",
        "srcLang": "en-US", "sourceReference": "bgroh@172.16.5.77",
        "glossaryTerms": [
          {"content": "process", "locale": "en-US", "lastModifiedDate": 1439435990000, "lastModifiedBy": ""}
        ],
        "termsCount": 2
      },
      entry2 = {
        "id": '2', "pos": "noun", "description": "desc",
        "srcLang": "en-US", "sourceReference": "hpeters@10.64.1.231",
        "glossaryTerms": [
          {"content": "implementation", "locale": "en-US", "lastModifiedDate": 1439435990000, "lastModifiedBy": ""}
        ],
        "termsCount": 2
      };

    var respGlossaryEntries = {"totalCount": 474, "glossaryEntries": [entry1, entry2]};

    var infoUrl = 'http://localhost/base/glossary/info';
    var dataUrl = 'http://localhost/base/glossary/src/en-US?page=1&sizePerPage=5000&sort=src_content';

    var GlossaryStore = require('../GlossaryStore');
    var MockRequest = require('superagent');

    MockRequest.__setResponse(infoUrl, {error: false, body: respLocaleStats});
    MockRequest.__setResponse(dataUrl, {error: false, body: respGlossaryEntries});

    GlossaryStore.addChangeListener(function() {
      var state = GlossaryStore.init();
      expect(state.canAddNewEntry).toEqual(data.permission.insertGlossary);
      expect(state.canUpdateEntry).toEqual(data.permission.updateGlossary);
      expect(state.srcLocale).toEqual(respLocaleStats.srcLocale);
      expect(_.size(state.locales)).toEqual(_.size(respLocaleStats.transLocale));
      expect(state.locales[zhHans.locale.localeId]).toEqual(zhHans);
      expect(state.locales[zhHant.locale.localeId]).toEqual(zhHant);
      expect(_.size(state.localeOptions)).toEqual(_.size(respLocaleStats.transLocale));
      expect(_.size(state.glossary)).toEqual(_.size(respGlossaryEntries.glossaryEntries));

      var resultEntry1 = state.glossary[entry1.id];
      expect(resultEntry1.id).toEqual(entry1.id);
      expect(resultEntry1.pos).toEqual(entry1.pos);
      expect(resultEntry1.description).toEqual(entry1.description);
      expect(resultEntry1.srcTerm.locale).toEqual(entry1.srcLang);
      expect(resultEntry1.srcTerm.reference).toEqual(entry1.sourceReference);

      var resultEntry2 = state.glossary[entry2.id];
      expect(resultEntry2.id).toEqual(entry2.id);
      expect(resultEntry2.pos).toEqual(entry2.pos);
      expect(resultEntry2.description).toEqual(entry2.description);
      expect(resultEntry2.srcTerm.locale).toEqual(entry2.srcLang);
      expect(resultEntry2.srcTerm.reference).toEqual(entry2.sourceReference);

      expect(state.original_glossary).toEqual(state.glossary);

      expect(state.totalCount).toEqual(respGlossaryEntries.totalCount);
      expect(_.size(state.glossaryHash)).toEqual(respGlossaryEntries.totalCount);
    });

    GlossaryStore.init();
  });
});
