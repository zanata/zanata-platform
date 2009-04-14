package org.jboss.shotoku.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.shotoku.ContentManager;
import org.jboss.shotoku.Directory;
import org.jboss.shotoku.Node;
import org.jboss.shotoku.cache.ShotokuCacheItem;
import org.jboss.shotoku.cache.ShotokuCache;
import org.jboss.shotoku.aop.Inject;
import org.jboss.shotoku.aop.CacheItem;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class ShotokuServlet extends HttpServlet {
    public static class TestCacheItem extends ShotokuCacheItem<String, String> {
        public void update(String key, String currentObject) {
            put(key, "new");
        }

        public String init(String key) {
            return "init";
        }
    }

    @Inject(prefix="shotoku-test")
    ContentManager test;

    /*private void readInputStream(InputStream is) throws IOException {
         int b;
         StringBuffer sb = new StringBuffer();
         while ((b = is.read()) != -1)
             sb.append(b);
     }*/

    /*@CacheItem
    private TestCacheItem tci;*/

    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        

        //System.out.println("Cache content: " + tci.get("A"));

        /*try {
			response.setContentType("text/html");
			PrintWriter pw = response.getWriter();
			pw.write("Depends test: " + test + "<br />");
			pw.write("<br />");
			
			try {
				Directory d = test.getRootDirectory().newDirectory("Z");
				d.save("");
				
				test.getRootDirectory().newNode("BB").save("");
				
				d = test.getDirectory("Z");
				Node n = d.newNode("A");
				n.save("C");
				
				test.getRootDirectory().getNode("BB").getLastModification();
				d.getNode("A").getLastModification();
			} finally {
				test.getDirectory("Z").delete();
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}         */

        //NodeList nl = new NodeList();
        //pw.write(nl.getFeed("default/feeds/templates/hello-world.vm", null, new HashMap<String, Object>()).toString());

        /*int processed;
		
          pw.write("Shotoku file read: <br />");
          long start = Calendar.getInstance().getTimeInMillis();
          processed = 0;
          for (Node n : wikiCm.getRootDirectory().getNodes().toList()) {
              readInputStream(wikiCm.getNode(n.getName()).getContentInputStream());
              processed++;
          }
          long stop = Calendar.getInstance().getTimeInMillis();
          pw.write("Time: " + (stop-start) + ", processed: " + processed);
          pw.write("<br />");
          pw.write("<br />");
		
          String pathPrefix = "/home/adamw/portal-content/wiki-content/";
		
          pw.write("Normal file read: <br />");
          start = Calendar.getInstance().getTimeInMillis();
          String[] files = new File(pathPrefix).list();
          processed = 0;
          for (String file : files) {
              File f = new File(pathPrefix + file);
              if (f.isFile()) {
                  readInputStream(new FileInputStream(f));
                  processed++;
              }
          }
          stop = Calendar.getInstance().getTimeInMillis();
          pw.write("Time: " + (stop-start) + ", processed: " + processed);*/
    }

}
