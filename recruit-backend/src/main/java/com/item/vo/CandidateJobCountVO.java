package com.item.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CandidateJobCountVO implements Serializable {

    @JsonProperty("applications")
    private int applications;
    @JsonProperty("screened")
    private int screened;
    @JsonProperty("ai-vetted")
    private int AIVetted;
    @JsonProperty("pending-review")
    private int pendingReview;
    @JsonProperty("ready")
    private int ready;
    @JsonProperty("background-checked")
    private int backgroundChecked;
    @JsonProperty("denied")
    private int denied;
    @JsonProperty("manual-review")
    private int manualReview;

}