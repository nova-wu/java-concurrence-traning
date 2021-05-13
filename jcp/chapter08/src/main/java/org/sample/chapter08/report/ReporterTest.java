package org.sample.chapter08.report;

public class ReporterTest {
    public static void main(String [] args) throws Exception {
        Reporter reporter = new Reporter();

        reporter.report("T1", 100);
        reporter.report("T1", 200);
        reporter.report("T1", 300);

        reporter.report("T2", 200);
        reporter.report("T2", 300);
        reporter.report("T2", 400);

        reporter.destroyReportWorker("T1");
        reporter.destroyReportWorker("T2");
    }
}
