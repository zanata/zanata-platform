package org.zanata.client.config;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String text) {
      if (text == null)
         return null;
      else
        return text.trim();
    }

    @Override
    public String unmarshal(String v) throws Exception {
      if (v == null)
         return null;
      else
        return v.trim();
    }
}