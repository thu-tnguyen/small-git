package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayDeque;


import static gitlet.Blob.*;
import static gitlet.Command.checkOutFourArgs;
import static gitlet.Commit.*;
import static gitlet.Utils.*;

/** Utils mainly for Main class methods.
 *  Also includes static final PATHS and FILES of use to all classes.
 *  @author Thu Nguyen
 */

class MainUtils {
    /** Updating the BRANCH in "branch_head.txt" to have new ID as this
     *  BRANCH'S head. */
    /** String of path to working directory. */
    static final String WORK_DIR_STR = System.getProperty("user.dir");
    /** Path to .gitlet directory. */
    static final Path DOT_GITLET_DIR = Paths.get(
            System.getProperty("user.dir"), ".gitlet");
    /** Path to .gitlet/objects directory. Used for storing blobs and
     * commits. */
    static final Path OBJECTS_DIR = Paths.get(
            System.getProperty("user.dir"), ".gitlet", "objects");
    /** File used to track staging area for addition. */
    static final File ADD_STAGE_FILE = join(
            DOT_GITLET_DIR.toString(), "add.txt");
    /** File used to track the name of the current branch. */
    static final File CURRENT_BRANCH = join(
            DOT_GITLET_DIR.toString(), "current_branch.txt");
    /** File used to track staging area for removal. */
    static final File RM_STAGE_FILE = join(DOT_GITLET_DIR.toString(), "rm.txt");
    /** File used to track all branches and their head commit. */
    static final File BRANCH_HEAD_FILE = join(
            DOT_GITLET_DIR.toString(), "branch_head.txt");


    /** Return HEAD ID of BRANCH. If does not exist, return empty string. */
    static String getBranchHeadID(String branch) {
        String[] branchHeadLines = readContentsAsString(
                BRANCH_HEAD_FILE).split("\\n");
        for (String line : branchHeadLines) {
            String[] branchAndHead = line.split(":");
            if (branchAndHead[0].compareTo(branch) == 0) {
                return branchAndHead[1];
            }
        }
        return "";
    }

    /** Return an array with current BRANCH as element at index 0 and HEAD ID
     *  as element at index 1. If branch does not exist, return empty array. */
    static String[] getCurrentBranchAndHeadID() {
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        String[] branchHeadLines = readContentsAsString(
                BRANCH_HEAD_FILE).split("\\n");
        for (String line : branchHeadLines) {
            String[] branchHead = line.split(":");
            if (branchHead[0].compareTo(currentBranch) == 0) {
                return branchHead;
            }
        }
        return new String[2];
    }

    /** Return HashSet of lines within FILE. */
    static HashSet<String> readFileLines(File file) {
        if (Files.exists(file.toPath())
                && !readContentsAsString(file).isEmpty()) {
            return new HashSet<>(Arrays.asList(
                    readContentsAsString(file).split("\\n")));
        }
        return new HashSet<>();
    }

    /** Return ArrayList of lines within FILE. */
    static String[] readFileLinesAsArray(File file) {
        if (Files.exists(file.toPath())
                && !readContentsAsString(file).isEmpty()) {
            return readContentsAsString(file).split("\\n");
        }
        return new String[0];
    }

    /** Update and overwrite the head of a BRANCH with specified ID out to
     *  "branch_head.txt". If ID is already truncated,
     *  i.e. equals UID_LENGTH, write it out. Else, truncate it. */
    static void updateHeadOfBranch(String branch, String id) {
        if (id.length() == UID_LENGTH + 5) {
            id = id.substring(5);
        }
        String[] branchHeadLines = readFileLinesAsArray(BRANCH_HEAD_FILE);
        for (int i = 0; i < branchHeadLines.length; i++) {
            String[] branchAndHead = branchHeadLines[i].split(":");
            String thisBranch = branchAndHead[0];
            if (thisBranch.compareTo(branch) == 0) {
                branchHeadLines[i] = branchAndHead[0] + ":" + id;
                break;
            }
        }
        writeContents(BRANCH_HEAD_FILE,  String.join("\n", branchHeadLines));
    }

    /** Update and overwrite the given FILE with NEWLINE while eliminating
     *  duplicates. Create the FILE if it does not exist.
     *  WARNING: files are assumed to have different item/object on
     *  each line. */
    static void addLineToFile(File file, String newLine) {
        if (Files.notExists(file.toPath())) {
            writeContents(file, newLine + "\n");
        } else {
            HashSet<String> updateFileLines = readFileLines(file);
            updateFileLines.add(newLine);
            writeContents(file, String.join("\n", updateFileLines));
        }
    }

    /** Return true if BRANCH is already the current branch.*/
    static boolean isCurrentBranch(String branch) {
        if (readContentsAsString(join(CURRENT_BRANCH)).compareTo(
                branch) == 0) {
            return true;
        }
        return false;
    }

    /** Check that the number of ARGS equals to EXPECTED, throw
     *  operand error if not equal. */
    static void checkOperands(int args, int expected) {
        if (args != expected) {
            throw error("Incorrect operands.");
        }
    }

    /** Return whether BRANCH exists. */
    static boolean hasBranch(String branch) {
        String[] branchHeadLines = readFileLinesAsArray(BRANCH_HEAD_FILE);
        for (int i = 0; i < branchHeadLines.length; i++) {
            if (branchHeadLines[i].split(":")[0].compareTo(branch) == 0) {
                return true;
            }
        }
        return false;
    }

    /** Return true if this COMMIT contains this FILENAME. */
    static boolean hasFile(String fileName, Commit commit) {
        for (String blobId : commit.getBlobIDArray()) {
            String blobName = getBlobFileName(blobId);
            if (fileName.compareTo(blobName) == 0) {
                return true;
            }
        }
        return false;
    }

    /** Overwrite files in WORKING DIRECTORY by
     *  FILENAME to files with COMMITID. */
    static void checkOutFilesOverwrite(String fileName, String commitID) {
        Commit commit = getCommit(commitID);
        checkOutFilesOverwrite(fileName, commit);
    }

    /** Overwrite files in WORKING DIRECTORY by
     *  FILENAME to files with COMMIT. */
    static void checkOutFilesOverwrite(String fileName, Commit commit) {
        for (String blobID : commit.getBlobIDArray()) {
            String blobFileName = getBlobFileName(blobID);
            File blobFile = getBlobFile(blobID);
            if (fileName.length() > 0) {
                if (blobFileName.compareTo(fileName) == 0) {
                    writeContents(join(WORK_DIR_STR, blobFileName),
                            readContentsAsString(blobFile));
                    break;
                }
            } else {
                writeContents(join(WORK_DIR_STR, blobFileName),
                        readContentsAsString(blobFile));
            }
        }
    }

    /** Takes all files in the commit at the head of the given branch, and
     *  puts them in the working directory, overwriting the versions of the
     *  files that are already there if they exist. At the end of this
     *  command, the given branch will now be considered the current branch
     *  (HEAD). Any files that are tracked in the current branch but are not
     *  present in the checked-out branch are deleted.
     *  If working folder contains untracked file (that is not
     *  .gitlet directory) from either head commit or COMMIT. */
    static void revertFileToCommit(Commit commit) {
        Commit currentHeadCommit = getCommit(getCurrentBranchAndHeadID()[1]);
        if (hasUntrackedFiles(currentHeadCommit, getBlobsNameMapId(
                commit.getBlobIDArray()))) {
            throw error("There is an untracked file in the way;"
                    + " delete it or add it first.");
        }
        HashSet<String> currentTrackedBlobsFileNames = getBlobsName(
                currentHeadCommit.getBlobIDArray());
        HashSet<String> commitTrackedBlobsFileNames = getBlobsName(
                commit.getBlobIDArray());
        for (File file : join(WORK_DIR_STR).listFiles()) {
            if (currentTrackedBlobsFileNames.contains(file.getName())
                    && !commitTrackedBlobsFileNames.contains(file.getName())) {
                restrictedDelete(file);
            }
        }
        checkOutFilesOverwrite("", commit);
        if (Files.exists(ADD_STAGE_FILE.toPath())) {
            ADD_STAGE_FILE.delete();
        }
        if (Files.exists(RM_STAGE_FILE.toPath())) {
            RM_STAGE_FILE.delete();
        }
    }

    /** Printing out modified but not staged" files lexicographically, that is:
     *  if it is:
     *    - Tracked in the current commit, changed in the working directory, but
     *      not staged
     *    - Staged for addition, but with different contents than in the working
     *      directory
     *    - Staged for addition, but deleted in the working directory
     *    - Not staged for removal, but tracked in the current commit and
     *      deleted from the working directory.
     *
     *  And printing  out "Untracked Files") is for files present in the working
     *  directory but neither staged for addition nor tracked. This includes
     *  files that have been staged for removal, but then re-added without
     *  Gitlet's knowledge. */
    static void printStatusModifiedAndUntracked() {
        Commit currentCommit = getCommit(getCurrentBranchAndHeadID()[1]);
        HashSet<String> rmStage = readFileLines(RM_STAGE_FILE);
        HashMap<String, String> addStage = getBlobsNameMapId(
                readFileLinesAsArray(ADD_STAGE_FILE));
        HashMap<String, String> curCommitBlobNameMapId = getBlobsNameMapId(
                currentCommit.getBlobIDArray());
        ArrayList<String> modifiedBlobs = new ArrayList<>();
        for (File file : join(WORK_DIR_STR).listFiles()) {
            String fileName = file.getName();
            if (fileName.compareTo(".gitlet") == 0) {
                continue;
            }
            String fileID = fileName + sha1(readContentsAsString(file));
            if (addStage.containsKey(fileName)) {
                if (addStage.get(fileName).compareTo(fileID) != 0) {
                    modifiedBlobs.add(fileName + " (modified)");
                }
            } else if (curCommitBlobNameMapId.containsKey(fileName)
                    && fileID.compareTo(
                            curCommitBlobNameMapId.get(fileName)) != 0) {
                modifiedBlobs.add(fileName + " (modified)");
            }
        }
        for (String fileName : addStage.keySet()) {
            if (!Files.exists(join(WORK_DIR_STR, fileName).toPath())) {
                modifiedBlobs.add(fileName + " (deleted)");
            }
        }
        for (String fileName : curCommitBlobNameMapId.keySet()) {
            if (!Files.exists(join(WORK_DIR_STR, fileName).toPath())
                    && !rmStage.contains(fileName)) {
                modifiedBlobs.add(fileName + " (deleted)");
            }
        }
        Collections.sort(modifiedBlobs);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String modded : modifiedBlobs) {
            System.out.println(modded);

        }
        System.out.println();
        ArrayList<String> untrackedBlobs = untrackedFilesName(
                currentCommit, null);
        Collections.sort(untrackedBlobs);
        System.out.println("=== Untracked Files ===");
        for (String untracked : untrackedBlobs) {
            System.out.println(untracked);
        }
        System.out.println();
    }

    /** Return true if there are untracked files, which are files present
     * in the working directory but neither staged for addition nor tracked
     * by CURRENTCOMMIT. If parameter BRANCHBLOBNAMEMAPID passed is not null,
     * compare to see if the presumably untracked file exists in it and has
     * different content. */
    static boolean hasUntrackedFiles(Commit currentCommit,
                             HashMap<String, String> branchBlobNameMapID) {
        HashSet<String> currCommitFileNames = getBlobsName(
                currentCommit.getBlobIDArray());
        HashSet<String> addStageFileNames = getBlobsName(
                readFileLinesAsArray(ADD_STAGE_FILE));
        for (File file : join(WORK_DIR_STR).listFiles()) {
            String fileName = file.getName();
            if (fileName.compareTo(".gitlet") != 0
                    && !addStageFileNames.contains(fileName)
                    && !currCommitFileNames.contains(fileName)) {
                if (branchBlobNameMapID == null) {
                    return true;
                } else if (branchBlobNameMapID.containsKey(fileName)
                        && branchBlobNameMapID.get(fileName).compareTo(
                                readContentsAsString(file)) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns an array list of names of untracked files, which are files
     * present in the working directory but neither staged for addition nor
     * tracked in CURRENTCOMMIT. If parameter BRANCHBLOBNAMEMAPID passed
     * is not null, add untracked file which exists in it and has different
     * content. */
    static ArrayList<String> untrackedFilesName(Commit currentCommit,
                                HashMap<String, String> branchBlobNameMapID) {
        ArrayList<String> untrackedFiles = new ArrayList<>();
        HashSet<String> currCommitFileNames = getBlobsName(
                currentCommit.getBlobIDArray());
        HashSet<String> addStageFileNames = getBlobsName(
                readFileLinesAsArray(ADD_STAGE_FILE));
        for (File file : join(WORK_DIR_STR).listFiles()) {
            String fileName = file.getName();
            if (fileName.compareTo(".gitlet") != 0
                    && !addStageFileNames.contains(fileName)
                    && !currCommitFileNames.contains(fileName)) {
                if (branchBlobNameMapID == null) {
                    untrackedFiles.add(fileName);
                } else if (branchBlobNameMapID.containsKey(fileName)
                        && branchBlobNameMapID.get(fileName).compareTo(
                                readContentsAsString(file)) != 0) {
                    untrackedFiles.add(fileName);
                }
            }
        }
        return untrackedFiles;
    }

    /** Return a hash map from blobs' name to their ID, using blobs from
     *  BLOBIDARR. */
    static HashMap<String, String> getBlobsNameMapId(String[] blobIdArr) {
        HashMap<String, String> blobsNameMapId = new HashMap<>();
        for (String id : blobIdArr) {
            blobsNameMapId.put(getBlobFileName(id), id);
        }
        return blobsNameMapId;
    }

    /** Return a hash set from blobs' ID, using blobs from BLOBIDARR. */
    static HashSet<String> getBlobsId(String[] blobIdArr) {
        return new HashSet<>(Arrays.asList(blobIdArr));
    }

    /** Return a split point of CCOMMIT and BCOMMIT, where BRANCH is the given
     *  branch and BID is this branch's head ID.
     *  Choose the candidate split point that is closest to the head of the
     *  current branch (that is, is reachable by following the fewest parent
     *  pointers along some path).
     *
     *  If the split point is the same commit as the given branch,
     *  the merge is complete. If the split point is the current branch,
     *  then the current branch is set to the same commit as the given branch
     *  and the operation ends. */
    static Commit getSplitPoint(Commit cCommit, Commit bCommit,
                                String bID, String branch) {
        if (sameCommit(cCommit, bCommit)) {
            throw error("Well even Prof. Hilfinger didn't expect this.");
        }
        Commit splitPoint = null;
        ArrayDeque<Commit> fringe = new ArrayDeque<>();
        HashSet<Commit> allAncestors = new HashSet<>();
        HashSet<Commit> cBranchAncestors = new HashSet<>();
        Commit tempCommit = null;

        fringe.add(cCommit);
        fringe.add(bCommit);
        allAncestors.add(cCommit);
        allAncestors.add(bCommit);
        cBranchAncestors.add(cCommit);

        outmostloop:
        while (!fringe.isEmpty()) {
            Commit c = fringe.remove();
            if (c == null) {
                continue;
            }
            for (int i = 0; i < 2; i += 1) {
                if (c.getParentsID()[i] == null) {
                    continue;
                }
                Commit cNext = getCommit(c.getParentsID()[i]);
                for (Commit commit : allAncestors) {
                    if (sameCommit(commit, cNext)) {
                        splitPoint = commit;
                        break outmostloop;
                    }
                }
                if (tempCommit == null && cBranchAncestors.contains(c)
                        && i == 1) {
                    tempCommit = cNext;
                    continue;
                }
                fringe.add(cNext);
                if (tempCommit != null) {
                    fringe.add(tempCommit);
                    allAncestors.add(tempCommit);
                    tempCommit = null;
                }
                allAncestors.add(cNext);
                if (cBranchAncestors.contains(c)) {
                    cBranchAncestors.add(cNext);
                }
            }
        }
        if (sameCommit(splitPoint, bCommit)) {
            throw error("Given branch is an ancestor of the current branch.");
        } else if (sameCommit(splitPoint, cCommit)) {
            revertFileToCommit(bCommit);
            updateHeadOfBranch(branch, bID);
            throw error("Current branch fast-forwarded.");
        }
        return splitPoint;
    }

    /** Overwrite content of file with FILENAME in working directory with
     *  formatted replacement for files with merge conflicts. This format
     *  combines contents from both CBLOBNAMEMAPID and BBLOBNAMEMAPID.
     *  Stage the file to be added in next commit.
     *  Return the new blob ID from the process. */
    static String mergeConflictReplace(HashMap<String, String> cBlobNameMapID,
                                     HashMap<String, String> bBlobNameMapID,
                                     String fileName) {
        String currString = (cBlobNameMapID.containsKey(fileName))
                ? readContentsAsString(join(OBJECTS_DIR.toString(),
                cBlobNameMapID.get(fileName)))
                : "";
        String branchString = (bBlobNameMapID.containsKey(fileName))
                ? readContentsAsString(join(OBJECTS_DIR.toString(),
                bBlobNameMapID.get(fileName)))
                : "";
        String replacementContent = "<<<<<<< HEAD\n" + currString
                + "=======\n" + branchString + ">>>>>>>\n";
        writeContents(join(WORK_DIR_STR, fileName), replacementContent);
        return fileName + sha1(replacementContent);
    }

    /** Merges files from the given branch into the current branch.
     *  Create a new commit from current commit map of blob name and ID's
     *  CMAP and that of given branch BMAP, where the head commit of which
     *  has ID BID. The split point commit map of blob name and ID's
     *  SPLITPTMAP. */
    static void merge(HashMap<String, String> cMap, String bID,
        HashMap<String, String> splitPtMap, HashMap<String, String> bMap) {
        HashMap<String, String> toBeStaged = getBlobsNameMapId(
                    readFileLinesAsArray(ADD_STAGE_FILE));
        HashSet<String> toBeRm = new HashSet<>();
        boolean hasConflict = false;
        for (String name : splitPtMap.keySet()) {
            String splitPtId = splitPtMap.get(name);
            boolean bContains = bMap.containsKey(name);
            boolean cContains = cMap.containsKey(name);
            boolean bContainsModified = bContains
                    && bMap.get(name).compareTo(splitPtId) != 0;
            boolean cContainsModified = cContains
                    && cMap.get(name).compareTo(splitPtId) != 0;
            if (bContainsModified && cContains
                    && cMap.get(name).compareTo(splitPtId) == 0) {
                checkOutFourArgs(new String[] {"checkout", bID,
                    "--", name});
                toBeStaged.put(name, splitPtMap.get(name));
            } else if ((cContainsModified && bContainsModified
                    && cMap.get(name).compareTo(bMap.get(name)) != 0)
                    || (cContainsModified && !bContains)
                    || (bContainsModified && !cContains)) {
                toBeStaged.put(name, mergeConflictReplace(cMap, bMap, name));
                hasConflict = true;
            } else if (cContains && bContains
                    && cMap.get(name).compareTo(bMap.get(name)) == 0) {
                toBeStaged.put(name, splitPtMap.get(name));
            } else if ((!bContains && cContains && cMap.get(name).compareTo(
                    splitPtId) == 0) || (!cContains && bContains
                    && bMap.get(name).compareTo(splitPtId) == 0)) {
                addLineToFile(RM_STAGE_FILE, name);
                restrictedDelete(Paths.get(WORK_DIR_STR, name).toString());
                toBeRm.add(name);
            }
        }
        for (String name : cMap.keySet()) {
            if (!splitPtMap.containsKey(name) && !bMap.containsKey(name)) {
                toBeStaged.put(name, cMap.get(name));
            }
        }
        for (String name : bMap.keySet()) {
            if ((bMap.containsKey(name) && cMap.containsKey(name)
                    && !splitPtMap.containsKey(name)
                    && cMap.get(name).compareTo(bMap.get(name)) != 0)) {
                toBeStaged.put(name, mergeConflictReplace(cMap, bMap, name));
                hasConflict = true;
            }
            if (!splitPtMap.containsKey(name) && !cMap.containsKey(name)) {
                String branchBlobContent = readContentsAsString(join(
                        OBJECTS_DIR.toString(), bMap.get(name)));
                writeContents(join(WORK_DIR_STR, name), branchBlobContent);
                toBeStaged.put(name, bMap.get(name));
            }
        }
        toBeStaged = removeSetFromMap(toBeStaged, toBeRm);
        writeContents(ADD_STAGE_FILE, String.join("\n", toBeStaged.values()));
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Remove item in MAP for every key equal to item in SET. Return the
     *  updated hash map. */
    static HashMap<String, String> removeSetFromMap(HashMap<String, String> map,
                                                    HashSet<String> set) {
        for (String key : set) {
            if (map.containsKey(key)) {
                map.remove(key);
            }
        }
        return map;
    }
}
