package org.jboss.shotoku.tags.test;

import org.jboss.shotoku.tags.tools.TagTools;
import org.jboss.shotoku.tags.ShotokuTag;
import org.jboss.shotoku.tags.Tag;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagsTestServlet extends HttpServlet {
    static int i = 2;

    protected void doGet(HttpServletRequest request, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setContentType("text/html");

        try {
            Tag t = new ShotokuTag(
                    "tag0",
                    "adamw",
                    "default/members/kosmos/freezone/kosmonauts.html",
                    null,
                    new Date());
            System.out.println("Adding tag: " + t);
            TagTools.getService().addTag(t);
        } catch (Exception e) {
            System.out.println("EXCEPTION!");
            e.printStackTrace();
        }
        /*int threads = Integer.parseInt(request.getParameter("threads"));
        int iterations = Integer.parseInt(request.getParameter("iterations"));

        httpServletResponse.getWriter().println("Starting test ...");
        new TagsServiceMultiThreadTest().start(threads, iterations);
        httpServletResponse.getWriter().println("Done.");   */

        /*List<String> params = new ArrayList<String>();
        params.add("adamw");
        params.add("default/members/default/freezone/welcome.html");
        httpServletResponse.getWriter().println(
                TagTools.getService().getFeedLink(FeedType.AUTHOR_TAG,
                        params, "rss2"));
        httpServletResponse.getWriter().println("<br />");
        httpServletResponse.getWriter().println(
                TagTools.getService().getFeedLink(FeedType.RESOURCE,
                        "default/members/default/freezone/welcome.html",
                        "rss2"));   */

        /*httpServletResponse.getWriter().println(TagTools.getService().getServiceName());

        try {
            TagTools.getService().addTag(new ShotokuTag("n" + i++, "a1", "r1", "d1",
                    Calendar.getInstance().getTime()));
        } catch (TagAddException e) {
            e.printStackTrace();
        }

        try {
            for (Tag t : TagTools.getService().getTags("r1")) {
                httpServletResponse.getWriter().println(t.getName() + " - " + t.getData());
            }
        } catch (TagGetException e) {
            e.printStackTrace();
        }      */

        /*httpServletResponse.getWriter().println(
            TagTools.getService().getFeedCount(FeedType.AUTHOR_ALL, "adamw"));
        TagTools.getService().increaseFeedCount(FeedType.AUTHOR_ALL, "adamw");
        httpServletResponse.getWriter().println(
            TagTools.getService().getFeedCount(FeedType.AUTHOR_ALL, "adamw"));*/

        /*try {
            TagTools.getService().getUniqueTagsByAuthor("adamw");
        } catch (TagGetException e) {
            throw new ServletException(e);
        }*/
    }
}
