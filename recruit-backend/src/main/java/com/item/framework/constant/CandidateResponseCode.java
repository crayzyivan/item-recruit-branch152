package com.item.framework.constant;

/**
 * @author : lh
 */
public enum CandidateResponseCode implements IGlobalStatusCode {
    CANDIDATE_NOT_FOUND(100, "candidate not found."),
    CANDIDATE_RESUME_NOT_FOUND(200, "candidate resume not found."),
    CANDIDATE_NOT_APPLY_REPEATEDLY(300, "You have already sent the re-sent email. Please do not resend it again."),
    EMAIL_SEND_FAILURE(310, "Failed to send the invitation email. Please try again later."),
    NOT_FOUND_DELIVERY_RECORD(311, "No delivery record found."),
    NOT_ALLOWED_REAPPLY(312, "Re-invitation is only allowed for rejected records."),
    CANDIDATE_CURRENT_PARAM_NOT_EQUALS(400, "candidate info exception."),
    CANDIDATE_INFO_FETCH_FAILED(401,"Failed to retrieve candidate information. Please try again later."),
    RESUME_INVALID_FORMAT(402,"Only PDF formats are supported."),
    RESUME_FILE_EMPTY(403,"The resume file cannot be empty."),
    RESUME_FILE_TOO_LARGE(404,"The resume file must not exceed 4MB."),
    CANDIDATE_RESUME_UPLOAD_FAILED(405,"Resume upload failed"),
    CANDIDATE_SHARE_LINK_INVALID(500, "Candidate share link is invalid or has been tampered with."),
    INTERVIEW_REPORT_NOT_FOUND(600, "Interview report not found."),
    INTERVIEW_TIME_CONFLICT(700, "Interview time conflict."),
    NO_DELIVERY_RECORD_FOUND(710, "No delivery record found."),
    NO_NEED_REPEAT_ANSWER(720, "No need to repeat the answer."),

    QUESTION_5S_TEST_LINK_EXCEPTION(800, "An error occurred with the 5S answer link."),
    QUESTION_5S_TEST_LINK_INVALID(805, "An invalid occurred with the 5S answer link."),
    QUESTION_5S_TEST_LINK_EXPIRED(810, "The link has expired."),
    QUESTION_5S_TEST_INVALID_LINK(815, "The link is invalid."),
    ;


    private final int statusCode;

    private final String message;

    CandidateResponseCode(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public int getCode() {
        return CANDIDATE_MODULE + this.statusCode;
    }

    @Override
    public String getMsg() {
        return this.message;
    }
}
