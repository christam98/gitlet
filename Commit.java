package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.lang.Object.*;



/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     * @param date Time of the commit
     * @param message String of what changes has been made by the author
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    public String message;
    public String date;
    public String parent1;
    public String parent2;
    public HashMap<String, String> files;


    public Commit(String message, String date, String parent1, String parent2) {
        this.message = message;
        this.date = date;
        this.parent1 = parent1;
        files = new HashMap<>();
        this.parent2 = parent2;
    }

    public static void log() {
        Commit head = Utils.readObject(Repository.HEAD, Commit.class);
        System.out.println(head.parent1);
        System.out.println(head.parent2);
        while (head != null) {
            if (head.parent2 == null) {
                String line1 = "===" + "\n";
                String line2 = "commit " + sha1(Utils.serialize(head)) + "\n";
                String line3 = "Date: " + head.date + "\n";
                String line4 = head.message + "\n";
                String line5 = "\n";
                System.out.println(line1 + line2 + line3 + line4 + line5);
            } else {
                String line1 = "===" + "\n";
                String line2 = "commit " + sha1(Utils.serialize(head)) + "\n";
                String line3 = "Merge " + head.parent1.substring(0, 6) + " " + head.parent2.substring(0,6) + "\n";
                String line4 = head.date + "\n";
                String line5 = head.message + "\n";
                System.out.println(line1 + line2 + line3 + line4 + line5);
            }
            if (head.parent1 != null) {
                File nextHead = Utils.join(Repository.COMMITS, head.parent1);
                head = Utils.readObject(nextHead, Commit.class);
            } else {
                head = null;
            }
        }
    }

    public static void globallog() {
        List<String> commits = Utils.plainFilenamesIn(Repository.COMMITS);
        for (int i = 0; i < commits.size(); i ++) {
            Commit aCommit = readObject(join(Repository.COMMITS, commits.get(i)), Commit.class);
            if (aCommit.parent2 == null) {
                String line1 = "===" + "\n";
                String line2 = "commit " + sha1(serialize(aCommit)) + "\n";
                String line3 = "Date: " + aCommit.date + "\n";
                String line4 = aCommit.message + "\n";
                String line5 = "\n";
                System.out.println(line1 + line2 + line3 + line4 + line5);
            } else {
                String line1 = "===" + "\n";
                String line2 = "commit " + sha1(Utils.serialize(aCommit)) + "\n";
                String line3 = "Merge " + String.format("%" + 7 + "s", aCommit.parent1) + " " + String.format("%" + 7 + "s", aCommit.parent2);
                String line4 = aCommit.date + "\n";
                String line5 = aCommit.message + "\n";
                System.out.println(line1 + line2 + line3 + line4 + line5);
            }
        }
    }

    public void saveCommit() {
        File outfile = join(Repository.COMMITS, sha1(serialize(this)));
        Utils.writeObject(outfile, this);
    }
    public Commit fromCommit(String hash) {
        File outfile = Utils.join(Repository.COMMITS, hash);
        Commit result = Utils.readObject(outfile, Commit.class);
        return result;
    }

    public void saveHead() {
        Utils.writeObject(Repository.HEAD, this);
    }

    public void saveMaster() {
        Utils.writeObject(Repository.MASTER, this);
    }

}
