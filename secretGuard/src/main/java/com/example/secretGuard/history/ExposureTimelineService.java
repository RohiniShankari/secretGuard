
package com.example.secretGuard.history;

import com.example.secretGuard.scanner.SecretScanner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExposureTimelineService {

    public List<ExposureEvent> identifyIntroducedEvents(
            List<GitHistoryScanService.HistoricalFinding> findings) {

        List<ExposureEvent> events = new ArrayList<>();

        for (GitHistoryScanService.HistoricalFinding finding : findings) {

            events.add(new ExposureEvent(
                    "INTRODUCED",
                    finding.commitHash(),
                    finding.commitMessage(),
                    finding.committedAt()
            ));
        }

        return events;
    }

    public List<ExposureEvent> identifyRemovedEvents(
            List<GitHistoryScanService.HistoricalFinding> historicalFindings,
            List<SecretScanner.SecretFinding> currentFindings) {

        List<ExposureEvent> events = new ArrayList<>();

        Set<String> currentFingerprints = new HashSet<>();

        for (SecretScanner.SecretFinding finding : currentFindings) {
            currentFingerprints.add(finding.fingerprint());
        }

        Set<String> processedFingerprints = new HashSet<>();

        for (GitHistoryScanService.HistoricalFinding historicalFinding
                : historicalFindings) {

            SecretScanner.SecretFinding finding =
                    historicalFinding.finding();

            String fingerprint = finding.fingerprint();

            if (currentFingerprints.contains(fingerprint)) {
                continue;
            }

            if (!processedFingerprints.add(fingerprint)) {
                continue;
            }

            events.add(new ExposureEvent(
                    "REMOVED_FROM_CURRENT",
                    historicalFinding.commitHash(),
                    "Secret is no longer present in the current snapshot.",
                    historicalFinding.committedAt()
            ));
        }

        return events;
    }

public List<ExposureEvent> identifyStillPresentEvents(
        List<GitHistoryScanService.HistoricalFinding> historicalFindings,
        List<SecretScanner.SecretFinding> currentFindings) {

    List<ExposureEvent> events = new ArrayList<>();

    Set<String> currentFingerprints = new HashSet<>();

    for (SecretScanner.SecretFinding finding : currentFindings) {
        currentFingerprints.add(finding.fingerprint());
    }

    Set<String> processedFingerprints = new HashSet<>();

    for (GitHistoryScanService.HistoricalFinding historicalFinding
            : historicalFindings) {

        SecretScanner.SecretFinding finding =
                historicalFinding.finding();

        String fingerprint = finding.fingerprint();

        if (!currentFingerprints.contains(fingerprint)) {
            continue;
        }

        if (!processedFingerprints.add(fingerprint)) {
            continue;
        }

        events.add(new ExposureEvent(
                "STILL_PRESENT",
                historicalFinding.commitHash(),
                "Secret is still present in the current snapshot.",
                historicalFinding.committedAt()
        ));
    }

    return events;
}

}

