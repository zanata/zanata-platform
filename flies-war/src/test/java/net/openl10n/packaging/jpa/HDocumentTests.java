package net.openl10n.packaging.jpa;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import net.openl10n.packaging.jpa.document.HDocument;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.junit.Test;

public class HDocumentTests {

	@Test
	public void testFullPathConstructors(){
		HDocument d = new HDocument("/my/doc1.txt", ContentType.TextPlain);
		assertThat(d.getDocId(), is("/my/doc1.txt"));
		assertThat(d.getName(), is("doc1.txt"));
		assertThat(d.getPath(), is("/my/"));
		assertThat(d.getContentType(), is(ContentType.TextPlain));
		
		d = new HDocument("doc1.txt", ContentType.TextPlain);
		assertThat(d.getDocId(), is("doc1.txt"));
		assertThat(d.getName(), is("doc1.txt"));
		assertThat(d.getPath(), is(""));
		assertThat(d.getContentType(), is(ContentType.TextPlain));
		
		d = new HDocument("/doc1.txt", ContentType.TextPlain);
		assertThat(d.getDocId(), is("/doc1.txt"));
		assertThat(d.getName(), is("doc1.txt"));
		assertThat(d.getPath(), is("/"));
		assertThat(d.getContentType(), is(ContentType.TextPlain));
		
		assertThat(d.getLocale(), is(LocaleId.EN_US));
		
		d = new HDocument("/doc1.txt", ContentType.TextPlain, new LocaleId("de"));
		assertThat(d.getLocale(), is( new LocaleId("de") ));
		
	}
}
