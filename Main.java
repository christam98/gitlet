package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        //if (args.length == 0) {
        // Throw some exception.
        //}
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.intializeGit();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String fileName = args[1];
                Repository.add(fileName);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                /*
                1) Iterate through the priors Commits Hashmap
                2) Compare the files in HashMap w/ the files in the Staging Area
                    --> This includes the ones staged for removal.
                3) Add a SHA id for the files .
                 */
                String message = args[1];
                Repository.commit(message);
                break;
            case "rm":
                String file_Name = args[1];
                Repository.rm(file_Name);
                break;
            case "log":
                Commit.log();
                break;
            case "global-log":
                Commit.globallog();
                break;
            case "find":
                String commitMsg = args[1];
                Repository.find(commitMsg);
                break;
            case "status":
                Repository.status();
                break;
            case "checkout":
                if (args.length == 3) {
                    Repository.checkoutFileName(args[2]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                }
                if (args.length == 4) {
                    Repository.checkoutCommit(args[1], args[3]);
                }
                break;
            case "branch":
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.merge(args[1]);
                break;
        }
    }
}
