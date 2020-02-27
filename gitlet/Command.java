package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;

import static gitlet.Blob.*;
import static gitlet.Commit.*;
import static gitlet.MainUtils.*;
import static gitlet.Utils.*;
import static gitlet.Utils.error;

/** The class contains all command methods implementation of Gitlet. All
 *  helper functions can be found in MainUtils.java.
 *  @author thunguyen
 * */

class Command {
    /** Creates a new Gitlet version-control system in the current
     *  directory. This system will automatically start with one commit.
     *  It will have a single branch: master, which initially points to
     *  this initial commit, and master will be the current branch. The
     *  timestamp for this initial commit will be 00:00:00 UTC, Thursday,
     *  1 January 1970. All repositories will automatically share this
     *  commit.
     *  File name is the "[commit's ID].txt". File's content is serialized
     *  commit object.
     *  Tracking branches and its current head (by ID) by logging into
     *  "branch_head.txt", i.e. [BRANCH_NAME]:[COMMIT_ID].
     *  ARGS is the inputted parameter. */
    static void doInit(String[] args) {
        checkOperands(args.length, 1);
        new File(DOT_GITLET_DIR.toString()).mkdir();
        new File(OBJECTS_DIR.toString()).mkdir();
        Commit commit = new Commit();
        writeContents(BRANCH_HEAD_FILE, "master:"
                + commit.getID().substring(5) + "\n");
        writeContents(CURRENT_BRANCH, "master");
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     *  The staging area is in .gitlet. Staging an already-staged file
     *  overwrites the previous entry in the staging area with the new
     *  contents.
     *
     *  If the current working version of the file is identical to the
     *  version in the current commit, do not stage it to be added, and
     *  remove it from the staging area if it is already there. If the
     *  file had been marked to be removed (gitlet rm), delete that mark.
     *  Tracking staging area (files to add) in "add.txt" using blob id.
     *  ARGS is the inputted parameter. */
    static void doAdd(String[] args) {
        checkOperands(args.length, 2);
        Path toAddFilePath = Paths.get(WORK_DIR_STR, args[1]);
        if (Files.notExists(toAddFilePath)) {
            throw error("File does not exist.");
        }
        Blob blob = new Blob(args[1], toAddFilePath.toFile());
        HashSet<String> rmStageHash = readFileLines(RM_STAGE_FILE);
        if (rmStageHash.contains(args[1])) {
            rmStageHash.remove(args[1]);
            writeContents(RM_STAGE_FILE, String.join("\n",  rmStageHash));
            return;
        }
        HashSet<String> updateFileLines = readFileLines(ADD_STAGE_FILE);
        HashSet<String> currentHeadCommit = getBlobsId(getCommit(
                getCurrentBranchAndHeadID()[1]).getBlobIDArray());
        if (currentHeadCommit.contains(blob.getID())) {
            updateFileLines.remove(blob.getFileName());
            writeContents(ADD_STAGE_FILE, String.join(
                    "\n", updateFileLines));
            return;
        }
        updateFileLines.add(blob.getID());
        writeContents(ADD_STAGE_FILE, String.join(
                "\n", String.join("\n", updateFileLines)));
    }

    /** Saves a snapshot of certain files in the current commit and
     *  staging area so they can be restored at a later time, creating
     *  a new commit. The commit is said to be tracking the saved files.
     *  By default, each commit's snapshot of files will be exactly the
     *  same as its parent commit's snapshot of files; it will keep
     *  versions of files exactly as they are, and not update them. A
     *  commit will only update files it is tracking that have been staged
     *  at the time of commit, in which case the commit will now include
     *  the version of the file that was staged instead of the version
     *  it got from its parent. A commit will save and start tracking any
     *  files that were staged but weren't tracked by its parent. Finally,
     *  files tracked in the current commit may be untracked in the new
     *  commit as a result of the rm command. ARGS is the inputted parameter. */
    static void doCommit(String[] args) {
        checkOperands(args.length, 2);
        boolean hasAddStage = Files.exists(ADD_STAGE_FILE.toPath());
        boolean hasRmStage = Files.exists(RM_STAGE_FILE.toPath());
        if (!hasAddStage && !hasRmStage) {
            throw error("No changes added to the commit.");
        }
        if (args[1].compareTo("\\s") == 0 || args[1].length() < 1) {
            throw error("Please enter a commit message.");
        }
        HashSet<String> blobsToCommit = new HashSet<>();
        Commit commit;
        HashMap<String, String> add = getBlobsNameMapId(
                readFileLinesAsArray(ADD_STAGE_FILE));
        HashSet<String> rm = readFileLines(RM_STAGE_FILE);
        String[] branchHeadID = getCurrentBranchAndHeadID();
        Commit headCommit = getCommit(branchHeadID[1]);
        for (String parentBlobId : headCommit.getBlobIDArray()) {
            String parentBlobName = getBlobFileName(parentBlobId);
            if (!add.containsKey(parentBlobName)
                    && (!hasRmStage || !rm.contains(parentBlobName))) {
                blobsToCommit.add(parentBlobId);
            }
        }
        blobsToCommit.addAll(add.values());
        commit = new Commit(args[1],
                blobsToCommit.toArray(new String[blobsToCommit.size()]),
                branchHeadID[1]);
        updateHeadOfBranch(branchHeadID[0], commit.getID());
        if (hasAddStage) {
            ADD_STAGE_FILE.delete();
        }
        if (hasRmStage) {
            RM_STAGE_FILE.delete();
        }
    }

    /** Unstage the file if it is currently staged. If the file is tracked
     *  in the current commit, mark it to indicate that it is not to be
     *  included in the next commit, and remove the file from the working
     *  directory if the user has not already done so. ARGS is the inputted
     *  parameter. */
    static void doRm(String[] args) {
        checkOperands(args.length, 2);
        Commit headCommit = getCommit(getCurrentBranchAndHeadID()[1]);
        HashMap<String, String> addBlobs = getBlobsNameMapId(
                readFileLinesAsArray(ADD_STAGE_FILE));
        boolean fileTracked = false;
        if (addBlobs.containsKey(args[1])) {
            fileTracked = true;
            addBlobs.remove(args[1]);
            writeContents(ADD_STAGE_FILE, String.join("\n", addBlobs.values()));
        }
        for (String blobId : headCommit.getBlobIDArray()) {
            String blobName = getBlobFileName(blobId);
            if (blobName.compareTo(args[1]) == 0) {
                fileTracked = true;
                addLineToFile(RM_STAGE_FILE, blobName);
                restrictedDelete(Paths.get(
                        WORK_DIR_STR, args[1]).toString());
            }
        }
        if (!fileTracked) {
            throw error("No reason to remove the file.");
        }
    }

    /** Starting at the current head commit, display information about
     *  each commit backwards along the commit tree until the initial
     *  commit, following the first parent commit links, ignoring any
     *  second parents found in merge commits. ARGS is the inputted
     *  parameter. */
    static void doLog(String[] args) {
        checkOperands(args.length, 1);
        String nextId = getCurrentBranchAndHeadID()[1];
        Commit nextCommit = getCommit(nextId);
        while (true) {
            String log = nextCommit.getLogMessage();
            String parent1Id = nextCommit.getParentsID()[0];
            String parent2Id = nextCommit.getParentsID()[1];
            System.out.println("===");
            System.out.println("commit " + nextId);
            if (parent2Id != null) {
                System.out.println("Merge: " + parent1Id.substring(0, 7)
                        + " " + parent2Id.substring(0, 7));
            }
            System.out.println("Date: " + nextCommit.getTimeStamp());
            System.out.println(log);
            System.out.println();
            if (parent1Id == null) {
                break;
            }
            nextCommit = getCommit(parent1Id);
            nextId = parent1Id;
        }
    }

    /** Like log, except displays information about all commits ever made.
     * The order of the commits does not matter. ARGS is the inputted
     * parameter. */
    static void doGlobalLog(String[] args) {
        checkOperands(args.length, 1);
        for (File file : OBJECTS_DIR.toFile().listFiles()) {
            String fileName = file.getName();
            if (isCommitId(fileName)) {
                String uid = fileName.substring(5);
                Commit thisCom = getCommit(uid);
                System.out.println("===");
                System.out.println("commit " + uid);
                if (thisCom.getParentsID()[1] != null) {
                    System.out.println("Merge: "
                            + thisCom.getParentsID()[0].substring(0, 7) + " "
                            + thisCom.getParentsID()[1].substring(0, 7));
                }
                System.out.println("Date: " + thisCom.getTimeStamp());
                System.out.println(thisCom.getLogMessage());
                System.out.println();
            }
        }
    }

    /** Prints out the ids of all commits that have the given commit message,
     *  one per line. If there are multiple such commits, it prints the ids
     *  out on separate lines. The commit message is a single operand; to
     *  indicate a multiword message, put the operand in quotation marks,
     *  as for the commit command. ARGS is the inputted parameter. */
    static void doFind(String[] args) {
        checkOperands(args.length, 2);
        boolean commitNotExist = true;
        for (File file : OBJECTS_DIR.toFile().listFiles()) {
            String fileName = file.getName();
            if (isCommitId(fileName)) {
                String uid = fileName.substring(5);
                if (getCommit(uid).getLogMessage().compareTo(args[1]) == 0) {
                    commitNotExist = false;
                    System.out.println(uid);
                }
            }
        }
        if (commitNotExist) {
            throw error("Found no commit with that message.");
        }
    }

    /** Displays all branches that currently exist, and marks the "current"
     *  branch followed by "*". Also, displays files staged or marked for
     *  untracking. */
    static void doStatus(String[] args) {
        checkOperands(args.length, 1);
        String[] branchesAndHead = readFileLinesAsArray(BRANCH_HEAD_FILE);
        String[] stagedBlobIds = readFileLinesAsArray(ADD_STAGE_FILE);
        String[] rmBlobIds = readFileLinesAsArray(RM_STAGE_FILE);
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        Arrays.sort(branchesAndHead);
        Arrays.sort(stagedBlobIds);
        Arrays.sort(stagedBlobIds);
        System.out.println("=== Branches ===");
        for (String line : branchesAndHead) {
            String branch = line.split(":")[0];
            if (branch.compareTo(currentBranch) == 0) {
                System.out.print("*");
            }
            System.out.println(line.split(":")[0]);
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String line : stagedBlobIds) {
            System.out.println(getBlobFileName(line));
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String line : rmBlobIds) {
            System.out.println(line);
        }
        System.out.println();
        printStatusModifiedAndUntracked();
    }

    /** [2 ARGS]
     *  > java gitlet.Main checkout [branch name]
     *  Return true if checkout is successful.
     *  Takes all files in the commit at the head of the given branch, and
     *  puts them in the working directory, overwriting the versions of the
     *  files that are already there if they exist. Also, at the end of this
     *  command, the given branch will now be considered the current branch
     *  (HEAD). Any files that are tracked in the current branch but are not
     *  present in the checked-out branch are deleted. The staging area is
     *  cleared, unless the checked-out branch is the current branch. */
    static boolean checkOutTwoArgs(String[] args) {
        if (args.length == 2) {
            if (!hasBranch(args[1])) {
                throw error("No such branch exists");
            }
            if (isCurrentBranch(args[1])) {
                throw error("No need to checkout the current branch.");
            }
            revertFileToCommit(getCommit(getBranchHeadID(args[1])));
            writeContents(CURRENT_BRANCH, args[1]);
            return true;
        }
        return false;
    }

    /** [3 ARGS]
     *  > java gitlet.Main checkout -- [file name]:
     *  Return true if checkout is successful.
     *  Takes the version of the file as it exists in the head commit, the
     *  front of the current branch, and puts it in the working directory,
     *  overwriting the version of the file that's already there if there
     *  is one. The new version of the file is not staged. */
    static boolean checkOutThreeArgs(String[] args) {
        if (args.length == 3) {
            if (args[1].compareTo("--") != 0) {
                throw error("Incorrect operands.");
            }
            String commitID = getCurrentBranchAndHeadID()[1];
            Commit commit = getCommit(commitID);
            if (!hasFile(args[2], commit)) {
                throw error("File does not exist in that commit.");
            }
            checkOutFilesOverwrite(args[2], commit);
            return true;
        }
        return false;
    }

    /** [4 ARGS]
     *  > java gitlet.Main checkout [commit id] -- [file name]
     *  Return true if checkout is successful.
     *  Takes the version of the file as it exists in the commit with the
     *  given id, and puts it in the working directory, overwriting the
     *  version of the file that's already there if there is one. The new
     *  version of the file is not staged. */
    static boolean checkOutFourArgs(String[] args) {
        if (args.length == 4) {
            if (args[2].compareTo("--") != 0) {
                throw error("Incorre ct operands.");
            }
            String commitID = args[1];
            if (commitID.length() != UID_LENGTH + 5) {
                commitID = recoverUID(args[1]);
            }
            Commit commit = getCommit(commitID);
            if (commit == null) {
                throw error("No commit with that id exists.");
            }
            if (!hasFile(args[3], commit)) {
                throw error("File does not exist in that commit.");
            }
            checkOutFilesOverwrite(args[3], commit);
            return true;
        }
        return false;
    }


    /** [2 ARGS]
     *  > java gitlet.Main checkout [branch name]
     *  Takes all files in the commit at the head of the given branch, and
     *  puts them in the working directory, overwriting the versions of the
     *  files that are already there if they exist. Also, at the end of this
     *  command, the given branch will now be considered the current branch
     *  (HEAD). Any files that are tracked in the current branch but are not
     *  present in the checked-out branch are deleted. The staging area is
     *  cleared, unless the checked-out branch is the current branch.
     *
     *  [3 ARGS]
     *  > java gitlet.Main checkout -- [file name]:
     *  Takes the version of the file as it exists in the head commit, the
     *  front of the current branch, and puts it in the working directory,
     *  overwriting the version of the file that's already there if there
     *  is one. The new version of the file is not staged.
     *
     *  [4 ARGS]
     *  > java gitlet.Main checkout [commit id] -- [file name]
     *  Takes the version of the file as it exists in the commit with the
     *  given id, and puts it in the working directory, overwriting the
     *  version of the file that's already there if there is one. The new
     *  version of the file is not staged. */
    static void doCheckout(String[] args) {
        if (checkOutTwoArgs(args) || checkOutThreeArgs(args)
                || checkOutFourArgs(args)) {
            return;
        } else {
            throw error("Incorrect operands.");
        }
    }

    /** Creates a new branch with the given name, and points it at the current
     *  head node. A branch is nothing more than a name for a reference (a
     *  SHA-1 identifier) to a commit node. This command does NOT immediately
     *  switch to the newly created branch (just as in real Git).
     *  ARGS is the parameter. */
    static void doBranch(String[] args) {
        checkOperands(args.length, 2);
        for (String line : readFileLinesAsArray(BRANCH_HEAD_FILE)) {
            if (line.split(":")[0].compareTo(args[1]) == 0) {
                throw error("A branch with that name already exists.");
            }
        }
        addLineToFile(BRANCH_HEAD_FILE, args[1] + ":"
                + getCurrentBranchAndHeadID()[1]);
    }

    /** Deletes the branch with the given name. This only means to delete the
     *  pointer associated with the branch; it does not mean to delete all
     *  commits that were created under the branch, or anything like that.
     *  ARGS is the parameter. */
    static void doRmBranch(String[] args) {
        checkOperands(args.length, 2);
        if (Files.exists(BRANCH_HEAD_FILE.toPath())) {
            String branchHeadToRemove = "";
            HashSet<String> branchHeadLines = readFileLines(BRANCH_HEAD_FILE);
            if (args[1].compareTo(readContentsAsString(CURRENT_BRANCH)) == 0) {
                throw error("Cannot remove the current branch.");
            }
            for (String line: branchHeadLines) {
                String[] branchAndHead = line.split(":");
                if (branchAndHead[0].compareTo(args[1]) == 0) {
                    branchHeadToRemove = line;
                }
            }
            if (branchHeadToRemove.length() < 1) {
                throw error("A branch with that name does not exist.");
            }
            branchHeadLines.remove(branchHeadToRemove);
            writeContents(BRANCH_HEAD_FILE, String.join("\n", branchHeadLines));
        }
    }

    /** Checks out all the files tracked by the given commit. Removes tracked
     *  files that are not present in that commit. Also moves the current
     *  branch's head to that commit node. See the intro for an example of what
     *  happens to the head pointer after using reset. The [commit id] may be
     *  abbreviated as for checkout. The staging area is cleared. The command
     *  is essentially checkout of an arbitrary commit that also changes the
     *  current branch head. ARGS is the parameter. */
    static void doReset(String[] args) {
        checkOperands(args.length, 2);
        String commitID = args[1];
        if (args[1].length() != UID_LENGTH + 5) {
            commitID = recoverUID(args[1]);
        }
        Commit commit = getCommit(commitID);
        if (commit == null) {
            throw error("No commit with that id exists.");
        }
        revertFileToCommit(commit);
        updateHeadOfBranch(getCurrentBranchAndHeadID()[0], commitID);
    }

    /** Merges files from the given branch into the current branch.
     *  ARGS is the parameter. */
    static void doMerge(String[] args) {
        checkOperands(args.length, 2);
        String[] currBranchAndHead = getCurrentBranchAndHeadID();
        String givenBranchHead = getBranchHeadID(args[1]);
        if ((Files.exists(ADD_STAGE_FILE.toPath())
                && !readContentsAsString(ADD_STAGE_FILE).isEmpty())
                || (Files.exists(RM_STAGE_FILE.toPath())
                && !readContentsAsString(ADD_STAGE_FILE).isEmpty())) {
            throw error("You have uncommitted changes.");
        } else if (givenBranchHead.length() == 0) {
            throw error("A branch with that name does not exist.");
        } else if (args[1].compareTo(currBranchAndHead[0]) == 0) {
            throw error("Cannot merge a branch with itself.");
        }
        Commit newCommit;
        Commit currCommit = getCommit(currBranchAndHead[1]);
        Commit branchCommit = getCommit(givenBranchHead);
        HashMap<String, String> branchBlobNameMapID = getBlobsNameMapId(
                branchCommit.getBlobIDArray());
        if (hasUntrackedFiles(currCommit, branchBlobNameMapID)) {
            throw error("There is an untracked file in the way;"
                    + " delete it or add it first.");
        }
        HashMap<String, String> currBlobNameMapID = getBlobsNameMapId(
                currCommit.getBlobIDArray());
        Commit splitPoint = getSplitPoint(currCommit, branchCommit,
                givenBranchHead, args[1]);
        HashMap<String, String> splitPtBlobNameMapID = getBlobsNameMapId(
                splitPoint.getBlobIDArray());
        merge(currBlobNameMapID, givenBranchHead, splitPtBlobNameMapID,
                branchBlobNameMapID);
        newCommit = new Commit("Merged " + args[1]
                + " into " + currBranchAndHead[0] + ".",
                readFileLinesAsArray(ADD_STAGE_FILE),
                currBranchAndHead[1], givenBranchHead);
        updateHeadOfBranch(currBranchAndHead[0], newCommit.getID());
        if (Files.exists(ADD_STAGE_FILE.toPath())) {
            ADD_STAGE_FILE.delete();
        }
        if (Files.exists(RM_STAGE_FILE.toPath())) {
            RM_STAGE_FILE.delete();
        }
    }
}
