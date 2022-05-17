package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  The structure of this Repository is as follows:
 *
 *  .gitlet/ -- top level folder for all persistent data in the project 2 folder
 *      - .index/ -- folder containing all files that are staged to be committed
 *      - commit -- file containing all commits
 *
 */
public class    Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    /** Store the file objects of the commits */
    public static final File BLOBS = join(GITLET_DIR, "blobs");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    /** The directory that will store the heads for master and other branches.  */
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    public static final File MASTER = join(BRANCHES, "master");
    // Could probably save the branches as a Hashmap
    public static final File HEAD = join(GITLET_DIR, "head");
    public static final File INDEX = join(GITLET_DIR, "index");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     */

    public static void intializeGit() throws IOException {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            BLOBS.mkdir();
            COMMITS.mkdir();
            BRANCHES.mkdir();
            MASTER.createNewFile();
            HEAD.createNewFile();
            INDEX.createNewFile();
            Commit firstCommit = new Commit("first commit", new Date(0).toString(), null, null);
            firstCommit.saveCommit();
            firstCommit.saveHead();
            firstCommit.saveMaster();
            StagingArea area0 = new StagingArea();
            StagingArea.saveFiles(area0);
        } else {
            System.out.println("You already initialized Git");
        }
    }

    public static void add(String file) throws IOException {
        // Checks the working directory if the file exists.
        File temp = join(CWD, file);
        StagingArea index = StagingArea.fromFiles();
        Commit head = readObject(HEAD, Commit.class);
        if (!temp.exists()) {
            System.out.println("File does not exist");
        } else if (sha1(readContents(temp)).equals(head.files.get(temp))) {
            rm(file);
        } else {
            System.out.println("Read CWD file as String : " + readContentsAsString(temp));
            File outFile = join(BLOBS, sha1((readContents(temp))));
            index.addFiles.put(file, sha1((readContents(temp))));
            writeContents(outFile, readContents(temp));
            System.out.println("Saved file is : " + readContentsAsString(outFile));
            StagingArea.saveFiles(index);
        }
    }



    public static void rm(String file) {
        File temp = new File(CWD, file);
        Commit head = readObject(HEAD, Commit.class);
        StagingArea index = StagingArea.fromFiles();
        if (!index.addFiles.containsKey(file) && !head.files.containsKey(file)) { //Doesn't take into account that the files might not exist.
            System.out.println("No reason to remove the file");
            return;
        }
        if (index.addFiles.containsKey(file)) { //Removes the file if it is in the staging area.
            index.addFiles.remove(file);
            StagingArea.saveFiles(index);
            System.out.println(file + " has been removed from the staging area");
        } else if (head.files.containsKey(file) && plainFilenamesIn(CWD).contains(file)) { //Deletes the file if it is tracked by the head commit.
            if (plainFilenamesIn(CWD).contains(file)) {
                Utils.restrictedDelete(file);
            } else {
                System.out.println(file + " has already been removed");
            }
            StagingArea.saveFiles(index);
            System.out.println(file + " has been deleted from the CWD");
        }
    }

    public static void commit(String message) {
        StagingArea index = StagingArea.fromFiles();
        String currentBranch = StagingArea.fromFiles().currentBranch;

        System.out.println(currentBranch);
        Commit parent = readObject(join(BRANCHES, currentBranch), Commit.class);
        Commit commit = new Commit(message, new Date().toString(), (sha1((serialize(parent)))), null);

        System.out.println("Parent Commit before change: " + sha1(serialize(parent)));
        System.out.println("StagingArea files: " + index.addFiles.keySet());
        for (String key : index.addFiles.keySet()) {
            parent.files.put(key, index.addFiles.get(key));
        }
        for (int i = 0; i < index.rmFiles.size(); i ++) {
            String file = index.rmFiles.get(i);
            parent.files.remove(file);
        }
        commit.files = parent.files;
        commit.saveCommit();
        commit.saveHead();
        index.addFiles.clear();
        index.rmFiles.clear();
        StagingArea.saveFiles(index);
        System.out.println("New commit: " + sha1(serialize(commit)));
        Utils.writeObject(join(BRANCHES, currentBranch), commit);
    }

    public static void find(String message) {
        List<String> commitHashes = Utils.plainFilenamesIn(COMMITS);
        boolean commitExists = false;
        for (int i = 0; i < commitHashes.size(); i ++) {
            Commit someCommit = Utils.readObject(Utils.join(COMMITS, commitHashes.get(i)), Commit.class);
            if (someCommit.message.equals(message)) {
                System.out.println(commitHashes.get(i));
                commitExists = true;
            }
        }
        if (!commitExists) {
            System.out.println("Found no commit with that message");
        }
    }

    public static void status() {
        StagingArea stage = StagingArea.fromFiles();
        String branch = stage.currentBranch;
        Commit commit = Utils.readObject(HEAD, Commit.class);
        List<String> cwd = plainFilenamesIn(CWD);

        System.out.println("Head files " + commit.files.keySet());

        String line1 = "=== Branches ===" + "\n";
        System.out.println(line1);

        List<String> branchNames = Utils.plainFilenamesIn(BRANCHES);
        for (int i = 0; i < branchNames.size(); i ++) {
            System.out.println(branchNames);
        }
        for (int i = 0; i < branchNames.size(); i ++ ) {
            if (branchNames.get(i).equals(stage.currentBranch)) {
                System.out.println("*" + branchNames.get(i));
            }
            else {
                System.out.println(branchNames.get(i));
            }
        }

        System.out.println("=== Staged Files ===");
        for (String file : stage.addFiles.keySet()) {
            System.out.println(stage.addFiles.get(file));
            System.out.println(file);
        }

        System.out.println("=== Removed Files ===");

        for (int i = 0; i < stage.rmFiles.size(); i ++) {
            System.out.println(stage.rmFiles.get(i));
        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String file : commit.files.keySet()) {
            System.out.println(commit.files.keySet());

            if (plainFilenamesIn(CWD).contains(file)
                    && !sha1(readContents(join(CWD, file))).equals(commit.files.get(file))
                    && (!stage.addFiles.containsKey(file))) {
                        System.out.println(file + " modified");
            } else if  (plainFilenamesIn(CWD).contains(file)
                    && (stage.addFiles.containsKey(file)
                    && (!stage.addFiles.get(file).equals(sha1(readContents(join(CWD, file))))))) {
                        System.out.println(file + " modified");
            } else if (stage.addFiles.containsKey(file) && !plainFilenamesIn(CWD).contains(file)) {
                System.out.println(file + " deleted");
            }
        }


        System.out.println("=== Untracked Files ===");
        ArrayList<String> filesUntracked = new ArrayList();
        for (String file : cwd) {
            if (!commit.files.containsKey((file)) && !stage.addFiles.containsKey(file)) {
                filesUntracked.add(file);
            }
        }
        for (String file : filesUntracked) {
            System.out.println(file);
        }
    }

    public static void checkoutFileName(String fileName) {
        Commit head = readObject(HEAD, Commit.class);
        if (!head.files.containsKey(fileName)) {
            System.out.println("File does not exist in that commit");
        } else {
            String fileHash = head.files.get(fileName);
            File fromFile = join(BLOBS, fileHash);
            File toFile = join(CWD, fileName);
            System.out.println(readContentsAsString(fromFile));
            Utils.writeContents(toFile, readContents(fromFile));
        }
    }

    public static void checkoutCommit(String commitID, String fileName) {
        List<String> commitIDS = plainFilenamesIn(COMMITS);
        File file = join(COMMITS, commitID);
        Commit commit =  Utils.readObject(file, Commit.class);
        if (!commitIDS.contains(commitID)) {
            System.out.println("No commit with that id exists");
        } else if (!commit.files.containsKey(fileName)) {
            System.out.println("File does not exist for that commit");
        } else {
            String fileHash = commit.files.get(fileName);
            File fromFile = join(BLOBS, fileHash);
            File toFile = join(CWD, fileName);
            Utils.writeContents(toFile, readContents(fromFile));
        }
    }

    public static void checkoutBranch(String branch) {
        //If a working file is untracked in the current branch and would be overwritten by
        // the checkout, print There is an untracked file in the way; delete it, or add and
        // commit it first. and exit; perform this check before doing anything else. Do not
        // change the CWD.

        //  A commit will save and start tracking any files that were staged for addition
        //  but weren’t tracked by its parent.
        StagingArea stage = StagingArea.fromFiles();
        Commit headCommit = readObject(HEAD, Commit.class);
        Commit branchCommit = readObject(join(BRANCHES, branch), Commit.class);
        List<String> CWDFiles = plainFilenamesIn(CWD);
        for (String fileName : CWDFiles) {
            if (!headCommit.files.containsKey(fileName) && branchCommit.files.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first");
                return;
            }
        }
        List<String> branches = plainFilenamesIn(BRANCHES);
        if (!branches.contains(branch)) {
            System.out.println("No such branch exists");
            return;
        }
        if (stage.currentBranch.equals(branch)) {
            System.out.println("No need to checkout the current branch");
            return;
        }
        System.out.println(branchCommit.files.keySet());
        System.out.println(branchCommit.files.get("Grub.txt"));
        for (String fileName : branchCommit.files.keySet()) {
            File toFile = join(CWD, fileName);
            writeContents(toFile, readContents(join(BLOBS, branchCommit.files.get(fileName))));
        }
        for (String file : headCommit.files.keySet()) {
            if (!branchCommit.files.keySet().contains(file)) {
                restrictedDelete(file);
            }
        }
        stage.addFiles.clear();
        stage.rmFiles.clear();
        stage.currentBranch = branch;
        StagingArea.saveFiles(stage);
        Utils.writeObject(HEAD, branchCommit); // Turns the branch into the HEAD.
    }


    public static void branch(String branch) {
        List<String> branches = plainFilenamesIn(BRANCHES);
        StagingArea stage = StagingArea.fromFiles();
        if (branches.contains(branch)) {
            System.out.println("A branch with that name already exist.");
        }
        File newBranch = join(BRANCHES, branch);
        byte[] head = readContents(HEAD);
        Utils.writeContents(newBranch, head);
    }

    public static void rmBranch(String branch) {
        List<String> branches = plainFilenamesIn(BRANCHES);
        StagingArea stage = StagingArea.fromFiles();
        if (!branches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
        } else if (stage.currentBranch == branch) {
            System.out.println("Cannot remove the current branch");
        } else join(BRANCHES, branch).delete();
    }
    public static void reset(String commitID) {
        if (!plainFilenamesIn(COMMITS).contains(commitID)) {
            System.out.println("No commit with that id exists");
        }
        Commit resetCommit = readObject(join(COMMITS, commitID), Commit.class);
        List<String> CWDFiles = plainFilenamesIn(CWD);
        for (String resetFiles : resetCommit.files.keySet()) {
            checkoutCommit(commitID, resetFiles);
        }
        for (String cwdFile : CWDFiles) {
            if (resetCommit.files.containsKey(cwdFile)) {
                restrictedDelete(cwdFile);
            }
        }
        writeObject(HEAD, resetCommit);
        StagingArea stage = StagingArea.fromFiles();
        stage.addFiles.clear();
        stage.rmFiles.clear();
        StagingArea.saveFiles(stage);
    }

    public static void merge(String branchName) {
        StagingArea stage = StagingArea.fromFiles();
        Commit headCommit = readObject(HEAD, Commit.class);
        Commit branchCommit = readObject(join(BRANCHES, branchName), Commit.class);
        Commit ancestorCommit = findSplit(headCommit, branchCommit);
        List<String> CWDFiles = plainFilenamesIn(CWD);
        boolean conflict = false;

        HashMap<String, String> filesUntracked = new HashMap<>();
        for (String file : CWDFiles) {
            if (!headCommit.files.containsKey((file)) && !stage.addFiles.containsKey(file)) {
                filesUntracked.put(file, sha1(readContents(join(CWD, file))));
            }
        }
        for (String file : branchCommit.files.keySet()) {
            if (filesUntracked.containsKey(file) && (!branchCommit.files.get(file).equals(filesUntracked.get(file)))) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it");
                return;
            }
        }

        if (stage.addFiles.size() > 0 || stage.rmFiles.size() > 0) {
            System.out.println("1");
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!plainFilenamesIn(BRANCHES).contains(branchName)) {
            System.out.println("2");
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (stage.currentBranch.equals(branchName)) {
            System.out.println("3");
            System.out.println("Can not merge branch with itself.");
            return;
        }
        if (branchCommit.equals(ancestorCommit)) {
            System.out.println("4");
            System.out.println("Given branch is ancestor of the current branch");
            return;
        } else if (headCommit.equals(ancestorCommit)) {
            System.out.println("5");
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded");
            return;
        }
        Set<String> allFiles = new HashSet<String>();
        System.out.println(ancestorCommit.message);
        if (!ancestorCommit.files.keySet().isEmpty()) {
            System.out.println("6");
            allFiles.addAll(ancestorCommit.files.keySet());
        }
        if (!headCommit.files.keySet().isEmpty()) {
            System.out.println("7");
            allFiles.addAll(headCommit.files.keySet());
        }
        if (!branchCommit.files.keySet().isEmpty()) {
            System.out.println("8");
            allFiles.addAll(branchCommit.files.keySet());
        }

        HashMap<String, String> mergedFiles = new HashMap<>();
        for (String file : allFiles) {
            System.out.println("9");
            if (ancestorCommit.files.containsKey(file) && headCommit.files.containsKey(file)
                    && (branchCommit.files.containsKey(file))) {
                System.out.println("10");
                if (ancestorCommit.files.get(file).equals(headCommit.files.get(file))
                        && (!ancestorCommit.files.get(file).equals(branchCommit.files.get(file)))) {
                    mergedFiles.put(file, branchCommit.files.get(file));
                    System.out.println("10-1");
                } else if (ancestorCommit.files.get(file).equals(branchCommit.files.get(file))
                        && (!ancestorCommit.files.get(file).equals(headCommit.files.get(file)))) {
                    mergedFiles.put(file, headCommit.files.get(file));
                    System.out.println("10-2");
                }
                // didn't consider when one branch doesn't have files names.
            } else if (headCommit.files.containsKey(file) && ancestorCommit.files.containsKey(file)
                    && headCommit.files.get(file).equals(branchCommit.files.get(file))) {
                System.out.println("11");
                mergedFiles.put(file, headCommit.files.get(file));
            } else if (!ancestorCommit.files.containsKey(file) && !headCommit.files.containsKey(file)
                    && branchCommit.files.containsKey(file)) {
                System.out.println("12");
                mergedFiles.put(file, branchCommit.files.get(file));
            } else if (!ancestorCommit.files.containsKey(file) && !branchCommit.files.containsKey(file)
                    && headCommit.files.containsKey(file)) {
                System.out.println("13");
                mergedFiles.put(file, headCommit.files.get(file));
            } else if ((ancestorCommit.files.containsKey(file) && headCommit.files.containsKey(file))
                    && ancestorCommit.files.get(file).equals(headCommit.files.get(file))
                    && !branchCommit.files.containsKey(file)) {
                System.out.println("14");
                // Do Nothing
            } else if (ancestorCommit.files.containsKey(file) && branchCommit.files.containsKey(file)
                    && ancestorCommit.files.get(file).equals(branchCommit.files.get(file))
                    && !headCommit.files.containsKey((file))) {
                System.out.println("15");
                //Do Nothing
            } else if ((headCommit.files.containsKey(file) && branchCommit.files.containsKey(file))
                && (!headCommit.files.get(file).equals(branchCommit.files.get(file)))) {
                System.out.println("16");
                conflict = true;
                String line1 = "<<<<<<< HEAD" + "\n";
                String line2 = "";
                if (headCommit.files.containsKey(file)) {
                    line2 = Utils.readContentsAsString(Utils.join(BLOBS, headCommit.files.get(file)))+ "\n";
                }
                String line3 = "=======\n";
                String line4 = "";
                if (branchCommit.files.containsKey(file)) {
                    line4 = Utils.readContentsAsString(Utils.join(BLOBS, branchCommit.files.get(file))) + "\n";
                }
                String line5 = ">>>>>>>";
                Utils.writeContents(join(CWD, file), line1 + line2 + line3 + line4 + line5);
                writeContents(join(BLOBS, sha1(readContents(join(CWD, file)))), readContents(join(CWD, file)));
                mergedFiles.put(file, sha1(readContents(join(CWD, file))));
            }
        } // where the for-loop stops
        if (conflict) {
            System.out.println("Encountered a merge conflict");
        }


        Commit newCommit = new Commit("Merged " +stage.currentBranch + " into " + branchName, new Date().toString(), sha1((serialize(headCommit))), sha1(serialize(branchCommit)));
        System.out.println("Parent 2 is " + newCommit.parent2);
        newCommit.files = mergedFiles;
        stage.addFiles.clear();
        stage.rmFiles.clear();
        newCommit.saveHead();
        newCommit.saveCommit();
        StagingArea.saveFiles(stage);
        for (String files : mergedFiles.keySet()) {
            String fileHash = mergedFiles.get(files);
            File fromFile = join(BLOBS, fileHash);
            File toFile = join(CWD, files);
            writeContents(toFile, readContents(fromFile));
        }
    }



    // master is the contents in the head commit
    public static Commit findSplit(Commit head, Commit other) {
        String headHash = sha1(serialize(head));
        String branchHash = sha1(serialize(other));
        HashSet<String> commits = new HashSet<>();
        System.out.println("headHash is " + headHash);
        System.out.println("branchHash is " + branchHash);

        while (head != null) {
            commits.add(sha1(serialize(head)));
            if (head.parent1 != null) {
                File nextHead = Utils.join(Repository.COMMITS, head.parent1);
                head = Utils.readObject(nextHead, Commit.class);
            } else {
                head = null;
            }
        }

       while (other != null) {
           if (commits.contains(sha1(serialize(other)))) {
               return other;
           }
           if (other.parent1 != null) {
               File nextHead = Utils.join(Repository.COMMITS, other.parent1);
               other = Utils.readObject(nextHead, Commit.class);
           } else {
               other = null;
           }
       }
       return null;
    }
}
