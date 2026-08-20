package com.tapsense.app.ui.troubleshoot

import com.tapsense.app.R

enum class TroubleshootIssue(val labelRes: Int) {
    NO_REACTION(R.string.troubleshoot_issue_no_reaction),
    READER_SILENT(R.string.troubleshoot_issue_reader_silent),
    PAY_FAILING(R.string.troubleshoot_issue_pay_failing),
    CANNOT_SCAN(R.string.troubleshoot_issue_cannot_scan),
    DONT_KNOW_WHERE(R.string.troubleshoot_issue_dont_know_where),
    MODEL_MISSING(R.string.troubleshoot_issue_model_missing),
}
