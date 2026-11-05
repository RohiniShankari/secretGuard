
package com.example.secretGuard.history;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.FileHeader.PatchType;
//import org.eclipse.jgit.diff.PatchType;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHistoryScanner {

    public List<CommitInfo> getCommitHistory(Path repositoryPath)
            throws IOException {

        List<CommitInfo> commits = new ArrayList<>();

        try (Git git = Git.open(repositoryPath.toFile());
             RevWalk revWalk = new RevWalk(git.getRepository())) {

            ObjectId head = git.getRepository().resolve(Constants.HEAD);

            if (head == null) {
                return commits;
            }

            revWalk.markStart(revWalk.parseCommit(head));

            for (RevCommit commit : revWalk) {
                commits.add(new CommitInfo(
                        commit.getName(),
                        commit.getFullMessage(),
                        commit.getAuthorIdent().getName(),
                        commit.getAuthorIdent().getWhen().toInstant()
                ));
            }
        }

        return commits;
    }

    public List<ChangedFile> getChangedFiles(
            Path repositoryPath, String commitHash) throws IOException {

        List<ChangedFile> changedFiles = new ArrayList<>();

        try (Git git = Git.open(repositoryPath.toFile());
             RevWalk revWalk = new RevWalk(git.getRepository());
             ObjectReader reader = git.getRepository().newObjectReader();
             DiffFormatter formatter =
                     new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            ObjectId commitId =
                    git.getRepository().resolve(commitHash);

            if (commitId == null) {
                throw new IllegalArgumentException("Invalid commit hash");
            }

            RevCommit commit = revWalk.parseCommit(commitId);

            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, commit.getTree().getId());

            formatter.setRepository(git.getRepository());
            formatter.setDetectRenames(true);

            List<DiffEntry> entries;

            if (commit.getParentCount() == 0) {
                // First commit: compare against an empty repository tree.
                entries = formatter.scan(
                        new EmptyTreeIterator(),
                        newTree
                );
            } else {
                // Other commits: compare against the first parent.
                RevCommit parent =
                        revWalk.parseCommit(commit.getParent(0).getId());

                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                oldTree.reset(reader, parent.getTree().getId());

                entries = formatter.scan(oldTree, newTree);
            }

            for (DiffEntry entry : entries) {
                changedFiles.add(new ChangedFile(
                        entry.getChangeType().name(),
                        entry.getOldPath(),
                        entry.getNewPath()
                ));
            }
        }

        return changedFiles;
    }

    public List<AddedLine> getAddedLines(
            Path repositoryPath, String commitHash) throws IOException {

        List<AddedLine> addedLines = new ArrayList<>();

        try (Git git = Git.open(repositoryPath.toFile());
             RevWalk revWalk = new RevWalk(git.getRepository());
             ObjectReader reader = git.getRepository().newObjectReader();
             DiffFormatter formatter =
                     new DiffFormatter(DisabledOutputStream.INSTANCE)) {

            ObjectId commitId =
                    git.getRepository().resolve(commitHash);

            if (commitId == null) {
                throw new IllegalArgumentException("Invalid commit hash");
            }

            RevCommit commit = revWalk.parseCommit(commitId);

            CanonicalTreeParser newTree = new CanonicalTreeParser();
            newTree.reset(reader, commit.getTree().getId());

            formatter.setRepository(git.getRepository());
            formatter.setDetectRenames(true);

            List<DiffEntry> entries;

            if (commit.getParentCount() == 0) {
                entries = formatter.scan(
                        new EmptyTreeIterator(),
                        newTree
                );
            } else {
                RevCommit parent =
                        revWalk.parseCommit(commit.getParent(0).getId());

                CanonicalTreeParser oldTree = new CanonicalTreeParser();
                oldTree.reset(reader, parent.getTree().getId());

                entries = formatter.scan(oldTree, newTree);
            }

            for (DiffEntry entry : entries) {

                // Deleted files have no new content to inspect.
                if (entry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                    continue;
                }

                FileHeader fileHeader = formatter.toFileHeader(entry);

                // Skip binary files.
                if (fileHeader.getPatchType() != PatchType.UNIFIED) {
                    continue;
                }

                ObjectLoader loader = reader.open(
                        entry.getNewId().toObjectId()
                );

                // Skip files larger than 1 MB.
                if (loader.getSize() > 1024 * 1024) {
                    continue;
                }

                String content = new String(
                        loader.getBytes(),
                        StandardCharsets.UTF_8
                );

                String[] lines = content.split("\\R", -1);

                EditList edits = fileHeader.toEditList();

                for (Edit edit : edits) {
                    for (int i = edit.getBeginB();
                         i < edit.getEndB(); i++) {

                        if (i < lines.length) {
                            addedLines.add(new AddedLine(
                                    entry.getNewPath(),
                                    i + 1,
                                    lines[i]
                            ));
                        }
                    }
                }
            }
        }

        return addedLines;
    }

    public record CommitInfo(
            String commitHash,
            String message,
            String author,
            Instant committedAt
    ) {}

    public record ChangedFile(
            String changeType,
            String oldPath,
            String newPath
    ) {}

    public record AddedLine(
            String filePath,
            int lineNumber,
            String content
    ) {}
}

