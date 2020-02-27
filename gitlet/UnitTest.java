package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.nio.file.Paths;

import static gitlet.Utils.*;
import static gitlet.MainUtils.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Thu Nguyen
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** Dummy test to avoid error. */
    @Test
    public void dummyTest() {
        System.out.println();
    }

    /** Given that Hello.txt is created, test doAdd. */
    @Test
    public void addTest() {
        Main.main("add ", "Hello.txt");
    }

    /** Given that Hello.txt has been added, test commit. */
    @Test
    public void commitTest() {
//        String path =System.getProperty("user.dir") + "/.gitlet/objects/comm_5ad24a26b2476df4a13a2c478ec9750dbf3417d8.txt";
//        Commit comm = readObject(join(path), Commit.class);
        Main.main("commit", "Created Hello.txt");
    }

    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void ChainTest() {
        Main.main("init");
        Main.main("add ", "World.txt");
        Main.main("add ", "Hello.txt");
        Main.main("commit ", "“Created World.txt and\n" +
                "tried to add Hello.txt”");
    }

    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleIntegratedTest() {
        Main.main("init");
        Main.main("add", "wug.txt");
        Main.main("add", "world.txt");
        Main.main("commit", "version 1 of wug.txt and world.txt");

        writeContents(join("wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        writeContents(join("hello.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/hello.txt")));
        Main.main("add", "wug.txt");
        Main.main("add", "hello.txt");
        Main.main("commit", "version 1 of hello.txt and version 2 of wug.txt");
        Main.main("log");
        Main.main("global-log");
    }
    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleIntegratedTest2() {
        Main.main("init");
        Main.main("add", "wug.txt");
        Main.main("add", "world.txt");
        Main.main("commit", "version 1");
        writeContents(join("wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "wug.txt");
        Main.main("commit", "version 1");
        Main.main("log");
    }
    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleIntegratedTest2Continue1() {
        Main.main("reset", "d888dfabacd1e586fda79cd93adf2a95007f4de6");
    }
    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleIntegratedTest2Continue2() {
        Main.main("log");
        Main.main("find", "version 1");
        Main.main("add", "world.txt");
    }
    @Test
    public void simpleIntegratedTest2Continue3() {
        Main.main("rm", "wug.txt");
        Main.main("status");
    }

    /** Given the info above, test log. */
    @Test
    public void logTest() {
//        String path =System.getProperty("user.dir") + "/.gitlet/objects/comm_5ad24a26b2476df4a13a2c478ec9750dbf3417d8.txt";
//        Commit comm = readObject(join(path), Commit.class);
        Main.main("log-global");
    }
    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleCheckoutP2() {
        Main.main("commit", "version 1 of hello.txt and version 2 of wug.txt");
    }



    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void check() {
//        Main.main("reset", "265cae8139d0d7792a4a8d97e446de1549cc050e");
        System.out.println(Paths.get(System.getProperty("user.dir"),".gitlet"));
//        System.out.println(readContentsAsString(join("notwug.txt")));
    }
    /** Test doAdd, Commit, Log altogether. */
    @Test
    public void simpleIntegratedTest3() {
        writeContents(join(WORK_DIR_STR, "wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "world.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/world.txt")));
        Main.main("init");
        Main.main("branch", "a branch");
        Main.main("add", "wug.txt");
        Main.main("commit", "added wug.txt");
        Main.main("checkout", "a branch");
        Main.main("add", "world.txt");
        Main.main("commit", "added world.txt");

        Main.main("merge", "master");
        Main.main("add", "hello.txt");
        Main.main("commit", "added hello.txt");
        Main.main("rm", "world.txt");
        Main.main("commit", "removed world.txt");
        writeContents(join(WORK_DIR_STR, "world.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/world.txt")));
        Main.main("add", "world.txt");
        Main.main("checkout", "master");
        writeContents(join(WORK_DIR_STR, "wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/hello.txt")));
        Main.main("add", "wug.txt");

        Main.main("status");
        Main.main("commit", "changed wug.txt");
    }
    @Test
    public void test36() {
        // prelude1.inc
        Main.main("init");
        Main.main("branch", "B1");
        Main.main("branch", "B2");
        Main.main("checkout", "B1");
        writeContents(join(WORK_DIR_STR, "h.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "h.txt");
        Main.main("commit", "Add h.txt");
        Main.main("checkout", "B2");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "f.txt added");
        Main.main("branch", "C1");

        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("rm", "f.txt");
        Main.main("commit", "g.txt added, f.txt removed");
        Main.main("checkout", "B1");
        Main.main("merge", "C1");
        Main.main("merge", "B2");
    }

    @Test
    public void test13() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");

        Main.main("rm", "f.txt");
        Main.main("status");
    }
    @Test
    public void test14() {
        // setup1.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("rm", "f.txt");
        Main.main("status");
    }
    @Test
    public void test15() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("rm", "f.txt");
        Main.main("add", "f.txt");
        Main.main("status");
    }
    @Test
    public void test18() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("add", "f.txt");
        Main.main("status");
    }
    @Test
    public void test20() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("rm", "f.txt");
        Main.main("commit", "Removed f.txt");
        Main.main("status");
    }
    @Test
    public void test33() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("branch", "other");
        writeContents(join(WORK_DIR_STR, "h.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug2.txt")));
        Main.main("add", "h.txt");
        Main.main("rm", "g.txt");
        Main.main("commit", "Add h.txt and remove g.txt");
        Main.main("checkout", "other");
        Main.main("rm", "f.txt");
        writeContents(join(WORK_DIR_STR, "k.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug3.txt")));
        Main.main("add", "k.txt");
        Main.main("commit", "Add k.txt and remove f.txt");
        Main.main("checkout", "master");
        Main.main("merge", "other");

        Main.main("log");
        Main.main("status");
    }

    @Test
    public void test37_1() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("branch", "other");
        writeContents(join(WORK_DIR_STR, "h.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug2.txt")));
        Main.main("add", "h.txt");
        Main.main("rm", "g.txt");
        Main.main("commit", "Add h.txt and remove g.txt");
        Main.main("checkout", "other");
        Main.main("rm", "f.txt");
        writeContents(join(WORK_DIR_STR, "k.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug3.txt")));
        Main.main("add", "k.txt");
        Main.main("commit", "Add k.txt and remove f.txt");
        Main.main("log");

        Main.main("checkout", "master");
        Main.main("log");
        writeContents(join(WORK_DIR_STR, "m.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "m.txt");
    }
    @Test
    public void test37_2() {
        Main.main("reset", "80c60c387783f17ec8f5b6d16efe26f2c0843b6f");
        Main.main("status");
    }

    @Test
    public void test39_1() {
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "wug.txt");
        Main.main("commit", "version 1 of wug.txt");
        writeContents(join(WORK_DIR_STR, "wug.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "wug.txt");
        Main.main("commit", "version 2 of wug.txt");
        Main.main("log");
    }
    @Test
    public void test39_2() {
        Main.main("checkout", "186f7b", "--", "wug.txt");
        Main.main("checkout", "a72491", "--", "wug.txt");
    }

    @Test
    public void test40() {
        // setup2.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "g.txt");
        Main.main("add", "f.txt");
        Main.main("commit", "Two files");
        // end setup2.inc
        Main.main("branch", "b1");
        writeContents(join(WORK_DIR_STR, "h.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug2.txt")));
        Main.main("add", "h.txt");
        Main.main("commit", "Add h.txt");
        Main.main("branch", "b2");
        Main.main("rm", "f.txt");
        Main.main("commit", "remove f.txt");
//        Main.main("merge", "b1");
        Main.main("checkout", "b2");
        Main.main("merge", "master");
    }

    @Test
    public void test43b() {
        // prelude1.inc
        Main.main("init");
        Main.main("branch", "given");
        Main.main("checkout", "given");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Add f.txt containing wug.txt");
        Main.main("branch", "B");
        Main.main("checkout", "master");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Add f.txt containing notwug.txt");
        Main.main("checkout", "given");
        Main.main("merge", "master");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Reset f to notwug.txt");
        Main.main("rm", "f.txt");
        Main.main("commit", "given now empty.");
        Main.main("checkout", "B");
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug2.txt")));
        Main.main("add", "g.txt");
        Main.main("commit", "Added g.txt");
        Main.main("checkout", "master");
        Main.main("merge", "B");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Reset f to wug.txt");
        Main.main("merge", "given");
    }

    @Test
    public void test43() {
        Main.main("init");
        Main.main("branch", "given");
        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Add f.txt containing wug.txt");
        Main.main("checkout", "given");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Add f.txt containing notwug.txt");
        Main.main("branch", "B");
        Main.main("merge", "master");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notwug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Reset f to notwug.txt");
        Main.main("rm", "f.txt");
        Main.main("commit", "given now empty.");
        Main.main("checkout", "master");
        writeContents(join(WORK_DIR_STR, "g.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug2.txt")));
        Main.main("add", "g.txt");
        Main.main("commit", "Added g.txt");
        Main.main("merge", "B");

        writeContents(join(WORK_DIR_STR, "f.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/wug.txt")));
        Main.main("add", "f.txt");
        Main.main("commit", "Reset f to wug.txt");
        Main.main("merge", "given");
    }

    @Test
    public void test44() {
        // prelude1.inc
        Main.main("init");
        writeContents(join(WORK_DIR_STR, "A.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/a.txt")));
        writeContents(join(WORK_DIR_STR, "B.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/b.txt")));
        writeContents(join(WORK_DIR_STR, "C.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/c.txt")));
        writeContents(join(WORK_DIR_STR, "D.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/d.txt")));
        writeContents(join(WORK_DIR_STR, "E.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/e.txt")));
        Main.main("add", "A.txt");
        Main.main("add", "B.txt");
        Main.main("add", "C.txt");
        Main.main("add", "D.txt");
        Main.main("add", "E.txt");
        Main.main("commit", "msg1");
        Main.main("branch", "branch1");
        Main.main("rm", "C.txt");
        Main.main("rm", "D.txt");
        writeContents(join(WORK_DIR_STR, "F.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notf.txt")));
        writeContents(join(WORK_DIR_STR, "A.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/nota.txt")));
        Main.main("add", "A.txt");
        Main.main("add", "F.txt");
        Main.main("commit", "msg2");
        Main.main("checkout", "branch1");
        Main.main("rm", "C.txt");
        Main.main("rm", "E.txt");
        writeContents(join(WORK_DIR_STR, "B.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/notb.txt")));
        writeContents(join(WORK_DIR_STR, "G.txt"), readContentsAsString(join(WORK_DIR_STR + "/../testing/src/g.txt")));
        Main.main("add", "B.txt");
        Main.main("add", "G.txt");
        Main.main("commit", "msg3");
        Main.main("merge", "master");

    }
}


