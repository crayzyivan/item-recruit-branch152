package com.item.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CandidateJobCountDTO implements Serializable {

    private int applications;

    private int screened;

    private int AIVetted;

    private int pendingReview;

    private int manualReview;

    private int ready;

    private int backgroundChecked;

    private int denied;

}