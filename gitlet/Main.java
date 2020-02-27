package gitlet;

import static gitlet.Utils.*;
import java.nio.file.Files;
import static gitlet.MainUtils.*;
import static gitlet.Command.*;

/** Main class for Gitlet, the tiny version-control system.
 *  Receives commands and passes them to matched command methods.
 *  @author Thu Nguyen
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        /** Read command. */
        try {
            if (args.length == 0) {
                throw error("Please enter a command.");
            }
            String cmd = args[0];
            if (cmd.compareTo("init") != 0) {
                if (Files.notExists(DOT_GITLET_DIR)) {
                    throw error("Not in an initialized Gitlet"
                            + "directory.");
                }
            } else {
                if (Files.exists(DOT_GITLET_DIR)) {
                    throw error("Gitlet version-control system "
                            + "already exists in the current directory.");
                }
            }
            executeCommand(cmd, args);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /** Execute the command CMD, while passing its arguments ARGS. */
    static void executeCommand(String cmd, String[] args) {
        switch (cmd) {
        case "init":
            doInit(args);
            break;
        case "add":
            doAdd(args);
            break;
        case "commit":
            doCommit(args);
            break;
        case "rm":
            doRm(args);
            break;
        case "log":
            doLog(args);
            break;
        case "global-log":
            doGlobalLog(args);
            break;
        case "find":
            doFind(args);
            break;
        case "status":
            doStatus(args);
            break;
        case "checkout":
            doCheckout(args);
            break;
        case "branch":
            doBranch(args);
            break;
        case "rm-branch":
            doRmBranch(args);
            break;
        case "reset":
            doReset(args);
            break;
        case "merge":
            doMerge(args);
            break;
        default:
            throw error("No command with that name exists.");
        }
    }
}
