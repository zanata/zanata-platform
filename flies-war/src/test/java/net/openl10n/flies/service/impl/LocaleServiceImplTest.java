package net.openl10n.flies.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.dao.SupportedLanguageDAO;
import net.openl10n.flies.model.FliesLocalePair;
import net.openl10n.flies.model.HSupportedLanguage;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


@Test(groups = { "business-tests" })
public class LocaleServiceImplTest
{
   private LocaleServiceImpl testLocaleServiceImpl;
   private SupportedLanguageDAO mockDAO;

   // private static final Logger LOGGER =
   // Logger.getLogger(LocaleServiceImplTest.class);

   @BeforeMethod(firstTimeOnly = true)
   public void setup()
   {
      this.testLocaleServiceImpl = new LocaleServiceImpl();
      this.mockDAO = EasyMock.createMock(SupportedLanguageDAO.class);
      this.testLocaleServiceImpl.supportedLanguageDAO = mockDAO;
   }


   @Test
   public void testGetAllJavaLanguages() throws Exception
   {
      List<LocaleId> loc = this.testLocaleServiceImpl.getAllJavaLanguages();
      StringBuilder st = new StringBuilder("");
      for (LocaleId localeId : loc)
      {
         st.append(localeId.getId() + ",");
      }
      System.out.println(st);

      String re = new String(
            "af,af-NA,af-ZA,ak,ak-GH,am,am-ET,ar,ar-AE,ar-BH,ar-DZ,ar-EG,ar-IQ,ar-JO,ar-KW,ar-LB,ar-LY,ar-MA,ar-OM,ar-QA,ar-SA,ar-SD,ar-SY,ar-TN,ar-YE,as,as-IN,asa,asa-TZ,az,az-Cyrl,az-AZ-Cyrl,az-Latn,az-AZ-Latn,be,be-BY,bem,bem-ZM,bez,bez-TZ,bg,bg-BG,bm,bm-ML,bn,bn-BD,bn-IN,bo,bo-CN,bo-IN,ca,ca-ES,cgg,cgg-UG,chr,chr-US,cs,cs-CZ,cy,cy-GB,da,da-DK,dav,dav-KE,de,de-AT,de-BE,de-CH,de-DE,de-LI,de-LU,ebu,ebu-KE,ee,ee-GH,ee-TG,el,el-CY,el-GR,en,en-AU,en-BE,en-BW,en-BZ,en-CA,en-GB,en-HK,en-IE,en-IN,en-JM,en-MH,en-MT,en-MU,en-NA,en-NZ,en-PH,en-PK,en-SG,en-TT,en-US,en-US-POSIX,en-VI,en-ZA,en-ZW,eo,es,es-AR,es-BO,es-CL,es-CO,es-CR,es-DO,es-EC,es-ES,es-GQ,es-GT,es-HN,es-MX,es-NI,es-PA,es-PE,es-PR,es-PY,es-SV,es-US,es-UY,es-VE,et,et-EE,eu,eu-ES,fa,fa-AF,fa-IR,ff,ff-SN,fi,fi-FI,fil,fil-PH,fo,fo-FO,fr,fr-BE,fr-BL,fr-CA,fr-CF,fr-CH,fr-CI,fr-CM,fr-FR,fr-GN,fr-GP,fr-LU,fr-MC,fr-MF,fr-MG,fr-ML,fr-MQ,fr-NE,fr-RE,fr-SN,ga,ga-IE,gl,gl-ES,gsw,gsw-CH,gu,gu-IN,guz,guz-KE,gv,gv-GB,ha,ha-Latn,ha-GH-Latn,ha-NE-Latn,ha-NG-Latn,haw,haw-US,he,he-IL,hi,hi-IN,hr,hr-HR,hu,hu-HU,hy,hy-AM,id,id-ID,ig,ig-NG,ii,ii-CN,is,is-IS,it,it-CH,it-IT,ja,ja-JP,jmc,jmc-TZ,ka,ka-GE,kab,kab-DZ,kam,kam-KE,kde,kde-TZ,kea,kea-CV,khq,khq-ML,ki,ki-KE,kk,kk-Cyrl,kk-KZ-Cyrl,kl,kl-GL,kln,kln-KE,km,km-KH,kn,kn-IN,ko,ko-KR,kok,kok-IN,kw,kw-GB,lag,lag-TZ,lg,lg-UG,lt,lt-LT,luo,luo-KE,luy,luy-KE,lv,lv-LV,mas,mas-KE,mas-TZ,mer,mer-KE,mfe,mfe-MU,mg,mg-MG,mk,mk-MK,ml,ml-IN,mr,mr-IN,ms,ms-BN,ms-MY,mt,mt-MT,naq,naq-NA,nb,nb-NO,nd,nd-ZW,ne,ne-IN,ne-NP,nl,nl-BE,nl-NL,nn,nn-NO,nyn,nyn-UG,om,om-ET,om-KE,or,or-IN,pa,pa-Arab,pa-PK-Arab,pa-Guru,pa-IN-Guru,pl,pl-PL,ps,ps-AF,pt,pt-BR,pt-GW,pt-MZ,pt-PT,rm,rm-CH,ro,ro-MD,ro-RO,rof,rof-TZ,ru,ru-MD,ru-RU,ru-UA,rw,rw-RW,rwk,rwk-TZ,saq,saq-KE,seh,seh-MZ,ses,ses-ML,sg,sg-CF,shi,shi-Latn,shi-MA-Latn,shi-Tfng,shi-MA-Tfng,si,si-LK,sk,sk-SK,sl,sl-SI,sn,sn-ZW,so,so-DJ,so-ET,so-KE,so-SO,sq,sq-AL,sr,sr-Cyrl,sr-BA-Cyrl,sr-ME-Cyrl,sr-RS-Cyrl,sr-Latn,sr-BA-Latn,sr-ME-Latn,sr-RS-Latn,sv,sv-FI,sv-SE,sw,sw-KE,sw-TZ,ta,ta-IN,ta-LK,te,te-IN,teo,teo-KE,teo-UG,th,th-TH,ti,ti-ER,ti-ET,to,to-TO,tr,tr-TR,tzm,tzm-Latn,tzm-MA-Latn,uk,uk-UA,ur,ur-IN,ur-PK,uz,uz-Arab,uz-AF-Arab,uz-Cyrl,uz-UZ-Cyrl,uz-Latn,uz-UZ-Latn,vi,vi-VN,vun,vun-TZ,xog,xog-UG,yo,yo-NG,zh,zh-Hans,zh-CN-Hans,zh-HK-Hans,zh-MO-Hans,zh-SG-Hans,zh-Hant,zh-HK-Hant,zh-MO-Hant,zh-TW-Hant,zu,zu-ZA,");
      Assert.assertEquals(st.toString(), re);
   }

   @Test
   public void testGetAllSupportedLanguages()
   {
      List<HSupportedLanguage> lan = new ArrayList<HSupportedLanguage>();
      lan.add(new HSupportedLanguage(new LocaleId("as-IN")));
      lan.add(new HSupportedLanguage(new LocaleId("pt-BR")));
      EasyMock.expect(mockDAO.findAll()).andReturn(lan);
      EasyMock.replay(mockDAO);
      List<FliesLocalePair> sup = this.testLocaleServiceImpl.getAllLocales();
      Assert.assertEquals(sup.size(), 2);
      String loc1 = sup.get(0).getLocaleId().getId();
      Assert.assertEquals(loc1, "as-IN");
      String loc2 = sup.get(1).getLocaleId().getId();
      Assert.assertEquals(loc2, "pt-BR");
      EasyMock.verify(mockDAO);
   }
}
