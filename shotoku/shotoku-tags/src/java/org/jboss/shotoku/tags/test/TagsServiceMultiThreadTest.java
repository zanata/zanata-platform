package org.jboss.shotoku.tags.test;

import org.jboss.shotoku.tags.tools.TagTools;
import org.jboss.shotoku.tags.ShotokuTag;
import org.jboss.shotoku.tags.Tag;

import java.util.Random;
import java.util.Date;

/**
 * @author Adam Warski (adamw@aster.pl)
 */
public class TagsServiceMultiThreadTest {
    private void start(final int iterations) {
        new Thread() {
            private String[] tagNames = {
                    "tag0", "tag1", "tag2", "tag3", "tag4", "tag5" };

            private String[] authors = { "admin", "adamw" };

            private String[] resIds = {
                    "default/members/kosmos/freezone/kosmonauts.html",
                    "default/members/kosmos/freezone/contribution.html",
                    "default/members/kosmos/freezone/compatibility.html"
            };

            private Random random = new Random();

            private int getRandom(int end) {
                return random.nextInt(end);
            }

            public void run() {
                int exceptions = 0;
                for (int i=0; i<iterations; i++) {
                    try {
                        switch (getRandom(13)) {
                            case 0:
                                TagTools.getService().getTag(
                                        tagNames[getRandom(tagNames.length)]);
                                break;
                            case 1:
                            case 9:
                            case 10:
                            case 11:
                            case 12:
                                Tag t = new ShotokuTag(
                                        tagNames[getRandom(tagNames.length)],
                                        authors[getRandom(authors.length)],
                                        resIds[getRandom(resIds.length)],
                                        null,
                                        new Date());
                                System.out.println("Adding tag: " + t);
                                TagTools.getService().addTag(t);
                                break;
                            case 2:
                                TagTools.getService().getAllTags();
                                break;
                            case 3:
                                TagTools.getService().getTagsByAuthor(
                                        authors[getRandom(authors.length)]);
                                break;
                            case 4:
                                TagTools.getService().getTags(
                                        resIds[getRandom(resIds.length)]);
                                break;
                            case 5:
                                TagTools.getService().getTags(
                                        tagNames[getRandom(tagNames.length)],
                                        authors[getRandom(authors.length)]);
                                break;
                            case 6:
                                TagTools.getService().getUniqueTags(
                                        resIds[getRandom(resIds.length)]);
                                break;
                            case 7:
                                TagTools.getService().deleteTag(
                                        TagTools.getService().getTag(
                                                tagNames[getRandom(
                                                        tagNames.length)]));
                                break;
                            case 8:
                                TagTools.getService().getUniqueTagsByAuthor(
                                        authors[getRandom(authors.length)]);

                        }
                        //TagTools.getService().
                    } catch (Exception e) {
                        System.out.println("Exception caught (3): " +
                                e.getMessage());
                        exceptions++;
                    }
                }

                System.out.println("Done " + iterations + " operations, "
                        + exceptions + " of them threm exceptions.");
            }
        }.start();
    }

    public void start(int threads, int iterations) {
        for (int i = 0; i<threads; i++) {
            start(iterations);
        }
    }
}
